package com.group2.tapeat.data.repository

import com.group2.tapeat.data.service.KitchenApiService

/**
 * Jembatan penghubung antara UI (ViewModel) dan API kitchen.
 * Mengelola pesanan yang masuk ke layar koki.
 */
class KitchenRepository(private val apiService: KitchenApiService) {

    // Ambil daftar pesanan yang sedang/harus dimasak (PENDING / COOKING)
    suspend fun getActiveQueue() =
        apiService.getActiveQueue()

    // Ambil daftar pesanan yang sudah selesai dimasak (DELIVERED)
    suspend fun getCompletedQueue() =
        apiService.getCompletedQueue()

    // Update progress masakan (COOKING, DELIVERED, dll)
    suspend fun updateOrderStatus(orderId: Int, status: String) =
        apiService.updateOrderStatus(orderId, status)
}
