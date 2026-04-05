package com.group2.tapeat.data.repository

import com.group2.tapeat.data.dto.OrderRequest
import com.group2.tapeat.data.service.ApiService

class TapeatRepository(private val apiService: ApiService) {

    // Memanggil API getProducts
    suspend fun getProducts(availableOnly: Boolean = false, category: String? = null) =
        apiService.getProducts(availableOnly, category)

    // Memanggil API createOrder
    suspend fun createOrder(request: OrderRequest) =
        apiService.createOrder(request)

    // Memanggil API getUnpaidOrders (Untuk Kasir)
    suspend fun getUnpaidOrders() =
        apiService.getUnpaidOrders()

    // Memanggil API updateOrderStatus (Untuk Dapur & Kasir)
    suspend fun updateOrderStatus(orderId: Int, status: String) =
        apiService.updateOrderStatus(orderId, status)
}
