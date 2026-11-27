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

**Android CI [run #111](https://github.com/berkah-pos/btp/actions/runs/46066169926)** completed successfully on source head `07d4f67a10f2fc2714f0a53c366ae724814991cc`.

- `testDebugUnitTest`: **35 tests, 0 failures, 0 errors, 0 skipped**, verified from seven uploaded JUnit XML reports.
- `compileDebugAndroidTestKotlin`: successful.
- `lintDebug`: **0 errors, 3 warnings**: `OldTargetApi` at target API 36; `AndroidGradlePluginVersion` recommends Gradle 9.9.3 and AGP 9.5.3/9.3.3. The selected Gradle 9.7.0 / AGP 9.3.1 pair stays within Kotlin 2.4.21's fully-supported matrix.
- `assembleDebug`: successful; debug APK uploaded.
- APK ZIP alignment and all four packaged 64-bit native ELF 16 KiB checks passed.
- Merged release manifest and release runtime dependency inventory captured.
- Play Store icon and feature graphic rendered and uploaded.
- Signed release AAB **SKIPPED** on the pull-request event.
- Strict quality gates against a committed dependency baseline were **SKIPPED** because the baseline is not yet committed.

Artifacts:
- [Debug APK, seven JUnit XML reports, lint, Room schemas, and release audit](https://github.com/berkah-pos/btp/actions/runs/46066169926/artifacts/15230114223).
- [Play Store icon and feature graphic](https://github.com/berkah-pos/btp/actions/runs/46066169926/artifacts/15230783541).

**POS and Room Instrumentation Tests [run #61](https://github.com/berkah-pos/btp/actions/runs/46066169924)** completed successfully on the API 36 GitHub-hosted emulator.

- **5 instrumentation tests, 0 failures, 0 errors, 0 skipped**, verified from the uploaded JUnit XML.
- Two historical SQLite v1 fixtures migrated through production `MIGRATION_1_2`; Room validated schema v2.
- Three checkout tests cover duplicate cart-line aggregation, rollback after a conditional stock-decrement failure, and concurrent attempts to sell the last stock unit.
- [POS/Room instrumentation report and emulator outputs](https://github.com/berkah-pos/btp/actions/runs/46066169924/artifacts/15230593958).

**Dependency verification — candidate from Run #110; not yet approved**

- Source commit: `403fcf21a3b57037f6b24319431727ceda98e36a`; Android CI quality job succeeded.
- Artifact `berkah-pos-debug-110`, ID `15230362895`, archive SHA-256 `93c62c7389f631186775b8c2fcc9345015cbd066fe1bdc990d3b6632c36238fb`.
- `gradle/verification-metadata.xml` SHA-256 `6d0c1f37295c615c9621fda3b6ea3a32ab0da463c117766ba6b6fe06795a08f3`: **578 components / 960 unique SHA-256 artifact entries**. Runs #110 and #111 produced identical metadata. Structural validation found no duplicate coordinates, malformed hashes, or unexpected checksum origins.
- Compared with Run #106's AGP 9.5.3 graph (462/758), the AGP 9.3.1 graph contains 175 additional components, 59 fewer components, and one additional artifact hash on a shared component. Most changes are AGP/build and Android Test Platform dependencies; the separate `releaseRuntimeClasspath` inventory does not show added app runtime SDKs.
- Metadata sets `verify-metadata=true` and `verify-signatures=false`; checksum pinning is not publisher-signature authentication.
- The candidate baseline is not committed and clean `--dependency-verification=strict` **has not passed**. The guarded workflow is pinned to Run #110; its manual baseline dispatch and clean strict verification remain outstanding.

## Keputusan teknis

- Room memakai schema versi 2 dan migration `1 → 2` yang memeriksa kolom historis sebelum menambah kolom nullable. Schema v1 asli dan bentuk historis diuji dengan fixture SQLite nyata.
- Checkout menyimpan header transaksi, item snapshot, pengurangan stok, dan snapshot profil toko dalam satu transaksi Room.
- Backup JSON versi 3 menerima format versi 2; validator memeriksa relasi, nominal, duplikasi ID/SKU/nomor transaksi, dan overflow sebelum restore.
- Room dan Preferences DataStore tidak berbagi satu transaksi ACID. Restore memakai kompensasi preference dan satu operation gate aplikasi untuk menyerialkan backup/restore dengan mutasi profil dan ambang stok.
- File backup JSON tidak dienkripsi oleh aplikasi; pengguna harus menyimpannya secara aman.

## Identitas aplikasi

Candidate applicationId: `id.berkahtigaputeri.basicpos`. Sebelum signed release, pemilik aplikasi harus mengonfirmasi ID permanen melalui Play Console dan mengisi GitHub Actions Repository Variable `BTP_APPLICATION_ID`. Jangan mengubah applicationId setelah publikasi pertama.

## Riwayat dan review

- [PR #1 — Android foundation](https://github.com/berkah-pos/btp/pull/1), [PR #2 — koreksi dokumentasi pasca-merge](https://github.com/berkah-pos/btp/pull/2), dan [PR #3 — koreksi Android SDK/CI](https://github.com/berkah-pos/btp/pull/3) telah merged pada 27 November 2026.
- [PR #4 — hardening fondasi, backup, Room migration, POS checkout, dan aksesibilitas](https://github.com/berkah-pos/btp/pull/4) tetap **open, draft, dan belum merged**.
- Semua perubahan lanjutan berada di `feat/basic-pos-mvp`; tidak ada commit langsung ke `main`.
- Android CI #111 dan POS/Room instrumentation #61 lulus pada source head `07d4f67`. JUnit XML membuktikan 35 JVM tests dan 5 emulator instrumentation tests lulus. PR #4 tetap draft dan belum merged.

## Cloud build

Workflow memakai GitHub-hosted `ubuntu-24.04`, JDK 17, AGP 9.3.1, Kotlin/Compose Compiler 2.4.21, KSP 2.3.14, Android SDK API 37.1, Build Tools 36.0.0, dan Gradle 9.7.0.

Kombinasi AGP/Gradle berada dalam irisan rentang fully-supported Kotlin 2.4.21: Gradle 7.6.3–9.7.0 dan AGP 8.5.2–9.3.1. AGP 9.3.1 adalah minimum untuk compile SDK API 37.1. Android CI #111 membuktikan 35 JVM tests, Compose plugin resolution, lint, debug build, dan audit APK 16 KiB berhasil pada toolchain tersebut. POS/Room instrumentation #61 membuktikan lima test pada emulator API 36.

Lint menghasilkan tiga warning dan nol error: `OldTargetApi` (target SDK 36), rekomendasi Gradle 9.9.3, dan rekomendasi AGP 9.5.3/9.3.3. Versi yang lebih baru tidak dipilih karena akan keluar dari batas fully-supported Kotlin 2.4.21.

Dependency baseline kandidat dari Run #110: 578 komponen, 960 SHA-256 artifact entries; XML SHA-256 `6d0c1f37295c615c9621fda3b6ea3a32ab0da463c117766ba6b6fe06795a08f3`. Metadata identik pada Run #111. File belum dikomit dan strict dependency verification belum lulus. Signed AAB tidak dibuat pada PR.

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

Backup JSON versi 3 berada di lokasi pilihan pengguna dan **tidak dienkripsi oleh aplikasi**. File memuat profil toko, kategori, produk, stok, transaksi, snapshot identitas toko per transaksi, rincian struk, dan preferensi ambang stok rendah. Restore tetap menerima backup versi 2; transaksi lama tanpa snapshot tetap menampilkan identitas toko sebagai tidak tersedia, bukan mengambil profil terbaru. Decoder UTF-8 strict menolak byte sequence malformed atau truncated alih-alih diam-diam menggantinya dengan karakter pengganti; ketiga test decoder lulus pada Android CI #111. Automatic Android backup dan transfer perangkat dikecualikan melalui manifest serta aturan backup legacy/Android 12+. Backup POS tetap dilakukan manual melalui file JSON yang dipilih pengguna.

## Repository public

Repository ini public. Standard GitHub-hosted runners untuk public repository tidak dikenai biaya menit runner, tetapi artifact storage tetap memiliki kuota dan retensi. Jangan commit signing keystore, password, token, atau data usaha nyata.
