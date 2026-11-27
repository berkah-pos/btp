# Google Play release checklist

Status: pre-release review remains **BLOCKED**. The latest evidence is GitHub Actions run [#22](https://github.com/berkah-pos/btp/actions/runs/45981321510), commit `eb8309247e76f2bc73faac6d67809ff65652fc61`, on 27 November 2026.

## Latest verified CI evidence

- `testDebugUnitTest`: **24 tests, 0 failures, 0 errors, 0 skipped**.
- `lintDebug`: **0 errors, 1 warning** (`OldTargetApi` for `targetSdk=36`).
- `assembleDebug`: **success**; debug APK and test/lint reports uploaded.
- Google Play icon and feature graphic rendering/upload: **success**.
- Release manifest processing: **success**. The merged release manifest was saved in the debug Actions Artifact for inspection.
- Release runtime dependency inventory: **success**. The captured graph contains AndroidX/Jetpack, Compose, Navigation, Lifecycle, Room, SQLite, DataStore, Kotlin/Coroutines, Okio, and Guava ListenableFuture. No advertising, analytics, Firebase, crash-reporting, or HTTP client SDK was identified in the reviewed release runtime graph.
- Merged release manifest inspection: package candidate `id.berkahtigaputeri.basicpos`, `minSdk=24`, `targetSdk=36`; no `INTERNET`, contacts, phone, camera, location, or broad-storage permission. The only declared `uses-permission` is the app's own signature-protected dynamic-receiver permission.
- Signed release AAB: **SKIPPED** because this is a pull-request workflow.
- Strict dependency verification: **SKIPPED**. The generated SHA-256 metadata remains an unreviewed bootstrap baseline and is not committed.

Artifacts:
- [Debug APK, reports, Room schema v2, dependency metadata, and release audit](https://github.com/berkah-pos/btp/actions/runs/45981321510/artifacts/15189108166) — 14-day retention.
- [Play Store icon and feature graphic](https://github.com/berkah-pos/btp/actions/runs/45981321510/artifacts/15188978482) — 90-day retention.

This is CI and source-level evidence. It is **not** evidence of signed AAB validity, installation/startup on physical devices, UI automation, Play Console submission, or Google Play approval.

## Identitas dan kompatibilitas
- [ ] Konfirmasi applicationId candidate `id.berkahtigaputeri.basicpos` dan periksa keunikan di Play Console.
- [x] `targetSdk=36`; memenuhi minimum API 36 yang berlaku pada tanggal pemeriksaan.
- [x] Merged release manifest: `minSdk=24`, `targetSdk=36`; compile SDK 37.1 pada konfigurasi build.
- [ ] Uji startup, navigasi, transaksi, backup/restore, dan share struk pada perangkat/API yang didukung.
- [ ] Siapkan screenshot aktual dari aplikasi hasil build; aset icon/feature graphic bukan pengganti screenshot aplikasi.

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
- [x] Jalankan unit test debug variant (24 lulus pada run #22).
- [x] Jalankan lint debug (0 error; 1 warning OldTargetApi).
- [x] Bangun debug APK pada GitHub-hosted runner.
- [x] Capture merged release manifest dan release runtime dependency inventory.
- [ ] Review dan commit `gradle/verification-metadata.xml`; jalankan ulang dengan strict dependency verification.
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
- [ ] Kirim ke Internal Testing.
- [ ] Periksa Pre-launch Report dan uji startup/alur utama pada perangkat.
- [ ] Konfirmasi jenis akun developer dan tanggal pembuatannya. Jika akun **personal** dibuat setelah 13 November 2023, Google Play mewajibkan closed test dengan minimal **12 tester yang terus opted-in selama 14 hari berturut-turut** sebelum mengajukan production access. Internal testing tidak menggantikan syarat closed test. Lihat [persyaratan testing resmi Google Play](https://support.google.com/googleplay/android-developer/answer/14151465).
- [ ] Selesaikan semua blocker sebelum mengajukan production.

## Blocker yang sudah diketahui
- Application ID masih kandidat dan belum dikonfirmasi/terdaftar di Play Console.
- Identitas legal perusahaan, alamat korespondensi, dan kontak privasi resmi belum diberikan.
- Privacy Policy belum di-host di URL HTTPS publik.
- Upload key dan empat GitHub Environment secrets belum tersedia.
- Schema Room v1 KSP historis belum ditemukan. Migration test 1 → 2 tidak boleh menggunakan schema v1 buatan.
- Dependency verification metadata belum direview/commit; strict gate belum PASS.
- Signed release AAB, signature verification, dan release SHA-256 belum tersedia.
- Belum ada startup/device/UI test, internal testing, atau Pre-launch Report.
- Jenis akun/tanggal pembuatan akun developer belum dikonfirmasi; syarat closed test 12/14 hari perlu dipastikan apakah berlaku.
- Data Safety, Ads, target audience, content rating, dan reviewer access declaration belum diselesaikan di Play Console.
