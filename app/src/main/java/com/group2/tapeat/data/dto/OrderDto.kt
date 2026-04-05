package com.group2.tapeat.data.dto

// Mewakili payload untuk POST /api/orders
data class OrderRequest(
    val orderType: String, // Bisa "DINE_IN" atau "TAKEAWAY"
    val tableNumber: String?, // Hanya ada jika orderType == "DINE_IN"
    val customerName: String?, // Hanya ada jika orderType == "TAKEAWAY"
    val items: List<OrderItemRequest>
)

data class OrderItemRequest(
    val productId: Int,
    val quantity: Int
)

// Mewakili response standar dari GET pesanan
data class OrderResponse(
    val id: Int,
    val orderType: String,
    val tableNumber: String?,
    val customerName: String?,
    val status: String,
    val items: List<OrderItemResponse>
)

data class OrderItemResponse(
    val id: Int,
    val product: ProductResponse, // Menggunakan ProductResponse yang sudah ada
    val quantity: Int,
    val priceAtPurchase: Double,
    val subtotal: Double
)
