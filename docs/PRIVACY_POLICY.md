# Kebijakan Privasi BERKAH POS

**Status dokumen: DRAF — belum layak dipublikasikan.**

Tanggal draf: 27 November 2026

## Pengendali aplikasi
Aplikasi BERKAH POS diterbitkan untuk layanan BERKAH TIGA PUTERI. Identitas badan usaha, alamat korespondensi, alamat email dukungan, dan URL kebijakan publik belum diberikan. Informasi tersebut wajib dilengkapi sebelum pengajuan Google Play.

- Nama pengendali: BERKAH TIGA PUTERI
- Alamat: BELUM DISEDIAKAN — BLOCKER PUBLIKASI
- Email privasi/dukungan: BELUM DISEDIAKAN — BLOCKER PUBLIKASI
- URL publik kebijakan: BELUM DISEDIAKAN — BLOCKER PUBLIKASI

## Data yang dimasukkan pengguna
Pada rancangan aplikasi Basic POS, pengguna dapat menyimpan profil toko, kategori, produk, SKU, harga, stok, transaksi, rincian item, dan informasi struk. Data tersebut disimpan dalam database SQLite di perangkat pengguna.

## Pengiriman data
Rilis Basic POS yang direncanakan tidak memerlukan akun, server aplikasi, atau koneksi internet untuk fungsi POS. Fondasi repository saat ini tidak mendeklarasikan permission INTERNET dan tidak memasukkan Firebase, analytics, crash reporting, atau SDK iklan. Pernyataan final ini tetap harus diverifikasi kembali terhadap seluruh dependency dan manifest pada build release.

## Backup dan berbagi file
Fitur backup manual JSON tersedia melalui Android Storage Access Framework. Pengguna memilih lokasi untuk menulis file cadangan atau memilih file untuk pemulihan. File berisi profil toko, kategori, produk, stok, transaksi, dan rincian item struk. File JSON tidak dienkripsi oleh aplikasi; pengguna harus menyimpannya di lokasi aman dan tidak membagikannya kepada pihak yang tidak dipercaya. Ketika pengguna memilih aplikasi penerima melalui fitur berbagi struk, isi struk diteruskan ke aplikasi yang dipilih pengguna. Pengembang tidak menerima file tersebut melalui aplikasi.

Pemulihan mengganti data POS lokal dengan isi file setelah validasi, dalam satu transaksi database. File yang tidak sesuai format atau tidak konsisten ditolak dan tidak diterapkan sebagian.

Automatic Android application backup dinonaktifkan pada konfigurasi saat ini.

## SDK dan pihak ketiga
Aplikasi menggunakan Jetpack Compose, AndroidX, Room/SQLite, Kotlin Coroutines, dan Preferences DataStore untuk fungsi lokal. Tidak ada SDK iklan, analytics, Firebase, crash reporting, atau SDK pengumpulan data pihak ketiga yang direncanakan. Daftar dependency dan semua SDK transitif tetap harus diverifikasi dari dependency graph release sebelum publikasi.

## Retensi dan penghapusan
Data POS disimpan di perangkat sampai pengguna memulihkan backup lain, menghapus data melalui pengaturan Android, atau menghapus aplikasi. Penghapusan profil/produk/transaksi satu per satu mengikuti operasi yang tersedia di aplikasi. File backup yang diekspor tetap berada di lokasi yang dipilih pengguna dan harus dihapus sendiri oleh pengguna bila tidak lagi diperlukan.

## Hak pengguna
Pengguna mengendalikan data usaha yang dimasukkan ke aplikasi dan dapat meminta informasi atau mengajukan pertanyaan privasi melalui kontak pengendali yang harus dilengkapi sebelum publikasi.

## Perubahan kebijakan
Perubahan substansial akan dicantumkan pada halaman kebijakan publik dan versi aplikasi terkait.

---
Dokumen ini bukan URL kebijakan publik. Jangan mengirim aplikasi ke Google Play sebelum seluruh informasi pengendali, kontak, dan URL HTTPS publik tersedia serta isi kebijakan sesuai implementasi release sebenarnya.
