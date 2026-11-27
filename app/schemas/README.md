# Room schemas

Room schema JSON hasil KSP disimpan pada direktori ini dan harus dikomit pada setiap perubahan schema database. Jangan menghapus schema versi lama setelah rilis karena dibutuhkan untuk pengujian migrasi.

The database is now at schema version 2. The 1 → 2 migration adds nullable store-identity snapshot columns to `sales_transactions` and must preserve all existing rows.

The first generated schema JSON is still pending a successful KSP/Room cloud build. Do not hand-author or fabricate exported Room schema files. After CI becomes available, commit the generated version 2 schema and add a migration test using the exported version 1 schema before treating the database upgrade as verified.
