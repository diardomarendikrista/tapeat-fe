package com.group2.tapeat.data.service

import com.group2.tapeat.data.dto.OrderRequest
import com.group2.tapeat.data.dto.OrderResponse
import retrofit2.http.*

/**
 * API khusus transaksi Pesanan dan Kasir.
 */
interface OrderApiService {
    /**
     * contoh pakai :
     * api/orders/unpaid
     */
    @GET("api/orders/unpaid")
    suspend fun getUnpaidOrders(): List<OrderResponse>

    // Ambil semua riwayat pesanan
    @GET("api/orders")
    suspend fun getAllOrders(): List<OrderResponse>

    // Buat pesanan baru dari Kiosk (Status awal otomatis UNPAID)
    @POST("api/orders")
    suspend fun createOrder(
        @Body request: OrderRequest
    ): OrderResponse

    // Detail Pesanan
    @GET("api/orders/{id}")
    suspend fun getOrderDetail(
        @Path("id") id: Int
    ): OrderResponse

    /**
     * Update status pesanan dari Kasir (Konfirmasi bayar atau Batal)
     * contoh pakai :
     * api/orders/12/status?status=PENDING
     */
    @PUT("api/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: Int,                // ID pesanan
        @Query("status") status: String     // PENDING (bayar) atau CANCELLED (batal)
    ): OrderResponse
}
