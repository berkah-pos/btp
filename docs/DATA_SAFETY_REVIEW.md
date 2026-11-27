# Google Play Data Safety — source and build review

Status: **NOT SUBMITTED / NOT FINAL**. The evidence below is a source/build review, not a completed Play Console declaration.

## Latest release-variant evidence

GitHub Actions [run #102](https://github.com/berkah-pos/btp/actions/runs/46050284320), source/documentation head `a34e0e1f05aff9aa5cf39ccdb839f5e07fa60bfb`, captured and uploaded:
- Merged release manifest: `minSdk=24`, `targetSdk=36`, `allowBackup=false`, cleartext traffic disabled, and no `INTERNET` permission.
- Release runtime dependency inventory for review; no Firebase, analytics, Crashlytics, advertising SDK, OkHttp, Retrofit, or other HTTP client was identified in this captured graph.
- Debug APK, lint report, and test reports.
- APK ZIP alignment and four packaged 64-bit native library ELF 16 KiB checks passed.

Run #102 completed **34 JVM tests** (0 failures/errors/skips), including three strict UTF-8 backup-decoder tests; lint (0 errors, 2 warnings), instrumentation-source compilation, debug APK build, APK alignment, and four native-library 16 KiB checks. The release-signing job was skipped for the PR event. This does not prove signed AAB compliance or physical-device behavior.

POS and Room Instrumentation Tests [run #53](https://github.com/berkah-pos/btp/actions/runs/46050284322) completed **4 API 36 emulator tests** with zero failures/errors/skips. Two tests cover Room v1→v2 migration; two cover duplicate checkout lines, historical receipt snapshots, and rollback after a conditional stock update failure. The strict UTF-8 decoder and all three decoder tests are included in the successful Android CI #102 run.

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
- Run #96's pinned metadata SHA-256 is `b75c079d854746d701e5312fd11745038d7f1d2052d6818c81e8f69ff812eec6` (459 components / 754 unique SHA-256 artifact entries). The source artifact archive digest was verified, and Runs #98, #99, #100, and #102 regenerated byte-identical metadata. The baseline remains uncommitted, clean strict verification has not passed, and Gradle signature verification is disabled (`verify-signatures=false`).

