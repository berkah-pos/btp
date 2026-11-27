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

**Android CI [run #100](https://github.com/berkah-pos/btp/actions/runs/46039337155)** selesai **success** pada 27 November 2026 untuk PR #4 source/documentation head `4c66e0cd96019ceeaa1a92319c88ec751563d50b`.

- `testDebugUnitTest`: **31 test, 0 failure, 0 error, 0 skipped**, diverifikasi dari enam JUnit XML dalam artifact.
- `compileDebugAndroidTestKotlin`: **success**.
- `lintDebug`: **0 error, 2 warning** — `OldTargetApi` untuk `targetSdk=36` dan `AndroidGradlePluginVersion` karena Gradle 9.9.2 memiliki patch 9.9.3 yang lebih baru.
- `assembleDebug`: **success**; APK debug diunggah.
- `zipalign -c -P 16 -v 4` dan audit ELF `PT_LOAD` 16 KiB: **success**; keempat library native pada ABI `arm64-v8a` dan `x86_64` lulus.
- Merged release manifest dan release runtime dependency inventory berhasil ditangkap.
- App icon dan feature graphic Google Play berhasil dirender dan diunggah.
- Signed release AAB **SKIPPED** pada pull-request run.

Artifacts:
- [Debug APK, enam JUnit XML, laporan lint, Room schemas, dan release audit](https://github.com/berkah-pos/btp/actions/runs/46039337155/artifacts/15218593773).
- [Play Store icon dan feature graphic](https://github.com/berkah-pos/btp/actions/runs/46039337155/artifacts/15218748665).

**POS and Room Instrumentation Tests [run #51](https://github.com/berkah-pos/btp/actions/runs/46039337136)** selesai **success** pada API 36 GitHub-hosted emulator.

- **4 instrumentation test, 0 failure, 0 error, 0 skipped**, diverifikasi dari JUnit XML.
- Dua fixture schema v1 (KSP asli dan bentuk historis) berhasil melewati production `MIGRATION_1_2`; Room memvalidasi schema v2.
- Dua checkout instrumentation test membuktikan agregasi baris duplikat, total dan pengurangan stok yang benar, snapshot identitas toko historis, serta rollback stok ketika conditional decrement berikutnya gagal.
- [POS/Room instrumentation report dan emulator outputs](https://github.com/berkah-pos/btp/actions/runs/46039337136/artifacts/15219183361).

**Dependency verification — provenance yang sedang dipin**

- Source baseline: Android CI [run #96](https://github.com/berkah-pos/btp/actions/runs/46032064890), quality job **success**, source commit `0739913749fa0783a9d6784ad7444bccbca3fa3f`.
- Artifact `berkah-pos-debug-96`, ID `15215416046`, archive SHA-256 `fde80191ddb1b52e9388615ac1ab0a27f566422c903547bf1f2b4b258ca67947`. Archive diunduh dan digest diverifikasi.
- `gradle/verification-metadata.xml` dari artifact tersebut memiliki SHA-256 `b75c079d854746d701e5312fd11745038d7f1d2052d6818c81e8f69ff812eec6`: **459 components / 754 unique SHA-256 artifact entries**.
- Android CI #98, #99, dan #100 menghasilkan metadata dengan hash dan inventory identik setelah `--refresh-dependencies`.
- Manual dispatch #95 sebelumnya gagal karena guard masih mengharapkan kandidat lama `9adc4cac…` (455/745). Workflow pada branch saat ini sudah dipindahkan ke source Run #96 dan hash `b75c079d…`; commit `81b9cef` menyelaraskan nomor run pada manifest provenance.
- Metadata menetapkan `verify-metadata=true`, tetapi `verify-signatures=false`; checksum pinning bukan autentikasi tanda tangan publisher.
- File baseline belum dikomit dan clean `--dependency-verification=strict` **belum lulus**.

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
- Android CI #100 dan POS/Room instrumentation #51 lulus pada source/documentation head `4c66e0c`; dua checkout instrumentation test terverifikasi. Commit baru `65cb277` menambahkan strict UTF-8 decoding pada restore backup dan tiga test unit; hasil CI untuk commit baru ini masih pending.

## Cloud build

Workflow memakai standard GitHub-hosted `ubuntu-24.04`, JDK 17, AGP 9.5.3, built-in Kotlin/Compose Compiler 2.4.21, Android SDK API 37.1, Build Tools 36.0.0, dan Gradle 9.9.2. Android CI #100 meluluskan 31 JVM tests, lint (0 error, 2 warning), instrumentation-source compilation, debug APK, serta audit APK 16 KiB. POS/Room instrumentation run #51 meluluskan empat test: dua migrasi SQLite v1→v2 dan dua POS checkout.

Dependency baseline kini dipin ke artifact Run #96 dan direproduksi identik pada Run #98, #99, dan #100: 459 komponen, 754 checksum, XML SHA-256 `b75c079d854746d701e5312fd11745038d7f1d2052d6818c81e8f69ff812eec6`. File belum dikomit dan strict dependency verification belum lulus. Signed AAB tidak dibuat pada PR.

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

Backup JSON versi 3 berada di lokasi pilihan pengguna dan **tidak dienkripsi oleh aplikasi**. File memuat profil toko, kategori, produk, stok, transaksi, snapshot identitas toko per transaksi, rincian struk, dan preferensi ambang stok rendah. Restore tetap menerima backup versi 2; transaksi lama tanpa snapshot tetap menampilkan identitas toko sebagai tidak tersedia, bukan mengambil profil terbaru. Commit `65cb277` menambahkan decoder UTF-8 strict agar byte sequence rusak ditolak, bukan diam-diam diganti karakter pengganti; tiga test baru menunggu hasil CI. Automatic Android backup dan transfer perangkat dikecualikan melalui manifest serta aturan backup legacy/Android 12+. Backup POS tetap dilakukan manual melalui file JSON yang dipilih pengguna.

## Repository public

Repository ini public. Standard GitHub-hosted runners untuk public repository tidak dikenai biaya menit runner, tetapi artifact storage tetap memiliki kuota dan retensi. Jangan commit signing keystore, password, token, atau data usaha nyata.
