# Room schemas

Room schema JSON yang diekspor KSP dikomit di direktori ini pada setiap perubahan skema database. Jangan menghapus versi lama setelah dirilis karena file tersebut diperlukan untuk pengujian migrasi.

## Current schema

- Version 2: `id.berkahtigaputeri.basicpos.data.local.BerkahPosDatabase/2.json`.
- Schema v2 dihasilkan oleh KSP dan disertakan pada artifact debug dari run [45975233350](https://github.com/berkah-pos/btp/actions/runs/45975233350) untuk commit PR #4 `7e371fd29482b3e81359c73b4d34fd5945c42bf2`; file hasil ekspor tersebut telah dikomit di repository.
- Migrasi 1 → 2 menambahkan lima kolom snapshot identitas toko nullable pada `sales_transactions` melalui `ALTER TABLE`; migrasi tidak menghapus tabel atau baris transaksi.

## Migration-test gap

Schema v1 hasil ekspor KSP dari build historis belum tersedia di repository maupun artifact yang dapat diverifikasi. Jangan membuat schema v1 secara manual. Karena itu migrasi 1 → 2 belum dapat dinyatakan teruji terhadap database v1 aktual. Tambahkan schema v1 asli dan migration test menggunakan fixture tersebut sebelum release.
