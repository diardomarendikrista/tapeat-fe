package com.group2.tapeat.data.repository

import com.group2.tapeat.data.service.ProductApiService
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * Jembatan penghubung antara UI (ViewModel) dan API Produk.
 * Mengelola semua request yang berkaitan dengan entitas Menu/Produk.
 */
class ProductRepository(private val apiService: ProductApiService) {

    // Ambil daftar produk (Bisa di-filter berdasarkan nama, kategori, dan ketersediaan stok)
    suspend fun getProducts(
        name: String? = null,
        category: String? = null,
        availableOnly: Boolean = false
    ) =
        apiService.getProducts(name, category, availableOnly)

    // Ambil detail satu produk berdasarkan ID
    suspend fun getProductById(id: Int) =
        apiService.getProductById(id)

    // Buat produk baru (pakai gambar Multipart)
    suspend fun createProduct(
        name: RequestBody,
        price: RequestBody,
        stock: RequestBody,
        category: RequestBody,
        image: MultipartBody.Part?
    ) =
        apiService.createProduct(name, price, stock, category, image)


    // Edit produk yang sudah ada (pakai gambar Multipart)
    suspend fun updateProduct(
        id: Int,
        name: RequestBody,
        price: RequestBody,
        stock: RequestBody,
        category: RequestBody,
        image: MultipartBody.Part?
    ) =
        apiService.updateProduct(id, name, price, stock, category, image)

    // Hapus produk (Soft Delete - mengubah isActive jadi false di backend)
    suspend fun deleteProduct(id: Int) =
        apiService.deleteProduct(id)
}
