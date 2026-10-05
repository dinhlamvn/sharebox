package com.dinhlam.sharebox.transfer

import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** Bounded, flat asset namespace: no archive-controlled filesystem paths. */
object PortableArchive {
    const val MAX_BYTES = 512L * 1024 * 1024
    const val MAX_MANIFEST_BYTES = 4L * 1024 * 1024
    const val MAX_ENTRIES = 10000
    private val assetName = Regex("assets/[a-f0-9]{64}")

    fun validEntry(name: String) = name == "manifest.json" || assetName.matches(name)

    fun copy(input: InputStream, output: OutputStream, limit: Long = MAX_BYTES): Long {
        var total = 0L
        val buffer = ByteArray(64 * 1024)
        while (true) {
            val count = input.read(buffer)
            if (count == -1) return total
            total += count
            require(total <= limit) { "Package exceeds the supported size limit" }
            output.write(buffer, 0, count)
        }
    }

    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val count = input.read(buffer)
                if (count == -1) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun write(output: OutputStream, manifest: String, assets: Map<String, File>) {
        val bytes = manifest.toByteArray(Charsets.UTF_8)
        require(bytes.size <= MAX_MANIFEST_BYTES && assets.size < MAX_ENTRIES)
        require(assets.values.sumOf { it.length() } + bytes.size <= MAX_BYTES)
        ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry("manifest.json"))
            zip.write(bytes)
            zip.closeEntry()
            assets.forEach { (name, file) ->
                require(assetName.matches(name)) { "Invalid asset name" }
                zip.putNextEntry(ZipEntry(name))
                file.inputStream().use { copy(it, zip) }
                zip.closeEntry()
            }
        }
    }

    /** Caller owns a fresh staging directory and cleans it on every failure. */
    fun read(input: InputStream, directory: File): Map<String, File> {
        require(directory.isDirectory && directory.list().orEmpty().isEmpty())
        val entries = linkedMapOf<String, File>()
        var total = 0L
        ZipInputStream(input).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                require(!entry.isDirectory && validEntry(entry.name)) { "Invalid package entry" }
                require(entry.name !in entries && entries.size < MAX_ENTRIES) {
                    "Duplicate entry or too many files"
                }
                val file = File(directory, entry.name)
                check(file.parentFile!!.isDirectory || file.parentFile!!.mkdirs())
                val limit = if (entry.name == "manifest.json") {
                    minOf(MAX_MANIFEST_BYTES, MAX_BYTES - total)
                } else MAX_BYTES - total
                total += file.outputStream().use { copy(zip, it, limit) }
                zip.closeEntry() // Also verifies the entry CRC.
                entries[entry.name] = file
            }
        }
        require("manifest.json" in entries) { "Missing package manifest" }
        return entries
    }
}
