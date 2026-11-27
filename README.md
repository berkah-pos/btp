# BERKAH POS

Aplikasi Basic POS offline-first untuk UMKM Indonesia, dikembangkan oleh **BERKAH TIGA PUTERI**.

## Status implementasi

Fondasi POS dan alur MVP sudah tersedia pada `main` melalui [PR #1](https://github.com/berkah-pos/btp/pull/1). Branch pengembangan aktif `feat/basic-pos-mvp` berisi hardening fondasi dan dokumentasi lanjutan.

Source code saat ini mencakup:
- Dashboard penjualan harian, jumlah transaksi, produk, dan stok rendah yang dihitung dari Room.
- Profil usaha dan preferensi ambang stok rendah melalui DataStore.
- CRUD produk, pencarian nama/SKU, kategori, dan stok.
- Kasir, keranjang, diskon/pajak nominal, pembayaran, kembalian, dan checkout Room atomik.
- Riwayat transaksi, detail item snapshot, dan berbagi struk. Identitas toko juga disimpan per transaksi agar struk historis tidak berubah ketika profil toko diperbarui.
- Backup/restore JSON lokal melalui Android Storage Access Framework.
- Informasi SaaS yang URL HTTPS/email-nya dikonfigurasi melalui GitHub Actions Variables.
- Pilihan data contoh yang eksplisit, hanya katalog produk berlabel `[CONTOH]`, tanpa transaksi palsu.

## Validasi cloud yang sudah teramati

**Baseline build (27 November 2026):** PR #3 memperbaiki provisioning Android API 37, mematok Android Gradle Plugin 9.5.1, dan menghapus deklarasi model `DashboardSummary` yang duplikat. Run [45969254895](https://github.com/berkah-pos/btp/actions/runs/45969254895) pada commit PR tersebut berhasil menjalankan:
- `testDebugUnitTest`: 16 test, 0 gagal, 0 error, 0 skipped.
- `lintDebug`: 0 error dan 8 warning.
- `assembleDebug`: berhasil; APK debug dan laporan lint/test diunggah sebagai Actions Artifact.

Hasil tersebut membuktikan baseline source pada PR #3, **bukan** commit hardening terbaru di branch ini. Perubahan di branch ini masih harus melewati workflow cloud sendiri. Delapan warning baseline mencakup advisory versi toolchain/dependency, target API, konfigurasi backup Android 12+, resource launcher tidak terpakai, dan resource shrinking; hardening untuk backup/resource shrinking/resource launcher sudah diterapkan di branch ini dan menunggu verifikasi ulang lint.

Belum ada bukti uji startup di perangkat fisik/emulator, UI automation, internal testing, atau Google Play Pre-launch Report. Job signed release AAB dilewati pada run PR; AAB release harus dibuat melalui `workflow_dispatch` pada `main` setelah application ID, baseline dependency verification, dan upload signing secrets tersedia.

## Keputusan teknis

- Kotlin + Jetpack Compose + Material 3.
- MVVM, Coroutines, Flow, Navigation Compose.
- Room/SQLite sebagai sumber data POS lokal. Database schema versi 2 memiliki migrasi non-destruktif dari versi 1 untuk menambah snapshot identitas toko pada struk lama.
- Ekspor Room schema v2 dari KSP dikomit di `app/schemas/id.berkahtigaputeri.basicpos.data.local.BerkahPosDatabase/2.json`.
- Preferences DataStore untuk ambang stok rendah.
- Repository Pattern untuk memisahkan UI/domain dari storage.
- Semua nilai uang disimpan sebagai integer Rupiah (`Long`), bukan Float/Double.
- Tidak ada akun, backend, Firebase, analytics, crash reporting, iklan pihak ketiga, atau permission jaringan yang dideklarasikan.
- Android minSdk 24, compileSdk 37, targetSdk 36.
- AGP 9.5.1, Gradle 9.8.2, built-in Kotlin 2.4.20, KSP 2.3.14, Coroutines 1.11.0, JDK 17.

## Identitas aplikasi

Candidate applicationId: `id.berkahtigaputeri.basicpos`. Debug build menggunakan kandidat ini. Sebelum signed release, konfirmasi ID permanen melalui Play Console dan isi GitHub Actions Repository Variable `BTP_APPLICATION_ID`; release workflow akan gagal bila belum diisi. Jangan mengubah applicationId setelah publikasi pertama.

## Branch dan review

- `main`: baseline kanonis dengan implementasi MVP hasil merge PR #1 dan koreksi CI PR #3 serta dokumentasi PR #2.
- `feat/basic-pos-mvp`: hardening fondasi dan pengembangan lanjutan; perubahan diajukan melalui pull request, tidak langsung dipublikasikan.
- [PR #1 — Android foundation](https://github.com/berkah-pos/btp/pull/1), [PR #2 — status dokumentasi](https://github.com/berkah-pos/btp/pull/2), dan [PR #3 — koreksi Android SDK/CI](https://github.com/berkah-pos/btp/pull/3) telah merged pada 27 November 2026.

## Cloud build

Workflow `.github/workflows/android-ci.yml` memakai GitHub-hosted `ubuntu-24.04`, JDK 17, Android SDK API 37, Build Tools 36.0.0, dan Gradle 9.8.2. Push ke `main` dan pull request menuju `main` menjalankan unit test, lint, dan debug build. Branch feature divalidasi melalui PR agar tidak membuat build duplikat pada setiap push. Signed release AAB hanya melalui manual `workflow_dispatch` pada `main`, dengan GitHub Environment `android-release` dan upload-key secrets. Tidak ada self-hosted runner atau publikasi otomatis.

Gradle Wrapper di-bootstrap oleh runner cloud dari Gradle 9.8.2; wrapper JAR tidak disimpan di repository. Dependency verification metadata sudah dihasilkan oleh job awal, tetapi belum dikomit sebagai baseline yang ditinjau. Karena itu CI masih mempunyai jalur bootstrap checksum; jangan menganggap strict verification aktif sampai metadata di-review dan dikomit.

## Konfigurasi promosi dan dukungan

GitHub Actions Repository Variables opsional:
- `BTP_SAAS_URL`: URL HTTPS resmi layanan SaaS.
- `BTP_SUPPORT_EMAIL`: email dukungan resmi.

Keduanya sengaja kosong secara default. Jangan mengisi alamat placeholder atau informasi perusahaan yang belum diverifikasi.

## Dokumentasi

- [Arsitektur](docs/ARSITEKTUR.md)
- [Kebijakan Privasi — draf](docs/PRIVACY_POLICY.md)
- [Syarat dan Ketentuan — draf](docs/TERMS_OF_SERVICE.md)
- [Google Play checklist](docs/PLAY_STORE_CHECKLIST.md)
- [Release process](docs/RELEASE_PROCESS.md)

## Backup dan privasi

Backup JSON versi 3 berada di lokasi pilihan pengguna dan **tidak dienkripsi oleh aplikasi**. File memuat profil toko, kategori, produk, stok, transaksi, snapshot identitas toko per transaksi, rincian struk, serta preferensi ambang stok rendah. Restore tetap menerima backup versi 2; transaksi lama tanpa snapshot tetap menampilkan identitas toko sebagai tidak tersedia, bukan mengambil profil toko terbaru. Backup sistem Android dinonaktifkan dan aturan backup legacy/Android 12+ mengecualikan data aplikasi; backup POS dilakukan secara manual melalui file JSON yang dipilih pengguna.

## Repository public

Repository ini sengaja public berdasarkan keputusan operasional sebelumnya untuk memakai standard GitHub-hosted runners tanpa biaya menit runner. Artifact storage tetap mempunyai kuota dan retensi. Jangan commit signing keystore, password, token, atau data usaha nyata.
