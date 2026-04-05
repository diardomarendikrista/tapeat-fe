package com.group2.tapeat.data.container

//import com.group2.tapeat.data.repository.KitchenRepository
//import com.group2.tapeat.data.repository.OrderRepository
import com.group2.tapeat.data.repository.ProductRepository
import com.group2.tapeat.data.service.KitchenApiService
import com.group2.tapeat.data.service.OrderApiService
import com.group2.tapeat.data.service.ProductApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Wadah sentral untuk merakit mesin Retrofit dan menyuntikkannya ke Repository.
 */
class TapeatContainer {
    companion object {
        const val BASE_URL = "https://tapeat-be.diardo.my.id/"
    }

    // Setup Utama Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Inisialisasi API Services (Jembatan ke Server)
    private val productApiService: ProductApiService by lazy {
        retrofit.create(ProductApiService::class.java)
    }

    private val orderApiService: OrderApiService by lazy {
        retrofit.create(OrderApiService::class.java)
    }

    private val kitchenApiService: KitchenApiService by lazy {
        retrofit.create(KitchenApiService::class.java)
    }

    // Inisialisasi Repository (Wadah yang akan dipakai ViewModel)
    val productRepository: ProductRepository by lazy {
        ProductRepository(productApiService)
    }

//    val orderRepository: OrderRepository by lazy {
//        OrderRepository(orderApiService)
//    }
//
//    val kitchenRepository: KitchenRepository by lazy {
//        KitchenRepository(kitchenApiService)
//    }
}
