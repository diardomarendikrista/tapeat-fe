package com.group2.tapeat.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.group2.tapeat.data.container.TapeatContainer
import com.group2.tapeat.data.dto.ProductResponse
import com.group2.tapeat.ui.theme.TapeatTheme
import com.group2.tapeat.ui.viewmodel.AdminViewModel

/**
 * Layar utama Admin untuk manajemen produk (CRUD).
 *
 * Layar ini menampilkan daftar semua produk (termasuk yang stoknya habis)
 * dan menyediakan aksi untuk menambah, mengedit, serta menghapus produk.
 *
 * Komponen pendukung:
 * - [ProductAdminCard]  → menampilkan satu item produk dalam bentuk card
 * - [ProductFormView]   → bottom sheet form untuk tambah/edit produk
 * - [AdminViewModel]    → menyimpan state dan mengelola logika bisnis
 */
@Composable
fun AdminView() {
    // Context dibutuhkan oleh ViewModel untuk membaca file gambar dari URI galeri
    val context = LocalContext.current

    // Instansiasi ViewModel menggunakan Factory agar bisa menerima ProductRepository
    val viewModel: AdminViewModel = viewModel(
        factory = AdminViewModel.Factory(TapeatContainer().productRepository)
    )

    // Mengamati semua state dari ViewModel
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val showFormSheet by viewModel.showFormSheet.collectAsState()
    val editingProduct by viewModel.editingProduct.collectAsState()
    val productToDelete by viewModel.productToDelete.collectAsState()

    // State untuk menampilkan snackbar notifikasi di bagian bawah layar
    val snackbarHostState = remember { SnackbarHostState() }

    // Tampilkan snackbar saat ada pesan sukses (misal: "Produk berhasil ditambahkan")
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    // Tampilkan snackbar saat ada pesan error (misal: "Gagal memuat produk")
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    // Menarik data terbaru dari server setiap kali tab Admin dibuka.
    // Ini memastikan stok yang ditampilkan selalu sinkron,
    // misalnya setelah Kasir atau Kiosk melakukan transaksi.
    LaunchedEffect(Unit) {
        viewModel.loadProducts()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // FAB di pojok kanan bawah untuk membuka form tambah produk baru
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddForm() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Produk")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Judul halaman
            Text(
                text = "Manajemen Menu",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            when {
                // State 1: Sedang loading dan belum ada data → tampilkan spinner
                isLoading && products.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                // State 2: Tidak ada produk sama sekali → tampilkan pesan kosong
                products.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Belum ada produk", color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap + untuk menambah menu baru",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // State 3: Ada data → tampilkan daftar produk
                else -> {
                    // LazyColumn hanya me-render item yang terlihat di layar (efisien untuk list panjang)
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // key = { it.id } membantu Compose mengidentifikasi item secara unik
                        // sehingga animasi recompose lebih efisien
                        items(products, key = { it.id }) { product ->
                            ProductAdminCard(
                                product = product,
                                onEdit = { viewModel.openEditForm(product) },
                                onDelete = { viewModel.confirmDelete(product) }
                            )
                        }
                        // Spacer tambahan agar item terakhir tidak tertutup FAB
                        item { Spacer(modifier = Modifier.height(72.dp)) }
                    }
                }
            }
        }
    }

    // Tampilkan bottom sheet form jika showFormSheet = true
    // editingProduct menentukan mode: null = Tambah, tidak null = Edit
    if (showFormSheet) {
        ProductFormView(
            editingProduct = editingProduct,
            isLoading = isLoading,
            onDismiss = { viewModel.closeForm() },
            onSave = { name, price, stock, category, imageUri ->
                viewModel.saveProduct(context, name, price, stock, category, imageUri)
            }
        )
    }

    // Dialog konfirmasi sebelum menghapus produk
    // Hanya muncul jika productToDelete tidak null
    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Hapus Menu") },
            text = {
                Text("Yakin ingin menghapus \"${product.name}\"? Menu ini akan disembunyikan dari katalog.")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteProduct() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text("Batal")
                }
            }
        )
    }
}

/**
 * Preview statis untuk AdminView.
 * Menggunakan data dummy agar bisa ditampilkan di panel preview Android Studio
 * tanpa memerlukan ViewModel atau koneksi jaringan.
 */
@Preview(showBackground = true)
@Composable
fun AdminViewPreview() {
    TapeatTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Manajemen Menu",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            // Produk normal dengan stok tersedia
            ProductAdminCard(
                product = ProductResponse(
                    id = 1, name = "Nasi Goreng", price = 25000.0,
                    stock = 77, imageUrl = null, category = "makanan", isActive = true
                ),
                onEdit = {}, onDelete = {}
            )
            ProductAdminCard(
                product = ProductResponse(
                    id = 2, name = "Es Teh", price = 8000.0,
                    stock = 95, imageUrl = null, category = "minuman", isActive = true
                ),
                onEdit = {}, onDelete = {}
            )
            // Produk dengan stok habis - teks stok akan berwarna merah
            ProductAdminCard(
                product = ProductResponse(
                    id = 3, name = "Steak Wellington", price = 95000.0,
                    stock = 0, imageUrl = null, category = "makanan", isActive = true
                ),
                onEdit = {}, onDelete = {}
            )
        }
    }
}