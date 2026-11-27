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

Run [#22](https://github.com/berkah-pos/btp/actions/runs/45981321510), commit `eb8309247e76f2bc73faac6d67809ff65652fc61`, completed successfully:
- 24 debug unit tests, no failures/errors/skips.
- Debug lint: 0 errors and 1 `OldTargetApi` warning at target API 36.
- Debug APK built.
- Merged release manifest and release runtime dependency inventory captured.
- Release audit confirms no INTERNET or personal-data runtime permission in the merged manifest; the only declared `uses-permission` is the app's signature-protected dynamic-receiver permission.
- Signed AAB job was skipped because this was a pull-request run.

Artifacts:
- [Debug APK, test/lint reports, schema v2, verification metadata, release audit](https://github.com/berkah-pos/btp/actions/runs/45981321510/artifacts/15189108166), 14-day retention.
- [Play Store listing graphics](https://github.com/berkah-pos/btp/actions/runs/45981321510/artifacts/15188978482), 90-day retention.

These artifacts are evidence for the exact commit above, not for later changes unless the workflow is rerun.

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

Workflow saat ini dapat menghasilkan `gradle/verification-metadata.xml` pada build pertama melalui `--write-verification-metadata sha256`. File harus diperiksa dan dikomit ke branch tepercaya sebelum release. Release job menolak berjalan tanpa file tersebut dan menjalankan build dengan `--dependency-verification=strict`. Sampai metadata hasil generator tersedia sebagai file repository yang ditinjau, dependency verification belum berstatus PASS.
