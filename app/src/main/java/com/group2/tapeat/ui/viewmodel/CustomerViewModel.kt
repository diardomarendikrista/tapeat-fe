package com.group2.tapeat.ui.model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group2.tapeat.data.dto.OrderItemRequest
import com.group2.tapeat.data.dto.OrderRequest
import com.group2.tapeat.data.dto.ProductResponse
import com.group2.tapeat.data.repository.OrderRepository
import com.group2.tapeat.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CustomerViewModel(
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {
    // STATE PRODUK
    private val _products = MutableStateFlow<List<ProductResponse>>(emptyList())
    val products: StateFlow<List<ProductResponse>> = _products

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // STATE KERANJANG
    // Pakai mutableStateListOf agar UI Compose otomatis re-render saat isi berubah
    val cart = mutableStateListOf<ProductResponse>()

    // STATE ORDER
    val isOrderSuccess = mutableStateOf(false)
    val lastOrderId = mutableStateOf("")

    init {
        fetchProducts()
    }

    /**
     * Mengambil data produk aktif dari API
     */
    fun fetchProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // availableOnly = true karena ini untuk Kiosk Pelanggan
                val response = productRepository.getProducts(availableOnly = true)
                _products.value = response
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Logika Checkout (Kirim ke API)
     */
    fun checkout(orderType: String, tableNumber: String?, customerName: String?) {
        viewModelScope.launch {
            try {
                // Map isi cart ke format yang diminta API (productId & quantity)
                val itemsRequest = cart.groupBy { it.id }.map { (id, items) ->
                    OrderItemRequest(productId = id, quantity = items.size)
                }

                val request = OrderRequest(
                    orderType = orderType,
                    tableNumber = tableNumber,
                    customerName = customerName,
                    items = itemsRequest
                )

                val response = orderRepository.createOrder(request)

                // Jika sukses
                lastOrderId.value = "ORD-${response.id}"
                cart.clear()
                isOrderSuccess.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addToCart(product: ProductResponse) {
        cart.add(product)
    }

    fun removeFromCart(product: ProductResponse) {
        cart.removeAll { it.id == product.id }
    }

    fun resetSuccessState() {
        isOrderSuccess.value = false
    }
}
