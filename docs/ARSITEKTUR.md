# Arsitektur BERKAH POS

## Versi toolchain yang dipatok

- Android Gradle Plugin: 9.6.0
- Gradle: 9.8.2
- JDK: 17
- Built-in Kotlin / Compose Compiler plugin: 2.4.20
- Kotlin Symbol Processing: 2.3.14
- Compose BOM: 2026.09.00
- Kotlin Coroutines: 1.11.0
- AndroidX Activity Compose: 1.13.0
- Navigation Compose: 2.10.2
- Lifecycle: 2.11.1
- Room: 2.8.5
- Preferences DataStore: 1.3.0
- Android `minSdk=24`, `compileSdk=37`, `targetSdk=36`

Versi disimpan di `gradle/libs.versions.toml`. Compile SDK 37 dipakai untuk kompatibilitas Compose stabil; target SDK tetap 36 sesuai persyaratan Google Play minimum pada tanggal baseline ini. Periksa kembali ketentuan Play sebelum setiap rilis.

## Android compatibility

Google Play currently requires new Android apps and updates to target at least API 36 from 31 August 2026. Project baseline uses `targetSdk=36`; re-check the policy immediately before submission. Compile SDK is 37 because the selected Compose stable BOM requires the newer SDK level.

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
Room menyimpan profil toko, kategori, produk, penjualan, dan snapshot item transaksi. Snapshot item mempertahankan nama/SKU/harga saat checkout agar struk lama tidak berubah ketika master produk diedit atau dihapus.

DAO menyediakan pencarian produk/nomor transaksi, ringkasan agregat penjualan harian, hitungan stok rendah, serta transaksi atomik untuk stok dan checkout. Penjualan harian menggunakan rentang kalender lokal dan dihitung ulang pada pergantian tengah malam.

Preferences DataStore menyimpan ambang stok rendah pengguna (0–1.000.000 unit, default 5). Dashboard mengamati DataStore dan menjalankan query Room dengan ambang tersebut.

Automatic Android backup dinonaktifkan pada manifest. Backup JSON manual tersedia melalui Android Storage Access Framework. Restore memvalidasi format, jumlah data, ID/referensi, SKU, nominal transaksi, dan subtotal item sebelum mengganti database dalam satu transaksi Room. Ukuran backup dibatasi 25 MB. File backup tidak dienkripsi oleh aplikasi; pengguna bertanggung jawab menyimpannya secara aman.

## Dependency injection
Fondasi menggunakan `AppContainer` manual. Tidak ada Hilt/Dagger untuk menghindari dependency dan plugin tambahan yang belum diperlukan.

## Identitas aplikasi
Candidate namespace/applicationId saat ini: `id.berkahtigaputeri.basicpos`. Harus diperiksa ketersediaannya dan disetujui sebelum release pertama; applicationId tidak boleh diganti setelah publikasi pertama.
