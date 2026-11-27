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

- Room Migration Tests [run #34](https://github.com/berkah-pos/btp/actions/runs/46018281201) completed **success** on the API 36 GitHub-hosted emulator.
- JUnit XML confirms **2 tests, 0 failures, 0 errors, 0 skipped**; instrumentation exit code 0.
- `migratesOriginalKspVersionOneAndPreservesSalesAndReceiptItems` passed.
- `migratesHistoricalVersionOneWithProductIdAndReceiptSnapshotsAlreadyPresent` passed.
- Both tests open genuine SQLite v1 fixtures through production `MIGRATION_1_2`; Room validates the resulting v2 schema.
- Earlier Run #41 only compiled the instrumentation source and did not execute emulator tests; that checkpoint is superseded by Run #34.

Jangan membuat ulang atau mengubah schema v1 secara manual. Gunakan fixture KSP asli yang sudah dikomit.
