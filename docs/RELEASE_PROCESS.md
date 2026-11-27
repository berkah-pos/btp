# Proses release cloud

## CI dan runner
- Semua job memakai standard GitHub-hosted `ubuntu-24.04`; self-hosted runner dilarang.
- Repository ini sengaja public. Standard runners public repository tidak dikenakan biaya per menit, tetapi artifact storage tetap berkuota.
- Pull request menuju `main` menjalankan unit test, lint, dan debug build. Push langsung ke `main` juga menjalankan quality workflow; branch feature yang memiliki PR tidak menjalankan build push duplikat.
- Quality job juga memproses merged release manifest dan mengekspor `releaseRuntimeClasspath` dependency inventory ke `release-audit/` dalam debug artifact. Ini memungkinkan review permission dan SDK tanpa membuka signing secrets.
- Release AAB hanya melalui `workflow_dispatch` di branch `main` dengan input `build_release=true`.
- Workflow dibatasi permission `contents: read`; secret hanya tersedia di job release melalui Environment.
- Build tidak pernah memublikasikan otomatis ke Google Play.

## Latest audit checkpoint

GitHub Actions [run #50](https://github.com/berkah-pos/btp/actions/runs/45992766270) completed successfully on 27 November 2026 for PR head `0ab07e7c5f4a77307d1349029fbbf73452c85c80`:
- 24 debug JVM unit tests, no failures/errors/skips (verified from four JUnit XML files).
- `compileDebugAndroidTestKotlin` passed; Room instrumentation sources compiled, but the migration tests did not run.
- Debug lint: 0 errors and 1 `OldTargetApi` warning at target API 36.
- Debug APK built and uploaded (14,944,217 bytes).
- 16 KiB APK ZIP alignment passed; all four packaged native ELF LOAD segments passed.
- Merged release manifest and runtime dependency inventory captured.
- Play Store app icon and feature graphic rendered/uploaded.
- Signed AAB job was skipped because this was a pull-request run.

Artifacts:
- [Debug APK, test/lint reports, Room schemas, verification metadata, release audit](https://github.com/berkah-pos/btp/actions/runs/45992766270/artifacts/15195500151), 14-day retention.
- [Play Store listing graphics](https://github.com/berkah-pos/btp/actions/runs/45992766270/artifacts/15194771833), 90-day retention.

These artifacts validate PR head `0ab07e7...`. Later commits changed the Room workflow and documentation; they did not change application source or release configuration.

The Room migration workflow [run #9](https://github.com/berkah-pos/btp/actions/runs/45992766223) failed before instrumentation tests because the x86_64 emulator could not access `/dev/kvm`. The uploaded startup diagnostic explicitly reported missing KVM permissions and required hardware acceleration. Commit `f1585bd` adds a per-run ACL for the hosted runner user, checks emulator acceleration, and bounds ADB device wait. [Run #10](https://github.com/berkah-pos/btp/actions/runs/45997083558) is the verification attempt; the migration remains **NOT PASS** until `connectedDebugAndroidTest` completes and both migration tests pass.

The original KSP Room v1 schema is committed at [`be1797a`](https://github.com/berkah-pos/btp/commit/be1797ad5ff9eab6bca125d4fdb784589edb6647), with schema v2 and tests for the original and intermediate v1 layouts. This resolves the earlier fixture-availability blocker; execution on the emulator remains pending.

## 16 KB page-size checks

The quality job now runs `zipalign -c -P 16 -v 4` on the debug APK and checks `PT_LOAD` alignment for any packaged `arm64-v8a` or `x86_64` shared libraries. The signed AAB job also requires bundletool to report `PAGE_ALIGNMENT_16K` and runs the same ELF audit on the bundle. The debug ZIP and native ELF alignment checks **passed** on Run #41. The signed AAB check has not run because PR workflows deliberately skip release signing.

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
- `gradle/verification-metadata.xml` harus dihasilkan dari dependency resolution cloud pertama, ditinjau, lalu dikomit sebagai baseline tepercaya.
- Runs #38 and #41 produced byte-identical bootstrap metadata (SHA-256 `45c3de7dd53c00bc4a35afd64a2067fa05d10fe8ab8cb7ef22e86342ebb941b0`): 464 components and 761 SHA-256 artifact entries. The coordinates and configured repositories were reviewed for plausibility, but byte-for-byte reproducibility is not independent publisher-signature proof. The file is still uncommitted and strict verification remains skipped.
- Jangan menganggap metadata yang dibuat dan langsung diverifikasi dalam job yang sama sebagai baseline tepercaya; itu hanya bootstrap TOFU.
- Setelah metadata disetujui dan dikomit, aktifkan `--dependency-verification=strict` untuk seluruh quality dan release build.
- Setiap perubahan dependency harus melalui review metadata sebelum masuk branch utama.

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
