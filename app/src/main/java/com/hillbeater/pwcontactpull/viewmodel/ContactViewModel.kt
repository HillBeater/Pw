package com.hillbeater.pwcontactpull.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hillbeater.pwcontactpull.data.model.ApiResponse
import com.hillbeater.pwcontactpull.data.model.ContactApiModel
import com.hillbeater.pwcontactpull.data.repository.ContactRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ContactViewModel : ViewModel() {
    private val repo = ContactRepository()

    private val _contacts = MutableLiveData<List<ContactApiModel>>()
    private val _error = MutableLiveData<String>()
    private val _loading = MutableLiveData<Boolean>()

    val contacts: LiveData<List<ContactApiModel>> get() = _contacts
    val error: LiveData<String> get() = _error
    val loading: LiveData<Boolean> get() = _loading

    fun getContacts() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val users = repo.fetchContacts()
                _contacts.value = users
                _error.value = ""
            } catch (e: IOException) {
                _error.value = "Please check your internet connection."
            } catch (e: HttpException) {
                _error.value = "Server error: ${e.message()}"
            } catch (e: Exception) {
                _error.value = "Something went wrong. Please try again."
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateContact(updatedContact: ContactApiModel) {
        val currentList = _contacts.value?.toMutableList() ?: mutableListOf()
        val index = currentList.indexOfFirst { it.id == updatedContact.id }
        if (index != -1) {
            currentList[index] = updatedContact
            _contacts.value = currentList
        }
    }
}

