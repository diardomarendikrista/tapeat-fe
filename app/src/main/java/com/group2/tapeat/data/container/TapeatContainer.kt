package com.group2.tapeat.data.container

import com.group2.tapeat.data.repository.TapeatRepository
import com.group2.tapeat.data.service.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TapeatContainer {
    companion object {
        val BASE_URL = "https://tapeat-be.diardo.my.id/"
    }

    // Pakai retrofit untuk buat service
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Inisialisasi ApiService secara lazy (baru dibuat saat dipanggil)
    private val retrofitService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    // Inisialisasi Repository dengan memasukkan retrofitService ke dalamnya
    val tapeatRepository: TapeatRepository by lazy {
        TapeatRepository(retrofitService)
    }
}
