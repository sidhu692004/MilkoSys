
package com.sudhanshu.milkosys

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserOrderDetailAdapter(private val items: List<OrderItem>) :
    RecyclerView.Adapter<UserOrderDetailAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvItemName)
        val tvQty: TextView = v.findViewById(R.id.tvItemQty)
        val tvPrice: TextView = v.findViewById(R.id.tvItemPrice)
        val tvLineTotal: TextView = v.findViewById(R.id.tvItemLineTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_order_item, parent, false)
        return ItemViewHolder(v)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.productName
        holder.tvQty.text = "Qty: ${item.quantity}"
        holder.tvPrice.text = "₹${item.price}"
        holder.tvLineTotal.text = "Total: ₹${item.lineTotal}"
    }

    override fun getItemCount(): Int = items.size
}
