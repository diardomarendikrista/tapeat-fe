package com.group2.tapeat.data.repository

import com.group2.tapeat.data.dto.OrderRequest
import com.group2.tapeat.data.service.OrderApiService

/**
 * Jembatan penghubung antara UI (ViewModel) dan API Order.
 * Mengelola siklus transaksi dari Kiosk ke Kasir.
 */
class OrderRepository(private val apiService: OrderApiService) {

    // Ambil daftar pesanan yang statusnya masih UNPAID (Khusus Kasir)
    suspend fun getUnpaidOrders() =
        apiService.getUnpaidOrders()

    // Ambil semua riwayat transaksi pesanan
    suspend fun getAllOrders() =
        apiService.getAllOrders()

    // Checkout pesanan baru dari Kiosk (Memotong stok secara otomatis)
    suspend fun createOrder(request: OrderRequest) =
        apiService.createOrder(request)

    // Ambil detail transaksi pesanan beserta item-itemnya
    suspend fun getOrderDetail(id: Int) =
        apiService.getOrderDetail(id)

    // Update status pesanan (Konfirmasi PENDING atau batalkan pesanan / CANCELLED)
    suspend fun updateOrderStatus(id: Int, status: String) =
        apiService.updateOrderStatus(id, status)
}
