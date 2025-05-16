package com.hillbeater.pwcontactpull.data.api

import com.hillbeater.pwcontactpull.data.model.ApiResponse
import retrofit2.http.GET

interface ContactApiService {
    @GET("api/contacts")
    suspend fun getContacts(): ApiResponse
}
