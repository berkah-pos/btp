# Implementasi Basic POS — BERKAH TIGA PUTERI

Status: blueprint teknis untuk proyek Android yang ada, bukan spesifikasi aplikasi baru dari nol.

## 1. Identitas dan stack

- Nama aplikasi: **Basic POS**
- Brand/penerbit: **BERKAH TIGA PUTERI**
- Domain perusahaan: `berkahtigaputeri.co.id`
- Android application ID: `id.co.berkahtigaputeri.btp`
- Kotlin namespace: `id.berkahtigaputeri.basicpos`
- Native Android: Kotlin, Jetpack Compose, Material 3
- Architecture: MVVM + Repository
- Database utama: Room / SQLite lokal
- Preferences: Preferences DataStore
- Minimum Android: API 24
- Target Android: API 36
- Compile SDK: API 37.1
- Build: AGP 9.3.1, Gradle 9.7.0, JDK 17, Kotlin 2.4.21

Application ID domain-based sudah menjadi konfigurasi build. Ini **belum membuktikan** ID sudah dibuat/direservasi di Play Console; verifikasi ketersediaan tetap harus dilakukan saat akun developer yang sah membuat aplikasi pertama kali. Jangan mengganti application ID setelah publikasi.

## 2. Arsitektur dan alur data

`Compose Screen → ViewModel → PosRepository → PosRepositoryImpl → PosDao → Room`

Room adalah source of truth untuk profil toko, kategori, produk, stok, header transaksi, dan item struk. Preferences DataStore hanya menyimpan preferensi ringan, saat ini ambang stok rendah. AppContainer manual menghubungkan dependency; belum perlu Hilt/Dagger.

Basic POS adalah **offline-first**. Tidak ada login, signup SaaS, remote sync, atau API backend yang diaktifkan saat ini. Bagian SaaS di Pengaturan adalah informasi yang hanya membuka URL HTTPS jika URL asli sudah dikonfigurasi; itu bukan signup flow palsu.

## 3. Perilaku POS yang sudah ada

- CRUD produk: nama wajib, SKU opsional, harga Rupiah, stok, kategori.
- Kategori sederhana.
- Kasir dan keranjang; baris produk duplikat diagregasi.
- Subtotal, diskon nominal Rupiah, pajak nominal Rupiah, total, pembayaran, dan kembalian.
- Checkout memvalidasi stok terkini dan menyimpan pengurangan stok, header transaksi, serta item struk pada satu transaksi Room.
- Snapshot nama/SKU/harga produk dan profil toko menjaga struk lama tidak berubah saat master data diperbarui.
- Dashboard: penjualan hari ini, jumlah transaksi, produk, dan stok rendah.
- Riwayat transaksi dan detail/berbagi struk.
- Backup/restore JSON manual melalui Android Storage Access Framework; restore memvalidasi payload sebelum mengganti data.

Diskon dan pajak yang ada berupa nominal, bukan persen dan bukan perhitungan pajak otomatis. Nilai uang disimpan sebagai integer Long Rupiah; arithmetic overflow ditolak.

## 4. Privasi dan Data Safety

Data toko, produk, kategori, SKU, harga, stok, transaksi, item struk, dan preferensi disimpan lokal di Room/DataStore. Release manifest yang diaudit tidak meminta INTERNET; dependency review tidak menemukan Firebase, analytics, crash-reporting, ad SDK, atau HTTP client.

Ekspor backup dilakukan setelah pengguna memilih lokasi. JSON **tidak dienkripsi oleh aplikasi**; file dapat berisi seluruh data toko dan riwayat transaksi, sehingga harus disimpan di tempat aman. Berbagi struk diteruskan melalui chooser Android ke aplikasi yang dipilih pengguna.

Privacy Policy dan Terms di repo tetap draf. Sebelum publikasi, lengkapi pengendali legal, alamat korespondensi, kontak privasi/dukungan, dan URL HTTPS publik. Data Safety form harus sesuai binary final dan semua SDK transitif; dokumen review bukan pengganti form Play Console.

## 5. Struktur proyek saat ini

```text
btp/
├── .github/workflows/
│   ├── android-ci.yml
│   ├── ci-admission-check.yml
│   └── room-migration-tests.yml
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   ├── schemas/
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/id/berkahtigaputeri/basicpos/
│       │   │   ├── BerkahPosApplication.kt
│       │   │   ├── MainActivity.kt
│       │   │   ├── core/money/
│       │   │   │   ├── Rupiah.kt
│       │   │   │   └── RupiahFormatter.kt
│       │   │   ├── data/
│       │   │   │   ├── backup/
│       │   │   │   │   ├── PosBackupOperationGate.kt
│       │   │   │   │   ├── PosBackupPayloadDecoder.kt
│       │   │   │   │   ├── PosBackupRestoreCoordinator.kt
│       │   │   │   │   ├── PosBackupService.kt
│       │   │   │   │   └── PosBackupValidator.kt
│       │   │   │   ├── local/
│       │   │   │   │   ├── BerkahPosDatabase.kt
│       │   │   │   │   ├── dao/PosDao.kt
│       │   │   │   │   └── entity/PosEntities.kt
│       │   │   │   ├── preferences/UserPreferencesRepository.kt
│       │   │   │   └── repository/PosRepositoryImpl.kt
│       │   │   ├── domain/
│       │   │   │   ├── model/
│       │   │   │   │   ├── CheckoutCalculator.kt
│       │   │   │   │   └── PosModels.kt
│       │   │   │   └── repository/PosRepository.kt
│       │   │   ├── navigation/PosNavigation.kt
│       │   │   └── ui/
│       │   │       ├── cashier/{CashierScreen.kt,CashierViewModel.kt}
│       │   │       ├── dashboard/{DashboardScreen.kt,DashboardViewModel.kt}
│       │   │       ├── products/{ProductsScreen.kt,ProductsViewModel.kt}
│       │   │       ├── settings/{SettingsScreen.kt,SettingsViewModel.kt}
│       │   │       ├── transactions/{TransactionsScreen.kt,TransactionsViewModel.kt}
│       │   │       └── theme/{Color.kt,Theme.kt,Type.kt}
│       │   └── res/
│       │       ├── drawable/
│       │       ├── mipmap-anydpi/
│       │       ├── mipmap-anydpi-v26/
│       │       ├── values/{colors.xml,strings.xml,styles.xml}
│       │       └── xml/{backup_rules.xml,data_extraction_rules.xml}
│       ├── test/java/id/berkahtigaputeri/basicpos/
│       │   ├── core/money/{RupiahTest.kt,RupiahFormatterTest.kt}
│       │   ├── data/backup/
│       │   │   ├── PosBackupOperationGateTest.kt
│       │   │   ├── PosBackupPayloadDecoderTest.kt
│       │   │   ├── PosBackupRestoreCoordinatorTest.kt
│       │   │   └── PosBackupValidatorTest.kt
│       │   └── domain/model/CheckoutCalculatorTest.kt
│       └── androidTest/java/id/berkahtigaputeri/basicpos/
│           ├── data/local/BerkahPosDatabaseMigrationTest.kt
│           └── data/repository/PosRepositoryCheckoutTest.kt
├── docs/
│   ├── ARSITEKTUR.md
│   ├── DATA_SAFETY_REVIEW.md
│   ├── IMPLEMENTASI_BASIC_POS.md
│   ├── PLAY_STORE_CHECKLIST.md
│   ├── PRIVACY_POLICY.md
│   ├── RELEASE_PROCESS.md
│   ├── STORE_LISTING.md
│   ├── TERMS_OF_SERVICE.md
│   └── store-assets/{app-icon.svg,feature-graphic.svg}
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/gradle-wrapper.properties
├── scripts/
│   ├── check-native-libraries-16kb.sh
│   └── validate-dependency-baseline.py
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
└── README.md
```

File inti sudah berisi implementasi utuh; bukan skeleton yang menunggu ditulis ulang:

- Domain dan checkout: `domain/model/PosModels.kt`, `CheckoutCalculator.kt`
- Room: `data/local/entity/PosEntities.kt`, `dao/PosDao.kt`, `BerkahPosDatabase.kt`
- Repository dan checkout atomik: `data/repository/PosRepositoryImpl.kt`
- UI/logic: seluruh screen dan ViewModel pada `ui/`
- Backup: semua file dalam `data/backup/`
- Pengujian: tujuh JVM test class dan dua instrumentation test class.

## 6. Validasi yang teramati

Android CI #125 (source head `1eda0369eedb3d88cff9ba7c3a077cebb24ce8c5`; commit ini menambahkan blueprint dokumentasi): https://github.com/berkah-pos/btp/actions/runs/46083906927

- 35 unit tests: 0 failure, 0 error, 0 skipped.
- Compile instrumented Kotlin, lint, debug APK, merged release manifest, dan runtime dependency inventory berhasil.
- Unsigned release AAB dibangun, Bundletool validation berhasil, PAGE_ALIGNMENT_16K terdeteksi, dan pemeriksaan native ELF 16 KB lulus.
- AAB SHA-256: `1d38a5c4814ebb861b0bf0b695726399c26708c14070248947fb40bb8c4ac336`.

POS/Room instrumentation #75: https://github.com/berkah-pos/btp/actions/runs/46083906878

- Lima tes API 36 emulator: 0 failure, 0 error, 0 skipped.
- Mencakup migrasi Room v1 dan checkout termasuk rollback serta race pada stok terakhir.

Dua test checkout tambahan ditambahkan pada commit `07c1d41`: rollback ketika insert item struk gagal setelah stok/header ditulis, dan preservasi snapshot struk setelah produk dihapus. Keduanya hanya boleh dihitung lulus setelah PR-head Android CI dan POS/Room instrumentation menyelesaikan runner dan JUnit report menunjukkan nol failure/error/skip.

Android CI #125 dan POS/Room instrumentation #75 selesai sukses pada head `1eda0369eedb3d88cff9ba7c3a077cebb24ce8c5`. Commit tersebut menambahkan blueprint dokumentasi, bukan perubahan source aplikasi. Run #125 membuktikan 35 JVM tests lulus; Run #75 membuktikan lima instrumentation tests pada emulator API 36. AAB kandidat Run #125 tidak ditandatangani dan bukan binary produksi siap Play Console.

## 7. Batch pekerjaan lanjutan

### Batch 2 — onboarding dan uji UI
- First-run profile setup dan panduan input produk pertama.
- Empty-state yang eksplisit untuk katalog, keranjang, riwayat.
- UI/instrumentation flow: launch → create product → checkout → receipt → history.
- Uji layout pada layar kecil, rotasi, process recreation, dan accessibility.

### Batch 3 — promosi SaaS yang nyata
- Aktifkan external link hanya setelah URL HTTPS resmi hidup dan kontennya diverifikasi.
- Landing page menjelaskan perbedaan Basic vs SaaS, manfaat, proses pendaftaran, kontak resmi, dan biaya yang berlaku.
- Jangan tampilkan signup atau pembayaran yang tidak benar-benar berfungsi.

### Batch 4 — integrasi SaaS (setelah API tersedia)
- OpenAPI contract, auth/token lifecycle, tenant/store identity, sync outbox, idempotency, retry/backoff, cursor, conflict policy, dan opt-in.
- Retensi, penghapusan akun/data, TLS, dan Data Safety review sebelum menambah jalur data jaringan.

### Batch 5 — rilis Google Play
- Lengkapi Privacy Policy publik HTTPS, Terms, kontak, alamat, Data Safety, Ads, content rating, target audience, dan screenshot aktual.
- Buat akun developer Organization melalui perwakilan resmi perusahaan yang memenuhi persyaratan usia Google; siapkan D-U-N-S, dokumen legal, identitas authorized representative, payments profile, email, telepon, dan website organisasi.
- Konfirmasi application ID saat membuat app di Play Console.
- Buat upload key dan simpan hanya di GitHub Environment Secrets; bangun signed AAB, periksa signature/certificate, lalu internal testing dan Pre-launch Report.

## 8. Blocker yang belum boleh dianggap selesai

- Application ID sudah dipatok di Gradle dan tercantum di merged release manifest, tetapi belum dikonfirmasi tersedia/terdaftar di Play Console.
- Developer Organization account dan identitas organisasi belum diverifikasi.
- D-U-N-S serta dokumen badan usaha/perwakilan belum diverifikasi.
- Privacy Policy publik, alamat resmi, email privasi/dukungan, dan URL SaaS belum tersedia.
- Build #125 mencatat `BTP_SAAS_URL` dan `BTP_SUPPORT_EMAIL` kosong, jadi tautan SaaS/email tidak aktif.
- Upload keystore belum disiapkan; AAB masih unsigned.
- Screenshot perangkat fisik dan Pre-launch Report belum tersedia.

## 9. Referensi resmi

- Android application ID / namespace: https://developer.android.com/build/configure-app-module
- Google Play target API: https://support.google.com/googleplay/android-developer/answer/11926878
- Play Data Safety and Privacy Policy: https://support.google.com/googleplay/android-developer/answer/10144311
- Organization account information: https://support.google.com/googleplay/android-developer/answer/13628312
- Developer identity verification: https://support.google.com/android-developer-console/answer/16641416
