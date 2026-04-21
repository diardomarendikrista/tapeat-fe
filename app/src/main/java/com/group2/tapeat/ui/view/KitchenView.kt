package com.group2.tapeat.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.group2.tapeat.data.dto.OrderResponse
import com.group2.tapeat.ui.theme.TapeatTheme
import com.group2.tapeat.ui.viewmodel.KitchenViewModel

/**
 * Tampilan utama Kitchen (Dapur)
 * Menampilkan:
 * - Tab Masak (antrean aktif)
 * - Tab Riwayat (hanya yang selesai / DELIVERED)
 */
@Composable
fun KitchenView(
    viewModel: KitchenViewModel = viewModel()
) {

    // =========================
    // STATE DARI VIEWMODEL
    // =========================
    val activeOrders by viewModel.activeOrders.collectAsState() // antrean masak
    val doneOrders by viewModel.doneOrders.collectAsState()     // antrean selesai
    val isLoading by viewModel.isLoading.collectAsState()       // loading indikator

    // State untuk menentukan tab aktif
    var selectedTab by remember { mutableStateOf("active") }

    /**
     * LaunchedEffect akan dijalankan sekali saat composable pertama kali muncul.
     * Digunakan untuk fetch data dari server.
     */
    LaunchedEffect(Unit) {
        viewModel.fetchAll()
    }

    /**
     * Filtering data:
     * - Tab "active" → tampilkan semua antrean masak
     * - Tab "done" → hanya tampilkan yang status DELIVERED (selesai)
     */
    val orders = if (selectedTab == "active") {
        activeOrders
    } else {
        doneOrders.filter { it.status == "DELIVERED" }
    }

    // =========================
    // UI UTAMA
    // =========================
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Judul halaman
        Text("Kitchen Display", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(10.dp))

        // =========================
        // TAB NAVIGATION (PILL STYLE)
        // =========================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE5E7EB), shape = RoundedCornerShape(50))
                .padding(4.dp)
        ) {

            // TAB MASAK
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (selectedTab == "active") Color.White else Color.Transparent,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(vertical = 10.dp)
                    .clickable { selectedTab = "active" },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Masak (${activeOrders.size})", // jumlah antrean aktif
                    color = Color.Black
                )
            }

            // TAB RIWAYAT (HANYA SELESAI)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (selectedTab == "done") Color.White else Color.Transparent,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(vertical = 10.dp)
                    .clickable { selectedTab = "done" },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Riwayat (${doneOrders.count { it.status == "DELIVERED" }})",
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // =========================
        // LOADING INDICATOR
        // =========================
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // =========================
        // LIST ANTREAN
        // =========================
        LazyColumn {
            items(orders, key = { it.id }) { order ->
                KitchenOrderCard(
                    order = order,
                    isActive = selectedTab == "active",

                    // Aksi jika pesanan selesai
                    onDone = {
                        viewModel.updateStatus(order.id, "DELIVERED")
                    },

                    // Aksi jika pesanan dibatalkan
                    onCancel = {
                        viewModel.updateStatus(order.id, "CANCELLED")
                    }
                )
            }
        }
    }
}

/**
 * Card untuk menampilkan 1 pesanan
 */
@Composable
fun KitchenOrderCard(
    order: OrderResponse,
    isActive: Boolean,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {

    // Warna background berdasarkan status
    val backgroundColor = if (order.status == "DELIVERED") {
        Color(0xFFD1FAE5) // hijau muda (selesai)
    } else {
        Color.White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column {

            // =========================
            // HEADER ORDER
            // =========================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEFF2F7))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("ORD-${order.id}")     // ID pesanan
                    Text(order.orderType)      // tipe pesanan (DINE_IN / TAKEAWAY)
                }
            }

            // =========================
            // DETAIL ITEM PESANAN
            // =========================
            Column(modifier = Modifier.padding(10.dp)) {

                order.items.forEach {
                    Row {
                        Text("${it.quantity}x ")
                        Text(it.product.name)
                    }
                }

                // =========================
                // STATUS (HANYA DI RIWAYAT)
                // =========================
                if (!isActive) {
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "SELESAI",
                        color = Color(0xFF16A34A)
                    )
                }

                // =========================
                // ACTION BUTTON (TAB MASAK)
                // =========================
                if (isActive) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Row {
                        // Tombol batal
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Red
                            )
                        ) {
                            Text("Batal")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Tombol selesai
                        Button(
                            onClick = onDone,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF16A34A)
                            )
                        ) {
                            Text("Selesai")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Preview UI (untuk Android Studio Preview)
 */
@Preview(showBackground = true)
@Composable
fun KitchenPreview() {
    TapeatTheme {
        KitchenView()
    }
}