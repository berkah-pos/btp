package id.berkahtigaputeri.basicpos.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import id.berkahtigaputeri.basicpos.BuildConfig
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import id.berkahtigaputeri.basicpos.domain.model.StoreProfile

private enum class LegalDocument { PRIVACY, TERMS }

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val profile by viewModel.profile.collectAsState()
    val message by viewModel.message.collectAsState()
    val saving by viewModel.isSaving.collectAsState()
    val backupBusy by viewModel.isBackupBusy.collectAsState()
    val lowStockThreshold by viewModel.lowStockThreshold.collectAsState()
    val uriHandler = LocalUriHandler.current

    var showRestoreConfirmation by remember { mutableStateOf(false) }
    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) viewModel.exportBackup(uri)
    }
    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.restoreBackup(uri)
    }

    var storeName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var receiptFooter by remember { mutableStateOf("") }
    var validationMessage by remember { mutableStateOf<String?>(null) }
    var lowStockThresholdText by remember { mutableStateOf("5") }
    var thresholdValidationMessage by remember { mutableStateOf<String?>(null) }
    var legalDocument by remember { mutableStateOf<LegalDocument?>(null) }

    LaunchedEffect(lowStockThreshold) {
        lowStockThresholdText = lowStockThreshold.toString()
    }

    LaunchedEffect(profile) {
        storeName = profile?.storeName.orEmpty()
        ownerName = profile?.ownerName.orEmpty()
        address = profile?.address.orEmpty()
        phoneNumber = profile?.phoneNumber.orEmpty()
        receiptFooter = profile?.receiptFooter.orEmpty()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Pengaturan") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Profil usaha", style = MaterialTheme.typography.titleLarge)
            Text(
                "Informasi ini digunakan pada data toko dan struk transaksi.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = storeName,
                onValueChange = { storeName = it; validationMessage = null },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Nama toko *") }
            )
            OutlinedTextField(
                value = ownerName,
                onValueChange = { ownerName = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Nama pemilik (opsional)") }
            )
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Alamat (opsional)") }
            )
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Nomor telepon (opsional)") }
            )
            OutlinedTextField(
                value = receiptFooter,
                onValueChange = { receiptFooter = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Catatan bawah struk (opsional)") }
            )
            if (validationMessage != null) {
                Text(
                    validationMessage!!,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                    color = MaterialTheme.colorScheme.error
                )
            }
            Button(
                onClick = {
                    if (storeName.isBlank()) {
                        validationMessage = "Nama toko wajib diisi."
                    } else {
                        viewModel.saveProfile(
                            StoreProfile(
                                storeName = storeName,
                                ownerName = ownerName,
                                address = address,
                                phoneNumber = phoneNumber,
                                receiptFooter = receiptFooter
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !saving
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.semantics {
                            contentDescription = "Menyimpan profil toko"
                        }
                    )
                } else {
                    Text("Simpan profil toko")
                }
            }

            if (message != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { liveRegion = LiveRegionMode.Polite }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(message!!)
                        TextButton(onClick = viewModel::dismissMessage) {
                            Text("Tutup")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("BERKAH TIGA PUTERI", style = MaterialTheme.typography.titleLarge)
            Text(
                "Solusi POS dan Manajemen Bisnis",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Layanan SaaS perusahaan dapat membantu pengelolaan operasional usaha. BERKAH POS Basic pada tahap ini tetap berjalan secara lokal, gratis, dan tanpa akun.",
                style = MaterialTheme.typography.bodyMedium
            )
            if (BuildConfig.SAAS_URL.isNotBlank()) {
                TextButton(onClick = { uriHandler.openUri(BuildConfig.SAAS_URL) }) {
                    Text("Kunjungi layanan SaaS")
                }
            } else {
                Text(
                    "Tautan layanan SaaS belum dikonfigurasi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (BuildConfig.SUPPORT_EMAIL.isNotBlank()) {
                TextButton(onClick = { uriHandler.openUri("mailto:" + BuildConfig.SUPPORT_EMAIL) }) {
                    Text("Email dukungan: ${BuildConfig.SUPPORT_EMAIL}")
                }
            } else {
                Text(
                    "Email dukungan resmi belum dikonfigurasi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text("Pengaturan stok", style = MaterialTheme.typography.titleLarge)
            Text(
                "Produk dengan stok sama dengan atau di bawah ambang ini akan ditandai sebagai stok rendah pada ringkasan usaha.",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = lowStockThresholdText,
                onValueChange = {
                    lowStockThresholdText = it.filter(Char::isDigit)
                    thresholdValidationMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Ambang stok rendah (unit)") }
            )
            if (thresholdValidationMessage != null) {
                Text(
                    thresholdValidationMessage!!,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(
                onClick = {
                    val parsed = lowStockThresholdText.toLongOrNull()
                    when {
                        parsed == null -> thresholdValidationMessage = "Masukkan angka antara 0 dan 1.000.000."
                        parsed !in 0L..1_000_000L -> thresholdValidationMessage = "Ambang stok harus antara 0 dan 1.000.000."
                        else -> viewModel.saveLowStockThreshold(parsed)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = lowStockThresholdText.toLongOrNull()?.let { it in 0L..1_000_000L } == true
            ) {
                Text("Simpan pengaturan stok")
            }

            Text("Backup dan pemulihan", style = MaterialTheme.typography.titleLarge)
            Text(
                "Cadangan mencakup profil toko, kategori, produk, stok, transaksi, rincian struk, dan ambang stok rendah. File JSON tidak dienkripsi oleh aplikasi; simpan di lokasi yang aman.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = { exportBackupLauncher.launch("berkah-pos-backup.json") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !backupBusy
            ) {
                Text(if (backupBusy) "Memproses..." else "Ekspor backup JSON")
            }
            TextButton(
                onClick = { showRestoreConfirmation = true },
                enabled = !backupBusy
            ) {
                Text("Pulihkan dari file JSON")
            }
            Text(
                "Pemulihan akan mengganti seluruh data POS lokal dengan isi file cadangan. Simpan backup terbaru sebelum melanjutkan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )

            Text("Informasi hukum", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = { legalDocument = LegalDocument.PRIVACY }) {
                Text("Kebijakan Privasi")
            }
            TextButton(onClick = { legalDocument = LegalDocument.TERMS }) {
                Text("Syarat dan Ketentuan")
            }
            Text(
                "Dokumen yang ditampilkan merupakan draf. Informasi kontak pengendali dan URL HTTPS publik masih harus dilengkapi sebelum publikasi.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    when (legalDocument) {
        LegalDocument.PRIVACY -> AlertDialog(
            onDismissRequest = { legalDocument = null },
            title = { Text("Kebijakan Privasi — Draf") },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 440.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        """Status: DRAF — belum layak dipublikasikan.

Pengendali aplikasi
BERKAH TIGA PUTERI.
Alamat korespondensi: belum disediakan.
Email privasi/dukungan: belum disediakan.
URL kebijakan publik HTTPS: belum tersedia.

Data yang disimpan
BERKAH POS menyimpan profil toko, nama pemilik, alamat, nomor telepon, catatan struk, kategori, produk, SKU, harga, stok, transaksi, rincian item, dan preferensi ambang stok rendah pada perangkat melalui SQLite/Room dan Preferences DataStore.

Pengiriman dan akun
Fungsi POS inti tidak membutuhkan akun atau server. Source manifest tidak mendeklarasikan izin INTERNET. Tidak direncanakan Firebase, analytics, crash reporting, iklan pihak ketiga, atau SDK pengumpulan data. Dependency graph dan merged manifest release masih harus diperiksa.

Backup dan berbagi
Backup JSON dipilih dan disimpan pada lokasi yang ditentukan pengguna. File memuat profil, katalog, stok, transaksi, rincian struk, dan ambang stok rendah. File backup tidak dienkripsi oleh aplikasi; pengguna bertanggung jawab atas penyimpanan dan pembagiannya. Restore mengganti data lokal setelah file tervalidasi. Berbagi struk hanya diteruskan ke aplikasi yang dipilih pengguna.

Tautan eksternal
Tautan SaaS yang dikonfigurasi membuka browser; email dukungan membuka aplikasi email pilihan pengguna. Layanan eksternal tersebut tunduk pada kebijakan privasi masing-masing.

Retensi dan penghapusan
Data disimpan sampai diubah/dihapus pengguna, dipulihkan dari backup lain, dihapus melalui pengaturan penyimpanan Android, atau aplikasi dihapus. File backup yang diekspor harus dihapus sendiri oleh pengguna bila tidak diperlukan.

Hak dan kontak
Pertanyaan privasi dapat diajukan kepada pengendali setelah alamat dan kontak resmi dilengkapi. Perubahan kebijakan akan dicantumkan pada halaman publik setelah URL HTTPS disediakan.

Dokumen ini belum merupakan Privacy Policy publik dan tidak boleh dikirim sebagai URL final ke Google Play."""
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { legalDocument = null }) { Text("Tutup") }
            }
        )
        LegalDocument.TERMS -> AlertDialog(
            onDismissRequest = { legalDocument = null },
            title = { Text("Syarat dan Ketentuan — Draf") },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 440.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        """Status: DRAF — perlu pemeriksaan dan persetujuan pengelola.

1. Ruang lingkup
BERKAH POS Basic adalah aplikasi pencatatan kasir lokal yang dapat digunakan tanpa akun atau server.

2. Tanggung jawab pengguna
Pengguna bertanggung jawab atas ketepatan nama produk, harga, stok, pajak, diskon, dan nominal pembayaran yang dimasukkan.

3. Backup dan pemulihan
Backup JSON dapat berisi profil, katalog, stok, transaksi, rincian struk, dan ambang stok rendah. File tidak dienkripsi oleh aplikasi. Pengguna bertanggung jawab menyimpan backup secara aman dan memeriksa file sebelum membagikannya. Restore mengganti data lokal setelah validasi berhasil.

4. Batas penggunaan
Aplikasi tidak menggantikan kewajiban pembukuan, perpajakan, perlindungan konsumen, atau ketentuan usaha yang berlaku.

5. Ketersediaan dan kehilangan data
Pengembang tidak menjamin aplikasi bebas gangguan pada semua perangkat. Pengguna bertanggung jawab membuat dan menguji backup. Data yang tidak dibackup dapat hilang akibat kerusakan, reset, atau penghapusan aplikasi.

6. Layanan SaaS
Informasi SaaS BERKAH TIGA PUTERI bersifat informatif. Dokumen ini tidak mengaktifkan pembayaran atau langganan.

7. Kontak
Alamat korespondensi dan kontak resmi pengembang belum disediakan dan wajib dilengkapi sebelum publikasi."""
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { legalDocument = null }) { Text("Tutup") }
            }
        )
        null -> Unit
    }

    if (showRestoreConfirmation) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmation = false },
            title = { Text("Ganti data POS lokal?") },
            text = {
                Text(
                    "Data profil toko, produk, stok, kategori, transaksi, rincian struk, dan ambang stok rendah saat ini akan diganti oleh isi file backup. Operasi hanya diterapkan setelah file lolos validasi."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreConfirmation = false
                        importBackupLauncher.launch(arrayOf("application/json", "text/json"))
                    }
                ) { Text("Pilih file backup") }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmation = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
