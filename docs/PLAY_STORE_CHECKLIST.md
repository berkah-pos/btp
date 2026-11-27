# Google Play release checklist

Status: pre-release review remains **BLOCKED**. Latest completed Android quality workflow is [run #114](https://github.com/berkah-pos/btp/actions/runs/46068726107) on source head `14ad3c822cbe1bfd1b9c8b47f3320b266740d6d5`; latest completed POS/Room instrumentation run is [#64](https://github.com/berkah-pos/btp/actions/runs/46068726094). All 35 JVM tests and all five API 36 emulator tests passed. Submission remains planned for a **Google Play Organization developer account**.

## Latest verified Android CI evidence — Run #114

- `testDebugUnitTest`: **35 tests, 0 failures, 0 errors, 0 skipped**, verified from seven uploaded JUnit XML reports.
- `compileDebugAndroidTestKotlin`: successful.
- `lintDebug`: **0 errors, 3 warnings** (`OldTargetApi` at target API 36; `AndroidGradlePluginVersion` recommends Gradle 9.9.3 and AGP 9.5.3/9.3.3).
- `assembleDebug`: successful; debug APK uploaded.
- APK ZIP alignment and all four packaged 64-bit native ELF 16 KiB checks passed.
- Merged release manifest and release runtime dependency inventory captured; no new ads, analytics, Firebase, crash-reporting, or HTTP-client SDK appears in the inspected runtime graph.
- Play Store icon and feature graphic rendered/uploaded.
- Signed release AAB and strict dependency-verification jobs were skipped on the PR event.

Artifacts:
- [Debug APK, JUnit/lint reports, Room schemas, and release audit](https://github.com/berkah-pos/btp/actions/runs/46068726107/artifacts/15232700275).
- [Play Store listing graphics](https://github.com/berkah-pos/btp/actions/runs/46068726107/artifacts/15232775176).

## POS and Room instrumentation — Run #64

- `connectedDebugAndroidTest`: **5 tests, 0 failures, 0 errors, 0 skipped**; API 36 emulator.
- Both original KSP v1 and historical intermediate v1 SQLite fixtures migrated through production `MIGRATION_1_2`.
- Three repository-level checkout tests cover duplicate cart-line aggregation, historical snapshots, stock-decrement rollback, and concurrent attempts to sell the final unit.
- [POS/Room instrumentation report](https://github.com/berkah-pos/btp/actions/runs/46068726094/artifacts/15232217427).

## Dependency verification review

- Candidate source: Android CI Run #110, commit `403fcf21a3b57037f6b24319431727ceda98e36a`, quality job successful.
- Source artifact ID `15230362895`, archive SHA-256 `93c62c7389f631186775b8c2fcc9345015cbd066fe1bdc990d3b6632c36238fb`; downloaded archive digest verified.
- Metadata SHA-256 `6d0c1f37295c615c9621fda3b6ea3a32ab0da463c117766ba6b6fe06795a08f3`: **578 components / 960 unique SHA-256 artifact entries**; Runs #111, #112, and #114 resolved the same metadata.
- Structural validation found no duplicate or malformed checksum entries. Run #110's graph differs from Run #106 because AGP 9.3.1 resolves a different build/test tooling graph; release runtime inventory was reviewed separately.
- `verify-metadata=true`, `verify-signatures=false`. The candidate baseline is not committed and clean strict dependency verification has **not passed**.

## Identitas dan kompatibilitas
- [ ] Konfirmasi applicationId candidate `id.berkahtigaputeri.basicpos` dan periksa keunikan di Play Console.
- [x] Tipe akun untuk submission: **Organization** (dikonfirmasi pemilik proyek; verifikasi account profile tetap harus diselesaikan di Play Console).
- [x] Syarat closed testing khusus akun Personal baru (12 tester/14 hari): **NOT APPLICABLE** untuk submission melalui akun Organization ini. Persyaratan resmi 12/14 hari dibatasi pada akun Personal yang dibuat setelah 13 November 2023. Lihat [persyaratan testing resmi Google Play](https://support.google.com/googleplay/android-developer/answer/14151465).
- [x] `targetSdk=36`; memenuhi minimum API 36 yang berlaku pada tanggal pemeriksaan.
- [x] Merged release manifest: `minSdk=24`, `targetSdk=36`; compile SDK 37.1 pada konfigurasi build.
- [x] Jalankan dan periksa dua Room migration instrumentation test dengan `connectedDebugAndroidTest` pada API 36 cloud emulator: Run #64, 2/2 lulus.
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
- [x] Jalankan unit test debug variant: 35 lulus, 0 gagal pada Run #114.
- [x] Jalankan lint debug (0 error; 3 warnings: OldTargetApi dan dua AndroidGradlePluginVersion) pada Run #114.
- [x] Bangun debug APK pada GitHub-hosted runner; artifact diunggah pada Run #114.
- [x] Capture merged release manifest dan release runtime dependency inventory pada Run #114.
- [x] Review artifact candidate Run #110: 578 komponen / 960 artifact; archive SHA-256 93c62c7389f631186775b8c2fcc9345015cbd066fe1bdc990d3b6632c36238fb; XML SHA-256 6d0c1f37295c615c9621fda3b6ea3a32ab0da463c117766ba6b6fe06795a08f3. Ini baseline kandidat yang dipin dari Run #110 untuk toolchain AGP 9.3.1 / Gradle 9.7.0; belum dikomit dan clean strict verification belum lulus.
- [ ] Commit baseline metadata ke branch feature dan jalankan clean strict dependency verification; workflow guarded manual disiapkan, tetapi belum dijalankan.
- [x] Verify debug APK 16 KB ZIP alignment dan seluruh empat native ELF LOAD alignment pada Run #114.
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
- Room migration instrumentation test 1 → 2 **PASS** pada Run #64 (5 test total, termasuk 2 migrasi dan 3 checkout, 0 failure/error/skipped) di API 36 emulator. Run #14 sebelumnya gagal di jalur serializer `MigrationTestHelper`; test kini memakai fixture SQLite historis dan Room production migration.
- Dependency baseline guard kini menunjuk artifact Run #110, tetapi baseline belum dikomit dan clean strict gate belum PASS. Metadata Run #98 berasal dari toolchain sebelumnya dan bukan reproduksi baseline kandidat ini.
- Signed release AAB, signature verification, dan release SHA-256 belum tersedia.
- Belum ada bukti startup/device/UI test, Internal Testing, atau Pre-launch Report. Unit test JVM dan build CI tidak menggantikan pemeriksaan tersebut.
- Data Safety, Ads, target audience, content rating, dan reviewer access declaration belum diselesaikan di Play Console.
