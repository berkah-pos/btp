# Arsitektur BERKAH POS

## Versi toolchain yang dipatok

- Android Gradle Plugin: 9.5.3
- Gradle: 9.6.0
- JDK: 17
- Kotlin / Compose Compiler plugin: 2.4.21
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
Entity awal: profil toko, kategori, produk, transaksi, dan snapshot item transaksi. Item transaksi menyimpan nama/SKU/harga saat checkout agar struk lama tidak berubah saat master produk diedit.

Automatic Android backup dinonaktifkan pada manifest. Backup manual JSON akan diimplementasikan pada tahap berikutnya dan hanya berjalan setelah pengguna memilih ekspor/impor.

## Dependency injection
Fondasi menggunakan `AppContainer` manual. Tidak ada Hilt/Dagger untuk menghindari dependency dan plugin tambahan yang belum diperlukan.

## Identitas aplikasi
Candidate namespace/applicationId saat ini: `id.berkahtigaputeri.basicpos`. Harus diperiksa ketersediaannya dan disetujui sebelum release pertama; applicationId tidak boleh diganti setelah publikasi pertama.
