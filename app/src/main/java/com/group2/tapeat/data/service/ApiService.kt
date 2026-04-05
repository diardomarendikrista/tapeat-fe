package com.group2.tapeat.data.service

import com.group2.tapeat.data.dto.OrderRequest
import com.group2.tapeat.data.dto.OrderResponse
import com.group2.tapeat.data.dto.ProductResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // --- PRODUCT ENDPOINTS ---
    @GET("api/products")
    suspend fun getProducts(
        @Query("availableOnly") availableOnly: Boolean = false,
        @Query("category") category: String? = null
    ): List<ProductResponse>

    // --- ORDER ENDPOINTS ---
    @POST("api/orders")
    suspend fun createOrder(
        @Body request: OrderRequest
    ): OrderResponse

    @GET("api/orders/unpaid")
    suspend fun getUnpaidOrders(): List<OrderResponse>

    // --- KITCHEN QUEUE ENDPOINTS ---
    @PUT("api/queue/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") orderId: Int,
        @Query("status") status: String
    ): OrderResponse
}
