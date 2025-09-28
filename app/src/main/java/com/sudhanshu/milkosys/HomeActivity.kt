package com.sudhanshu.milkosys

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.razorpay.PaymentResultListener

class HomeActivity : AppCompatActivity(), PaymentResultListener {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            // User not logged in → redirect to MainActivity (login)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // User logged in → show HomeActivity content
        setContentView(R.layout.activity_home)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Load default fragment
        loadFragment(HomeFragment(), "HomeFragmentTag")

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment(), "HomeFragmentTag")
                R.id.nav_cart -> loadFragment(CartFragment(), "CartFragmentTag")
                R.id.nav_profile -> loadFragment(ProfileFragment(), "ProfileFragmentTag")
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .commit()
    }

    // ✅ Razorpay Payment Success Callback
    override fun onPaymentSuccess(paymentId: String?) {
        if (paymentId != null) {
            val fragment = supportFragmentManager.findFragmentByTag("CartFragmentTag")
            if (fragment is CartFragment) {
                fragment.onRazorpayPaymentSuccess(paymentId)
            }
            Toast.makeText(this, "Payment Success: $paymentId", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ Razorpay Payment Error Callback
    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment Failed: $response", Toast.LENGTH_LONG).show()
    }
}
