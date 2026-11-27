# Room schemas

Room schema JSON yang diekspor KSP dikomit di direktori ini pada setiap perubahan skema database. Jangan menghapus versi lama setelah dirilis karena file tersebut diperlukan untuk pengujian migrasi.

## Current schema

- Version 2: `id.berkahtigaputeri.basicpos.data.local.BerkahPosDatabase/2.json`.
- Schema v2 dihasilkan oleh KSP dalam GitHub Actions run [45969254895](https://github.com/berkah-pos/btp/actions/runs/45969254895) dan disimpan di repository.
- Migrasi 1 → 2 menambahkan lima kolom snapshot identitas toko nullable pada `sales_transactions`; migrasi menggunakan `ALTER TABLE` dan tidak menghapus tabel atau baris transaksi.

## Migration-test gap

Schema v1 yang diekspor dari build historis belum tersedia di repository maupun artifact yang dapat diverifikasi. Jangan membuat schema v1 secara manual. Karena itu, migrasi 1 → 2 belum dapat dinyatakan teruji terhadap database v1 aktual. Tambahkan schema v1 asli dan migration test menggunakan fixture tersebut sebelum rilis pertama.
