# Google Play release checklist

Status: pre-release review remains **BLOCKED**. Latest verified source/build evidence is GitHub Actions [run #41, attempt 2](https://github.com/berkah-pos/btp/actions/runs/45989244818), completed successfully on 27 November 2026. The validated PR head was `1365bab63bb91032b56bb4279f6dec212574d778`; the runner built merge commit `e4074059ef147bc64302431c7cee579679d169da`. The owner has confirmed that submission will use a **Google Play Organization developer account**.

## Latest verified CI evidence

- `testDebugUnitTest`: **24 tests, 0 failures, 0 errors, 0 skipped**, verified from four uploaded JUnit XML reports.
- `compileDebugAndroidTestKotlin`: **success**. This proves instrumentation test sources compile; it does not prove the migration tests ran.
- `lintDebug`: **0 errors, 1 warning** (`OldTargetApi` for `targetSdk=36`).
- `assembleDebug`: **success**; debug APK (14,944,217 bytes) uploaded.
- APK ZIP alignment check: **success**; four packaged native libraries across arm64-v8a/x86_64 passed ELF LOAD 16 KiB alignment checks.
- Merged release manifest and release runtime dependency inventory: **captured successfully**. The manifest has no `INTERNET`, contacts, phone, camera, location, or broad-storage permission; the only declared permission is the app's signature-protected dynamic-receiver permission.
- Captured runtime dependency inventory showed no Firebase, analytics, Crashlytics, advertising SDK, OkHttp, or Retrofit.
- Play Store icon 512×512 and feature graphic 1024×500: rendered and uploaded successfully.
- Signed release AAB: **SKIPPED** because the validated run was a pull request. No signed release AAB or upload-certificate evidence exists.
- Strict dependency verification: **SKIPPED**. The generated SHA-256 metadata is an unreviewed bootstrap artifact and is not a trusted committed baseline.

Artifacts:
- [Debug APK, unit-test/lint reports, Room schemas, bootstrap verification metadata, and release audit](https://github.com/berkah-pos/btp/actions/runs/45989244818/artifacts/15193663522) — 14-day retention.
- [Play Store icon and feature graphic](https://github.com/berkah-pos/btp/actions/runs/45989244818/artifacts/15192494773) — 90-day retention.

Room schema v1 from the original KSP export, schema v2, and two migration instrumentation test cases now exist in the PR branch. The PR CI compiled those test sources, but did **not** execute `connectedDebugAndroidTest`. A dedicated API 36 emulator workflow definition was added as `.github/workflows/room-migration-tests.yml`; there is not yet an emulator result to mark PASS.

This is source/CI evidence only. It is **not** evidence of successful physical-device startup, full UI automation, Play Console submission, Internal Testing, Pre-launch Report, or Google Play approval.

## Identitas dan kompatibilitas
- [ ] Konfirmasi applicationId candidate `id.berkahtigaputeri.basicpos` dan periksa keunikan di Play Console.
- [x] Tipe akun untuk submission: **Organization** (dikonfirmasi pemilik proyek; verifikasi account profile tetap harus diselesaikan di Play Console).
- [x] Syarat closed testing khusus akun Personal baru (12 tester/14 hari): **NOT APPLICABLE** untuk submission melalui akun Organization ini. Persyaratan resmi 12/14 hari dibatasi pada akun Personal yang dibuat setelah 13 November 2023. Lihat [persyaratan testing resmi Google Play](https://support.google.com/googleplay/android-developer/answer/14151465).
- [x] `targetSdk=36`; memenuhi minimum API 36 yang berlaku pada tanggal pemeriksaan.
- [x] Merged release manifest: `minSdk=24`, `targetSdk=36`; compile SDK 37.1 pada konfigurasi build.
- [ ] Jalankan dan periksa dua Room migration instrumentation test dengan `connectedDebugAndroidTest` pada API 36 cloud emulator (workflow terpisah).
- [ ] Siapkan screenshot aktual dari aplikasi hasil build; aset icon/feature graphic bukan pengganti screenshot aplikasi.

## Organization developer account verification
Google Play's current organization onboarding requires the organization identity in the linked Google payments profile, including a **D-U-N-S number**, legal organization name, and address. Keep the legal name and address in the Google payments profile consistent with the Dun & Bradstreet profile. Google may also require:
- [ ] D-U-N-S number for BERKAH TIGA PUTERI's legal organization (government-organization exception does not apply unless the account is a qualifying government entity).
- [ ] Official legal organization document (for example, business registration/incorporation evidence accepted for the registered country).
- [ ] Government-issued identity document for a person authorized to represent the organization.
- [ ] Verified organization phone number and organization website.
- [ ] Verified developer contact email and phone number; keep them operational.
- [ ] Google payments profile linked and verified; organization legal name/address match the D-U-N-S profile.
- [ ] Public developer profile contact details reviewed, including the organization phone number and email displayed on Google Play.

Google states that D-U-N-S registration is free and can take up to 30 days. Start this identity work early. Read [required organization account information](https://support.google.com/googleplay/android-developer/answer/13628312) and [organization identity verification](https://support.google.com/android-developer-console/answer/16641416).

## Keamanan dan privasi
- [x] Inspeksi merged release manifest: tidak ada INTERNET atau permission sensitif; satu app-defined signature permission.
- [x] Inspeksi `releaseRuntimeClasspath`: tidak teridentifikasi SDK ads, analytics, Firebase, crash-reporting, atau HTTP client.
- [x] Automatic Android backup dinonaktifkan dan aturan backup/extraction mengecualikan data aplikasi.
- [ ] Selesaikan Privacy Policy Bahasa Indonesia dan host di URL HTTPS publik tanpa login.
- [ ] Lengkapi identitas legal pengendali, alamat korespondensi, dan kontak privasi/dukungan.
- [ ] Isi Data Safety sesuai perilaku aktual, termasuk ekspor JSON dan pembagian struk yang dimulai pengguna.
- [ ] Isi deklarasi Ads setelah pemeriksaan final atas in-app promotion dan binary.
- [ ] Verifikasi perilaku backup/restore dan transfer file pada perangkat/API yang didukung.

## Build dan signing
- [x] Jalankan unit test debug variant: 24 lulus, 0 gagal pada Run #41 attempt 2.
- [x] Jalankan lint debug (0 error; 1 warning OldTargetApi) pada Run #41 attempt 2.
- [x] Bangun debug APK pada GitHub-hosted runner; artifact diunggah pada Run #41.
- [x] Capture merged release manifest dan release runtime dependency inventory pada Run #41.
- [ ] Review dan commit `gradle/verification-metadata.xml`; jalankan ulang dengan strict dependency verification. Run #38 dan #41 menghasilkan bootstrap metadata byte-identical (SHA-256 `45c3de7dd53c00bc4a35afd64a2067fa05d10fe8ab8cb7ef22e86342ebb941b0`, 464 komponen, 761 hash SHA-256). Reproducibility bukan bukti signature publisher; metadata belum dikomit dan strict verification masih dilewati.
- [x] Verify debug APK 16 KB ZIP alignment and packaged 64-bit ELF LOAD alignment pada Run #41.
- [ ] Verify the signed release AAB reports `PAGE_ALIGNMENT_16K` and passes the native ELF audit when an authorized release build is eventually run.
- [ ] Bangun release AAB dengan upload key yang disimpan hanya di GitHub Environment Secrets.
- [ ] Validasi AAB dengan bundletool.
- [ ] Verifikasi signature dan upload certificate fingerprint.
- [ ] Hasilkan SHA256SUMS dan metadata versi untuk release AAB.
- [ ] Unduh dan uji artefak release yang dibuat oleh workflow.

## Store listing dan review
- [x] Draft nama, short description, full description, dan release notes tersedia di `docs/STORE_LISTING.md`.
- [x] Source app icon dan feature graphic tersedia; CI berhasil merender keduanya.
- [ ] Siapkan screenshot aktual, support contact, dan website.
- [ ] Lengkapi target audience, content rating, Ads, Data Safety, dan App access di Play Console.
- [ ] Jelaskan bahwa reviewer tidak memerlukan akun dan fungsi POS inti berjalan offline.
- [ ] Kirim ke Internal Testing untuk QA (rekomendasi kualitas, bukan persyaratan closed testing personal 12/14 hari).
- [ ] Periksa Pre-launch Report dan uji startup/alur utama pada perangkat.
- [ ] Selesaikan semua blocker sebelum mengajukan production.

## Blocker yang sudah diketahui
- Application ID masih kandidat dan belum dikonfirmasi/terdaftar di Play Console.
- D-U-N-S number, linked/verified Google payments profile, organization legal documents, authorized representative ID, verified organization phone, and organization website have not yet been confirmed.
- Identitas legal perusahaan, alamat korespondensi, dan kontak privasi resmi belum diberikan untuk Privacy Policy.
- Privacy Policy belum di-host di URL HTTPS publik.
- Upload key dan empat GitHub Environment secrets belum tersedia.
- Room migration instrumentation test 1 → 2 belum dieksekusi pada emulator. Schema v1 asli sudah tersedia; jangan menandai migrasi PASS sampai kedua skenario uji berhasil dijalankan pada API 36.
- Dependency verification metadata belum direview/commit; strict gate belum PASS.
- Signed release AAB, signature verification, dan release SHA-256 belum tersedia.
- Belum ada bukti startup/device/UI test, Internal Testing, atau Pre-launch Report. Unit test JVM dan build CI tidak menggantikan pemeriksaan tersebut.
- Data Safety, Ads, target audience, content rating, dan reviewer access declaration belum diselesaikan di Play Console.
