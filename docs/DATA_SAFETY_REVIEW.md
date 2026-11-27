# Google Play Data Safety — source and build review

Status: **NOT SUBMITTED / NOT FINAL**. The evidence below is a source/build review, not a completed Play Console declaration.

## Latest release-variant evidence

GitHub Actions [run #79](https://github.com/berkah-pos/btp/actions/runs/46018281138), source head `cbeba6fea7576b6557e9e1b935a8f66a8104db91`, captured and uploaded:
- `release-audit/merged-release-manifest.xml`
- `release-audit/release-runtime-dependencies.txt`
- `release-audit/manifest-source-paths.txt`
- `release-audit/SHA256SUMS`

The merged release manifest reports candidate package `id.berkahtigaputeri.basicpos`, `minSdk=24`, `targetSdk=36`, and compile SDK 37.1. It contains no `INTERNET`, contacts, phone, camera, location, or broad-storage permission. The only `uses-permission` entry requests the app's own `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`, protected with signature-level permission. AndroidX Startup, Room's non-exported multi-instance invalidation service, and the AndroidX ProfileInstaller receiver are present.

The release manifest sets `android:allowBackup="false"`, uses explicit full-backup and data-extraction rule files, and sets `android:usesCleartextTraffic="false"`.

The Run #79 `releaseRuntimeClasspath` inventory contains AndroidX/Jetpack, Compose, Activity, Splashscreen, Navigation, Lifecycle, Room/SQLite, DataStore, Kotlin/Coroutines, Okio, Guava ListenableFuture, annotations, and JSpecify. No Firebase, analytics, Crashlytics, advertising SDK, OkHttp, Retrofit, or other HTTP client was identified in the captured graph. Re-check the exact release graph if dependencies, manifest, or application code change.

Artifacts:
- [Debug APK and release audit](https://github.com/berkah-pos/btp/actions/runs/46018281138/artifacts/15208270198).
- [Play Store icon and feature graphic](https://github.com/berkah-pos/btp/actions/runs/46018281138/artifacts/15207651178).

This is a debug-variant source/build audit, not a signed AAB inspection. The release-signing job was skipped for the PR event. Run #79 completed 29 unit tests (0 failures/errors/skips), lint (0 errors, 1 warning), debug APK build, and the 16 KiB APK/native-library checks. It does not prove signed AAB compliance or device behavior.

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
- Dependency verification metadata from Run #79 has SHA-256 `b75c079d854746d701e5312fd11745038d7f1d2052d6818c81e8f69ff812eec6`: 459 components and 754 unique SHA-256 artifact entries. All 754 current coordinates/artifacts match the previous Run #58 inventory; four obsolete plugin metadata entries were removed. The file remains an uncommitted bootstrap artifact; strict dependency verification was skipped and Gradle signature verification is disabled (`verify-signatures=false`).
