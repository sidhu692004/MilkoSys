package com.sudhanshu.milkosys

import com.google.firebase.Timestamp

data class UserOrderModel(
    var orderId: String = "",
    val uid: String = "",
    val name: String = "",
    val mobile: String = "",
    val address: String = "",
    val paymentMode: String = "",
    val status: String = "",
    val totalPrice: Int = 0,
    val timestamp: Timestamp? = null,
    val items: List<OrderItem> = emptyList()
)