package com.hillbeater.pwcontactpull.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentProviderOperation
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.hillbeater.pwcontactpull.R
import com.hillbeater.pwcontactpull.data.model.ContactApiModel
import java.io.ByteArrayOutputStream

class ContactFormActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etCourse: EditText
    private lateinit var btnSave: Button
    private lateinit var imgProfile: ImageView
    private lateinit var userImage: ImageView
    private lateinit var tvAddPicture: TextView
    private lateinit var loader: ProgressBar

    private var imageUri: Uri? = null
    private var isEditing = false
    private var contact: ContactApiModel? = null

    private val REQUEST_IMAGE_CAMERA = 1001
    private val REQUEST_IMAGE_GALLERY = 1002
    private val REQUEST_WRITE_CONTACTS = 1003

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_form)

        etName = findViewById(R.id.etFirstName)
        etSurname = findViewById(R.id.etSurname)
        etPhone = findViewById(R.id.etPhone)
        etEmail = findViewById(R.id.etEmail)
        etCourse = findViewById(R.id.etCompany)
        btnSave = findViewById(R.id.btnSave)
        imgProfile = findViewById(R.id.imgProfile)
        userImage = findViewById(R.id.userImage)
        tvAddPicture = findViewById(R.id.tvAddPicture)
        loader = findViewById(R.id.progressLoader)

        isEditing = intent.getBooleanExtra("is_editing", false)

        if (isEditing) {
            contact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra("contact_model", ContactApiModel::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getSerializableExtra("contact_model") as? ContactApiModel
            }

            contact?.let {
                val nameParts = it.fullName.trim().split(" ")
                val firstName = nameParts.getOrNull(0) ?: ""
                val lastName = nameParts.drop(1).joinToString(" ")

                etName.setText(firstName)
                etSurname.setText(lastName)
                etPhone.setText(it.phone)
                etEmail.setText(it.email)
                etCourse.setText(it.course)
            }
        }

        tvAddPicture.setOnClickListener {
            showImagePickerDialog()
        }

        btnSave.setOnClickListener {
            if (!isEditing) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_CONTACTS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_CONTACTS),
                        REQUEST_WRITE_CONTACTS
                    )
                } else {
                    saveContact()
                }
            } else {
                saveContact()
            }
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Camera", "Gallery")
        AlertDialog.Builder(this)
            .setTitle("Select Image From")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_IMAGE_CAMERA)
        } else {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAMERA)
        }
    }

    private fun openGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_IMAGE_GALLERY)
        } else {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, REQUEST_IMAGE_GALLERY)
        }
    }

    private fun saveContact() {
        val name = etName.text.toString().trim()
        val surname = etSurname.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val course = etCourse.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill name and phone", Toast.LENGTH_SHORT).show()
            return
        }

        val fullName = "$name $surname"

        if (isEditing) {
            val imageUrlString = imageUri?.toString() ?: ""

            val updatedContact = contact?.let {
                ContactApiModel(
                    id = it.id,
                    fullName = fullName,
                    phone = phone,
                    email = email,
                    course = course,
                    imageUrl = imageUrlString,
                    enrolledOn = it.enrolledOn
                )
            }

            val resultIntent = Intent().apply {
                putExtra("updated_contact", updatedContact)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            Toast.makeText(this, "Contact updated", Toast.LENGTH_SHORT).show()
        } else {
            loader.visibility = View.VISIBLE
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
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, fullName)
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

            if (email.isNotEmpty()) {
                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, email)
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                        .build()
                )
            }

            if (course.isNotEmpty()) {
                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, course)
                        .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
                        .build()
                )
            }

            imageUri?.let { uri ->
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val originalBitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 256, 256, true)

                    val stream = ByteArrayOutputStream()
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                    val photoBytes = stream.toByteArray()

                    ops.add(
                        ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, photoBytes)
                            .build()
                    )


                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to add image", Toast.LENGTH_SHORT).show()
                }
                loader.visibility = View.GONE
            }

            try {
                contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
                Toast.makeText(this, "Contact added to device", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show()
            }
        }

        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAMERA -> {
                    val bitmap = data?.extras?.get("data") as? Bitmap
                    bitmap?.let {
                        imgProfile.visibility = View.GONE
                        userImage.visibility = View.VISIBLE

                        userImage.setImageBitmap(it)
                        imageUri = getImageUriFromBitmap(it)
                    }
                }
                REQUEST_IMAGE_GALLERY -> {
                    imgProfile.visibility = View.GONE
                    userImage.visibility = View.VISIBLE

                    imageUri = data?.data
                    userImage.setImageURI(imageUri)
                }
            }
        }
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "TempImage", null)
        return Uri.parse(path)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                REQUEST_IMAGE_CAMERA -> openCamera()
                REQUEST_IMAGE_GALLERY -> openGallery()
                REQUEST_WRITE_CONTACTS -> saveContact()
            }
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}
