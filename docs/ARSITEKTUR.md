# Arsitektur BERKAH POS

## Versi toolchain yang dipatok

- Android Gradle Plugin: 9.3.1
- Gradle: 9.7.0
- JDK: 17
- Built-in Kotlin / Compose Compiler plugin: 2.4.21
- Kotlin Symbol Processing: 2.3.14
- Compose BOM: 2026.11.01
- Kotlin Coroutines: 1.11.0
- AndroidX Activity Compose: 1.13.0
- Navigation Compose: 2.10.2
- Lifecycle: 2.11.1
- Room: 2.8.5
- Preferences DataStore: 1.3.0
- Android `minSdk=24`, `compileSdk=37.1` (API 37 minor level 1), `targetSdk=36`

Versi disimpan di `gradle/libs.versions.toml`. Compile SDK API 37.1 dipakai karena Compose UI 1.13.0 dalam Compose BOM 2026.11.01 mensyaratkannya. AGP 9.3.1 adalah versi minimum yang tercantum untuk API 37.1 pada matriks kompatibilitas Android Studio. Runner memasang paket stabil `platforms;android-37.1` dari channel 0. `targetSdk=36` tetap terpisah dari compile SDK dan memenuhi minimum Google Play yang berlaku untuk aplikasi baru pada 31 Agustus 2026. Build Tools 36.0.0 dipatok terpisah. Periksa kembali ketentuan Play sebelum setiap rilis.

## Kompatibilitas toolchain yang diverifikasi

Kotlin 2.4.20–2.4.21 fully supports Gradle 7.6.3–9.7.0 dan AGP 8.5.2–9.3.1. Proyek memakai batas atas kedua rentang tersebut: Gradle 9.7.0 dan AGP 9.3.1. AGP 9.3 mensyaratkan minimal Gradle 9.5.0; Android Studio compatibility table menetapkan AGP 9.3.1 sebagai minimum untuk compile API 37.1.

Android CI #112 pada commit `e4d97e3` berhasil menjalankan Compose plugin resolution, 35 JVM tests, `compileDebugAndroidTestKotlin`, `lintDebug`, `assembleDebug`, dan pemeriksaan 16 KiB pada JDK 17. POS/Room instrumentation #62 menjalankan lima test pada emulator API 36. Metadata dependency Run #112 identik dengan kandidat Run #110: 578 komponen / 960 artifact hashes.

Lint tetap memiliki tiga warning dan nol error: targetSdk 36 (`OldTargetApi`), rekomendasi Gradle 9.9.3, dan rekomendasi AGP 9.5.3/9.3.3. Upgrade tidak dilakukan karena akan keluar dari matriks fully-supported Kotlin 2.4.21; evaluasi ulang perlu mencakup seluruh build, migration test, dependency baseline, dan audit release.

Referensi: [Kotlin Gradle compatibility](https://kotlinlang.org/docs/gradle-configure-project.html), [AGP 9.3 compatibility](https://developer.android.com/build/releases/agp-9-3-0-release-notes), dan [minimum AGP per Android API level](https://developer.android.com/studio/releases).

## Prinsip
- Local-first / offline-first: SQLite melalui Room menjadi sumber data utama.
- Tidak ada akun pengguna, backend, Firebase, analytics, iklan pihak ketiga, atau SDK jaringan pada fondasi aplikasi.
- UI menggunakan Jetpack Compose + Material 3.
- Pola MVVM: UI mengamati state dari ViewModel menggunakan Flow.
- Repository Pattern memisahkan UI/domain dari DAO sehingga adapter remote Laravel dapat ditambahkan kelak tanpa mengganti kontrak domain.
- Nilai Rupiah disimpan sebagai integer `Long`; floating-point tidak digunakan untuk uang.

## Lapisan
1. `ui`: screen dan ViewModel.
2. `domain`: model ringkasan dan kontrak repository.
3. `data/local`: Room database, entity, dan DAO.
4. `data/repository`: implementasi repository berbasis Room.
5. `data/preferences`: preferensi ringan melalui DataStore.

## Data lokal
Room database versi 2 menyimpan profil toko, kategori, produk, penjualan, snapshot identitas toko per transaksi, dan snapshot item transaksi. Snapshot identitas toko mempertahankan nama/alamat/kontak/footer pada saat checkout; snapshot item mempertahankan nama/SKU/harga. Perubahan profil atau master produk tidak mengubah struk historis. Migrasi 1 → 2 menambahkan kolom nullable tanpa menghapus transaksi yang sudah ada.

DAO menyediakan pencarian produk/nomor transaksi, ringkasan agregat penjualan harian, hitungan stok rendah, serta transaksi atomik untuk stok dan checkout. Penjualan harian menggunakan rentang kalender lokal dan dihitung ulang pada pergantian tengah malam.

Preferences DataStore menyimpan ambang stok rendah pengguna (0–1.000.000 unit, default 5). Dashboard mengamati DataStore dan menjalankan query Room dengan ambang tersebut.

Automatic Android backup dinonaktifkan melalui `android:allowBackup=false`. Untuk menghindari ambiguitas dan perilaku berbeda pada Android 12+, manifest juga menunjuk `@xml/backup_rules` (legacy Auto Backup) serta `@xml/data_extraction_rules` (Android 12+). Kedua file mengecualikan domain `root`, `file`, `database`, `sharedpref`, `external`, dan domain `device_*` yang didukung; aturan Android 12+ diterapkan terpisah untuk cloud backup, device-to-device transfer, dan cross-platform transfer iOS. Hal ini penting karena `allowBackup=false` sendiri tidak selalu mematikan device-to-device transfer pada perangkat tertentu. Lihat [Android Auto Backup documentation](https://developer.android.com/identity/data/autobackup). Ekspor/restore POS tetap hanya melalui JSON manual dengan Storage Access Framework. Backup JSON versi 3 tersedia melalui Android Storage Access Framework dan mencakup ambang stok rendah dari DataStore serta snapshot identitas toko per transaksi. Restore tetap menerima versi 2; transaksi lama tanpa snapshot toko tetap ditandai tidak memiliki snapshot dan tidak diisi dari profil toko terbaru. Restore memvalidasi format, jumlah data, ID/referensi, SKU, nominal transaksi, snapshot toko, subtotal item, dan rentang preferensi sebelum mengganti database dalam satu transaksi Room. Setelah memeriksa bahwa pemanggil belum dibatalkan, update DataStore dan transaksi Room dijalankan dalam satu bagian kritis NonCancellable agar pembatalan coroutine tidak memotong urutan lintas-store setelah mutasi dimulai. Jika salah satu operasi melempar error (termasuk CancellationException eksplisit), nilai preferensi sebelumnya dipulihkan; kegagalan kompensasi ditambahkan sebagai suppressed exception. Ini mencegah cancellation race, tetapi tetap best-effort terhadap process death atau power loss karena Room dan DataStore tidak berbagi satu transaksi ACID. Ukuran backup dibatasi 25 MB. File backup tidak dienkripsi oleh aplikasi; pengguna bertanggung jawab menyimpannya secara aman. Decoder UTF-8 strict menolak payload malformed/truncated alih-alih menerima karakter pengganti; ketiga test decoder lulus pada Android CI #112.

## Dependency injection
Fondasi menggunakan `AppContainer` manual. Tidak ada Hilt/Dagger untuk menghindari dependency dan plugin tambahan yang belum diperlukan.

## Identitas aplikasi
Candidate namespace/applicationId saat ini: `id.berkahtigaputeri.basicpos`. Harus diperiksa ketersediaannya dan disetujui sebelum release pertama; applicationId tidak boleh diganti setelah publikasi pertama.
