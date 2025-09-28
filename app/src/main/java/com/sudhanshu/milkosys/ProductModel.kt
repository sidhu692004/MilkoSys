package com.sudhanshu.milkosys

data class ProductModel(
    var productId: String = "",
    var productName: String = "",
    var rate: Int = 0,
    var quantity: Int = 0,
    var date: String = ""
) {
    // Helper to map old code (price vs rate)
    val name: String
        get() = productName

    val price: Int
        get() = rate
}
