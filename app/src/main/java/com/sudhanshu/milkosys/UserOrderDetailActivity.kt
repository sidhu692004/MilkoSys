package com.sudhanshu.milkosys

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class UserOrderDetailActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvCustomer: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvMobile: TextView
    private lateinit var tvTotal: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvPayment: TextView
    private lateinit var tvTime: TextView
    private lateinit var adapter: UserOrderDetailAdapter

    private val db = FirebaseFirestore.getInstance()
    private val itemList = mutableListOf<OrderItem>()
    private var orderId: String? = null
    private var userUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_order_detail)

        recyclerView = findViewById(R.id.recyclerViewUserOrderDetail)
        tvCustomer = findViewById(R.id.tvUserDetailCustomer)
        tvAddress = findViewById(R.id.tvUserDetailAddress)
        tvMobile = findViewById(R.id.tvUserDetailMobile)
        tvTotal = findViewById(R.id.tvUserDetailTotal)
        tvStatus = findViewById(R.id.tvUserDetailStatus)
        tvPayment = findViewById(R.id.tvUserDetailPayment)
        tvTime = findViewById(R.id.tvUserDetailTime)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UserOrderDetailAdapter(itemList)
        recyclerView.adapter = adapter

        orderId = intent.getStringExtra("orderId")
        userUid = intent.getStringExtra("userUid")

        fetchOrderDetail()
    }

    private fun fetchOrderDetail() {
        if (orderId.isNullOrEmpty() || userUid.isNullOrEmpty()) return

        db.collection("users")
            .document(userUid!!)
            .collection("orders")
            .document(orderId!!)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val order = doc.toObject(UserOrderModel::class.java)
                    order?.let {
                        tvCustomer.text = it.name
                        tvAddress.text = it.address
                        tvMobile.text = it.mobile
                        tvTotal.text = "₹${it.totalPrice}"
                        tvStatus.text = it.status
                        tvPayment.text = it.paymentMode
                        tvTime.text = it.timestamp?.toDate().toString()

                        itemList.clear()
                        itemList.addAll(it.items)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch order detail", Toast.LENGTH_SHORT).show()
            }
    }
}
