# Google Play Data Safety — source review

Status: **NOT SUBMITTED / NOT FINAL**. Ini penilaian source-level, bukan deklarasi Play Console yang sudah diselesaikan.

## Observasi implementasi saat ini
- Run GitHub Actions [45975233350](https://github.com/berkah-pos/btp/actions/runs/45975233350) berhasil menjalankan debug build pada 27 November 2026. Karena itu merged debug manifest telah diproses oleh build, tetapi isinya belum disimpan/direview sebagai bukti final. Merged release manifest dan release dependency graph masih harus diperiksa sebelum publikasi.
- Source manifest tidak mendeklarasikan permission `INTERNET`, contacts, phone, camera, location, atau broad storage. Ini harus diverifikasi ulang pada merged release manifest.
- Profil toko, kategori, produk, stok, header transaksi, item transaksi, dan preferensi ambang stok rendah disimpan lokal melalui Room/SQLite dan Preferences DataStore.
- Dependency yang dideklarasikan langsung adalah AndroidX/Jetpack, Room, Kotlin Coroutines, DataStore, Compose, dan Navigation. Tidak ada Firebase, analytics, crash reporting, ad SDK, atau remote POS backend yang sengaja dimasukkan.
- Tidak ada akun pengguna atau registrasi.
- Backup JSON manual mengekspor data yang dipilih pengguna melalui Android Storage Access Framework. File tidak dienkripsi oleh aplikasi.
- Berbagi struk menggunakan Android system chooser setelah pengguna memilih Share; pengguna memilih aplikasi penerima.
- Aplikasi tidak mengunggah data POS ke server BERKAH TIGA PUTERI.

## Interpretasi kebijakan untuk diverifikasi
Google Play mengecualikan data yang hanya diproses lokal di perangkat dan tidak dikirim keluar dari definisi data yang dikumpulkan. Transfer yang dimulai secara eksplisit oleh pengguna dan secara wajar diharapkan pengguna memiliki pengecualian tertentu dari definisi sharing. Ekspor backup dan berbagi struk tampak termasuk tindakan yang dimulai pengguna, tetapi interpretasi ini wajib dicocokkan dengan perilaku release aktual dan formulir Play Console. Catatan ini bukan deklarasi final.

## Kandidat jawaban jika inspeksi release tidak menemukan aliran data tambahan
- Data yang dikumpulkan off-device oleh BERKAH POS: kandidat **No**.
- Data yang dibagikan kepada pihak ketiga: kandidat **No** untuk pengiriman yang dioperasikan developer; transfer yang dimulai pengguna harus dinilai terhadap pengecualian user-initiated action.
- Data yang diproses lokal: profil toko, kategori, produk, stok, transaksi, dan rincian struk.
- Enkripsi in transit: tidak ada transfer POS yang dioperasikan aplikasi pada desain offline-first. File backup JSON tidak dienkripsi oleh aplikasi saat disimpan.
- Penghapusan akun: tidak berlaku selama tidak ada pembuatan akun/login. Pengguna dapat menghapus data lokal melalui kontrol penyimpanan Android/uninstall; belum ada kontrol in-app khusus untuk menghapus seluruh data POS.
- SDK: semua dependency transitif dan kontribusi manifest harus diperiksa sebelum memilih jawaban final.

## Verifikasi wajib sebelum release
1. Simpan dan inspeksi merged release manifest.
2. Inspeksi `releaseRuntimeClasspath` dan semua library/SDK transitif.
3. Pastikan tidak ada dependency yang mengirim telemetry atau data lain off-device.
4. Inspeksi external intent untuk berbagi struk serta ekspor/restore Storage Access Framework.
5. Pastikan tidak ada network client, WebView konten developer, crash reporting, ads, atau analytics yang tidak diungkapkan.
6. Cocokkan Privacy Policy final dengan perilaku aktual.
7. Lengkapi dan simpan Data Safety form di Play Console. Jangan menganggap catatan source review ini sebagai pengganti deklarasi.
8. Tinjau ulang deklarasi pada setiap release yang mengubah aliran data.

## Blocker privasi yang diketahui
- URL HTTPS kebijakan privasi publik belum tersedia.
- Kontak privasi dan alamat resmi pengendali belum tersedia.
- Release merged manifest dan dependency graph belum ditinjau; debug build yang berhasil bukan pengganti pemeriksaan release variant.
- Metadata dependency verification SHA-256 baru dihasilkan melalui bootstrap CI dan belum menjadi baseline strict yang direview/di-commit.
