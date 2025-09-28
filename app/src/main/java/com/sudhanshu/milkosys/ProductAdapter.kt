package com.sudhanshu.milkosys

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(
    private val products: List<ProductModel>,
    private val onAddToCart: (ProductModel, Int) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val tvQty: TextView = itemView.findViewById(R.id.tvProductQuantity)
        val btnAdd: Button = itemView.findViewById(R.id.btnAddToCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        holder.tvName.text = product.name
        holder.tvPrice.text = "₹${product.price}"

        // Set product image based on product name
        val imageRes = when (product.name.lowercase()) {
            "milk" -> R.drawable.milk
            "paneer (पनीर)" -> R.drawable.paneer
            "curd (दही)" -> R.drawable.dahi
            "ghee (घी)" -> R.drawable.ghee
            "lassi / Butter Milk" -> R.drawable.lassi
            else -> R.drawable.ic_placeholder
        }
        holder.ivImage.setImageResource(imageRes)

        if (product.quantity <= 0) {
            holder.tvQty.text = "Out of Stock"
            holder.tvQty.setTextColor(Color.RED)
            holder.btnAdd.isEnabled = false
        } else {
            holder.tvQty.text = "Available: ${product.quantity}"
            holder.tvQty.setTextColor(Color.BLACK)
            holder.btnAdd.isEnabled = true

            holder.btnAdd.setOnClickListener {
                showQuantityDialog(holder.itemView.context, product)
            }
        }
    }

    override fun getItemCount() = products.size

    private fun showQuantityDialog(context: Context, product: ProductModel) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_quantity_picker, null)
        val numberPicker = dialogView.findViewById<NumberPicker>(R.id.numberPicker)

        numberPicker.minValue = 1
        numberPicker.maxValue = product.quantity
        numberPicker.value = 1

        AlertDialog.Builder(context)
            .setTitle("Select Quantity")
            .setView(dialogView)
            .setPositiveButton("Add to Cart") { _, _ ->
                val selectedQty = numberPicker.value
                onAddToCart(product, selectedQty)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
