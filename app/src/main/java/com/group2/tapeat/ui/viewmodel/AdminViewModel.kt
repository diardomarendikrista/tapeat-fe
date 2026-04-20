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
 *
 * Bertanggung jawab atas:
 * - Menyimpan dan mengelola state UI (daftar produk, loading, error, dll)
 * - Menjalankan operasi CRUD produk melalui [ProductRepository]
 * - Mengonversi data form (String + URI) menjadi format Multipart untuk dikirim ke server
 *
 * Menggunakan [StateFlow] agar UI di Compose bisa mengamati perubahan state secara reaktif.
 *
 * @param repository Repository yang menjadi jembatan ke API produk.
 */
class AdminViewModel(private val repository: ProductRepository) : ViewModel() {

    // Daftar semua produk yang ditampilkan di layar Admin
    // Diisi ulang setiap kali loadProducts() dipanggil
    private val _products = MutableStateFlow<List<ProductResponse>>(emptyList())
    val products: StateFlow<List<ProductResponse>> = _products.asStateFlow()

    // True saat ada operasi jaringan yang sedang berjalan (fetch, save, delete)
    // Digunakan untuk menampilkan loading spinner dan menonaktifkan tombol
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Pesan error yang ditampilkan sebagai snackbar, null jika tidak ada error
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Pesan sukses yang ditampilkan sebagai snackbar, null jika tidak ada notifikasi
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Mengontrol visibilitas bottom sheet form tambah/edit
    // true = form terbuka, false = form tertutup
    private val _showFormSheet = MutableStateFlow(false)
    val showFormSheet: StateFlow<Boolean> = _showFormSheet.asStateFlow()

    // Produk yang sedang diedit. null berarti form dalam mode Tambah.
    private val _editingProduct = MutableStateFlow<ProductResponse?>(null)
    val editingProduct: StateFlow<ProductResponse?> = _editingProduct.asStateFlow()

    // Produk yang menunggu konfirmasi hapus. null berarti dialog hapus tidak tampil.
    private val _productToDelete = MutableStateFlow<ProductResponse?>(null)
    val productToDelete: StateFlow<ProductResponse?> = _productToDelete.asStateFlow()

    init {
        // Langsung load produk saat ViewModel pertama kali dibuat
        loadProducts()
    }

    /**
     * Mengambil semua produk dari backend.
     *
     * Menggunakan availableOnly=false agar produk dengan stok 0 tetap muncul
     * di layar Admin (untuk keperluan restock), berbeda dengan layar Kiosk
     * yang menggunakan availableOnly=true.
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
                // finally memastikan loading selalu dimatikan meski terjadi error
                _isLoading.value = false
            }
        }
    }

    /**
     * Membuka form dalam mode TAMBAH.
     * editingProduct di-set null agar ProductFormView tahu ini mode tambah (form kosong).
     */
    fun openAddForm() {
        _editingProduct.value = null
        _showFormSheet.value = true
    }

    /**
     * Membuka form dalam mode EDIT dengan data produk yang dipilih.
     * editingProduct di-set agar ProductFormView bisa pre-fill datanya.
     *
     * @param product Produk yang akan diedit.
     */
    fun openEditForm(product: ProductResponse) {
        _editingProduct.value = product
        _showFormSheet.value = true
    }

    /**
     * Menutup bottom sheet form dan mereset produk yang sedang diedit.
     */
    fun closeForm() {
        _showFormSheet.value = false
        _editingProduct.value = null
    }

    /**
     * Menyimpan produk yang akan dihapus ke state, memicu munculnya dialog konfirmasi.
     *
     * @param product Produk yang ingin dihapus.
     */
    fun confirmDelete(product: ProductResponse) {
        _productToDelete.value = product
    }

    /**
     * Membatalkan penghapusan dan menutup dialog konfirmasi.
     */
    fun cancelDelete() {
        _productToDelete.value = null
    }

    /**
     * Menjalankan soft delete pada produk yang tersimpan di [_productToDelete].
     *
     * Soft delete berarti produk tidak benar-benar dihapus dari database,
     * melainkan hanya di-set isActive = false di backend.
     * Ini menjaga integritas data riwayat transaksi lama yang mereferensikan produk ini.
     */
    fun deleteProduct() {
        val product = _productToDelete.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteProduct(product.id)
                _successMessage.value = "${product.name} berhasil dihapus"
                _productToDelete.value = null
                // Refresh list agar produk yang dihapus langsung hilang dari tampilan
                loadProducts()
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menghapus produk: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Menyimpan produk baru atau memperbarui produk yang sudah ada.
     *
     * Fungsi ini menangani seluruh proses persiapan data sebelum dikirim ke server:
     * 1. Validasi input (nama tidak kosong, harga & stok harus angka valid)
     * 2. Konversi String → RequestBody (format yang dibutuhkan Retrofit untuk multipart)
     * 3. Konversi URI gambar → MultipartBody.Part (membaca bytes dari galeri HP)
     * 4. Menentukan apakah ini operasi CREATE atau UPDATE berdasarkan [editingProduct]
     *
     * @param context   Diperlukan untuk membaca file gambar dari URI galeri perangkat.
     * @param name      Nama produk dari input field.
     * @param price     Harga produk dalam format String (akan divalidasi dan dikonversi ke Double).
     * @param stock     Jumlah stok dalam format String (akan divalidasi dan dikonversi ke Int).
     * @param category  Kategori produk, boleh kosong (opsional).
     * @param imageUri  URI gambar yang dipilih dari galeri, null jika tidak ganti gambar.
     */
    fun saveProduct(
        context: Context,
        name: String,
        price: String,
        stock: String,
        category: String,
        imageUri: Uri?
    ) {
        // --- Validasi input ---
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
                // --- Konversi teks ke RequestBody ---
                // Format "text/plain" diperlukan oleh Retrofit @Part untuk field teks dalam multipart
                val nameBody     = name.toRequestBody("text/plain".toMediaTypeOrNull())
                val priceBody    = priceDouble.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val stockBody    = stockInt.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val categoryBody = category.toRequestBody("text/plain".toMediaTypeOrNull())

                // --- Konversi URI gambar ke MultipartBody.Part ---
                // URI hanya berupa alamat file di HP, perlu dibaca sebagai bytes terlebih dahulu
                // kemudian dibungkus menjadi Part yang bisa dikirim bersama request multipart
                val imagePart = imageUri?.let { uri ->
                    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close() // selalu tutup stream setelah selesai membaca
                    bytes?.let {
                        val requestBody = it.toRequestBody("image/*".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("image", "image.jpg", requestBody)
                    }
                }

                // --- Tentukan CREATE atau UPDATE ---
                val editingId = _editingProduct.value?.id
                if (editingId != null) {
                    // Mode Edit → kirim PUT /api/products/{id}
                    repository.updateProduct(editingId, nameBody, priceBody, stockBody, categoryBody, imagePart)
                    _successMessage.value = "$name berhasil diperbarui"
                } else {
                    // Mode Tambah → kirim POST /api/products
                    repository.createProduct(nameBody, priceBody, stockBody, categoryBody, imagePart)
                    _successMessage.value = "$name berhasil ditambahkan"
                }

                closeForm()
                // Refresh list agar perubahan langsung terlihat di layar
                loadProducts()
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menyimpan produk: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Mereset pesan sukses setelah ditampilkan sebagai snackbar.
     * Dipanggil oleh UI setelah snackbar selesai ditampilkan.
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    /**
     * Mereset pesan error setelah ditampilkan sebagai snackbar.
     * Dipanggil oleh UI setelah snackbar selesai ditampilkan.
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Factory untuk membuat instance [AdminViewModel] dengan dependency [ProductRepository].
     *
     * Diperlukan karena ViewModel tidak boleh diinstansiasi langsung dengan konstruktor
     * yang menerima parameter — Android harus mengelola siklus hidupnya sendiri.
     * Factory ini dipanggil di [AdminView] saat memanggil viewModel(factory = ...).
     */
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