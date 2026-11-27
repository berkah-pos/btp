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
Fitur backup manual JSON akan memungkinkan pengguna memilih lokasi ekspor atau file pemulihan melalui Android Storage Access Framework. Ketika pengguna secara eksplisit membagikan file, penerima dan aplikasi pihak ketiga yang dipilih berada di luar kendali pengembang. Pengguna harus memeriksa isi file sebelum membagikannya.

Automatic Android application backup dinonaktifkan pada konfigurasi saat ini. Data dapat tetap ikut dalam salinan perangkat yang dibuat pengguna atau mekanisme sistem lain di luar kendali aplikasi.

## SDK dan pihak ketiga
Tidak direncanakan penggunaan SDK iklan, analytics, Firebase, atau SDK pengumpulan data pihak ketiga. Daftar SDK final harus dibuat dari pemeriksaan dependency graph release sebelum publikasi.

## Retensi dan penghapusan
Data POS disimpan sampai pengguna menghapusnya dari aplikasi atau menghapus data aplikasi/uninstall. Fitur penghapusan dan reset data harus disediakan dan diuji sebelum rilis.

## Hak pengguna
Pengguna mengendalikan data usaha yang dimasukkan ke aplikasi dan dapat meminta informasi atau mengajukan pertanyaan privasi melalui kontak pengendali yang harus dilengkapi sebelum publikasi.

## Perubahan kebijakan
Perubahan substansial akan dicantumkan pada halaman kebijakan publik dan versi aplikasi terkait.

---
Dokumen ini bukan URL kebijakan publik. Jangan mengirim aplikasi ke Google Play sebelum seluruh informasi pengendali, kontak, dan URL HTTPS publik tersedia serta isi kebijakan sesuai implementasi release sebenarnya.
