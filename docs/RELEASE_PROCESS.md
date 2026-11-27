# Proses release cloud

## CI
- Semua job memakai GitHub-hosted `ubuntu-24.04`; self-hosted runner dilarang.
- Pull request dan push pada branch utama/pengembangan menjalankan unit test, lint, dan debug build.
- Release bundle hanya melalui `workflow_dispatch` dengan input `build_release=true`.
- Release job menggunakan GitHub Environment bernama `android-release`.

## GitHub Environment secrets
Buat secrets berikut di Settings → Environments → android-release:
- `ANDROID_UPLOAD_KEYSTORE_BASE64`
- `ANDROID_UPLOAD_STORE_PASSWORD`
- `ANDROID_UPLOAD_KEY_ALIAS`
- `ANDROID_UPLOAD_KEY_PASSWORD`

Keystore harus dibuat/dipegang oleh pemilik aplikasi melalui mekanisme aman. Jangan pernah commit keystore atau password. Workflow gagal secara eksplisit jika secret belum tersedia; tidak membuat atau mengklaim upload key secara otomatis.

## Output
- Debug APK dan laporan test: artifact berumur pendek.
- Release AAB, SHA256SUMS, release-metadata.json, dan laporan: artifact dengan retention lebih panjang.
- AAB tidak dikomit ke repository.
- Publikasi ke Google Play tidak dilakukan otomatis.

## Signing
AAB ditandatangani memakai upload key. Google Play App Signing perlu diaktifkan/diterima pada Play Console. Verifikasi sertifikat hasil build dan cocokkan fingerprint dengan upload certificate yang terdaftar.

## Catatan wrapper
Workflow memasang Gradle versi tetap 9.6.0 pada runner, kemudian membangkitkan wrapper di workspace CI sebelum build. Wrapper JAR bootstrap harus ditambahkan dan di-commit pada checkpoint hardening CI setelah diperoleh melalui proses terverifikasi. Wrapper distribution URL saat ini telah dipatok di `gradle-wrapper.properties`.
