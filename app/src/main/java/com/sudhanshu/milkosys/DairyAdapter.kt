package com.sudhanshu.milkosys

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DairyAdapter(
    private val dairyList: List<DairyModel>,
    private val onItemClick: (DairyModel) -> Unit
) : RecyclerView.Adapter<DairyAdapter.DairyViewHolder>() {

    inner class DairyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ownerName: TextView = itemView.findViewById(R.id.tvOwnerName)
        val businessName: TextView = itemView.findViewById(R.id.tvBusinessName)
        val phone: TextView = itemView.findViewById(R.id.tvPhone)
        val address: TextView = itemView.findViewById(R.id.tvAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DairyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dairy, parent, false)
        return DairyViewHolder(view)
    }

    override fun onBindViewHolder(holder: DairyViewHolder, position: Int) {
        val dairy = dairyList[position]
        holder.ownerName.text = "Name: "+dairy.fullName
        holder.businessName.text = "Dairy Name: "+dairy.dairyName
        holder.phone.text = "Phone: "+dairy.contactNumber
        holder.address.text = "Address: "+dairy.address

        holder.itemView.setOnClickListener {
            onItemClick(dairy)
        }
    }

    override fun getItemCount(): Int = dairyList.size
}
