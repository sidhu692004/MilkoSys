package com.sudhanshu.milkosys

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnSignup: Button
    private lateinit var loginWithGoogle: Button
    private lateinit var ftPassword: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Firebase Auth instance
        auth = FirebaseAuth.getInstance()

        // Google Sign-In config
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize views
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnSignup = findViewById(R.id.btnSignup)
        loginWithGoogle = findViewById(R.id.loginWithGoogle)
        ftPassword = findViewById(R.id.ftPassword)

        // Login with Email/Password
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (TextUtils.isEmpty(email)) {
                etEmail.error = "Email required"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.error = "Password required"
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        checkUserRole(auth.currentUser?.uid)
                    } else {
                        Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Signup button (sirf test ke liye, role bhi save karenge)
        btnSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }

        // Forgot password
        ftPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
            finish()
        }

        // Google Login
        loginWithGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleLoginLauncher.launch(signInIntent)
        }
    }

    // Google login result handler
    private val googleLoginLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account)
                } catch (e: ApiException) {
                    Toast.makeText(this, "Google SignIn Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(uid)

                    dbRef.get().addOnSuccessListener { snapshot ->
                        if (!snapshot.exists()) {
                            // Pehli baar login, user create karo
                            val userMap = mapOf(
                                "email" to (account.email ?: ""),
                                "name" to (account.displayName ?: ""),
                                "role" to "user"
                            )
                            dbRef.setValue(userMap)
                                .addOnSuccessListener {
                                    checkUserRole(uid)
                                }
                        } else {
                            // User already exist, role check karo
                            checkUserRole(uid)
                        }
                    }
                } else {
                    Toast.makeText(this, "Google Login Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserRole(uid: String?) {
        if (uid == null) return
        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("role")
        dbRef.get().addOnSuccessListener { snapshot ->
            val role = snapshot.getValue(String::class.java)
            if (role == "user") {
                Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Access Denied! Role not user", Toast.LENGTH_SHORT).show()
                auth.signOut()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error checking role: ${it.message}", Toast.LENGTH_SHORT).show()
            auth.signOut()
        }
    }
}
