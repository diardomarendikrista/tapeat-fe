package com.group2.tapeat.ui.model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group2.tapeat.data.container.TapeatContainer
import com.group2.tapeat.data.dto.OrderItemRequest
import com.group2.tapeat.data.dto.OrderRequest
import com.group2.tapeat.data.dto.ProductResponse
import com.group2.tapeat.data.repository.OrderRepository
import com.group2.tapeat.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CustomerModel : ViewModel() {
    private val container = TapeatContainer()
    private val productRepository = container.productRepository
    private val orderRepository = container.orderRepository

    // State & Logic
    private val _products = MutableStateFlow<List<ProductResponse>>(emptyList())
    val products: StateFlow<List<ProductResponse>> = _products

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // STATE UI LOCAL
    var selectedCategory = mutableStateOf("Semua")
    var searchQuery = mutableStateOf("")

    // STATE CART
    val cart = mutableStateListOf<ProductResponse>()

    // STATE ORDER SUCCESS
    var isOrderSuccess = mutableStateOf(false)
    var lastOrderId = mutableStateOf("")

    init {
        fetchProducts()
    }

    fun fetchProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = productRepository.getProducts(availableOnly = true)
                _products.value = response
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkout(orderType: String, tableNumber: String?, customerName: String?) {
        viewModelScope.launch {
            try {
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

                lastOrderId.value = "ORD-${response.id}"
                cart.clear()
                isOrderSuccess.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Helpers
    fun addToCart(product: ProductResponse) =
        cart.add(product)

    fun removeFromCart(product: ProductResponse) =
        cart.removeAll {
            it.id == product.id
        }

    fun resetSuccessState() {
        isOrderSuccess.value = false
    }
}
