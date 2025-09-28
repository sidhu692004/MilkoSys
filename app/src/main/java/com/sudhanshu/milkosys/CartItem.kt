package com.sudhanshu.milkosys

data class CartItem(
    val productDocId: String = "",
    val productId: String = "",    // Firestore document ID of the product
    val productName: String = "",
    val dairyUid: String = "",
    val dairyName: String = "",
    var quantity: Int = 0,
    val price: Double = 0.0,
    var totalPrice: Double = 0.0
)
