package com.hillbeater.pwcontactpull.activities

import android.content.ContentProviderOperation
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hillbeater.pwcontactpull.R
import com.hillbeater.pwcontactpull.adapter.ContactAdapter
import com.hillbeater.pwcontactpull.data.model.ContactApiModel
import com.hillbeater.pwcontactpull.viewmodel.ContactViewModel
import java.util.*

class NewContactActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loader: ProgressBar
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var noContactsText: TextView
    private lateinit var syncButton: Button
    private lateinit var lastSyncedText: TextView
    private lateinit var btnBack : ImageButton
    private lateinit var successView : ConstraintLayout

    private val viewModel: ContactViewModel by viewModels()

    private val PERMISSIONS_REQUEST_WRITE_CONTACTS = 100

    private val editContactLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val updatedContact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getSerializableExtra("updated_contact", ContactApiModel::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getSerializableExtra("updated_contact") as? ContactApiModel
            }

            updatedContact?.let { contact ->
                viewModel.updateContact(contact)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_contact)

        recyclerView = findViewById(R.id.rvContacts)
        loader = findViewById(R.id.progressLoader)
        noContactsText = findViewById(R.id.tvNoContacts)
        syncButton = findViewById(R.id.btnSync)
        lastSyncedText = findViewById(R.id.tvLastSynced)
        btnBack = findViewById(R.id.btnBack)
        successView = findViewById(R.id.syncSuccessView)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val lastSynced = prefs.getString("last_synced_time", null)

        if (lastSynced.isNullOrEmpty()) {
            lastSyncedText.visibility = View.GONE
        } else {
            lastSyncedText.text = getString(R.string.last_synced_label, lastSynced)
            lastSyncedText.visibility = View.VISIBLE
        }

        contactAdapter = ContactAdapter { contact ->
            openEditContactActivity(contact)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = contactAdapter

        viewModel.getContacts()
        observeViewModel()

        syncButton.setOnClickListener {
            checkPermissionsAndSync()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun openEditContactActivity(contact: ContactApiModel) {
        val intent = Intent(this, ContactFormActivity::class.java).apply {
            putExtra("contact_model", contact)
            putExtra("is_editing", true)
        }
        editContactLauncher.launch(intent)
    }


    private fun observeViewModel() {
        viewModel.loading.observe(this) { isLoading ->
            loader.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.contacts.observe(this) { contactList ->
            contactAdapter.submitList(contactList)

            if (contactList.isNullOrEmpty()) {
                noContactsText.visibility = View.VISIBLE
                syncButton.visibility = View.GONE
                recyclerView.visibility = View.GONE
            } else {
                noContactsText.visibility = View.GONE
                syncButton.visibility = View.VISIBLE
                recyclerView.visibility = View.VISIBLE
            }
        }

        viewModel.error.observe(this) { errorMsg ->
            if (errorMsg.isNotEmpty()) {
                showErrorDialog(errorMsg)
            }
        }
    }

    private fun showErrorDialog(errorMsg: String?) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(errorMsg)
        builder.setCancelable(false)

        builder.setPositiveButton("Retry") { dialog, _ ->
            dialog.dismiss()
            viewModel.getContacts()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
            finish()
        }
        val dialog = builder.create()
        dialog.show()

        val darkBlue = ContextCompat.getColor(this, R.color.dark_blue)

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(darkBlue)
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(darkBlue)
    }

    private fun checkPermissionsAndSync() {
        if (checkSelfPermission(android.Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.WRITE_CONTACTS), PERMISSIONS_REQUEST_WRITE_CONTACTS)
        } else {
            syncAndSaveContacts()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_WRITE_CONTACTS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                syncAndSaveContacts()
            } else {
                Toast.makeText(this, "Permission denied to write contacts", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun syncAndSaveContacts() {
        val contactList = viewModel.contacts.value

        if (contactList.isNullOrEmpty()) {
            Toast.makeText(this, "No contacts to sync", Toast.LENGTH_SHORT).show()
            return
        }

        loader.visibility = View.VISIBLE
        syncButton.isEnabled = false

        for (contact in contactList) {
            addContactToDevice(contact.fullName, contact.phone, contact.email, contact.course)
        }

        noContactsText.visibility = if (contactList.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (contactList.isEmpty()) View.GONE else View.VISIBLE

        val currentDateTime = getCurrentDateTime()

        lastSyncedText.text = getString(R.string.last_synced_label, currentDateTime)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        prefs.edit().putString("last_synced_time", currentDateTime).apply()

        loader.visibility = View.GONE
        syncButton.isEnabled = true
        successView.visibility = View.VISIBLE

        successView.postDelayed({
            successView.visibility = View.GONE
            finish()
        }, 2000)

        Toast.makeText(this, "Contacts synced successfully", Toast.LENGTH_SHORT).show()
    }


    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy h:mm a", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun addContactToDevice(name: String, phone: String, email: String, course: String) {
        try {
            val ops = ArrayList<ContentProviderOperation>()

            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build()
            )

            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                    .build()
            )

            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build()
            )

            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, email)
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                    .build()
            )

            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, course)
                    .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
                    .build()
            )

            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to add contact: $name", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
