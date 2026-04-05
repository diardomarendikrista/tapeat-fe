package com.group2.tapeat.data.service

import com.group2.tapeat.data.dto.OrderResponse
import retrofit2.http.*

/**
 * API khusus antrean Dapur (Kitchen Display System).
 */
interface KitchenApiService {
    /**
     * contoh pakai :
     * api/queue/active
     */
    @GET("api/queue/active")
    suspend fun getActiveQueue(): List<OrderResponse>

    // Ambil daftar pesanan yang sudah selesai dimasak (DELIVERED)
    @GET("api/queue/done")
    suspend fun getCompletedQueue(): List<OrderResponse>

    /**
     * Update status pesanan dari Dapur.
     * contoh pakai:
     * api/queue/12/status?status=DELIVERED
     */
    @PUT("api/queue/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") orderId: Int,                   // ID dari order
        @Query("status") status: String             // COOKING, DELIVERED, dll
    ): OrderResponse
}
