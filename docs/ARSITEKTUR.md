# Arsitektur BERKAH POS

## Versi toolchain yang dipatok

- Android Gradle Plugin: 9.5.3
- Gradle: 9.9.2
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
- Android `minSdk=24`, `compileSdk=37`, `targetSdk=36`

Versi disimpan di `gradle/libs.versions.toml`. Compile SDK 37 dipakai untuk kompatibilitas Compose stabil; target SDK tetap 36 sesuai persyaratan Google Play minimum pada tanggal baseline ini. Paket Android 17 API 37 dipasang dari SDK Manager sebagai `platforms;android-37.0`; workflow CI memasangnya secara eksplisit, lalu membuat alias `platforms/android-37` untuk resolusi API level oleh Android Gradle Plugin. Build Tools 36.0.0 dipatok terpisah. Periksa kembali ketentuan Play sebelum setiap rilis.

## Android compatibility

Google Play currently requires new Android apps and updates to target at least API 36 from 31 August 2026. Project baseline uses `targetSdk=36`; re-check the policy immediately before submission. Compile SDK is 37 because the selected Compose stable BOM 2026.11.01 requires the newer SDK level.

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

Automatic Android backup dinonaktifkan melalui `android:allowBackup=false`. Untuk menghindari ambiguitas dan perilaku berbeda pada Android 12+, manifest juga menunjuk `@xml/backup_rules` (legacy Auto Backup) serta `@xml/data_extraction_rules` (Android 12+). Kedua file mengecualikan domain `root`, `file`, `database`, `sharedpref`, `external`, dan domain `device_*` yang didukung; aturan Android 12+ diterapkan terpisah untuk cloud backup, device-to-device transfer, dan cross-platform transfer iOS. Hal ini penting karena `allowBackup=false` sendiri tidak selalu mematikan device-to-device transfer pada perangkat tertentu. Lihat [Android Auto Backup documentation](https://developer.android.com/identity/data/autobackup). Ekspor/restore POS tetap hanya melalui JSON manual dengan Storage Access Framework. Backup JSON versi 3 tersedia melalui Android Storage Access Framework dan mencakup ambang stok rendah dari DataStore serta snapshot identitas toko per transaksi. Restore tetap menerima versi 2; transaksi lama tanpa snapshot toko tetap ditandai tidak memiliki snapshot dan tidak diisi dari profil toko terbaru. Restore memvalidasi format, jumlah data, ID/referensi, SKU, nominal transaksi, snapshot toko, subtotal item, dan rentang preferensi sebelum mengganti database dalam satu transaksi Room. Nilai DataStore diperbarui terpisah; bila transaksi Room gagal, nilai preferensi lama dipulihkan sebagai kompensasi. Room dan DataStore tidak berbagi satu transaksi ACID. Ukuran backup dibatasi 25 MB. File backup tidak dienkripsi oleh aplikasi; pengguna bertanggung jawab menyimpannya secara aman.

## Dependency injection
Fondasi menggunakan `AppContainer` manual. Tidak ada Hilt/Dagger untuk menghindari dependency dan plugin tambahan yang belum diperlukan.

## Identitas aplikasi
Candidate namespace/applicationId saat ini: `id.berkahtigaputeri.basicpos`. Harus diperiksa ketersediaannya dan disetujui sebelum release pertama; applicationId tidak boleh diganti setelah publikasi pertama.
