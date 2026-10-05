# Local boxes and portable transfers

The library is local by default. Saving or editing a share does not upload its
contents. Legacy persisted sync workers complete without publishing data.

## User flow

- Open a box, unlock it if needed, and tap the transfer icon.
- **Export .sharebox file** saves a ZIP package using Android's document picker.
  Send that file through any sharing service. Sign-in is not required.
- Open **Transfer boxes** from the box list or settings to import a package.
- **Upload to Firebase** is explicit and requires sign-in. It publishes a complete
  package and displays a `Firebase UID/box UUID` transfer code. A recipient enters
  the code under **Download with transfer code** to download and import locally.
  Reusing the code fetches the last completed publication, not unpublished edits.
- **Download existing cloud files for offline use** materializes legacy HTTPS
  file references. Export also does this before creating a package. Ordinary
  web links remain links; ShareBox does not mirror linked websites.

## Versions and conflicts

Schema 2 stores a complete box snapshot, a SHA-256 manifest revision, its ordered
ancestry, and a size and SHA-256 digest for every asset. Older and identical
imports are skipped. Descendants are applied even when intermediate exports were
not imported. Divergent history is rejected instead of using device timestamps.
Deletions are represented by absence in a complete descendant snapshot.

The app only permits boxes marked locally as publishers to export updates under a box ID.
Imported content can be edited locally, but it cannot be republished under that
identity. A locally modified box rejects incoming updates. **Keep edits in an
independent copy** creates a new box and new share IDs, preserving file references
and edits. It also records that the original's current edits have been backed up,
allowing a newer publisher snapshot to update the original. Further edits after
making that copy are protected again. Open the independent copy to export it.

Import packages only from trusted senders: checksums detect corruption, not sender impersonation. Packages are not cryptographically signed. Exports are not encrypted by the box passcode. Packages have a 512 MiB unpacked
limit, a 4 MiB manifest limit, fewer than 10,000 entries, and bounded revision
history. These limits are checked before database replacement. Import uses a
fresh private directory, validates hashes and paths, then commits metadata and
transfer history in one Room transaction. Failed imports leave active files
untouched. Superseded local asset directories are retained because independent
copies may still reference them; reference-aware garbage collection is future
work. App uninstall removes the local library, so keep exported backups.

Room schema 8 adds `box_transfer_state` through a non-destructive 7→8 automatic
migration. Existing boxes and shares remain intact. Schema-1 cloud manifests are
not accepted by this new transfer path; publishers must export a new package.
Existing legacy cloud objects are not deleted.

## Firebase setup

Merge `firebase/storage.rules.example` into the bucket's existing rules and
review overlapping grants. This change does not deploy rules or modify existing
cloud objects. The new namespace is:

```
box-packages/{firebaseAuthUid}/{boxId}/latest.sharebox
```

A single completed package replaces the previous object; there are no mutable
remote asset paths shared across revisions. Only the publishing Firebase UID
should have write access. The example permits authenticated recipients to get
packages when they know the code and does not grant listing. Use membership
rules if recipients must be restricted to specific accounts. A transfer code
is not an expiring invitation, and downloaded copies cannot be revoked.

There is no background polling and no multi-device publisher coordination.
Firebase transfer needs testing against deployed rules before release.

## Build

Use JDK 17 with the existing Gradle wrapper:

```
./gradlew :app:assembleDevDebug
```

## Platform references

- [Cloud Storage object replacement is atomic](https://docs.cloud.google.com/storage/docs/objects).
- [Firebase Storage authentication and rule conditions](https://firebase.google.com/docs/storage/security).
