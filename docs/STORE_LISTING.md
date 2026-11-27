# Draft Google Play Store listing — BERKAH POS

Status: DRAFT. Use only after the release build has passed functional testing and every claim below has been checked against the final binary.

## App identity
- App name: **BERKAH POS - Kasir UMKM**
- Candidate application ID: `id.co.berkahtigaputeri.basicpos` (derived from `berkahtigaputeri.co.id`; must be checked in Play Console before first publication)
- Category proposal: Business
- Developer/brand: BERKAH TIGA PUTERI
- Short description (under 80 characters): `Kasir offline untuk UMKM: produk, stok, transaksi, dan struk.`

## Full description draft

**BERKAH POS — Kasir Offline untuk UMKM**

Kelola pencatatan penjualan harian usaha kecil Anda dengan aplikasi kasir sederhana yang menyimpan data langsung di perangkat.

**Fitur utama**
- **Kasir offline:** pilih produk, atur jumlah, hitung subtotal, diskon, pajak, pembayaran, dan kembalian.
- **Manajemen produk:** simpan nama, SKU, harga, stok, dan kategori.
- **Ringkasan usaha:** pantau penjualan hari ini, jumlah transaksi, jumlah produk, dan stok rendah.
- **Riwayat dan struk:** lihat rincian transaksi dan bagikan struk melalui aplikasi yang Anda pilih.
- **Profil toko:** atur nama toko, pemilik, alamat, nomor telepon, dan catatan struk.
- **Backup dan pemulihan:** ekspor atau pulihkan profil, produk, stok, transaksi, struk, dan ambang stok rendah melalui file JSON yang Anda pilih sendiri.

**Privasi dan penggunaan**
- Tidak memerlukan akun.
- Fungsi POS inti dirancang berjalan tanpa koneksi internet.
- Data usaha disimpan di perangkat menggunakan SQLite.
- Tidak menggunakan Firebase, analytics, crash reporting, atau SDK iklan pihak ketiga pada rancangan aplikasi ini.
- File backup JSON tidak dienkripsi oleh aplikasi. Simpan file cadangan di tempat aman dan jangan membagikannya kepada orang yang tidak dipercaya.

BERKAH POS Basic merupakan aplikasi pencatatan kasir lokal. Informasi layanan POS dan manajemen bisnis dari BERKAH TIGA PUTERI tersedia pada halaman informasi di dalam aplikasi. Tautan dan kontak resmi akan ditampilkan setelah dikonfigurasi.

## App access instructions for reviewers
No login, no registration, and no account credentials are required. Open the app and use its local features. Internet access is not required for POS workflows. To explore the cashier flow, add a product manually or use the clearly labelled optional sample products in the Products screen. Sample data adds products only; it does not create sample transactions or sales.

## Declarations — draft, not submitted
- Ads: do not select a final answer until the release binary, dependency graph, and in-app SaaS promotion have been reviewed. The source contains no third-party ad SDK, banner, or interstitial, but it includes an informational page for the developer's own SaaS service.
- Data Safety: see `DATA_SAFETY_REVIEW.md`. Complete the Play Console form only after the release merged manifest and all transitive SDKs have been inspected.
- Target audience: choose in Play Console based on the actual intended users and applicable age requirements; not yet confirmed.
- Content rating: complete the IARC questionnaire in Play Console; not yet submitted.
- App access: no login or restricted app sections.
- Privacy Policy URL: BLOCKED — official public HTTPS URL is not available yet.
- Support email/website: BLOCKED until official values are supplied through repository Actions Variables.

## Required graphics
- App icon: source at `docs/store-assets/app-icon.svg`; render as a 512x512 32-bit PNG with alpha for Play Console.
- Feature graphic: source at `docs/store-assets/feature-graphic.svg`; render as a 1024x500 24-bit PNG/JPEG without alpha.
- Phone screenshots: NOT PROVIDED. They must be captured from the actual tested application, not generated mockups or placeholder screens.
- Preview video: optional and not prepared.

## Release notes draft — initial release
Rilis awal BERKAH POS Basic dengan kasir offline, katalog produk, pencatatan stok, riwayat transaksi, struk, profil toko, dan backup JSON. Finalize this text after the tested release binary is available.
