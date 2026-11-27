# Google Play Data Safety — source and build review

Status: **NOT SUBMITTED / NOT FINAL**. The evidence below is a source/build review, not a completed Play Console declaration.

## Latest release-variant evidence

GitHub Actions [run #89](https://github.com/berkah-pos/btp/actions/runs/46026654646), source head `b8bdd55c519efc081fa96f4ef5443482d86a3c14`, captured and uploaded:
- `release-audit/merged-release-manifest.xml`
- `release-audit/release-runtime-dependencies.txt`
- `release-audit/manifest-source-paths.txt`
- `release-audit/SHA256SUMS`

The merged release manifest reports candidate package `id.berkahtigaputeri.basicpos`, `minSdk=24`, `targetSdk=36`, and compile SDK 37.1. It contains no `INTERNET`, contacts, phone, camera, location, or broad-storage permission. The only `uses-permission` entry requests the app's own `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`, protected with signature-level permission. AndroidX Startup, Room's non-exported multi-instance invalidation service, and the AndroidX ProfileInstaller receiver are present.

The release manifest sets `android:allowBackup="false"`, uses explicit full-backup and data-extraction rule files, and sets `android:usesCleartextTraffic="false"`.

The Run #89 `releaseRuntimeClasspath` inventory was captured from the PR head. It contains AndroidX/Jetpack, Compose, Activity, Splashscreen, Navigation, Lifecycle, Room/SQLite, DataStore, Kotlin/Coroutines, Okio, Guava ListenableFuture, annotations, and JSpecify. No Firebase, analytics, Crashlytics, advertising SDK, OkHttp, Retrofit, or other HTTP client was identified in this captured graph. Re-check the exact release graph if dependencies, manifest, or application code change.

Artifacts:
- [Debug APK and release audit](https://github.com/berkah-pos/btp/actions/runs/46026654646/artifacts/15212576547).
- [Play Store icon and feature graphic](https://github.com/berkah-pos/btp/actions/runs/46026654646/artifacts/15212238982).

This is a debug-variant source/build audit, not a signed AAB inspection. The release-signing job was skipped for the PR event. Run #89 completed 29 unit tests (0 failures/errors/skips), lint (0 errors, 1 warning), debug APK build, and the 16 KiB APK/native-library checks. It does not prove signed AAB compliance or device behavior.

Commit `8269159` adds only application-level serialization of backup/restore and SettingsScreen data mutations plus JVM gate tests. It is newer than Run #89 and has not yet been validated by a runner; rerun the release manifest/dependency capture for the eventual release commit.

## Observasi implementasi

- Profil toko, kategori, produk, stok, header transaksi, item transaksi, and low-stock threshold preferences are stored locally using Room/SQLite and Preferences DataStore.
- No account or registration flow exists.
- The app does not declare the Android INTERNET permission and POS workflows are designed to work offline.
- Manual JSON backup uses Android Storage Access Framework. The user chooses the destination or source file. Backup JSON is not encrypted by the app.
- Receipt sharing uses Android's system chooser after the user selects Share; the user selects the receiving app.
- The SaaS information link opens an external browser only when a real HTTPS URL has been configured. Support email opens the user's selected email app.
- The app does not upload POS data to a BERKAH TIGA PUTERI backend.

## Interpretasi kebijakan untuk diverifikasi

Google Play's Data Safety rules distinguish data processed only on-device from data collected off-device. User-initiated transfers may qualify for a sharing exclusion when the transfer is expected and explicitly initiated by the user. The manual backup export and receipt share appear to be user-initiated, but the final interpretation must be checked against current Play Console wording and the actual release behavior. This document is not a final declaration.

## Candidate declaration, subject to final binary and console review

- Data collected off-device by BERKAH POS: candidate **No**.
- Data shared by developer-operated services: candidate **No**; user-directed export/share must be assessed under the user-initiated transfer rule.
- Data processed locally: shop profile, categories, products, SKU, prices, stock, sales, receipt details, and low-stock preferences.
- Data in transit: the app has no INTERNET permission in the inspected merged release manifest. JSON backups are not encrypted by the app at rest; the app does not upload them.
- Account deletion: not applicable while no account/login exists. The app has no in-app wipe-all-data control; users can clear app storage or uninstall through Android.
- SDKs: release runtime inventory reviewed as above. Re-review all transitive dependencies on every dependency change.

## Verifikasi wajib sebelum release

1. Re-run manifest and dependency capture for the exact release commit.
2. Review source code for any new network-capable APIs, external intents, and URI grants.
3. Inspect export/restore and receipt-sharing behavior on a device.
4. Confirm Privacy Policy wording and legal controller details.
5. Complete and save the Data Safety form in Play Console; this document is not a substitute for the form.
6. Complete the Ads declaration based on the final binary and informational SaaS page.
7. Revisit these declarations for every release that changes data flows or dependencies.

## Blocker privasi yang diketahui

- URL HTTPS kebijakan privasi publik belum tersedia.
- Kontak privasi dan alamat resmi pengendali belum tersedia.
- Data Safety and Ads declarations have not yet been submitted in Play Console.
- Signed release AAB and device verification remain outstanding.
- Refreshed dependency verification metadata from Runs #81 and #82 is byte-identical: SHA-256 `9adc4cacddb4fd405061e425103a801240f1f0db2c42a22ac9ed8dede7bd76ba`, 455 components and 745 unique SHA-256 artifact entries. All 745 entries shared with Run #80 retain identical checksums; nine stale metadata entries are absent from the refreshed resolution. The baseline remains uncommitted, clean strict verification has not passed, and Gradle signature verification is disabled (`verify-signatures=false`).

