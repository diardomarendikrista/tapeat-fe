package com.group2.tapeat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group2.tapeat.data.container.TapeatContainer
import com.group2.tapeat.data.dto.OrderResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel untuk layar Kitchen (Dapur)
 * Bertugas:
 * - Mengambil data antrean aktif (PENDING / COOKING)
 * - Mengambil data antrean selesai (DELIVERED)
 * - Mengubah status pesanan (DELIVERED / CANCELLED)
 *
 * Menggunakan StateFlow agar UI (Compose) bisa otomatis update saat data berubah
 */
class KitchenViewModel : ViewModel() {

    /**
     * Inisialisasi repository dari container
     * Repository ini yang berkomunikasi langsung dengan API (backend)
     */
    private val kitchenRepository = TapeatContainer().kitchenRepository

    // =========================
    // STATE: ANTREAN AKTIF
    // =========================
    private val _activeOrders = MutableStateFlow<List<OrderResponse>>(emptyList())

    /**
     * State public (read-only) untuk UI
     * Berisi daftar pesanan yang sedang diproses di dapur
     */
    val activeOrders: StateFlow<List<OrderResponse>> = _activeOrders

    // =========================
    // STATE: ANTREAN SELESAI
    // =========================
    private val _doneOrders = MutableStateFlow<List<OrderResponse>>(emptyList())

    /**
     * State public (read-only) untuk UI
     * Berisi daftar pesanan yang sudah selesai (DELIVERED)
     */
    val doneOrders: StateFlow<List<OrderResponse>> = _doneOrders

    // =========================
    // STATE: LOADING
    // =========================
    private val _isLoading = MutableStateFlow(false)

    /**
     * State untuk menampilkan loading indicator di UI
     */
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Init akan dijalankan saat ViewModel pertama kali dibuat
     * Otomatis mengambil semua data antrean dari server
     */
    init {
        fetchAll()
    }


    /**
     * Mengambil semua data antrean dari backend
     * - Active Queue (PENDING / COOKING)
     * - Completed Queue (DELIVERED)
     */
    fun fetchAll() {
        viewModelScope.launch {
            _isLoading.value = true // aktifkan loading
            try {
                // Ambil antrean aktif dari API
                _activeOrders.value = kitchenRepository.getActiveQueue()

                // Ambil antrean selesai dari API
                _doneOrders.value = kitchenRepository.getCompletedQueue()
            } catch (e: Exception) {
                // Menangani error (sementara hanya print log)
                e.printStackTrace()
            } finally {
                _isLoading.value = false // matikan loading
            }
        }
    }

    /**
     * Mengubah status pesanan
     *
     * Contoh penggunaan:
     * - "DELIVERED" → pesanan selesai dimasak
     * - "CANCELLED" → pesanan dibatalkan
     *
     * Setelah update berhasil:
     * → otomatis refresh data agar UI langsung update
     */
    fun updateStatus(orderId: Int, status: String) {
        viewModelScope.launch {
            try {
                // Kirim request ke backend untuk update status
                kitchenRepository.updateOrderStatus(orderId, status)

                // Refresh data setelah update berhasil
                fetchAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}