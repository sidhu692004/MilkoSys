package com.sudhanshu.milkosys

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sudhanshu.milkosys.fragments.HelpSupportFragment

class ProfileFragment : Fragment() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvMobile: TextView
    private lateinit var tvAddress: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnOrders: Button
    private lateinit var btnHelp: Button
    private lateinit var btnLogout: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val currentUserUid = auth.currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        tvName = view.findViewById(R.id.tvProfileName)
        tvEmail = view.findViewById(R.id.tvProfileEmail)
        tvMobile = view.findViewById(R.id.tvProfileMobile)
        tvAddress = view.findViewById(R.id.tvProfileAddress)
        btnEdit = view.findViewById(R.id.btnEditProfile)
        btnOrders = view.findViewById(R.id.btnMyOrders)
        btnHelp = view.findViewById(R.id.btnHelp)
        btnLogout = view.findViewById(R.id.btnLogout)

        // Load profile data from Firestore
        loadProfile()

        // Edit profile
        btnEdit.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Orders (you can implement UserOrdersActivity)
        btnOrders.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UserOrdersFragment()) // fragment_container tumhare main activity ka FrameLayout/FragmentContainerView id hoga
                .addToBackStack(null) // back press karne par profile wapas aayega
                .commit()
        }

        // Help / support
        btnHelp.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HelpSupportFragment()) // fragment_container tumhare main activity ka FrameLayout/FragmentContainerView id hoga
                .addToBackStack(null) // back press karne par profile wapas aayega
                .commit()        }

        // Logout
        btnLogout.setOnClickListener {
            val prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            val mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

            mGoogleSignInClient.revokeAccess().addOnCompleteListener {
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        return view
    }

    private fun loadProfile() {
        if (currentUserUid.isEmpty()) return

        db.collection("users").document(currentUserUid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val user = doc.toObject(UserProfile::class.java)
                    user?.let {
                        tvName.text = it.name
                        tvEmail.text = it.email
                        tvMobile.text = it.mobile
                        tvAddress.text = it.address
                    }
                } else {
                    Toast.makeText(requireContext(), "Profile not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        // Refresh profile when returning from EditProfileActivity
        loadProfile()
    }
}
