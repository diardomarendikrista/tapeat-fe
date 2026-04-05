package com.group2.tapeat.ui.model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group2.tapeat.data.container.TapeatContainer
import com.group2.tapeat.data.dto.OrderItemRequest
import com.group2.tapeat.data.dto.OrderRequest
import com.group2.tapeat.data.dto.ProductResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CustomerModel : ViewModel() {
    private val container = TapeatContainer()
    private val productRepository = container.productRepository
    private val orderRepository = container.orderRepository

    // STATE: : daftar products
    private val _products = MutableStateFlow<List<ProductResponse>>(emptyList())
    val products: StateFlow<List<ProductResponse>> = _products

    // STATE: Loading indikator
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

    /**
     * Memanggil API untuk mengambil daftar produk yang aktif (availableOnly = true)
     */
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

    /**
     * Mengirim data pesanan dari keranjang lokal ke server.
     * Jika berhasil, keranjang akan dikosongkan dan pop-up sukses akan muncul.
     *
     * @param orderType Jenis pesanan ("DINE_IN" atau "TAKEAWAY")
     * @param tableNumber Nomor meja pelanggan (Hanya dikirim jika DINE_IN)
     * @param customerName Nama pelanggan (Hanya dikirim jika TAKEAWAY)
     */
    fun checkout(
        orderType: String,
        tableNumber: String?,
        customerName: String?
    ) {
        viewModelScope.launch {
            try {
                // Mapping item keranjang yang sama untuk menghitung quantity
                val itemsRequest = cart.groupBy { it.id }.map { (id, items) ->
                    OrderItemRequest(productId = id, quantity = items.size)
                }

                // Membungkus data sesuai payload DTO
                val request = OrderRequest(
                    orderType = orderType,
                    tableNumber = tableNumber,
                    customerName = customerName,
                    items = itemsRequest
                )

                val response = orderRepository.createOrder(request)

                // Simpan ID pesanan untuk ditampilkan di layar sukses
                lastOrderId.value = "ORD-${response.id}"
                cart.clear() // Bersihkan keranjang dan tampilkan layar sukses
                isOrderSuccess.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Helpers

    //Menambahkan item menu ke dalam keranjang belanja (cart) lokal.
    fun addToCart(product: ProductResponse) =
        cart.add(product)

    // Mengembalikan status UI dari layar "Pesanan Sukses" kembali ke layar pemesanan awal.
    fun resetSuccessState() {
        isOrderSuccess.value = false
    }
}
