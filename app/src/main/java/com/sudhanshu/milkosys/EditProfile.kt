package com.sudhanshu.milkosys

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class EditProfileActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etMobile: EditText
    private lateinit var etAddress: EditText
    private lateinit var btnSave: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserUid = auth.currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        etName = findViewById(R.id.etEditName)
        etMobile = findViewById(R.id.etEditMobile)
        etAddress = findViewById(R.id.etEditAddress)
        btnSave = findViewById(R.id.btnSaveProfile)

        loadProfile()

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val mobile = etMobile.text.toString().trim()
            val address = etAddress.text.toString().trim()

            if (name.isEmpty() || mobile.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                saveProfile(name, mobile, address)
            }
        }
    }

    private fun loadProfile() {
        if (currentUserUid.isEmpty()) return
        db.collection("users").document(currentUserUid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val user = doc.toObject(UserProfile::class.java)
                    user?.let {
                        etName.setText(it.name)
                        etMobile.setText(it.mobile)
                        etAddress.setText(it.address)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfile(name: String, mobile: String, address: String) {
        if (currentUserUid.isEmpty()) return

        // Create UserProfile object
        val userProfile = UserProfile(
            uid = currentUserUid,
            name = name,
            email = auth.currentUser?.email ?: "",
            mobile = mobile,
            address = address
        )

        // Save or update using set() with merge
        db.collection("users").document(currentUserUid)
            .set(userProfile, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }
}
