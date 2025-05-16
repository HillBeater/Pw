package com.hillbeater.pwcontactpull.data.model

import java.io.Serializable

data class ApiResponse(
    val success: Boolean,
    val Data: ContactData
)

data class ContactData(
    val date: String,
    val totalUsers: Int,
    val users: List<ContactApiModel>
)

data class ContactApiModel(
    val id: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val course: String,
    val imageUrl: String,
    val enrolledOn: String
) : Serializable
