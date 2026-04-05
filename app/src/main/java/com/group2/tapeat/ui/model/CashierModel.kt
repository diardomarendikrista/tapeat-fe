package com.group2.tapeat.ui.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group2.tapeat.data.container.TapeatContainer
import com.group2.tapeat.data.dto.OrderResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CashierModel : ViewModel() {
    private val container = TapeatContainer()
    private val orderRepository = container.orderRepository

    // STATE: Menyimpan daftar antrean
    private val _unpaidOrders = MutableStateFlow<List<OrderResponse>>(emptyList())
    val unpaidOrders: StateFlow<List<OrderResponse>> = _unpaidOrders

    // STATE: Loading indikator
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchUnpaidOrders()
    }

    /**
     * Memanggil API untuk mengambil daftar pesanan berstatus UNPAID
     */
    fun fetchUnpaidOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = orderRepository.getUnpaidOrders()
                _unpaidOrders.value = response
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Mengubah status pesanan menjadi PENDING (sudah dibayar, dan akan masuk kitchen)
     */
    fun confirmPayment(orderId: Int) {
        viewModelScope.launch {
            _isLoading.value = true // Munculkan loading bar saat proses
            try {
                orderRepository.updateOrderStatus(orderId, "PENDING")
                // Tarik ulang data terbaru dari server setelah berhasil
                fetchUnpaidOrders()
            } catch (e: Exception) {
                e.printStackTrace()
                _isLoading.value = false // Matikan loading jika error
            }
        }
    }

    /**
     * Mengubah status pesanan menjadi CANCELLED (batal beli)
     */
    fun cancelOrder(orderId: Int) {
        viewModelScope.launch {
            _isLoading.value = true // Munculkan loading bar saat proses
            try {
                orderRepository.updateOrderStatus(orderId, "CANCELLED")
                // Tarik ulang data terbaru dari server setelah berhasil
                fetchUnpaidOrders()
            } catch (e: Exception) {
                e.printStackTrace()
                _isLoading.value = false // Matikan loading jika error
            }
        }
    }
}
