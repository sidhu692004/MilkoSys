package com.sudhanshu.milkosys

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var etResetEmail: EditText
    private lateinit var btnSendResetLink: Button
    private lateinit var tvBackToLogin: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_password)  // XML file ka naam dalna

        auth = FirebaseAuth.getInstance()

        etResetEmail = findViewById(R.id.etResetEmail)
        btnSendResetLink = findViewById(R.id.btnSendResetLink)
        tvBackToLogin = findViewById(R.id.tvBackToLogin)

        // Send Reset Link
        btnSendResetLink.setOnClickListener {
            val email = etResetEmail.text.toString().trim()

            if (TextUtils.isEmpty(email)) {
                etResetEmail.error = "Email required"
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Reset link sent to your email",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        // Back to Login
        tvBackToLogin.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
