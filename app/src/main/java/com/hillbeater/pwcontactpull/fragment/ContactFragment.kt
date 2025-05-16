package com.hillbeater.pwcontactpull.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hillbeater.pwcontactpull.R
import com.hillbeater.pwcontactpull.activities.ContactFormActivity
import com.hillbeater.pwcontactpull.activities.NewContactActivity
import com.hillbeater.pwcontactpull.adapter.DeviceContactAdapter

class ContactFragment : Fragment() {

    private lateinit var recyclerContacts: RecyclerView
    private lateinit var editTextSearch: EditText
    private lateinit var adapter: DeviceContactAdapter
    private lateinit var tvNoResults: TextView
    private lateinit var syncButton: FloatingActionButton
    private lateinit var addButton: FloatingActionButton

    private val contactsList = mutableListOf<String>()

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readGranted = permissions[Manifest.permission.READ_CONTACTS] == true
        val writeGranted = permissions[Manifest.permission.WRITE_CONTACTS] == true

        if (readGranted) {
            loadContacts()
        } else {
            Toast.makeText(requireContext(), "Permission denied to read contacts", Toast.LENGTH_SHORT).show()
        }

        if (!writeGranted) {
            Toast.makeText(requireContext(), "Permission denied to write contacts", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerContacts = view.findViewById(R.id.recyclerContacts)
        editTextSearch = view.findViewById(R.id.etSearch)
        tvNoResults = view.findViewById(R.id.tvNoResults)
        syncButton = view.findViewById(R.id.fabSync)
        addButton = view.findViewById(R.id.fabAdd)

        recyclerContacts.layoutManager = LinearLayoutManager(requireContext())
        adapter = DeviceContactAdapter(requireContext(), contactsList)
        recyclerContacts.adapter = adapter

        syncButton.setOnClickListener {
            clearSearchAndHideKeyboard()
            val intent = Intent(requireContext(), NewContactActivity::class.java)
            startActivity(intent)
        }

        addButton.setOnClickListener {
            clearSearchAndHideKeyboard()
            val intent = Intent(requireContext(), ContactFormActivity::class.java)
            startActivity(intent)
        }

        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
                updateNoResultsView(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        val permissionsNeeded = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_CONTACTS)
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.WRITE_CONTACTS)
        }

        if (permissionsNeeded.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsNeeded.toTypedArray())
        } else {
            loadContacts()
        }
    }

    private fun loadContacts() {
        contactsList.clear()
        val cr = requireContext().contentResolver
        val cursor = cr.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                val number = it.getString(numberIndex)
                contactsList.add("$name : $number")
            }
        }

        adapter = DeviceContactAdapter(requireContext(), contactsList)
        recyclerContacts.adapter = adapter
        updateNoResultsView(editTextSearch.text.toString())
    }

    private fun updateNoResultsView(query: String) {
        if (contactsList.isEmpty()) {
            tvNoResults.visibility = View.VISIBLE
            tvNoResults.text = getString(R.string.no_contact_found)
        } else if (adapter.itemCount == 0) {
            tvNoResults.visibility = View.VISIBLE
            tvNoResults.text = getString(R.string.no_result_for, query)
        } else {
            tvNoResults.visibility = View.GONE
        }
    }

    private fun clearSearchAndHideKeyboard() {
        editTextSearch.setText("")
        val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(editTextSearch.windowToken, 0)
        editTextSearch.clearFocus()
    }
}
