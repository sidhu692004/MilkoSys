package com.sudhanshu.milkosys

data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val price: Int = 0,
    val quantity: Int = 0,
    val lineTotal: Int = 0,
    val dairyName: String = "",
    val dairyUid: String = ""
)
