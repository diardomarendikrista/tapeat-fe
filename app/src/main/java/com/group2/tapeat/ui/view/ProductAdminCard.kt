package com.group2.tapeat.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.group2.tapeat.data.container.TapeatContainer
import com.group2.tapeat.data.dto.ProductResponse
import com.group2.tapeat.ui.theme.TapeatTheme

/**
 * Komponen card untuk menampilkan satu item produk di layar Admin.
 *
 * Menampilkan informasi lengkap produk meliputi gambar, nama, badge kategori,
 * harga, dan stok. Stok yang bernilai 0 akan ditampilkan dengan warna merah
 * sebagai indikator bahwa produk perlu di-restock.
 *
 * @param product Data produk yang akan ditampilkan.
 * @param onEdit  Callback yang dipanggil saat tombol edit (pensil) ditekan.
 * @param onDelete Callback yang dipanggil saat tombol hapus (tong sampah) ditekan.
 */
@Composable
fun ProductAdminCard(
    product: ProductResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- Gambar Produk ---
            // Menampilkan gambar dari URL server menggunakan Coil (AsyncImage).
            // Jika imageUrl null atau kosong, tampilkan icon placeholder.
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!product.imageUrl.isNullOrBlank()) {
                    // URL lengkap = BASE_URL + path relatif dari database
                    // Contoh: http://192.168.1.8:4095/uploads/uuid-burger.jpg
                    AsyncImage(
                        model = "${TapeatContainer.BASE_URL}${product.imageUrl}",
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Placeholder jika produk belum memiliki gambar
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // --- Info Produk ---
            Column(modifier = Modifier.weight(1f)) {
                // Nama produk — dipotong dengan ellipsis jika terlalu panjang
                Text(
                    text = product.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Badge kategori — hanya ditampilkan jika kategori tidak kosong
                if (!product.category.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = product.category,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Harga produk dalam format Rupiah
                Text(
                    text = "Rp ${"%,.0f".format(product.price)}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )

                // Stok produk — merah jika habis (0), abu-abu jika masih tersedia
                Text(
                    text = "Stok: ${product.stock} pcs",
                    fontSize = 12.sp,
                    color = if (product.stock == 0) MaterialTheme.colorScheme.error else Color.Gray
                )
            }

            // --- Tombol Aksi ---
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Tombol Edit → membuka form edit dengan data produk ini
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                // Tombol Hapus → memunculkan dialog konfirmasi soft delete
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Preview statis untuk [ProductAdminCard].
 * Menampilkan dua variasi: produk dengan stok normal dan produk dengan stok habis.
 */
@Preview(showBackground = true)
@Composable
fun ProductAdminCardPreview() {
    TapeatTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Produk dengan stok normal
            ProductAdminCard(
                product = ProductResponse(
                    id = 1,
                    name = "Classic Cheeseburger",
                    price = 45000.0,
                    stock = 12,
                    imageUrl = null,
                    category = "Meals",
                    isActive = true
                ),
                onEdit = {},
                onDelete = {}
            )
            // Produk dengan stok habis - teks stok akan berwarna merah
            ProductAdminCard(
                product = ProductResponse(
                    id = 2,
                    name = "Spicy Chicken Wings",
                    price = 35000.0,
                    stock = 0,
                    imageUrl = null,
                    category = "Meals",
                    isActive = true
                ),
                onEdit = {},
                onDelete = {}
            )
        }
    }
}