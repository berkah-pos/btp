package id.berkahtigaputeri.basicpos.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    var legalDocument by remember { mutableStateOf<LegalDocument?>(null) }

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
                Text(validationMessage!!, color = MaterialTheme.colorScheme.error)
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
                    CircularProgressIndicator()
                } else {
                    Text("Simpan profil toko")
                }
            }

            if (message != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
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
            Text(
                "Kontak dan tautan resmi perusahaan belum dikonfigurasi.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text("Backup dan pemulihan", style = MaterialTheme.typography.titleLarge)
            Text(
                "Cadangan mencakup profil toko, kategori, produk, stok, transaksi, dan rincian struk. File disimpan di lokasi yang Anda pilih.",
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
                Text(
                    "BERKAH POS Basic menyimpan profil usaha, produk, kategori, stok, transaksi, dan rincian struk di database SQLite pada perangkat. Fungsi utama tidak memerlukan akun atau server. Backup manual, bila digunakan, akan membuat file yang dipilih pengguna dan dapat dibagikan oleh pengguna. Tidak direncanakan analytics, iklan pihak ketiga, Firebase, atau SDK pengumpulan data.\n\nPengendali: BERKAH TIGA PUTERI. Alamat, kontak privasi, dan URL kebijakan publik belum disediakan. Dokumen ini belum layak menjadi Privacy Policy publik."
                )
            },
            confirmButton = {
                TextButton(onClick = { legalDocument = null }) { Text("Tutup") }
            }
        )
        LegalDocument.TERMS -> AlertDialog(
            onDismissRequest = { legalDocument = null },
            title = { Text("Syarat dan Ketentuan — Draf") },
            text = {
                Text(
                    "BERKAH POS Basic adalah aplikasi kasir lokal. Pengguna bertanggung jawab atas kebenaran harga, stok, pajak, diskon, dan pembayaran yang dimasukkan serta pengamanan file backup. Aplikasi tidak menggantikan kewajiban pembukuan atau perpajakan. Kontak pengembang resmi belum tersedia."
                )
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
                    "Data profil toko, produk, stok, kategori, dan transaksi saat ini akan diganti oleh isi file backup. Operasi hanya diterapkan setelah file lolos validasi."
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
