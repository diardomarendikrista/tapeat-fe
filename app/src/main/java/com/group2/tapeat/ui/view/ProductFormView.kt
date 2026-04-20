package com.group2.tapeat.ui.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.group2.tapeat.data.container.TapeatContainer
import com.group2.tapeat.data.dto.ProductResponse
import com.group2.tapeat.ui.theme.TapeatTheme

/**
 * Komponen bottom sheet berisi form untuk menambah atau mengedit produk.
 *
 * Mode form ditentukan secara otomatis dari parameter [editingProduct]:
 * - null       → mode **Tambah**: seluruh field kosong, judul "Tambah Menu Baru"
 * - tidak null → mode **Edit**: field terisi data produk, judul "Edit Menu"
 *
 * Fitur utama:
 * - Image picker untuk memilih gambar dari galeri perangkat
 * - Pre-fill data otomatis saat mode edit
 * - Validasi input dilakukan di [AdminViewModel] sebelum request dikirim
 * - Loading indicator pada tombol Simpan saat proses berlangsung
 *
 * @param editingProduct Produk yang sedang diedit, atau null jika mode tambah.
 * @param isLoading      True saat proses simpan sedang berjalan (menonaktifkan tombol).
 * @param onDismiss      Callback saat sheet ditutup atau tombol Batal ditekan.
 * @param onSave         Callback saat tombol Simpan ditekan, membawa data form.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormView(
    editingProduct: ProductResponse?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, price: String, stock: String, category: String, imageUri: Uri?) -> Unit
) {
    // true jika sedang dalam mode edit, false jika mode tambah
    val isEditMode = editingProduct != null

    // State tiap field form.
    // remember(editingProduct) memastikan nilai di-reset dan di-pre-fill
    // setiap kali produk yang diedit berubah.
    var name     by remember(editingProduct) { mutableStateOf(editingProduct?.name ?: "") }
    var price    by remember(editingProduct) { mutableStateOf(editingProduct?.price?.toInt()?.toString() ?: "") }
    var stock    by remember(editingProduct) { mutableStateOf(editingProduct?.stock?.toString() ?: "") }
    var category by remember(editingProduct) { mutableStateOf(editingProduct?.category ?: "") }

    // URI gambar yang baru dipilih dari galeri. null = belum pilih gambar baru.
    var selectedImageUri by remember(editingProduct) { mutableStateOf<Uri?>(null) }

    // Launcher untuk membuka galeri perangkat dan mendapatkan URI gambar yang dipilih
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        // skipPartiallyExpanded = true agar sheet langsung terbuka penuh, tidak setengah layar
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Judul form — berubah sesuai mode
            Text(
                text = if (isEditMode) "Edit Menu" else "Tambah Menu Baru",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // --- Area Upload Gambar ---
            // Seluruh Box bisa di-tap untuk membuka galeri.
            // Menampilkan salah satu dari 3 kondisi:
            // 1. Gambar baru yang dipilih (selectedImageUri tidak null)
            // 2. Gambar lama dari server (mode edit, belum ganti gambar)
            // 3. Placeholder kosong (mode tambah atau belum ada gambar)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                when {
                    // Kondisi 1: Gambar baru yang baru dipilih dari galeri
                    selectedImageUri != null -> {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Gambar baru",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }

                    // Kondisi 2: Mode edit dan produk sudah punya gambar di server
                    // Tampilkan gambar lama dengan overlay hint "tap untuk ganti"
                    isEditMode && !editingProduct?.imageUrl.isNullOrBlank() -> {
                        AsyncImage(
                            model = "${TapeatContainer.BASE_URL}${editingProduct?.imageUrl}",
                            contentDescription = "Gambar produk",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                        )
                        // Overlay gelap sebagai petunjuk bahwa gambar bisa diganti
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                                .clip(RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tap untuk ganti gambar",
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Kondisi 3: Belum ada gambar — tampilkan placeholder icon
                    else -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Upload Gambar", color = Color.Gray, fontSize = 13.sp)
                            Text("(Opsional)", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }
            }

            // --- Field Nama Produk ---
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Produk") },
                placeholder = { Text("Contoh: Nasi Goreng Special") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // --- Field Kategori (opsional) ---
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Kategori (Opsional)") },
                placeholder = { Text("Contoh: Makanan, Minuman, Snack") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // --- Field Harga & Stok (sejajar dalam satu baris) ---
            // Menggunakan KeyboardType.Number agar keyboard angka muncul otomatis
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Harga (Rp)") },
                    placeholder = { Text("0") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stok Awal") },
                    placeholder = { Text("0") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- Tombol Batal & Simpan ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tombol Batal → menutup sheet tanpa menyimpan
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Text("Batal")
                }

                // Tombol Simpan → mengirim data form ke ViewModel
                // Saat isLoading = true, tombol nonaktif dan menampilkan spinner
                Button(
                    onClick = { onSave(name, price, stock, category, selectedImageUri) },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        // Spinner kecil sebagai indikator proses upload sedang berjalan
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (isEditMode) "Simpan Perubahan" else "Simpan Menu")
                    }
                }
            }
        }
    }
}

/**
 * Preview statis untuk [ProductFormView] dalam mode Tambah (form kosong).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ProductFormViewPreview() {
    TapeatTheme {
        ProductFormView(
            editingProduct = null,
            isLoading = false,
            onDismiss = {},
            onSave = { _, _, _, _, _ -> }
        )
    }
}