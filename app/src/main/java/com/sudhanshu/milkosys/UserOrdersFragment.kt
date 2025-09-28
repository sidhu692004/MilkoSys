package com.sudhanshu.milkosys

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserOrdersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserOrderAdapter
    private val orderList = mutableListOf<UserOrderModel>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userUid = auth.currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_orders, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewUserOrders)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = UserOrderAdapter(orderList) { selectedOrder ->
            val intent = Intent(requireContext(), UserOrderDetailActivity::class.java)
            intent.putExtra("orderId", selectedOrder.orderId)
            intent.putExtra("userUid", userUid)
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        fetchOrders()

        return view
    }

    private fun fetchOrders() {
        if (userUid.isEmpty()) return

        db.collection("users")
            .document(userUid)
            .collection("orders")
            .orderBy("timestamp") // latest order last
            .get()
            .addOnSuccessListener { qs ->
                orderList.clear()
                for (doc in qs) {
                    val order = doc.toObject(UserOrderModel::class.java)
                    order.orderId = doc.id
                    orderList.add(order)
                }
                if (orderList.isEmpty()) {
                    Toast.makeText(requireContext(), "No orders found", Toast.LENGTH_SHORT).show()
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch orders", Toast.LENGTH_SHORT).show()
            }
    }
}
