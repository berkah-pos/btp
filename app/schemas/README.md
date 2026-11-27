# Room schemas

Room schema JSON hasil ekspor KSP disimpan per versi database. Jangan menghapus schema versi lama karena file tersebut dipakai oleh pengujian migrasi.

## Current schemas

- Version 1: `id.berkahtigaputeri.basicpos.data.local.BerkahPosDatabase/1.json`.
  Schema v1 asli hasil KSP dikomit melalui commit [`be1797a`](https://github.com/berkah-pos/btp/commit/be1797ad5ff9eab6bca125d4fdb784589edb6647).
- Version 2: `id.berkahtigaputeri.basicpos.data.local.BerkahPosDatabase/2.json`.
  Schema v2 diekspor oleh KSP dan diperiksa pada artifact CI.

## Migration 1 → 2

Migrasi mempertahankan data transaksi dan menambahkan hanya kolom yang belum ada. Ini menangani schema v1 asli serta dua bentuk database v1 historis yang mengalami perubahan tabel tanpa kenaikan nomor versi:

- `transaction_items.productId` beserta index-nya.
- Lima kolom snapshot struk nullable pada `sales_transactions`.

Instrumentation test ada pada `app/src/androidTest/java/id/berkahtigaputeri/basicpos/data/local/BerkahPosDatabaseMigrationTest.kt` dan mencakup kedua bentuk database tersebut.

## Verification status

- Run [#41, attempt 2](https://github.com/berkah-pos/btp/actions/runs/45989244818) berhasil mengompilasi `compileDebugAndroidTestKotlin`.
- Run #41 tidak menjalankan `connectedDebugAndroidTest`; karena itu migrasi 1 → 2 **belum dinyatakan lulus**.
- Workflow terpisah `.github/workflows/room-migration-tests.yml` ditambahkan pada branch PR. Workflow ini menjalankan tes pada API 36 cloud emulator ketika file migrasi/schema berubah dan menyediakan `workflow_dispatch`. Hasil emulator harus diperiksa sebelum migrasi dinyatakan PASS.

Jangan membuat ulang atau mengubah schema v1 secara manual. Gunakan fixture KSP asli yang sudah dikomit.
