# BERKAH POS

Aplikasi Basic POS offline-first untuk UMKM Indonesia, dikembangkan oleh **BERKAH TIGA PUTERI**.

## Status implementasi

Fondasi MVP POS tersedia pada `main` melalui [PR #1](https://github.com/berkah-pos/btp/pull/1). Implementasi mencakup:

- Dashboard penjualan harian, jumlah transaksi, produk, dan stok rendah dari Room.
- Profil usaha dan preferensi ambang stok rendah melalui DataStore.
- CRUD produk, pencarian nama/SKU, kategori, dan stok.
- Kasir, keranjang, diskon/pajak nominal, pembayaran, kembalian, dan checkout Room atomik.
- Riwayat transaksi, detail item snapshot, dan berbagi struk. Identitas toko dan rincian item disimpan sebagai snapshot saat checkout agar struk historis tidak berubah ketika master data diperbarui.
- Backup/restore JSON lokal melalui Android Storage Access Framework.
- Informasi SaaS opsional yang hanya ditampilkan ketika URL HTTPS/email dukungan resmi dikonfigurasi.
- Data contoh eksplisit yang hanya menambahkan katalog produk berlabel `[CONTOH]`, tanpa transaksi palsu.

## Validasi cloud yang teramati

Run [#26](https://github.com/berkah-pos/btp/actions/runs/45983299284) pada 27 November 2026 selesai **success** untuk head PR #4, commit `c8da7f1654c1641cd37d0bc91631476921d5a4d2`:

- `testDebugUnitTest`: **24 test, 0 failure, 0 error, 0 skipped**, diverifikasi dari empat XML test-result yang diunggah.
- `lintDebug`: **0 error, 1 warning** (`OldTargetApi`, karena `targetSdk=36`).
- `assembleDebug`: **BUILD SUCCESSFUL**; APK debug dan laporan test/lint berhasil diunggah.
- Merged release manifest dan release runtime dependency inventory berhasil ditangkap sebagai artifact audit.
- Render app icon 512×512 dan feature graphic 1024×500 berhasil; keduanya diunggah sebagai Actions Artifact.
- Job signed release AAB: **SKIPPED** karena run berasal dari pull request. Tidak ada AAB release atau publikasi yang dihasilkan.

Artifacts:
- [Debug APK, test/lint reports, schema v2, verification metadata, dan release audit](https://github.com/berkah-pos/btp/actions/runs/45983299284/artifacts/15190830767) — retensi 14 hari.
- [Play Store listing graphics](https://github.com/berkah-pos/btp/actions/runs/45983299284/artifacts/15190362585) — retensi 90 hari.

**Dependency verification belum aktif dalam mode strict.** Run #26 menggunakan bootstrap `--write-verification-metadata sha256`; metadata diunggah untuk review, tetapi belum dikomit sebagai baseline tepercaya. Step strict verification dilewati karena file belum tersedia pada branch. Jangan menganggap hash bootstrap dalam satu run sebagai verifikasi independen.

Hasil run #26 berlaku untuk commit yang disebutkan, bukan otomatis untuk commit berikutnya. Belum ada bukti startup pada perangkat fisik/emulator, UI automation, internal testing, atau Google Play Pre-launch Report.

## Keputusan teknis

- Kotlin + Jetpack Compose + Material 3.
- MVVM, Coroutines, Flow, Navigation Compose.
- Room/SQLite sebagai sumber data POS lokal; DataStore untuk preferensi ringan.
- Room schema versi 2 diekspor oleh KSP dan dikomit di `app/schemas/`.
- Migrasi Room 1 → 2 menambahkan lima kolom snapshot toko nullable pada `sales_transactions` tanpa menghapus tabel atau baris.
- **Migration test 1 → 2 belum terverifikasi:** schema v1 historis yang diekspor KSP belum tersedia. Jangan membuat fixture schema v1 secara manual.
- Semua nominal uang disimpan sebagai integer Rupiah (`Long`), bukan Float/Double.
- Tidak ada akun, backend POS, Firebase, analytics, crash reporting, iklan pihak ketiga, atau permission jaringan yang dideklarasikan pada source manifest.
- Android `minSdk=24`, `compileSdk=37.1`, `targetSdk=36`.
- AGP 9.5.3, Gradle 9.9.2, built-in Kotlin/Compose Compiler 2.4.21, KSP 2.3.14, Compose BOM 2026.11.01, Coroutines 1.11.0, JDK 17.

## Identitas aplikasi

Candidate applicationId: `id.berkahtigaputeri.basicpos`. Sebelum signed release, pemilik aplikasi harus mengonfirmasi ID permanen melalui Play Console dan mengisi GitHub Actions Repository Variable `BTP_APPLICATION_ID`. Jangan mengubah applicationId setelah publikasi pertama.

## Riwayat dan review

- [PR #1 — Android foundation](https://github.com/berkah-pos/btp/pull/1), [PR #2 — koreksi dokumentasi pasca-merge](https://github.com/berkah-pos/btp/pull/2), dan [PR #3 — koreksi Android SDK/CI](https://github.com/berkah-pos/btp/pull/3) telah merged pada 27 November 2026.
- [PR #4 — hardening fondasi dan backup](https://github.com/berkah-pos/btp/pull/4) memuat perubahan lanjutan yang divalidasi oleh run #45975233350. Status PR harus dilihat langsung di GitHub; README ini tidak mengasumsikan PR tersebut sudah merged.

## Cloud build

Workflow `.github/workflows/android-ci.yml` memakai standard GitHub-hosted `ubuntu-24.04`, JDK 17, Android SDK API 37.1, Build Tools 36.0.0, dan Gradle 9.9.2. Pull request menuju `main` serta push ke `main` menjalankan unit test, lint, dan debug build. Signed release AAB hanya melalui `workflow_dispatch` manual pada `main`, GitHub Environment `android-release`, dan upload-key secrets. Tidak ada self-hosted runner atau publikasi otomatis.

Runner membangkitkan Gradle Wrapper sementara dari Gradle 9.9.2 yang dipasang oleh action resmi. Distribution SHA-256 dan SHA-256 wrapper JAR diperiksa sebelum wrapper dipakai; wrapper JAR/scripts tidak dikomit. Perubahan dependency tetap harus diikuti review dan commit `gradle/verification-metadata.xml` sebelum strict dependency verification dapat menjadi gate permanen.

## Konfigurasi promosi dan dukungan

GitHub Actions Repository Variables opsional:
- `BTP_SAAS_URL`: URL HTTPS resmi layanan SaaS.
- `BTP_SUPPORT_EMAIL`: email dukungan resmi.

Keduanya kosong secara default. Jangan mengisi alamat placeholder atau informasi perusahaan yang belum diverifikasi.

## Dokumentasi

- [Arsitektur](docs/ARSITEKTUR.md)
- [Kebijakan Privasi — draf](docs/PRIVACY_POLICY.md)
- [Syarat dan Ketentuan — draf](docs/TERMS_OF_SERVICE.md)
- [Google Play checklist](docs/PLAY_STORE_CHECKLIST.md)
- [Data Safety source review](docs/DATA_SAFETY_REVIEW.md)
- [Release process](docs/RELEASE_PROCESS.md)

## Backup dan privasi

Backup JSON versi 3 berada di lokasi pilihan pengguna dan **tidak dienkripsi oleh aplikasi**. File memuat profil toko, kategori, produk, stok, transaksi, snapshot identitas toko per transaksi, rincian struk, dan preferensi ambang stok rendah. Restore tetap menerima backup versi 2; transaksi lama tanpa snapshot tetap menampilkan identitas toko sebagai tidak tersedia, bukan mengambil profil terbaru. Automatic Android backup dan transfer perangkat dikecualikan melalui manifest serta aturan backup legacy/Android 12+. Backup POS tetap dilakukan manual melalui file JSON yang dipilih pengguna.

## Repository public

Repository ini public. Standard GitHub-hosted runners untuk public repository tidak dikenai biaya menit runner, tetapi artifact storage tetap memiliki kuota dan retensi. Jangan commit signing keystore, password, token, atau data usaha nyata.
