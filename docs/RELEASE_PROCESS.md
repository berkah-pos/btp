# Proses release cloud

## CI dan runner
- Semua job memakai standard GitHub-hosted `ubuntu-24.04`; self-hosted runner dilarang.
- Repository ini sengaja public. Standard runners public repository tidak dikenakan biaya per menit, tetapi artifact storage tetap berkuota.
- Pull request menuju `main` menjalankan unit test, lint, dan debug build. Push langsung ke `main` juga menjalankan quality workflow; branch feature yang memiliki PR tidak menjalankan build push duplikat.
- Quality bootstrap retries known transient Compose Compiler and Android Gradle Plugin marker repository-resolution failures up to five attempts with bounded backoff (15/30/60/90 seconds); all other build failures fail immediately.
- Quality job juga memproses merged release manifest dan mengekspor `releaseRuntimeClasspath` dependency inventory ke `release-audit/` dalam debug artifact. Ini memungkinkan review permission dan SDK tanpa membuka signing secrets.
- **Signed** release AAB hanya melalui `workflow_dispatch` di branch `main` dengan input `build_release=true`. Pull-request quality workflow juga membangun kandidat AAB **unsigned** untuk validasi Bundletool dan 16 KiB; kandidat tersebut tidak ditandatangani dan tidak dipublikasikan.
- Workflow dibatasi permission `contents: read`; secret hanya tersedia di job release melalui Environment.
- Build tidak pernah memublikasikan otomatis ke Google Play.

## Latest audit checkpoint — 28 November 2026

**Android CI [run #116](https://github.com/berkah-pos/btp/actions/runs/46076835718)** completed successfully on source head `86be0f748a17442358ad3c787fae485fc8909158`:
- `testDebugUnitTest`: **35 tests, 0 failures, 0 errors, 0 skipped**, verified from seven JUnit XML files.
- `compileDebugAndroidTestKotlin`: successful.
- Debug lint: **0 errors, 3 warnings** (`OldTargetApi` at target API 36; `AndroidGradlePluginVersion` recommends newer Gradle 9.9.3 and AGP 9.5.3/9.3.3).
- Debug APK built and uploaded.
- `zipalign -c -P 16 -v 4` and all four packaged 64-bit native ELF 16 KiB checks passed.
- Merged release manifest, `releaseRuntimeClasspath` inventory, and Play Store graphics captured.
- Unsigned release AAB candidate built and uploaded; Bundletool 1.18.3 validation succeeded, `dump config` contained `PAGE_ALIGNMENT_16K`, and all four packaged 64-bit native ELF LOAD segments passed the 16 KiB audit. The candidate is not signed.
- Signed release AAB, reviewed-baseline strict verification, and clean strict verification were skipped.

Artifacts:
- [Debug APK, JUnit/lint reports, Room schemas, and release audit](https://github.com/berkah-pos/btp/actions/runs/46076835718/artifacts/15236081149).
- [Play Store listing graphics](https://github.com/berkah-pos/btp/actions/runs/46076835718/artifacts/15236021402).
- [Unsigned AAB candidate](https://github.com/berkah-pos/btp/actions/runs/46076835718/artifacts/15235407844); extracted AAB SHA-256: `4cd9ec06112cb9506f90fafd7530fa324d1d7d6e3327fa0f7973675de31c2a6f`.

**POS and Room Instrumentation Tests [run #66](https://github.com/berkah-pos/btp/actions/runs/46076835597)** completed successfully on the API 36 hosted emulator:
- `connectedDebugAndroidTest`: **5 tests, 0 failures, 0 errors, 0 skipped**.
- Two historical v1 SQLite fixtures migrated through production `MIGRATION_1_2`; Room validated schema v2.
- Three checkout tests cover duplicate-line aggregation, rollback after a conditional stock-decrement failure, and concurrent checkout of the last stock unit.
- [POS/Room instrumentation JUnit report and emulator outputs](https://github.com/berkah-pos/btp/actions/runs/46076835597/artifacts/15236175799).

All three strict UTF-8 backup-decoder tests passed in Android CI #116.

## 16 KB page-size checks

The quality job runs `zipalign -c -P 16 -v 4` on the debug APK and checks `PT_LOAD` alignment for packaged `arm64-v8a` or `x86_64` shared libraries. The PR quality job also builds an unsigned release AAB candidate, runs Bundletool `validate`, requires `dump config` to report `PAGE_ALIGNMENT_16K`, and audits native ELF alignment. Debug APK ZIP alignment, AAB page-alignment configuration, and all four packaged 64-bit native library checks **passed** on Run #116. This does not verify a signed AAB, upload certificate, or Play-distributed APK.

## Bundletool integrity

Bundletool 1.18.3 is downloaded from the official Google release URL. The workflow now checks the JAR SHA-256 before execution: `a099cfa1543f55593bc2ed16a70a7c67fe54b1747bb7301f37fdfd6d91028e29` (official GitHub release asset digest). This digest pin was added after Run #116; its first hosted-runner verification is pending on the new workflow commit.

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

The domain-derived candidate/permanent application ID is `id.co.berkahtigaputeri.basicpos` from `berkahtigaputeri.co.id`. The Kotlin source namespace remains `id.berkahtigaputeri.basicpos` to avoid an unnecessary package refactor. Confirm the candidate is available in Play Console and that no app has already been published under the previous ID. Configure repository Actions Variable `BTP_APPLICATION_ID` to exactly `id.co.berkahtigaputeri.basicpos`; Gradle fails closed on a mismatch and the signed release workflow also enforces the exact value. The value becomes permanent after first publication; do not change it later.

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

The reviewed candidate is pinned to Android CI Run #110 on source commit `403fcf21a3b57037f6b24319431727ceda98e36a`:
- Source run: [Android CI #110](https://github.com/berkah-pos/btp/actions/runs/46064751388), workflow ID `432452966`, run ID `46064751388`, branch `feat/basic-pos-mvp`; quality job `140727247533` succeeded.
- Source artifact: `berkah-pos-debug-110`, artifact ID `15230362895`, archive SHA-256 `93c62c7389f631186775b8c2fcc9345015cbd066fe1bdc990d3b6632c36238fb`.
- The archive digest was independently checked after download. Its `gradle/verification-metadata.xml` SHA-256 is `6d0c1f37295c615c9621fda3b6ea3a32ab0da463c117766ba6b6fe06795a08f3`, with **578 components / 960 unique SHA-256 artifact entries**. Android CI #111, #112, and #116 produced the same metadata hash and inventory.
- Structural validation found no duplicate component/artifact coordinates, malformed SHA-256 values, or unexpected checksum origins. `verify-metadata=true`; `verify-signatures=false`.
- Compared with Run #106 (AGP 9.5.3, 462/758), Run #110 (AGP 9.3.1, 578/960) adds 175 components, removes 59, and adds one artifact hash to a shared component. Most changes are build/AGP and Android Test Platform dependency graph changes. The separate release runtime inventory does not show added app runtime SDKs.
- The workflow guard is pinned to Run #110, its source commit, quality job, archive digest, metadata SHA, and 578/960 inventory. The baseline is not committed and `--dependency-verification=strict` has **not passed**.
- SHA-256 pinning is not publisher-signature authentication; signature verification remains disabled.

## Output dan retensi
- Debug APK, hasil test, lint, dan laporan: artifact dengan retensi 14 hari.
- Unsigned PR AAB candidate: artifact dengan retensi 30 hari. Signed release AAB, SHA256SUMS, metadata, upload certificate, signature check, dan build report: artifact dengan retensi 90 hari.
- AAB tidak dikomit ke source tree.
- GitHub Free artifact storage repository: 500 MB; retensi maksimum public repository: 90 hari. Pantau pemakaian dan hapus artifact kedaluwarsa bila dibutuhkan.
- GitHub Release pada repository public akan bersifat public. Jangan menyimpan artefak rahasia atau data pelanggan di Releases. Gunakan Actions Artifacts sebagai distribusi build; setiap artefak release yang dibagikan dari repository ini harus dianggap dapat diakses publik.

## Signing dan Play App Signing
Hanya signed release AAB yang dibuat pada manual release workflow menggunakan upload key di GitHub Environment Secrets; PR candidate AAB tetap unsigned. Google Play App Signing harus diaktifkan/diterima di Play Console. Verifikasi certificate fingerprint dari signed AAB dan cocokkan dengan upload certificate yang terdaftar di Play Console. Jangan mengklaim signing terverifikasi sebelum output pemeriksaan tersedia.

## Gradle Wrapper
Workflow menginstal Gradle 9.7.0 secara eksplisit pada GitHub-hosted runner, lalu membangkitkan wrapper dan menjalankan `./gradlew`. Wrapper distribution URL dipatok pada `gradle/wrapper/gradle-wrapper.properties`. Wrapper scripts/JAR hasil bootstrap disertakan dalam debug artifact untuk inspeksi, tidak disimpan sebagai hasil build ke source tree.

## Dependency verification baseline

The baseline candidate and provenance are the Run #110 artifact described in [Dependency verification](#dependency-verification). Its archive digest and 578/960 checksum inventory were independently checked. The guarded workflow is pinned to this exact source, but `gradle/verification-metadata.xml` remains absent from the branch and strict mode remains **NOT PASS**.
