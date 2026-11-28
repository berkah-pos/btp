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

- POS and Room Instrumentation Tests [run #75](https://github.com/berkah-pos/btp/actions/runs/46083906878) completed **success** on the API 36 GitHub-hosted emulator.
- JUnit XML confirms **5 tests, 0 failures, 0 errors, 0 skipped**; instrumentation exit code 0.
- `migratesOriginalKspVersionOneAndPreservesSalesAndReceiptItems` passed.
- `migratesHistoricalVersionOneWithProductIdAndReceiptSnapshotsAlreadyPresent` passed.
- `checkoutAggregatesDuplicateLinesAndKeepsHistoricalReceiptSnapshot` passed: duplicate lines became one receipt line, stock and totals were correct, and the old store identity remained on the receipt after profile update.
- `failedStockUpdateRollsBackEarlierDecrementsAndDoesNotCreateReceipt` passed: the second conditional update was forced to affect zero rows; the first stock decrement rolled back and no transaction was created.
- `concurrentCheckoutsCannotSellTheLastUnitTwice` passed: two concurrent requests for the final unit produced one receipt, one rejection, zero stock, and exactly one transaction.
- [JUnit report and emulator artifacts](https://github.com/berkah-pos/btp/actions/runs/46083906878/artifacts/15238442704).
- The two migration tests open genuine SQLite v1 fixtures through production `MIGRATION_1_2`; Room validates schema v2.
- The other three tests exercise repository checkout behavior, including a concurrent final-stock race.

Jangan membuat ulang atau mengubah schema v1 secara manual. Gunakan fixture KSP asli yang sudah dikomit.

