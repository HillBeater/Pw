package com.hillbeater.pwcontactpull.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: ContactApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://android-dev-assignment.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ContactApiService::class.java)
    }
}
