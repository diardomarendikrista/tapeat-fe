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
 * Card untuk satu item produk di daftar Admin.
 * Menampilkan gambar, nama, kategori, harga, stok,
 * serta tombol Edit dan Hapus.
 *
 * @param product Data produk yang akan ditampilkan.
 * @param onEdit Callback saat tombol edit ditekan.
 * @param onDelete Callback saat tombol hapus ditekan.
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
            // Gambar produk
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!product.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = "${TapeatContainer.BASE_URL}${product.imageUrl}",
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info produk
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
                Text(
                    text = "Rp ${"%,.0f".format(product.price)}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
                Text(
                    text = "Stok: ${product.stock} pcs",
                    fontSize = 12.sp,
                    color = if (product.stock == 0) MaterialTheme.colorScheme.error else Color.Gray
                )
            }

            // Tombol Edit & Hapus
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
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

@Preview(showBackground = true)
@Composable
fun ProductAdminCardPreview() {
    TapeatTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Preview produk dengan gambar placeholder
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
            // Preview produk stok habis
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