package com.group2.tapeat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group2.tapeat.data.container.TapeatContainer
import com.group2.tapeat.data.dto.OrderResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class KitchenViewModel : ViewModel() {

    // ✅ BENAR
    private val kitchenRepository = TapeatContainer().kitchenRepository

    private val _activeOrders = MutableStateFlow<List<OrderResponse>>(emptyList())
    val activeOrders: StateFlow<List<OrderResponse>> = _activeOrders

    private val _doneOrders = MutableStateFlow<List<OrderResponse>>(emptyList())
    val doneOrders: StateFlow<List<OrderResponse>> = _doneOrders

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchAll()
    }

    fun fetchAll() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _activeOrders.value = kitchenRepository.getActiveQueue()
                _doneOrders.value = kitchenRepository.getCompletedQueue()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateStatus(orderId: Int, status: String) {
        viewModelScope.launch {
            try {
                kitchenRepository.updateOrderStatus(orderId, status)
                fetchAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}