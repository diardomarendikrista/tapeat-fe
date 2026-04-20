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

@Composable
fun KitchenView(
    viewModel: KitchenViewModel = viewModel()
) {

    val activeOrders by viewModel.activeOrders.collectAsState()
    val doneOrders by viewModel.doneOrders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableStateOf("active") }

    // aplikasi menarik data terbaru dari server setiap kali tab Kitchen dibuka (agar update ketika kasir mengkonfirmasi pesanan)
    LaunchedEffect(Unit) {
        viewModel.fetchAll()
    }

    // FILTER DATA
    val orders = if (selectedTab == "active") {
        activeOrders
    } else {
        doneOrders.filter { it.status == "DELIVERED" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Kitchen Display", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(10.dp))

        // TAB MODERN
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE5E7EB), shape = RoundedCornerShape(50))
                .padding(4.dp)
        ) {

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
                    text = "Masak (${activeOrders.size})",
                    color = Color.Black
                )
            }

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

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        LazyColumn {
            items(orders, key = { it.id }) { order ->
                KitchenOrderCard(
                    order = order,
                    isActive = selectedTab == "active",
                    onDone = {
                        viewModel.updateStatus(order.id, "DELIVERED")
                    },
                    onCancel = {
                        viewModel.updateStatus(order.id, "CANCELLED")
                    }
                )
            }
        }
    }
}

@Composable
fun KitchenOrderCard(
    order: OrderResponse,
    isActive: Boolean,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {

    val backgroundColor = if (order.status == "DELIVERED") {
        Color(0xFFD1FAE5)
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEFF2F7))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("ORD-${order.id}")
                    Text(order.orderType)
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {

                order.items.forEach {
                    Row {
                        Text("${it.quantity}x ")
                        Text(it.product.name)
                    }
                }

                if (!isActive) {
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "SELESAI",
                        color = Color(0xFF16A34A)
                    )
                }

                if (isActive) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Row {
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

@Preview(showBackground = true)
@Composable
fun KitchenPreview() {
    TapeatTheme {
        KitchenView()
    }
}