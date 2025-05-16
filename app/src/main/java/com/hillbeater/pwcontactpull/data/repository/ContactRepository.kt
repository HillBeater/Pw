package com.hillbeater.pwcontactpull.data.repository

import com.hillbeater.pwcontactpull.data.api.RetrofitInstance
import com.hillbeater.pwcontactpull.data.model.ContactApiModel

class ContactRepository {
    suspend fun fetchContacts(): List<ContactApiModel> {
        val response = RetrofitInstance.api.getContacts()
        if (response.success) {
            return response.Data.users
        } else {
            throw Exception("API returned failure")
        }
    }
}