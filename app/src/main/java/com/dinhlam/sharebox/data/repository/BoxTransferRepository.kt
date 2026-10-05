package com.dinhlam.sharebox.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.dinhlam.sharebox.data.local.AppDatabase
import com.dinhlam.sharebox.data.local.converter.ShareDataConverter
import com.dinhlam.sharebox.data.local.dao.BoxDao
import com.dinhlam.sharebox.data.local.dao.ShareDao
import com.dinhlam.sharebox.data.local.entity.Box
import com.dinhlam.sharebox.data.local.entity.BoxTransferState
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.model.*
import com.dinhlam.sharebox.storage.FirebaseStorageManager
import com.dinhlam.sharebox.transfer.PortableArchive
import com.dinhlam.sharebox.transfer.RevisionPolicy
import com.dinhlam.sharebox.utils.FileUtils
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

@Singleton
class BoxTransferRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDatabase: AppDatabase,
    private val boxDao: BoxDao,
    private val shareDao: ShareDao,
    private val storageManager: FirebaseStorageManager,
    private val userHelper: UserHelper,
    private val gson: Gson,
    private val httpClient: OkHttpClient,
) {
    private val converter by lazy { ShareDataConverter(gson) }
    private val states get() = appDatabase.boxTransferStateDao()
    // Serialize publication and imports, including foreground and WorkManager callers.
    private val transfers = Mutex()

    suspend fun export(boxId: String): BoxTransferManifest = withContext(Dispatchers.IO) {
        transfers.withLock {
            require(userHelper.isSignedIn()) { "Sign in to share through Firebase. Local export works offline." }
            val prepared = prepare(boxId)
            try {
                val code = storageManager.uploadBoxPackage(boxId, prepared.file)
                prepared.manifest.copy(transferCode = code)
            } finally { prepared.file.parentFile?.deleteRecursively() }
        }
    }

    suspend fun exportLocal(boxId: String, destination: Uri): BoxTransferManifest = withContext(Dispatchers.IO) {
        transfers.withLock {
            val prepared = prepare(boxId)
            try {
                context.contentResolver.openOutputStream(destination, "wt")?.use { output ->
                    prepared.file.inputStream().use { PortableArchive.copy(it, output, MAX_ARCHIVE_BYTES) }
                } ?: error("Cannot write the selected file")
                prepared.manifest
            } finally { prepared.file.parentFile?.deleteRecursively() }
        }
    }

    suspend fun import(transferCode: String): Box = withContext(Dispatchers.IO) {
        transfers.withLock {
            require(userHelper.isSignedIn()) { "Sign in to download from Firebase, or import a local file." }
            val directory = temporary(context.cacheDir)
            try {
                val file = storageManager.downloadBoxPackage(transferCode.trim(), File(directory, "package"))
                require(file.length() <= MAX_ARCHIVE_BYTES) { "Package is too large" }
                file.inputStream().use { importStream(it, transferCode.substringAfterLast('/')) }
            } finally { directory.deleteRecursively() }
        }
    }

    suspend fun importLocal(source: Uri): Box = withContext(Dispatchers.IO) {
        transfers.withLock {
            context.contentResolver.openInputStream(source)?.use { importStream(it) }
                ?: error("Cannot read the selected file")
        }
    }

    /** A recipient can explicitly fork a box without gaining the original publication identity. */
    suspend fun makeEditableCopy(boxId: String): Box = withContext(Dispatchers.IO) {
        transfers.withLock {
            appDatabase.withTransaction {
                val original = boxDao.find(boxId) ?: error("Box not found")
                require(original.createdBy == userHelper.getCurrentUserId()) { "This box belongs to another local account" }
                val copy = original.copy(id = 0, boxId = UUID.randomUUID().toString(),
                    boxName = "${original.boxName} (copy)", createdDate = System.currentTimeMillis())
                boxDao.insert(copy)
                val shares = shareDao.findAllInBoxForTransfer(boxId).map {
                    it.copy(id = 0, shareId = UUID.randomUUID().toString(), shareBoxId = copy.boxId)
                }
                shareDao.insertAll(*shares.toTypedArray())
                // A durable fork now protects these edits if the original receives a newer snapshot.
                states.find(boxId)?.takeIf { !it.isPublisher }?.let {
                    states.put(it.copy(localFingerprint = fingerprint(snapshot(boxId))))
                }
                copy
            }
        }
    }

    suspend fun keepOffline(boxId: String) = withContext(Dispatchers.IO) {
        transfers.withLock { localize(boxId) }
    }

    private suspend fun localize(boxId: String) {
        val before = snapshot(boxId)
        require(before.box.createdBy == userHelper.getCurrentUserId()) { "This box belongs to another local account" }
        val fingerprintBefore = fingerprint(before)
        val directory = temporary(File(context.filesDir, "box-imports"))
        var committed = false
        try {
            var changed = false
            val local = before.shares.map { share ->
                val data = mapAssets(share.shareData) { uri ->
                    if (uri.scheme == "https") {
                        coroutineContext.ensureActive()
                        val file = File(directory, UUID.randomUUID().toString())
                        copyAsset(uri, file)
                        changed = true
                        FileUtils.getUriFromFile(context, file, (share.shareData as? ShareData.ShareFile)?.fileName)
                    } else uri
                }
                share.copy(shareData = data)
            }
            if (!changed) return
            appDatabase.withTransaction {
                require(fingerprint(snapshot(boxId)) == fingerprintBefore) { "Box changed during download. Try again." }
                local.forEach { shareDao.update(it) }
                states.find(boxId)?.takeIf { it.localFingerprint == fingerprintBefore }?.let {
                    states.put(it.copy(localFingerprint = fingerprint(snapshot(boxId))))
                }
            }
            committed = true
        } finally {
            if (!committed) directory.deleteRecursively()
        }
    }

    private data class Snapshot(val box: Box, val shares: List<Share>)
    private data class Prepared(val file: File, val manifest: BoxTransferManifest)

    private suspend fun snapshot(boxId: String): Snapshot = appDatabase.withTransaction {
        Snapshot(boxDao.find(boxId) ?: error("Box not found"),
            shareDao.findAllInBoxForTransfer(boxId).sortedBy { it.shareId })
    }

    private suspend fun prepare(boxId: String): Prepared {
        localize(boxId)
        val snapshot = snapshot(boxId)
        require(snapshot.box.createdBy == userHelper.getCurrentUserId()) { "This box belongs to another local account" }
        val previous = states.find(boxId)
        require(previous == null || previous.isPublisher) {
            "This is an imported box. Make an editable copy before publishing your own version."
        }
        val before = fingerprint(snapshot)
        val directory = temporary(context.cacheDir)
        try {
            val assetFiles = linkedMapOf<String, File>()
            val shares = snapshot.shares.map { share ->
                coroutineContext.ensureActive()
                val portableData = mapAssets(share.shareData) { uri ->
                    val temp = File(directory, UUID.randomUUID().toString())
                    copyAsset(uri, temp)
                    val hash = PortableArchive.sha256(temp)
                    val path = "assets/$hash"
                    if (path in assetFiles) temp.delete() else assetFiles[path] = temp
                    require(assetFiles.values.sumOf { it.length() } <= PortableArchive.MAX_BYTES) { "Box exceeds 512 MB" }
                    Uri.parse(path)
                }
                BoxTransferShare(share.shareId, share.shareUserId, converter.shareDataToString(portableData),
                    share.isVideoShare, share.shareNote, share.shareDate, share.createdAt, share.updatedAt)
            }
            val old = previous?.let { parseManifest(it.manifestJson) }
            val manifest = if (previous?.localFingerprint == before && old != null) old else {
                val draft = BoxTransferManifest(
                    boxId = snapshot.box.boxId, boxName = snapshot.box.boxName, boxDesc = snapshot.box.boxDesc,
                    createdBy = old?.createdBy ?: UUID.randomUUID().toString(),
                    createdDate = snapshot.box.createdDate, exportedAt = System.currentTimeMillis(),
                    shares = shares, revisionId = "", ancestors = old?.let { it.ancestors + it.revisionId }.orEmpty(),
                    assets = assetFiles.map { (path, file) -> BoxTransferAsset(path, path.substringAfter('/'), file.length()) },
                )
                draft.copy(revisionId = revisionHash(draft))
            }
            validate(manifest)
            // Catch mutations to files while they were being packaged.
            require(manifest.assets.all { assetFiles[it.path]?.let { file ->
                file.length() == it.size && PortableArchive.sha256(file) == it.sha256
            } == true }) { "Files changed during export. Try again." }
            val archive = File(directory, "box.sharebox")
            archive.outputStream().use { PortableArchive.write(it, gson.toJson(manifest), assetFiles) }
            appDatabase.withTransaction {
                require(fingerprint(snapshot(boxId)) == before) { "Box changed during export. Try again." }
                states.put(BoxTransferState(boxId, gson.toJson(manifest), before, true))
            }
            return Prepared(archive, manifest)
        } catch (error: Throwable) {
            directory.deleteRecursively()
            throw error
        }
    }

    private suspend fun importStream(input: java.io.InputStream, expectedBoxId: String? = null): Box {
        // A unique durable directory prevents partial downloads from touching live assets.
        val directory = temporary(File(context.filesDir, "box-imports"))
        var committed = false
        try {
            val entries = PortableArchive.read(input, directory)
            val manifest = parseManifest(entries.getValue("manifest.json").readText())
            require(expectedBoxId == null || manifest.boxId == expectedBoxId) { "Package does not match the transfer code" }
            require(entries.keys == manifest.assets.map { it.path }.toSet() + "manifest.json") { "Package assets do not match the manifest" }
            manifest.assets.forEach { asset ->
                coroutineContext.ensureActive()
                val file = entries.getValue(asset.path)
                require(file.length() == asset.size && PortableArchive.sha256(file) == asset.sha256) { "File checksum mismatch" }
            }
            val imported = manifest.shares.map { item ->
                val portableData = converter.stringToShareData(item.shareData)
                val data = mapAssets(portableData) { uri ->
                    val path = uri.toString()
                    require(manifest.assets.any { it.path == path }) { "File is missing from this package" }
                    FileUtils.getUriFromFile(context, entries.getValue(path), (portableData as? ShareData.ShareFile)?.fileName)
                }
                Share(shareId = item.shareId, shareUserId = userHelper.getCurrentUserId(),
                    shareData = data, isVideoShare = item.isVideoShare, shareNote = item.shareNote,
                    shareBoxId = manifest.boxId, shareDate = item.shareDate, synced = true,
                    createdAt = item.createdAt, updatedAt = item.updatedAt)
            }
            var applied = false
            val result = appDatabase.withTransaction {
                val existing = boxDao.find(manifest.boxId)
                val previous = states.find(manifest.boxId)
                require(existing == null || existing.createdBy == userHelper.getCurrentUserId()) { "This box belongs to another local account" }
                if (previous != null) {
                    val old = parseManifest(previous.manifestJson)
                    require(old.createdBy == manifest.createdBy) { "Publisher identity does not match" }
                    when (RevisionPolicy.decide(old.ancestors + old.revisionId, manifest.ancestors + manifest.revisionId)) {
                        RevisionPolicy.Decision.SKIP -> return@withTransaction existing ?: error("Box was deleted locally. Make a copy to restore it.")
                        RevisionPolicy.Decision.CONFLICT -> error("Conflicting versions. Your local files were kept. Ask the publisher for an update from the same revision history.")
                        RevisionPolicy.Decision.APPLY -> Unit
                    }
                    require(!previous.isPublisher) { "Cannot replace the publishing device's box with an imported update" }
                    require(existing != null && fingerprint(snapshot(manifest.boxId)) == previous.localFingerprint) {
                        "This box has local edits. Your files were kept. Make an editable copy before resolving the conflict."
                    }
                } else {
                    require(existing == null) { "A local box already uses this ID. Make a copy before importing." }
                }
                // IDs are globally unique in the legacy schema; never replace another box's record.
                imported.forEach { item ->
                    val collision = shareDao.findOne(item.shareId)
                    require(collision == null || (collision.shareBoxId == manifest.boxId && collision.shareUserId == userHelper.getCurrentUserId())) {
                        "A file ID conflicts with another local box"
                    }
                }
                val now = System.currentTimeMillis()
                val box = Box(id = existing?.id ?: 0, boxId = manifest.boxId,
                    boxName = manifest.boxName, boxDesc = manifest.boxDesc,
                    createdBy = userHelper.getCurrentUserId(), createdDate = manifest.createdDate,
                    passcode = existing?.passcode, lastSeen = now, synced = true,
                    createdAt = existing?.createdAt ?: now, updatedAt = now)
                boxDao.upsert(box)
                val incomingIds = imported.map { it.shareId }.toSet()
                // Complete snapshots encode deletions by absence. Only a clean descendant can reach here.
                shareDao.findAllInBoxForTransfer(manifest.boxId).filter { it.shareId !in incomingIds }.forEach { shareDao.delete(it) }
                val rows = imported.map { it.copy(id = shareDao.findOne(it.shareId)?.id ?: 0,
                    tagId = shareDao.findOne(it.shareId)?.tagId) }
                shareDao.upsertAll(rows)
                states.put(BoxTransferState(manifest.boxId, gson.toJson(manifest), fingerprint(snapshot(manifest.boxId)), false))
                applied = true
                box
            }
            committed = applied
            return result
        } finally {
            if (!committed) directory.deleteRecursively()
        }
    }

    private fun parseManifest(json: String): BoxTransferManifest {
        val obj = gson.fromJson(json, com.google.gson.JsonObject::class.java)
        require(obj.get("schemaVersion")?.asInt == BoxTransferManifest.CURRENT_SCHEMA_VERSION) {
            "Unsupported package version. Ask the publisher to export a new .sharebox file."
        }
        return gson.fromJson(obj, BoxTransferManifest::class.java).also(::validate)
    }

    private fun validate(manifest: BoxTransferManifest) {
        require(UUID.fromString(manifest.boxId).toString() == manifest.boxId) { "Invalid box ID" }
        require(manifest.boxName.isNotBlank() && manifest.createdBy.isNotBlank()) { "Missing box metadata" }
        require(manifest.ancestors.size < 10000 && manifest.ancestors.distinct().size == manifest.ancestors.size)
        require(manifest.revisionId !in manifest.ancestors && manifest.ancestors.all { HASH.matches(it) })
        require(manifest.revisionId == revisionHash(manifest)) { "Manifest checksum mismatch" }
        require(manifest.shares.size < PortableArchive.MAX_ENTRIES && manifest.shares.map { it.shareId }.distinct().size == manifest.shares.size)
        require(manifest.shares.all { UUID.fromString(it.shareId).toString() == it.shareId }) { "Invalid file ID" }
        require(manifest.assets.map { it.path }.distinct().size == manifest.assets.size)
        require(manifest.assets.all { HASH.matches(it.sha256) && it.path == "assets/${it.sha256}" && it.size in 0..PortableArchive.MAX_BYTES })
    }

    private fun revisionHash(manifest: BoxTransferManifest) = hash(gson.toJson(manifest.copy(revisionId = "", transferCode = null)))
    private fun hash(value: String) = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

    private suspend fun fingerprint(snapshot: Snapshot): String {
        val records = snapshot.shares.map { share ->
            val data = mapAssets(share.shareData) { uri ->
                if (uri.scheme == "content" || uri.scheme == "file") {
                    val digest = MessageDigest.getInstance("SHA-256")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            coroutineContext.ensureActive()
                            val count = input.read(buffer)
                            if (count == -1) break
                            digest.update(buffer, 0, count)
                        }
                    } ?: error("A local file is missing")
                    Uri.parse("sha256:" + digest.digest().joinToString("") { "%02x".format(it) })
                } else uri
            }
            listOf(share.shareId, converter.shareDataToString(data), share.shareNote,
                share.isVideoShare, share.shareDate, share.createdAt, share.updatedAt)
        }
        return hash(gson.toJson(listOf(snapshot.box.boxName, snapshot.box.boxDesc, records)))
    }

    private suspend fun mapAssets(data: ShareData, transform: suspend (Uri) -> Uri): ShareData = when (data) {
        is ShareData.ShareImage -> data.copy(uri = transform(data.uri))
        is ShareData.ShareImages -> data.copy(uris = data.uris.map { transform(it) })
        is ShareData.ShareFile -> data.copy(uri = transform(data.uri))
        else -> data
    }

    private fun copyAsset(uri: Uri, destination: File) {
        destination.outputStream().use { output ->
            if (uri.scheme == "https") {
                // Materialize legacy cloud references so every export is self-contained.
                httpClient.newCall(Request.Builder().url(uri.toString()).build()).execute().use { response ->
                    check(response.isSuccessful) { "Could not download an existing cloud file" }
                    response.body?.byteStream()?.use { PortableArchive.copy(it, output) } ?: error("Empty cloud response")
                }
            } else {
                require(uri.scheme == "content" || uri.scheme == "file") { "Unsupported file source" }
                context.contentResolver.openInputStream(uri)?.use { PortableArchive.copy(it, output) }
                    ?: error("A local file is missing")
            }
        }
    }

    private fun temporary(parent: File): File = File(parent, UUID.randomUUID().toString()).also { check(it.mkdirs()) }

    private companion object {
        val HASH = Regex("[a-f0-9]{64}")
        const val MAX_ARCHIVE_BYTES = PortableArchive.MAX_BYTES + 16L * 1024 * 1024
    }
}
