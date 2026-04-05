package com.group2.tapeat.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.group2.tapeat.data.dto.OrderResponse
import com.group2.tapeat.ui.model.CashierModel
import com.group2.tapeat.ui.theme.TapeatTheme
import com.group2.tapeat.util.formatRupiah

// ==========================================
// TAMPILAN UTAMA (CASHIER VIEW)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashierView(
    model: CashierModel = viewModel()
) {
    // Observasi state dari Model
    val unpaidOrders by model.unpaidOrders.collectAsState()
    val isLoading by model.isLoading.collectAsState()

    // memastikan data antrean selalu up-to-date (tidak stuck di data lama).
    LaunchedEffect(Unit) {
        model.fetchUnpaidOrders()
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Kasir (Pembayaran)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937) // Gray 800
                )
                Text(
                    text = "ANTREAN BELUM BAYAR",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
            }
        },
        containerColor = Color(0xFFF9FAFB) // Gray 50
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tampilkan loading bar jika sedang fetch
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (unpaidOrders.isEmpty() && !isLoading) {
                // Tampilan saat tidak ada pesanan
                EmptyCashierState()
            } else {
                // List Antrean Pesanan
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(unpaidOrders, key = { it.id }) { order ->
                        CashierOrderCard(
                            order = order,
                            // Cegah double-click dengan memeriksa status isLoading
                            onConfirm = {
                                if (!isLoading) model.confirmPayment(order.id)
                            },
                            onCancel = {
                                if (!isLoading) model.cancelOrder(order.id)
                            },
                            isLoading = isLoading
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENTS
// ==========================================
@Composable
fun CashierOrderCard(
    order: OrderResponse,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean
) {
    val orderTypeLabel = if (order.orderType == "DINE_IN") {
        "DINE-IN • Meja ${order.tableNumber ?: "-"}"
    } else {
        "TAKEAWAY • ${order.customerName ?: "-"}"
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Header Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF7ED)) // Orange 50
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ORD-${order.id}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    fontSize = 14.sp
                )

                Surface(
                    color = Color(0xFFFED7AA),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = orderTypeLabel,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9A3412),
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Body Card
            Column(modifier = Modifier.padding(16.dp)) {
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.quantity}x ${item.product.name}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF374151)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color(0xFFE5E7EB),
                    thickness = 1.dp
                )

                // Footer Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL TAGIHAN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = formatRupiah(order.totalAmount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tombol Aksi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        enabled = !isLoading, // Disable saat loading
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFEE2E2)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Batal",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        enabled = !isLoading, // Disable saat loading
                        modifier = Modifier.weight(2f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Confirm",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Konfirmasi & Bayar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCashierState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
            contentDescription = "Empty",
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFD1D5DB)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Belum ada pesanan masuk.",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CashierPreview() {
    TapeatTheme {
        CashierView()
    }
}
