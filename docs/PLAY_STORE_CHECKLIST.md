# Google Play release checklist

Status release: pemeriksaan berikut tetap NOT TESTED kecuali bila ada bukti spesifik. Baseline CI run [45969254895](https://github.com/berkah-pos/btp/actions/runs/45969254895) pada PR #3 mencatat 16 unit test lulus, lint 0 error/8 warning, dan debug APK berhasil dibuat. CI terbaru PR #4 (run [45972354170](https://github.com/berkah-pos/btp/actions/runs/45972354170)) masih berjalan pada saat dokumen ini diperbarui; hasil baseline tidak dianggap sebagai validasi commit PR #4.

## Identitas dan kompatibilitas
- [ ] Konfirmasi applicationId candidate `id.berkahtigaputeri.basicpos` dan periksa keunikan di Play Console.
- [ ] Target API minimal 36 atau lebih tinggi sesuai kebijakan pada tanggal submission.
- [ ] Verifikasi minSdk, compileSdk, manifest merger, dan kompatibilitas perangkat.
- [ ] Siapkan icon adaptif, splash, feature graphic, dan screenshot dari aplikasi aktual.

## Keamanan dan privasi
- [ ] Inspeksi semua permission pada merged manifest.
- [ ] Inspeksi dependency tree dan semua SDK/transitive SDK.
- [ ] Pastikan tidak ada permission INTERNET atau network SDK yang tidak dibutuhkan.
- [ ] Selesaikan Privacy Policy Bahasa Indonesia dan host di URL HTTPS publik tanpa login.
- [ ] Lengkapi identitas pengendali, alamat, dan kontak privasi/dukungan.
- [ ] Isi Data Safety sesuai perilaku aktual, termasuk ekspor dan pembagian file.
- [ ] Isi deklarasi Ads sesuai inspeksi release aktual.
- [ ] Nonaktifkan backup otomatis yang tidak dinyatakan pada kebijakan privasi.

## Build dan signing
- [ ] Jalankan unit test dan lint.
- [ ] Bangun release AAB dengan upload key yang disimpan hanya di GitHub Environment Secrets.
- [ ] Validasi AAB dengan bundletool.
- [ ] Verifikasi signature dan certificate fingerprint.
- [ ] Hasilkan SHA256SUMS dan metadata versi.
- [ ] Uji artefak dari download Actions.

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
- Signing key belum disiapkan.
- Migration test Room 1 → 2 belum tersedia karena schema v1 hasil KSP historis belum ditemukan.
- Dependency verification metadata belum dikomit sebagai baseline yang telah ditinjau; CI masih memakai bootstrap checksum.
- Belum ada signed release AAB, startup test perangkat, UI automation, internal testing, atau Pre-launch Report.
