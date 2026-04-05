package com.group2.tapeat.data.service

import com.group2.tapeat.data.dto.ProductResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * API khusus entitas Produk (Katalog Menu).
 */
interface ProductApiService {
    /**
     * contoh pakai :
     * api/products?availableOnly=true
     */
    @GET("api/products")
    suspend fun getProducts(
        @Query("name") name: String? = null,                    // fitur search
        @Query("category") category: String? = null,            // fitur filter
        @Query("availableOnly") availableOnly: Boolean = false  // Dipakai di Customer Order
    ): List<ProductResponse>

    // Detail Product
    @GET("api/products/{id}")
    suspend fun getProductById(@Path("id") id: Int): ProductResponse

    // Tambah Product
    @Multipart
    @POST("api/products")
    suspend fun createProduct(
        @Part("name") name: RequestBody,
        @Part("price") price: RequestBody,
        @Part("stock") stock: RequestBody,
        @Part image: MultipartBody.Part? = null
    ): ProductResponse

    // Edit Product
    @Multipart
    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Part("name") name: RequestBody,
        @Part("price") price: RequestBody,
        @Part("stock") stock: RequestBody,
        @Part image: MultipartBody.Part? = null
    ): ProductResponse

    // Hapus Product (Soft Delete di DB)
    @DELETE("api/products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int)
}
