package com.group2.tapeat.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.group2.tapeat.data.dto.ProductResponse
import com.group2.tapeat.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream

/**
 * ViewModel untuk layar Admin (Manajemen Produk).
 * Mengelola state list produk, form tambah/edit, dan operasi CRUD.
 */
class AdminViewModel(private val repository: ProductRepository) : ViewModel() {

    // --- State untuk daftar produk ---
    private val _products = MutableStateFlow<List<ProductResponse>>(emptyList())
    val products: StateFlow<List<ProductResponse>> = _products.asStateFlow()

    // --- State untuk loading indicator ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // --- State untuk pesan error ---
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // --- State untuk pesan sukses (snackbar) ---
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // --- State untuk kontrol bottom sheet form ---
    private val _showFormSheet = MutableStateFlow(false)
    val showFormSheet: StateFlow<Boolean> = _showFormSheet.asStateFlow()

    // --- State untuk produk yang sedang di-edit (null = mode tambah baru) ---
    private val _editingProduct = MutableStateFlow<ProductResponse?>(null)
    val editingProduct: StateFlow<ProductResponse?> = _editingProduct.asStateFlow()

    // --- State untuk dialog konfirmasi hapus ---
    private val _productToDelete = MutableStateFlow<ProductResponse?>(null)
    val productToDelete: StateFlow<ProductResponse?> = _productToDelete.asStateFlow()

    init {
        loadProducts()
    }

    /**
     * Mengambil semua produk dari backend (availableOnly=false agar stok 0 tetap muncul).
     */
    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _products.value = repository.getProducts(availableOnly = false)
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat produk: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Membuka form dalam mode TAMBAH (editingProduct = null).
     */
    fun openAddForm() {
        _editingProduct.value = null
        _showFormSheet.value = true
    }

    /**
     * Membuka form dalam mode EDIT dengan data produk yang dipilih.
     */
    fun openEditForm(product: ProductResponse) {
        _editingProduct.value = product
        _showFormSheet.value = true
    }

    /**
     * Menutup bottom sheet form.
     */
    fun closeForm() {
        _showFormSheet.value = false
        _editingProduct.value = null
    }

    /**
     * Menampilkan dialog konfirmasi hapus.
     */
    fun confirmDelete(product: ProductResponse) {
        _productToDelete.value = product
    }

    /**
     * Membatalkan dialog konfirmasi hapus.
     */
    fun cancelDelete() {
        _productToDelete.value = null
    }

    /**
     * Menghapus produk (soft delete).
     */
    fun deleteProduct() {
        val product = _productToDelete.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteProduct(product.id)
                _successMessage.value = "${product.name} berhasil dihapus"
                _productToDelete.value = null
                loadProducts()
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menghapus produk: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Menyimpan produk baru atau mengupdate produk yang sudah ada.
     * @param context Diperlukan untuk membaca file gambar dari URI.
     * @param name Nama produk.
     * @param price Harga produk (String, akan dikonversi).
     * @param stock Stok produk (String, akan dikonversi).
     * @param category Kategori produk (opsional).
     * @param imageUri URI gambar yang dipilih dari galeri (null = tidak ganti gambar).
     */
    fun saveProduct(
        context: Context,
        name: String,
        price: String,
        stock: String,
        category: String,
        imageUri: Uri?
    ) {
        // Validasi input sederhana
        if (name.isBlank()) {
            _errorMessage.value = "Nama produk tidak boleh kosong"
            return
        }
        val priceDouble = price.toDoubleOrNull()
        if (priceDouble == null || priceDouble < 0) {
            _errorMessage.value = "Harga tidak valid"
            return
        }
        val stockInt = stock.toIntOrNull()
        if (stockInt == null || stockInt < 0) {
            _errorMessage.value = "Stok tidak valid"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Konversi ke RequestBody untuk multipart
                val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
                val priceBody = priceDouble.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val stockBody = stockInt.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val categoryBody = category.toRequestBody("text/plain".toMediaTypeOrNull())

                // Konversi URI gambar ke MultipartBody.Part (jika ada)
                val imagePart = imageUri?.let { uri ->
                    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()
                    bytes?.let {
                        val requestBody = it.toRequestBody("image/*".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("image", "image.jpg", requestBody)
                    }
                }

                val editingId = _editingProduct.value?.id
                if (editingId != null) {
                    repository.updateProduct(editingId, nameBody, priceBody, stockBody, categoryBody, imagePart)
                } else {
                    repository.createProduct(nameBody, priceBody, stockBody, categoryBody, imagePart)
                }

                closeForm()
                loadProducts()
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menyimpan produk: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Reset pesan sukses setelah ditampilkan.
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    /**
     * Reset pesan error setelah ditampilkan.
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // --- factory untuk instansiasi ViewModel dengan dependency ---
    class Factory(private val repository: ProductRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
                return AdminViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}