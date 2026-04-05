package com.group2.tapeat.data.dto

// Mewakili response dari GET /api/products
data class ProductResponse(
    val id: Int,
    val name: String,
    val price: Double,
    val stock: Int,
    val imageUrl: String?, // Bisa null kalau belum ada gambar
    val category: String?, // kategori sifatnya opsional.
    val isActive: Boolean // Soft Delete
)
