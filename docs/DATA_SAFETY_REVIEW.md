# Google Play Data Safety — source and build review

Status: **NOT SUBMITTED / NOT FINAL**. The evidence below is a source/build review, not a completed Play Console declaration.

## Latest release-variant evidence

Android CI [run #116](https://github.com/berkah-pos/btp/actions/runs/46076835718) completed successfully on source head `86be0f748a17442358ad3c787fae485fc8909158`. Its uploaded artifact was downloaded and independently inspected:
- Merged release manifest: `minSdk=24`, `targetSdk=36`, `allowBackup=false`, cleartext traffic disabled, and no `INTERNET` permission; the only declared permission is the app-defined signature permission for dynamic receivers.
- Release runtime dependency inventory captured; no Firebase, analytics, Crashlytics, advertising SDK, OkHttp, Retrofit, or other HTTP client was identified by the targeted dependency review.
- `testDebugUnitTest`: **35 tests, 0 failures, 0 errors, 0 skipped**, verified from seven JUnit XML reports.
- `compileDebugAndroidTestKotlin`: successful; `lintDebug`: **0 errors, 3 warnings** (`OldTargetApi` and two `AndroidGradlePluginVersion` recommendations).
- Debug APK built and uploaded. APK ZIP alignment and all four packaged 64-bit native ELF 16 KiB checks passed.
- Play Store icon and feature graphic rendered and uploaded.
- Unsigned release AAB candidate was built, validated by Bundletool 1.18.3, and passed the `PAGE_ALIGNMENT_16K` plus four 64-bit native ELF checks. It is not signed or release-ready.
- Signed release AAB and clean strict dependency-verification jobs were skipped on the pull-request event; this is not signed-AAB or strict-verification evidence.

Artifacts:
- [Debug APK, JUnit/lint reports, Room schemas, and release audit](https://github.com/berkah-pos/btp/actions/runs/46076835718/artifacts/15236081149).
- [Play Store listing graphics](https://github.com/berkah-pos/btp/actions/runs/46076835718/artifacts/15236021402).
- [Unsigned AAB candidate](https://github.com/berkah-pos/btp/actions/runs/46076835718/artifacts/15235407844), extracted AAB SHA-256 `4cd9ec06112cb9506f90fafd7530fa324d1d7d6e3327fa0f7973675de31c2a6f`.

POS and Room Instrumentation Tests [run #66](https://github.com/berkah-pos/btp/actions/runs/46076835597) completed successfully on the API 36 GitHub-hosted emulator:
- **5 tests, 0 failures, 0 errors, 0 skipped**, verified from the uploaded JUnit XML; instrumentation exit code 0.
- Two original/historical Room v1 SQLite fixtures migrated through production `MIGRATION_1_2`.
- Three repository-level checkout tests cover duplicate-line aggregation and receipt snapshots, rollback after a conditional stock-decrement failure, and concurrent checkout of the final stock unit (one receipt, one rejection, zero stock, one transaction).
- [POS/Room instrumentation report and emulator artifacts](https://github.com/berkah-pos/btp/actions/runs/46076835597/artifacts/15236175799).

The debug release-variant manifest and dependency inventory are inspection evidence for this source head, not a signed release binary or a completed Play Console declaration.

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
- Signed release AAB, upload-certificate verification, and physical-device verification remain outstanding. The unsigned PR candidate is not a substitute.
- The reviewed dependency candidate remains Run #110: 578 components / 960 unique SHA-256 artifact entries, metadata SHA-256 `6d0c1f37295c615c9621fda3b6ea3a32ab0da463c117766ba6b6fe06795a08f3`. Android CI #116 independently regenerated byte-identical metadata. The baseline is still uncommitted and clean strict verification has not passed; Gradle publisher-signature verification remains disabled (`verify-signatures=false`).
