package com.group2.tapeat.ui.view

import coil.compose.AsyncImage
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
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
import coil.request.ImageRequest
import com.group2.tapeat.data.container.TapeatContainer
import com.group2.tapeat.data.dto.ProductResponse
import com.group2.tapeat.ui.model.CustomerModel
import com.group2.tapeat.ui.theme.TapeatTheme
import java.text.NumberFormat
import java.util.Locale

// =================
// Helpers
// =================
fun formatRupiah(price: Double): String {
    val localeID = Locale.forLanguageTag("id-ID")
    return NumberFormat.getCurrencyInstance(localeID).apply {
        maximumFractionDigits = 0
    }.format(price)
}

// ==========================================
// TAMPILAN UTAMA (CUSTOMER VIEW)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerView(
    model: CustomerModel = viewModel()
) {
    // Ambil data dari model menggunakan collectAsState
    val products by model.products.collectAsState()
    val isLoading by model.isLoading.collectAsState()

    // Ambil state dari model (menggunakan .value agar sinkron)
    var searchQuery by model.searchQuery
    var selectedCategory by model.selectedCategory
    var isOrderSuccess by model.isOrderSuccess

    // State lokal UI
    var isCheckoutOpen by remember { mutableStateOf(false) }

    // Ambil daftar kategori unik dari data produk asli
    val dynamicCategories = remember(products) {
        listOf("Semua") + products.mapNotNull { it.category }.distinct()
    }

    // LOGIKA FILTER GABUNGAN (Category + Search)
    val filteredProducts = products.filter { product ->
        val matchesCategory = selectedCategory == "Semua" || product.category == selectedCategory
        val matchesSearch = product.name.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    // LOGIKA AUTO-CLOSE
    // Jika drawer terbuka tapi keranjang kosong, tutup lacinya otomatis
    LaunchedEffect(model.cart.size) {
        if (model.cart.isEmpty() && isCheckoutOpen) {
            isCheckoutOpen = false
        }
    }

    // Tampilkan layar sukses jika state isOrderSuccess true
    if (isOrderSuccess) {
        OrderSuccessScreen(
            orderId = model.lastOrderId.value,
            onDismiss = {
                model.resetSuccessState()
            }
        )
    } else
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("TapEat Kiosk", fontWeight = FontWeight.Bold)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color.Black
                    )
                )
            },
            containerColor = Color(0xFFF3F4F6)
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // --- HEADER: Search Bar & Kategori ---
                    HeaderSection(
                        categories = dynamicCategories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it }
                    )

                    // Tampilkan loading bar jika sedang fetch
                    if (isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    // --- KONTEN: Grid Produk ---
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2), // 2 Kolom
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 100.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f) // Mengisi sisa ruang kosong
                    ) {
                        items(filteredProducts) { product ->
                            ProductCard(
                                product = product,
                                onAddToCart = {
                                    model.addToCart(product)
                                }
                            )
                        }
                    }

                    // --- FLOATING CART (Muncul jika keranjang tidak kosong) ---
                    // https://developer.android.com/reference/kotlin/androidx/compose/animation/AnimatedVisibility.composable?hl=en
                    AnimatedVisibility(
                        visible = model.cart.isNotEmpty(),
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it }),
                    ) {
                        FloatingCartBar(
                            cart = model.cart,
                            onCheckoutClick = { isCheckoutOpen = true }
                        )
                    }
                }

                // --- CHECKOUT BOTTOM SHEET ---
                if (isCheckoutOpen) {
                    CheckoutBottomSheet(
                        cart = model.cart,
                        onDismiss = { isCheckoutOpen = false },
                        // Sekarang tipenya sudah sinkron (menerima 2 string)
                        onPlaceOrder = { orderType, detail ->
                            model.checkout(
                                orderType = orderType,
                                tableNumber = if (orderType == "DINE_IN") detail else null,
                                customerName = if (orderType == "TAKEAWAY") detail else null
                            )
                            isCheckoutOpen = false
                        }
                    )
                }
            }
        }
}

// ==========================================
// COMPONENTS
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderSection(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text("Cari menu favorit...")
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear"
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                unfocusedContainerColor = Color(0xFFF9FAFB)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Chips Kategori
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            categories.forEach { category ->
                // https://developer.android.com/develop/ui/compose/components/chip?hl=id
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        onCategorySelected(category)
                    },
                    label = {
                        Text(category)
                    },
                    shape = RoundedCornerShape(percent = 50),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun ProductCard(product: ProductResponse, onAddToCart: () -> Unit) {
    val imageUrl = TapeatContainer.BASE_URL + product.imageUrl

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Container Gambar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFFE5E7EB)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop, // Scale agar gambar memenuhi box tanpa gepeng (crop center)
                        error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_report_image), // Tampilkan icon loading/error jika gambar gagal dimuat
                        placeholder = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery)
                    )
                } else {
                    // Jika URL null, pakai Icon Dummy
                    Icon(
                        Icons.Default.Image, // Ikon galeri bawaan Compose
                        contentDescription = "No Image",
                        tint = Color.Gray,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            // Detail Produk
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    lineHeight = 16.sp, // Menjaga tinggi agar sejajar meski teksnya panjang
                    modifier = Modifier.height(32.dp) // Menjaga tinggi agar sejajar meski teksnya panjang
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatRupiah(product.price),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onAddToCart,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Tambah", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FloatingCartBar(cart: List<ProductResponse>, onCheckoutClick: () -> Unit) {
    val totalHarga = cart.sumOf { it.price }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Ikon Keranjang + Badge Jumlah
                Box {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "Cart",
                        tint = Color.White
                    )
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-8).dp)
                    ) {
                        Text("${cart.size}", color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Total Harga", color = Color.LightGray, fontSize = 10.sp)
                    Text(
                        formatRupiah(totalHarga),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            Button(
                onClick = onCheckoutClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Checkout", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Popup Drawer Cart
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutBottomSheet(
    cart: androidx.compose.runtime.snapshots.SnapshotStateList<ProductResponse>,
    onDismiss: () -> Unit,
    onPlaceOrder: (String, String) -> Unit
) {
    // Mengelompokkan item yang sama untuk menghitung quantity
    val groupedCart = cart.groupBy { it }.map { it.key to it.value.size }
    val totalHarga = cart.sumOf { it.price }

    var orderType by remember { mutableStateOf("DINE_IN") }
    var customerDetail by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                "Konfirmasi Pesanan",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Toggle Tipe Pesanan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { orderType = "DINE_IN" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor =
                            if (orderType == "DINE_IN") MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent
                    )
                ) {
                    Text("Makan Sini")
                }

                OutlinedButton(
                    onClick = { orderType = "TAKEAWAY" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (orderType == "TAKEAWAY") MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    )
                ) {
                    Text("Bawa Pulang")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Input Detail (Nomor Meja / Nama)
            OutlinedTextField(
                value = customerDetail,
                onValueChange = { customerDetail = it },
                label = {
                    Text(
                        if (orderType == "DINE_IN") "Nomor Meja"
                        else "Nama Pelanggan"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray)
            Spacer(modifier = Modifier.height(8.dp))

            // Ringkasan Pesanan
            Text("Ringkasan", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // Kolom list pesanan dengan batasan tinggi agar bisa discroll jika terlalu banyak
            Column(
                modifier = Modifier
                    .heightIn(max = 200.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                groupedCart.forEach { (product, qty) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Bagian Kiri: Info Produk & Qty
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${qty}x ${product.name}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                formatRupiah(product.price * qty),
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        // Bagian Kanan: Tombol Hapus per Jenis
                        IconButton(
                            onClick = {
                                // Menghapus semua item yang sama (berdasarkan objek produk)
                                cart.removeAll { it == product }
                            },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Red)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus Semua ${product.name}",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))

            // Total dan Tombol Pesan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Total Bayar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Text(
                    formatRupiah(totalHarga),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onPlaceOrder(orderType, customerDetail) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = customerDetail.isNotBlank(), // Disable tombol jika input kosong
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Buat Pesanan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp)) // Jarak ekstra untuk area bawah HP
        }
    }
}

@Composable
fun OrderSuccessScreen(
    orderId: String,
    onDismiss: () -> Unit
) {
    // Layar penuh putih untuk menutupi menu
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon Centang Hijau Besar
            Surface(
                shape = RoundedCornerShape(100),
                color = Color(0xFFDCFCE7), // Green 100
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF16A34A), // Green 600
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Pesanan Terkirim!",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Silakan tunjukkan ID pesanan ini ke Kasir untuk melakukan pembayaran.",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = Color.Gray,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Box ID Pesanan
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ID PESANAN ANDA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = orderId,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Kembali ke Menu", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CustomerPreview() {
    TapeatTheme {
        CustomerView()
    }
}
