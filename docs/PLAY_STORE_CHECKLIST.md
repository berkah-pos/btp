# Google Play release checklist

Status: pre-release review remains **BLOCKED**. The latest fully verified technical evidence is GitHub Actions run [#26](https://github.com/berkah-pos/btp/actions/runs/45983299284), commit `c8da7f1654c1641cd37d0bc91631476921d5a4d2`, on 27 November 2026. The owner has confirmed that submission will use a **Google Play Organization developer account**.

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
- [Debug APK, reports, Room schema v2, dependency metadata, and release audit](https://github.com/berkah-pos/btp/actions/runs/45983299284/artifacts/15190830767) — 14-day retention.
- [Play Store icon and feature graphic](https://github.com/berkah-pos/btp/actions/runs/45983299284/artifacts/15190362585) — 90-day retention.

This is CI and source-level evidence. It is **not** evidence of signed AAB validity, installation/startup on physical devices, UI automation, Play Console submission, or Google Play approval.

## Identitas dan kompatibilitas
- [ ] Konfirmasi applicationId candidate `id.berkahtigaputeri.basicpos` dan periksa keunikan di Play Console.
- [x] Tipe akun untuk submission: **Organization** (dikonfirmasi pemilik proyek; verifikasi account profile tetap harus diselesaikan di Play Console).
- [x] Syarat closed testing khusus akun Personal baru (12 tester/14 hari): **NOT APPLICABLE** untuk submission melalui akun Organization ini. Persyaratan resmi 12/14 hari dibatasi pada akun Personal yang dibuat setelah 13 November 2023. Lihat [persyaratan testing resmi Google Play](https://support.google.com/googleplay/android-developer/answer/14151465).
- [x] `targetSdk=36`; memenuhi minimum API 36 yang berlaku pada tanggal pemeriksaan.
- [x] Merged release manifest: `minSdk=24`, `targetSdk=36`; compile SDK 37.1 pada konfigurasi build.
- [ ] Uji startup, navigasi, transaksi, backup/restore, dan share struk pada perangkat/API yang didukung.
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
- [x] Jalankan unit test debug variant (24 lulus pada run #22).
- [x] Jalankan lint debug (0 error; 1 warning OldTargetApi).
- [x] Bangun debug APK pada GitHub-hosted runner.
- [x] Capture merged release manifest dan release runtime dependency inventory.
- [ ] Review dan commit `gradle/verification-metadata.xml`; jalankan ulang dengan strict dependency verification. Runs #22 and #26 produced byte-identical bootstrap metadata (SHA-256 `3a9e25b7fb3c0ed4eb21bae8f74e83d1cd2f4c157f03a870e942b6550d0b15c0`, 462 components, 758 SHA-256 entries), but it is not committed and strict verification remains skipped.
- [ ] Verify the new debug APK 16 KB ZIP alignment and 64-bit ELF LOAD audit in the current PR run.
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
- Schema Room v1 KSP historis belum ditemukan. Migration test 1 → 2 tidak boleh menggunakan schema v1 buatan.
- Dependency verification metadata belum direview/commit; strict gate belum PASS.
- Signed release AAB, signature verification, dan release SHA-256 belum tersedia.
- Belum ada startup/device/UI test, internal testing, atau Pre-launch Report.
- Data Safety, Ads, target audience, content rating, dan reviewer access declaration belum diselesaikan di Play Console.
