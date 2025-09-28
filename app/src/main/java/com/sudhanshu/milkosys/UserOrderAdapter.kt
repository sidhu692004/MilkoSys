package com.sudhanshu.milkosys

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class UserOrderAdapter(
    private val orders: List<UserOrderModel>,
    private val onOrderClick: (UserOrderModel) -> Unit
) : RecyclerView.Adapter<UserOrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)
        val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)
        val tvOrderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        val tvOrderTotal: TextView = itemView.findViewById(R.id.tvOrderTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        holder.tvOrderId.text = "Order ID: ${order.orderId}"
        holder.tvOrderStatus.text = "Status: ${order.status}"
        holder.tvOrderTotal.text = "Total: ₹${order.totalPrice}"

        // format timestamp
        val dateStr = order.timestamp?.toDate()?.let {
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            sdf.format(it)
        } ?: "Unknown"

        holder.tvOrderDate.text = "Date: $dateStr"

        holder.itemView.setOnClickListener {
            onOrderClick(order)
        }
    }

    override fun getItemCount(): Int = orders.size
}
