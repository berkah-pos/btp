# Google Play release checklist

Status release: pemeriksaan berikut tetap **NOT TESTED** kecuali bila ada bukti spesifik. Run [45975233350](https://github.com/berkah-pos/btp/actions/runs/45975233350) pada 27 November 2026 memvalidasi head PR #4, commit `7e371fd29482b3e81359c73b4d34fd5945c42bf2`:

- `testDebugUnitTest`: 16 test, 0 failure, 0 error, 0 skipped.
- `lintDebug`: 0 error, 1 warning (`OldTargetApi` pada `targetSdk=36`).
- `assembleDebug`: BUILD SUCCESSFUL; debug APK dan laporan test/lint diunggah sebagai Actions Artifact.
- Render app icon 512×512 dan feature graphic 1024×500 berhasil.
- Signed release AAB: **SKIPPED** (workflow dipicu oleh pull request).
- Strict dependency verification: **SKIPPED**; metadata SHA-256 baru dihasilkan sebagai bootstrap artifact dan belum dikomit/review sebagai baseline tepercaya.

Hasil ini hanya membuktikan debug quality workflow pada commit tersebut. Hasil ini bukan bukti signed release, perangkat, atau Play Console.

## Identitas dan kompatibilitas
- [ ] Konfirmasi applicationId candidate `id.berkahtigaputeri.basicpos` dan periksa keunikan di Play Console.
- [ ] Target API minimal 36 atau lebih tinggi sesuai kebijakan pada tanggal submission.
- [ ] Verifikasi minSdk, compileSdk, merged release manifest, dan kompatibilitas perangkat.
- [ ] Siapkan screenshot aktual dari aplikasi; aset icon/feature graphic yang dirender CI bukan pengganti screenshot aplikasi.

## Keamanan dan privasi
- [ ] Inspeksi semua permission pada merged release manifest.
- [ ] Inspeksi dependency graph release dan semua SDK/transitive SDK.
- [ ] Pastikan tidak ada permission INTERNET atau network SDK yang tidak dibutuhkan.
- [ ] Selesaikan Privacy Policy Bahasa Indonesia dan host di URL HTTPS publik tanpa login.
- [ ] Lengkapi identitas pengendali, alamat, dan kontak privasi/dukungan.
- [ ] Isi Data Safety sesuai perilaku aktual, termasuk ekspor dan pembagian file.
- [ ] Isi deklarasi Ads sesuai inspeksi release aktual.
- [ ] Verifikasi konfigurasi backup Android pada perangkat/API yang didukung.

## Build dan signing
- [x] Jalankan unit test pada debug variant (16 lulus pada run #45975233350).
- [x] Jalankan lint debug (0 error; 1 warning OldTargetApi).
- [x] Bangun debug APK pada GitHub-hosted runner.
- [ ] Review dan commit `gradle/verification-metadata.xml`; jalankan ulang dengan strict dependency verification.
- [ ] Bangun release AAB dengan upload key yang disimpan hanya di GitHub Environment Secrets.
- [ ] Validasi AAB dengan bundletool.
- [ ] Verifikasi signature dan certificate fingerprint.
- [ ] Hasilkan SHA256SUMS dan metadata versi untuk release.
- [ ] Uji artefak release yang diunduh dari Actions.

## Store listing dan review
- [ ] Siapkan nama, short description, full description, support contact, website, dan release notes.
- [ ] Siapkan screenshot aktual, app icon, dan feature graphic.
- [ ] Lengkapi target audience, content rating, Ads, Data Safety, dan App access.
- [ ] Jelaskan bahwa reviewer tidak memerlukan akun dan fitur inti berjalan offline.
- [ ] Kirim ke Internal Testing.
- [ ] Periksa Pre-launch Report dan uji startup/alur utama pada perangkat.
- [ ] Periksa syarat closed testing/production access yang berlaku untuk jenis dan usia akun developer.
- [ ] Selesaikan semua blocker sebelum mengajukan production.

## Blocker yang sudah diketahui
- Application ID masih candidate dan perlu konfirmasi serta pemeriksaan Play Console.
- Identitas legal/kontak perusahaan belum tersedia.
- Privacy Policy HTTPS URL belum tersedia.
- Signing key belum disiapkan; tidak dibuat oleh CI.
- Migration test Room 1 → 2 belum tersedia karena schema v1 hasil KSP historis belum ditemukan.
- Dependency verification metadata dihasilkan oleh bootstrap CI, tetapi belum dikomit sebagai baseline yang telah ditinjau; strict verification belum PASS.
- Belum ada signed release AAB, startup test perangkat, UI automation, internal testing, atau Pre-launch Report.
