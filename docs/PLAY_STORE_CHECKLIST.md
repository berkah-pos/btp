# Google Play release checklist

Status: pre-release review remains **BLOCKED**. Latest fully successful Android quality workflow is [run #80](https://github.com/berkah-pos/btp/actions/runs/46019306696) for source head `5a96fc363e7dcdd4b765ac27b94274d9473c9e06`; latest successful Room migration run is [#35](https://github.com/berkah-pos/btp/actions/runs/46019306650). Later workflow and accessibility commits are awaiting fresh CI verification. Submission remains planned for a **Google Play Organization developer account**.

## Latest verified Android CI evidence — Run #80

- `testDebugUnitTest`: **29 tests, 0 failures, 0 errors, 0 skipped**, verified from five uploaded JUnit XML reports.
- `compileDebugAndroidTestKotlin`: successful.
- `lintDebug`: **0 errors, 1 warning** (`OldTargetApi` for `targetSdk=36`).
- `assembleDebug`: **success**; debug APK uploaded.
- Debug APK ZIP alignment and all four packaged 64-bit native-library 16 KiB ELF checks: **success**.
- Merged release manifest and release runtime dependency inventory: **captured successfully**.
- Play Store app icon 512×512 and feature graphic 1024×500: rendered and uploaded successfully.
- Signed release AAB: **SKIPPED** because this was a pull-request run. No signed release AAB or upload-certificate evidence exists.

Artifacts:
- [Debug APK, unit-test/lint reports, Room schemas, dependency metadata, and release audit](https://github.com/berkah-pos/btp/actions/runs/46019306696/artifacts/15208022108) — 14-day retention.
- [Play Store icon and feature graphic](https://github.com/berkah-pos/btp/actions/runs/46019306696/artifacts/15207772322) — 90-day retention.

## Room migration instrumentation — Run #35

- `connectedDebugAndroidTest` executed on the API 36 cloud emulator and completed **success**.
- JUnit XML confirms **2 tests, 0 failures, 0 errors, 0 skipped**; instrumentation exit code is `0`.
- Original Room v1 schema migration: **PASS**; sales and receipt item data were preserved.
- Historical intermediate v1 migration: **PASS**; existing product ID and receipt snapshots were preserved.
- Both real SQLite v1 fixtures were opened through production `MIGRATION_1_2`; Room validated the resulting v2 schema.

Artifact:
- [API 36 emulator JUnit report, APKs, and outputs](https://github.com/berkah-pos/btp/actions/runs/46019306650/artifacts/15207827414).

## Dependency verification review

- Run #80 metadata: SHA-256 `b75c079d854746d701e5312fd11745038d7f1d2052d6818c81e8f69ff812eec6`, **459 components / 754 unique SHA-256 artifact entries**.
- Refreshed metadata from independent Runs #81 and #82 was byte-identical: SHA-256 `9adc4cacddb4fd405061e425103a801240f1f0db2c42a22ac9ed8dede7bd76ba`, **455 components / 745 unique SHA-256 artifact entries**. Nine stale metadata entries were absent from the refreshed resolution; all 745 remaining entries retained the same checksum as Run #80.
- The full #81/#82 dispatch workflows failed at the baseline guard because it still expected the previous Run #80 hash/inventory; their Android quality jobs succeeded. The guard was corrected in commit `b06c8f3`.
- Commit `681cf52` makes metadata bootstrap always use `--refresh-dependencies` to avoid cache-dependent inventory drift.
- Metadata enables `verify-metadata=true` but keeps `verify-signatures=false`. This is integrity pinning, **not publisher-signature verification**.
- The reviewed baseline is not committed and clean `--dependency-verification=strict` has **not yet passed**. A guarded manual workflow dispatch is still required to commit the baseline and run the separate clean strict build.

This is source/CI evidence only.

This is source/CI evidence only. It is **not** evidence of physical-device startup, full UI automation, Play Console submission, Internal Testing, Pre-launch Report, or Google Play approval.

## Identitas dan kompatibilitas
- [ ] Konfirmasi applicationId candidate `id.berkahtigaputeri.basicpos` dan periksa keunikan di Play Console.
- [x] Tipe akun untuk submission: **Organization** (dikonfirmasi pemilik proyek; verifikasi account profile tetap harus diselesaikan di Play Console).
- [x] Syarat closed testing khusus akun Personal baru (12 tester/14 hari): **NOT APPLICABLE** untuk submission melalui akun Organization ini. Persyaratan resmi 12/14 hari dibatasi pada akun Personal yang dibuat setelah 13 November 2023. Lihat [persyaratan testing resmi Google Play](https://support.google.com/googleplay/android-developer/answer/14151465).
- [x] `targetSdk=36`; memenuhi minimum API 36 yang berlaku pada tanggal pemeriksaan.
- [x] Merged release manifest: `minSdk=24`, `targetSdk=36`; compile SDK 37.1 pada konfigurasi build.
- [x] Jalankan dan periksa dua Room migration instrumentation test dengan `connectedDebugAndroidTest` pada API 36 cloud emulator: Run #35, 2/2 lulus.
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
- [x] Jalankan unit test debug variant: 29 lulus, 0 gagal pada Run #80.
- [x] Jalankan lint debug (0 error; 1 warning OldTargetApi) pada Run #79.
- [x] Bangun debug APK pada GitHub-hosted runner; artifact diunggah pada Run #79.
- [x] Capture merged release manifest dan release runtime dependency inventory pada Run #79.
- [x] Review refreshed metadata Runs #81/#82: 455 komponen dan 745 checksum unik; seluruh 745 entri bersama memiliki checksum sama dengan Run #80, dan sembilan entri stale tidak lagi dipakai. Metadata SHA-256 `9adc4cacddb4fd405061e425103a801240f1f0db2c42a22ac9ed8dede7bd76ba`. Ini bukan bukti signature publisher (`verify-signatures=false`).
- [ ] Commit baseline metadata ke branch feature dan jalankan clean strict dependency verification; workflow guarded manual disiapkan, tetapi belum dijalankan.
- [x] Verify debug APK 16 KB ZIP alignment and packaged 64-bit ELF LOAD alignment pada Run #79.
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
- Room migration instrumentation test 1 → 2 **PASS** pada Run #35 (2 test, 0 failure/error/skipped) di API 36 emulator. Run #14 sebelumnya gagal di jalur serializer `MigrationTestHelper`; test kini memakai fixture SQLite historis dan Room production migration.
- Dependency verification metadata telah direview dari Run #79 tetapi belum dikomit; strict gate belum PASS.
- Signed release AAB, signature verification, dan release SHA-256 belum tersedia.
- Belum ada bukti startup/device/UI test, Internal Testing, atau Pre-launch Report. Unit test JVM dan build CI tidak menggantikan pemeriksaan tersebut.
- Data Safety, Ads, target audience, content rating, dan reviewer access declaration belum diselesaikan di Play Console.
