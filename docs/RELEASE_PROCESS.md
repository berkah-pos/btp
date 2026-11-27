# Proses release cloud

## CI dan runner
- Semua job memakai standard GitHub-hosted `ubuntu-24.04`; self-hosted runner dilarang.
- Repository ini sengaja public. Standard runners public repository tidak dikenakan biaya per menit, tetapi artifact storage tetap berkuota.
- Pull request menuju `main` menjalankan unit test, lint, dan debug build. Push langsung ke `main` juga menjalankan quality workflow; branch feature yang memiliki PR tidak menjalankan build push duplikat.
- Quality bootstrap retries only the known transient Compose compiler plugin repository-resolution failure up to five attempts with bounded backoff (15/30/60/90 seconds); all other build failures fail immediately.
- Quality job juga memproses merged release manifest dan mengekspor `releaseRuntimeClasspath` dependency inventory ke `release-audit/` dalam debug artifact. Ini memungkinkan review permission dan SDK tanpa membuka signing secrets.
- Release AAB hanya melalui `workflow_dispatch` di branch `main` dengan input `build_release=true`.
- Workflow dibatasi permission `contents: read`; secret hanya tersedia di job release melalui Environment.
- Build tidak pernah memublikasikan otomatis ke Google Play.

## Latest audit checkpoint — 27 November 2026

**Android CI [run #98](https://github.com/berkah-pos/btp/actions/runs/46034740526)** completed successfully for source head `81b9cef36414019822ee1b2e42e31449fb43c229`:
- `testDebugUnitTest`: **31 tests, 0 failures, 0 errors, 0 skipped**, verified from six JUnit XML files.
- `compileDebugAndroidTestKotlin`: successful.
- Debug lint: **0 errors, 2 warnings** (`OldTargetApi` for target API 36 and `AndroidGradlePluginVersion` because Gradle 9.9.2 has a newer 9.9.3 patch).
- Debug APK built and uploaded.
- `zipalign -c -P 16 -v 4` and all four packaged 64-bit native ELF 16 KiB checks passed.
- Merged release manifest and release runtime dependency inventory captured.
- Play Store icon and feature graphic rendered/uploaded.
- Signed AAB job skipped, as expected for a pull-request run.

Artifacts:
- [Debug APK, JUnit/lint reports, Room schemas, and release audit](https://github.com/berkah-pos/btp/actions/runs/46034740526/artifacts/15215569160).
- [Play Store listing graphics](https://github.com/berkah-pos/btp/actions/runs/46034740526/artifacts/15216955134).

**Room Migration Tests [run #49](https://github.com/berkah-pos/btp/actions/runs/46034740545)** completed successfully on the API 36 hosted emulator:
- `connectedDebugAndroidTest`: **2 tests, 0 failures, 0 errors, 0 skipped**.
- Original KSP v1 and historical intermediate v1 layouts migrated through production `MIGRATION_1_2`.
- Existing sales and receipt data were preserved and Room validated schema v2.
- [Room migration JUnit report, APKs, and emulator outputs](https://github.com/berkah-pos/btp/actions/runs/46034740545/artifacts/15215524359).

Two additional repository-level POS checkout instrumentation tests are included in the current commit. They are **pending CI** and are not included in Run #49.

## 16 KB page-size checks

The quality job runs `zipalign -c -P 16 -v 4` on the debug APK and checks `PT_LOAD` alignment for packaged `arm64-v8a` or `x86_64` shared libraries. The signed AAB job also requires bundletool to report `PAGE_ALIGNMENT_16K` and audits native ELF alignment. Debug ZIP alignment and all four packaged 64-bit native library checks **passed** on Run #98. The signed AAB check has not run because PR workflows deliberately skip release signing.

## Organization Play Console account

The owner confirmed that the app will be submitted through a Google Play Organization developer account. The personal-account-only closed-testing gate (12 opted-in testers for 14 continuous days for new Personal accounts created after 13 November 2023) is not an Organization-account submission requirement. Internal testing and the Pre-launch Report remain recommended quality checks.

Before release, complete organization verification in Play Console:
- D-U-N-S number and matching legal organization name/address in the linked Google payments profile.
- Official organization registration document and identity document for an authorized representative, if requested by Google.
- Verified organization phone number, verified developer email/phone, and official organization website.
- Confirm public developer-profile contact details.

Google says D-U-N-S registration is free and can take up to 30 days. Use the official [organization account information requirements](https://support.google.com/googleplay/android-developer/answer/13628312) and [identity verification guidance](https://support.google.com/android-developer-console/answer/16641416).

## Public repository variables

Optional repository Actions Variables:
- `BTP_SAAS_URL`: HTTPS URL for the BERKAH TIGA PUTERI SaaS information page.
- `BTP_SUPPORT_EMAIL`: official support email address.

The Gradle build accepts these through `ORG_GRADLE_PROJECT_btpSaasUrl` and `ORG_GRADLE_PROJECT_btpSupportEmail`. Both default to empty; the app displays no external destination until real values are configured. The SaaS URL is restricted to HTTPS. Do not populate these variables with a placeholder or an unverified company address.

## Permanent application ID

Before a signed release, confirm the permanent application ID in Play Console and configure the repository Actions Variable `BTP_APPLICATION_ID`. The Gradle build defaults to the documented candidate for debug builds, but the signed release workflow fails when this variable is empty. The value becomes permanent after first publication; do not change it later.

## GitHub Environment `android-release`

Buat dan konfigurasi environment melalui Settings → Environments → android-release:
1. Batasi deployment branches/tags agar hanya branch `main` yang diizinkan.
2. Aktifkan required reviewer/approval manual sebelum job release dapat mengakses secret.
3. Tambahkan secrets berikut:
   - `ANDROID_UPLOAD_KEYSTORE_BASE64`
   - `ANDROID_UPLOAD_STORE_PASSWORD`
   - `ANDROID_UPLOAD_KEY_ALIAS`
   - `ANDROID_UPLOAD_KEY_PASSWORD`

Keystore harus dibuat/dipegang pemilik aplikasi melalui proses aman. Jangan pernah commit keystore atau password. Workflow gagal secara eksplisit jika secret belum tersedia; tidak membuat atau mengklaim upload key secara otomatis. Jangan pernah mengirim secret ke workflow pull request atau fork.

## Dependency verification

The current candidate is pinned to the refreshed, reproducible Android CI Run #96 source:
- Source run: [Android CI #96](https://github.com/berkah-pos/btp/actions/runs/46032064890), workflow ID `432452966`, run ID `46032064890`, branch `feat/basic-pos-mvp`, commit `0739913749fa0783a9d6784ad7444bccbca3fa3f`; its quality job succeeded.
- Source artifact: `berkah-pos-debug-96`, artifact ID `15215416046`, archive SHA-256 `fde80191ddb1b52e9388615ac1ab0a27f566422c903547bf1f2b4b258ca67947`.
- The archive digest was independently checked after download. Its `gradle/verification-metadata.xml` SHA-256 is `b75c079d854746d701e5312fd11745038d7f1d2052d6818c81e8f69ff812eec6`, with **459 components / 754 unique artifact SHA-256 entries**.
- Android CI Run #98 regenerated byte-identical metadata from a refreshed dependency resolution.
- Manual dispatch Run #95 failed because its older guard expected `9adc4cac…` (455/745), while the generated file was `b75c079d…` (459/754). The current branch workflow now pins Run #96, the artifact archive digest, the XML hash, and the matching inventory; commit `81b9cef` corrected the source run number in the provenance manifest.
- Metadata keeps `verify-metadata=true` and `verify-signatures=false`. SHA-256 pinning detects artifact changes after review; it does not authenticate publisher identity.
- The baseline file is not committed. A clean `--dependency-verification=strict` build has **not passed**. Do not mark strict verification enabled until the exact baseline is committed and a clean runner completes strict quality gates.

## Output dan retensi
- Debug APK, hasil test, lint, dan laporan: artifact dengan retensi 14 hari.
- Release AAB, SHA256SUMS, metadata, sertifikat upload, signature check, dan build report: artifact dengan retensi 90 hari.
- AAB tidak dikomit ke source tree.
- GitHub Free artifact storage repository: 500 MB; retensi maksimum public repository: 90 hari. Pantau pemakaian dan hapus artifact kedaluwarsa bila dibutuhkan.
- GitHub Release pada repository public akan bersifat public. Jangan menyimpan artefak rahasia atau data pelanggan di Releases. Gunakan Actions Artifacts sebagai distribusi build; setiap artefak release yang dibagikan dari repository ini harus dianggap dapat diakses publik.

## Signing dan Play App Signing
AAB ditandatangani menggunakan upload key yang disimpan di GitHub Environment Secrets. Google Play App Signing harus diaktifkan/diterima di Play Console. Verifikasi certificate fingerprint dari AAB dan cocokkan dengan upload certificate yang terdaftar di Play Console. Jangan mengklaim signing terverifikasi sebelum output pemeriksaan tersedia.

## Gradle Wrapper
Workflow menginstal Gradle 9.9.2 secara eksplisit pada GitHub-hosted runner, lalu membangkitkan wrapper dan menjalankan `./gradlew`. Wrapper distribution URL dipatok pada `gradle/wrapper/gradle-wrapper.properties`. Wrapper scripts/JAR hasil bootstrap disertakan dalam debug artifact untuk inspeksi, tidak disimpan sebagai hasil build ke source tree.

## Dependency verification baseline

The baseline candidate and provenance are the Run #96 artifact described in [Dependency verification](#dependency-verification). It has been independently checked and reproduced by Run #98, but `gradle/verification-metadata.xml` remains absent from the branch and strict mode remains **NOT PASS**.

