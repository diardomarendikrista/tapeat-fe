package com.group2.tapeat.ui.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group2.tapeat.data.container.TapeatContainer
import com.group2.tapeat.data.dto.OrderResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel untuk layar Kasir.
 * Bertugas mengambil data antrean pesanan yang belum dibayar (UNPAID)
 * dan mengelola aksi konfirmasi atau pembatalan pesanan.
 */
class CashierViewModel : ViewModel() {
    // Inisialisasi Repository dari Container
    private val orderRepository = TapeatContainer().orderRepository

    // STATE: Menyimpan daftar pesanan yang belum dibayar
    private val _unpaidOrders = MutableStateFlow<List<OrderResponse>>(emptyList())
    val unpaidOrders: StateFlow<List<OrderResponse>> = _unpaidOrders

    // STATE: Menyimpan status loading saat memanggil API
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        // Otomatis fetch data saat halaman kasir dibuka
        fetchUnpaidOrders()
    }

    /**
     * Memanggil API: GET /api/orders/unpaid
     */
    fun fetchUnpaidOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = orderRepository.getUnpaidOrders()
                _unpaidOrders.value = response
            } catch (e: Exception) {
                e.printStackTrace()
                // Idealnya di sini bisa ditambahkan state untuk Error Handling (Snackbar/Toast)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Memanggil API: PUT /api/orders/{id}/status?status=PENDING
     * Digunakan saat pelanggan sudah membayar, pesanan diteruskan ke dapur.
     */
    fun confirmPayment(orderId: Int) {
        viewModelScope.launch {
            try {
                // Update status ke PENDING agar masuk ke antrean dapur
                orderRepository.updateOrderStatus(orderId, "PENDING")
                // Refresh list antrean kasir setelah berhasil
                fetchUnpaidOrders()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Memanggil API: PUT /api/orders/{id}/status?status=CANCELLED
     * Digunakan jika pelanggan batal memesan/membayar. Stok akan otomatis kembali di backend.
     */
    fun cancelOrder(orderId: Int) {
        viewModelScope.launch {
            try {
                // Update status ke CANCELLED
                orderRepository.updateOrderStatus(orderId, "CANCELLED")
                // Refresh list antrean kasir setelah berhasil
                fetchUnpaidOrders()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
