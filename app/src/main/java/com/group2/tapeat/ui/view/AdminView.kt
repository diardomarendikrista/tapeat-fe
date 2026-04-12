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
 * Komponen yang dipakai:
 * - [ProductAdminCard] → card satu item produk
 * - [ProductFormSheet] → bottom sheet form tambah/edit
 */
@Composable
fun AdminView() {
    val context = LocalContext.current
    val viewModel: AdminViewModel = viewModel(
        factory = AdminViewModel.Factory(TapeatContainer().productRepository)
    )

    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val showFormSheet by viewModel.showFormSheet.collectAsState()
    val editingProduct by viewModel.editingProduct.collectAsState()
    val productToDelete by viewModel.productToDelete.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Tampilkan snackbar saat ada pesan sukses atau error
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            // Header
            Text(
                text = "Manajemen Menu",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            when {
                // Loading pertama kali (list masih kosong)
                isLoading && products.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                // Tidak ada produk sama sekali
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
                // Tampilkan daftar produk
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(products, key = { it.id }) { product ->
                            ProductAdminCard(
                                product = product,
                                onEdit = { viewModel.openEditForm(product) },
                                onDelete = { viewModel.confirmDelete(product) }
                            )
                        }
                        // Spacer bawah agar FAB tidak menutupi item terakhir
                        item { Spacer(modifier = Modifier.height(72.dp)) }
                    }
                }
            }
        }
    }

    // Bottom Sheet Form Tambah / Edit
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

    // Dialog Konfirmasi Hapus
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