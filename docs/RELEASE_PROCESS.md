# Proses release cloud

## CI dan runner
- Semua job memakai standard GitHub-hosted `ubuntu-24.04`; self-hosted runner dilarang.
- Repository ini sengaja public. Standard runners public repository tidak dikenakan biaya per menit, tetapi artifact storage tetap berkuota.
- Pull request menuju `main` menjalankan unit test, lint, dan debug build. Push langsung ke `main` juga menjalankan quality workflow; branch feature yang memiliki PR tidak menjalankan build push duplikat.
- Quality job juga memproses merged release manifest dan mengekspor `releaseRuntimeClasspath` dependency inventory ke `release-audit/` dalam debug artifact. Ini memungkinkan review permission dan SDK tanpa membuka signing secrets.
- Release AAB hanya melalui `workflow_dispatch` di branch `main` dengan input `build_release=true`.
- Workflow dibatasi permission `contents: read`; secret hanya tersedia di job release melalui Environment.
- Build tidak pernah memublikasikan otomatis ke Google Play.

## Latest audit checkpoint — 27 November 2026

**Android CI [run #58](https://github.com/berkah-pos/btp/actions/runs/45999675603)** completed successfully for PR head `aeb015914455606e7b9c985a36e011dbcb3a6137`:
- 24 JVM unit tests: **0 failures, 0 errors, 0 skipped**, verified from four JUnit XML files.
- Debug lint: **0 errors, 1 `OldTargetApi` warning** at target API 36.
- Debug APK built and uploaded.
- APK ZIP alignment and native ELF 16 KiB checks passed.
- Merged release manifest and runtime dependency inventory captured.
- Play Store icon and feature graphic rendered/uploaded.
- Signed AAB job skipped, as expected for a pull-request run.

Artifacts:
- [Debug APK, unit/lint reports, Room schemas, dependency metadata, release audit](https://github.com/berkah-pos/btp/actions/runs/45999675603/artifacts/15198380221), 14-day retention.
- [Play Store listing graphics](https://github.com/berkah-pos/btp/actions/runs/45999675603/artifacts/15198001347), 90-day retention.

**Room Migration Tests [run #17](https://github.com/berkah-pos/btp/actions/runs/45999675609)** completed successfully on the API 36 hosted emulator:
- `connectedDebugAndroidTest`: **2 tests, 0 failures, 0 errors, 0 skipped**; instrumentation exit code 0.
- Original KSP v1 layout → v2: passed, with legacy sale and receipt item values preserved.
- Historical intermediate v1 layout → v2: passed, retaining product ID and all existing receipt snapshots.
- Room opened each v1 SQLite database through the production `MIGRATION_1_2` and validated the final schema.

[Room migration JUnit report and APKs](https://github.com/berkah-pos/btp/actions/runs/45999675609/artifacts/15197568120), 30-day retention.

The previous Room run #14 reached the instrumentation task but failed before migration assertions because `MigrationTestHelper` hit a Kotlin Serialization generated-serializer `AbstractMethodError`. The test implementation now creates the two historical SQLite v1 fixtures directly and uses `Room.databaseBuilder(...).addMigrations(MIGRATION_1_2)`; Run #17 verifies the real migration and Room schema validation.

**Dependency verification review:** Run #58 produced metadata SHA-256 `3a9e25b7fb3c0ed4eb21bae8f74e83d1cd2f4c157f03a870e942b6550d0b15c0`, with 462 components and 758 unique SHA-256 artifact entries. All 758 entries shared with Run #41 match byte-for-byte; the only three entries present in the older inventory were Room Testing artifacts removed with the helper dependency. This is a reproducibility/integrity review, not publisher-signature proof (`verify-signatures=false`). The baseline is reviewed but not yet committed; clean strict verification is still pending.

## 16 KB page-size checks

The quality job now runs `zipalign -c -P 16 -v 4` on the debug APK and checks `PT_LOAD` alignment for any packaged `arm64-v8a` or `x86_64` shared libraries. The signed AAB job also requires bundletool to report `PAGE_ALIGNMENT_16K` and runs the same ELF audit on the bundle. The debug ZIP and native ELF alignment checks **passed** on Run #58. The signed AAB check has not run because PR workflows deliberately skip release signing.

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

- Bootstrap source: Android CI Run #58 artifact `berkah-pos-debug-58`.
- Reviewed metadata SHA-256: `3a9e25b7fb3c0ed4eb21bae8f74e83d1cd2f4c157f03a870e942b6550d0b15c0`.
- Inventory: **462 components / 758 unique SHA-256 artifact entries**, no missing or duplicate artifact hashes.
- Independent comparison with the prior Run #41 metadata: all 758 shared component/artifact coordinates have identical checksums. The three entries removed from the older inventory belong to `androidx.room:room-testing` / `room-testing-android`, no longer used by instrumentation tests.
- The metadata sets `verify-metadata=true` and `verify-signatures=false`. It pins artifact bytes for subsequent resolution; it does not authenticate publisher signatures or prove the initial downloads were signed by their publishers.
- A guarded `workflow_dispatch` input `commit_dependency_baseline=true` is added to `.github/workflows/android-ci.yml`. It is restricted to `feat/basic-pos-mvp`, downloads the same-run quality artifact, checks the reviewed SHA-256 and XML inventory, and commits only `gradle/verification-metadata.xml` using a separate job with `contents: write`.
- A separate clean GitHub-hosted runner then runs JVM tests, instrumentation source compilation, lint, debug build, release manifest processing, and the release runtime dependency report using `--dependency-verification=strict`.
- Do not treat the baseline as completed until that clean strict job passes. Dependency graph changes require renewed metadata review before the baseline is updated.

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

`gradle/verification-metadata.xml` has not yet been committed. Run #38 and #41 generated identical metadata, but strict dependency verification is still **NOT PASS** until the baseline is reviewed, committed, and a subsequent strict-mode quality build succeeds.
