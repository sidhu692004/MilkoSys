package com.sudhanshu.milkosys

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.Tasks
import com.razorpay.Checkout
import org.json.JSONObject

class CartFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var totalPriceText: TextView
    private lateinit var btnPurchase: Button
    private lateinit var adapter: CartAdapter

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserUid = auth.currentUser?.uid ?: ""

    private val cartItems = mutableListOf<CartItem>()
    private val cartDocIds = mutableListOf<String>()

    private var selectedPaymentMode = "COD"
    private var pendingName = ""
    private var pendingMobile = ""
    private var pendingAddress = ""

    override fun onCreateView(
        inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, savedInstanceState: Bundle?
    ): android.view.View? {
        val v = inflater.inflate(R.layout.fragment_cart, container, false)

        recyclerView = v.findViewById(R.id.recyclerViewCart)
        totalPriceText = v.findViewById(R.id.textTotalPrice)
        btnPurchase = v.findViewById(R.id.btnPurchase)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = CartAdapter(
            items = cartItems,
            onPlus = { pos -> changeQuantity(pos, +1) },
            onMinus = { pos -> changeQuantity(pos, -1) },
            onRemove = { pos -> removeItem(pos) }
        )
        recyclerView.adapter = adapter

        loadCart()

        btnPurchase.setOnClickListener { showUserInfoDialogAndPurchase() }

        return v
    }

    private fun loadCart() {
        if (currentUserUid.isEmpty()) return

        db.collection("users").document(currentUserUid).collection("cart")
            .get()
            .addOnSuccessListener { qs ->
                cartItems.clear()
                cartDocIds.clear()
                for (doc in qs) {
                    val item = doc.toObject(CartItem::class.java).copy(productDocId = doc.id)
                    cartItems.add(item)
                    cartDocIds.add(doc.id)
                }
                adapter.notifyDataSetChanged()
                updateTotal()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load cart", Toast.LENGTH_SHORT).show()
            }
    }

    private fun changeQuantity(position: Int, delta: Int) {
        if (position !in cartItems.indices) return
        val item = cartItems[position]
        val newQty = (item.quantity + delta).coerceAtLeast(1)
        if (newQty == item.quantity) return

        val docRef = db.collection("users").document(currentUserUid)
            .collection("cart").document(cartDocIds[position])

        val newTotal = item.price * newQty
        docRef.update("quantity", newQty, "totalPrice", newTotal)
            .addOnSuccessListener {
                item.quantity = newQty
                item.totalPrice = newTotal
                adapter.notifyItemChanged(position)
                updateTotal()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update quantity", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeItem(position: Int) {
        if (position !in cartItems.indices) return
        val docRef = db.collection("users").document(currentUserUid)
            .collection("cart").document(cartDocIds[position])

        docRef.delete()
            .addOnSuccessListener {
                cartItems.removeAt(position)
                cartDocIds.removeAt(position)
                adapter.notifyItemRemoved(position)
                updateTotal()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to remove item", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateTotal() {
        val total = cartItems.sumOf { it.price * it.quantity }
        totalPriceText.text = "Total: ₹$total"
    }

    private fun showUserInfoDialogAndPurchase() {
        if (cartItems.isEmpty()) {
            Toast.makeText(requireContext(), "Cart is empty", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_address, null)
        val etName: EditText = dialogView.findViewById(R.id.etNameDialog)
        val etMobile: EditText = dialogView.findViewById(R.id.etMobileDialog)
        val etAddress: EditText = dialogView.findViewById(R.id.etAddressDialog)
        val rbCOD: RadioButton = dialogView.findViewById(R.id.rbCOD)
        val rbOnline: RadioButton = dialogView.findViewById(R.id.rbOnline)

        rbCOD.setOnCheckedChangeListener { _, isChecked -> if (isChecked) selectedPaymentMode = "COD" }
        rbOnline.setOnCheckedChangeListener { _, isChecked -> if (isChecked) selectedPaymentMode = "Online" }

        AlertDialog.Builder(requireContext())
            .setTitle("Enter Your Details")
            .setView(dialogView)
            .setPositiveButton("Confirm") { dialog, _ ->
                val name = etName.text.toString().trim()
                val mobile = etMobile.text.toString().trim()
                val address = etAddress.text.toString().trim()

                if (name.isEmpty() || mobile.isEmpty() || address.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                } else {
                    pendingName = name
                    pendingMobile = mobile
                    pendingAddress = address

                    if (selectedPaymentMode == "COD") {
                        makePurchase(name, mobile, address, "Cash on Delivery", utr = null)
                    } else {
                        startRazorpayPayment(name, mobile, address)
                    }
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startRazorpayPayment(name: String, mobile: String, address: String) {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_RLPoM91Z4Pouah") // your Razorpay test key

        try {
            val options = JSONObject()
            val totalAmount = cartItems.sumOf { it.price * it.quantity } * 100 // in paise
            options.put("name", "MilkoSys Dairy")
            options.put("description", "Order Payment")
            options.put("currency", "INR")
            options.put("amount", totalAmount)

            val prefill = JSONObject()
            prefill.put("email", "test@example.com")
            prefill.put("contact", mobile)
            options.put("prefill", prefill)

            checkout.open(requireActivity(), options)

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error in payment: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // ✅ Called from HomeActivity after Razorpay success
    fun onRazorpayPaymentSuccess(paymentId: String) {
        makePurchase(
            pendingName,
            pendingMobile,
            pendingAddress,
            "Online Payment",
            utr = paymentId
        )
    }

    private fun makePurchase(name: String, mobile: String, address: String, paymentMode: String, utr: String?) {
        if (currentUserUid.isEmpty() || cartItems.isEmpty()) return

        val stockTasks = cartItems.map { item ->
            db.collection("users").document(item.dairyUid)
                .collection("DairyProducts").document(item.productId)
                .get()
                .continueWith { task ->
                    val doc = task.result
                    doc != null && doc.exists() && (doc.getLong("quantity") ?: 0L) >= item.quantity
                }
        }

        Tasks.whenAllComplete(stockTasks).addOnSuccessListener { results ->
            val allAvailable = results.all { (it.result as? Boolean) == true }
            if (!allAvailable) {
                Toast.makeText(requireContext(), "Some items are out of stock", Toast.LENGTH_LONG).show()
                return@addOnSuccessListener
            }

            val orderRef = db.collection("users").document(currentUserUid)
                .collection("orders")

            val orderMap = hashMapOf(
                "uid" to currentUserUid,
                "name" to name,
                "mobile" to mobile,
                "address" to address,
                "paymentMode" to paymentMode,
                "utrNumber" to (utr ?: ""),
                "timestamp" to FieldValue.serverTimestamp(),
                "totalPrice" to cartItems.sumOf { it.price * it.quantity },
                "status" to "Order Confirmed",
                "items" to cartItems.map { ci ->
                    mapOf(
                        "productId" to ci.productId,
                        "productName" to ci.productName,
                        "price" to ci.price,
                        "quantity" to ci.quantity,
                        "lineTotal" to ci.price * ci.quantity,
                        "dairyName" to ci.dairyName,
                        "dairyUid" to ci.dairyUid
                    )
                }
            )

            orderRef.add(orderMap).addOnSuccessListener {
                cartItems.forEach { ci ->
                    val prodRef = db.collection("users").document(ci.dairyUid)
                        .collection("DairyProducts").document(ci.productId)

                    prodRef.get().addOnSuccessListener { snap ->
                        val currentQty = (snap.getLong("quantity") ?: 0L).toInt()
                        prodRef.update("quantity", (currentQty - ci.quantity).coerceAtLeast(0))
                    }
                }
                clearCart()
                Toast.makeText(requireContext(), "Order placed successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to place order", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearCart() {
        val batch = db.batch()
        val cartRef = db.collection("users").document(currentUserUid).collection("cart")
        cartRef.get().addOnSuccessListener { qs ->
            for (doc in qs) batch.delete(doc.reference)
            batch.commit().addOnSuccessListener {
                cartItems.clear()
                cartDocIds.clear()
                adapter.notifyDataSetChanged()
                updateTotal()
            }
        }
    }
}
