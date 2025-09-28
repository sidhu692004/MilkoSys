package com.sudhanshu.milkosys

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CartAdapter(
    private val items: MutableList<CartItem>,
    private val onPlus: (position: Int) -> Unit,
    private val onMinus: (position: Int) -> Unit,
    private val onRemove: (position: Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartVH>() {

    inner class CartVH(v: View) : RecyclerView.ViewHolder(v) {
        val ivImage: ImageView = v.findViewById(R.id.ivCartProduct) // new ImageView
        val tvName: TextView = v.findViewById(R.id.tvCartName)
        val tvPrice: TextView = v.findViewById(R.id.tvCartPrice)
        val tvQty: TextView = v.findViewById(R.id.tvCartQuantity)
        val tvDairy: TextView = v.findViewById(R.id.tvCartDairy)
        val btnPlus: ImageButton = v.findViewById(R.id.btnPlus)
        val btnMinus: ImageButton = v.findViewById(R.id.btnMinus)
        val btnRemove: ImageButton = v.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartVH(view)
    }

    override fun onBindViewHolder(holder: CartVH, position: Int) {
        val item = items[position]

        // Set product image based on product name
        val imageRes = when (item.productName.lowercase()) {
            "milk" -> R.drawable.milk
            "paneer (पनीर)" -> R.drawable.paneer
            "curd (दही)" -> R.drawable.dahi
            "ghee (घी)" -> R.drawable.ghee
            "lassi / butter milk" -> R.drawable.lassi
            else -> R.drawable.ic_placeholder
        }
        holder.ivImage.setImageResource(imageRes)

        holder.tvName.text = item.productName
        holder.tvQty.text = "Qty: ${item.quantity}"
        holder.tvPrice.text = "₹${(item.price * item.quantity)}"
        holder.tvDairy.text = "From: ${item.dairyName}"

        holder.btnPlus.setOnClickListener { onPlus(holder.bindingAdapterPosition) }
        holder.btnMinus.setOnClickListener { onMinus(holder.bindingAdapterPosition) }
        holder.btnRemove.setOnClickListener { onRemove(holder.bindingAdapterPosition) }
    }

    override fun getItemCount(): Int = items.size
}
