package com.sudhanshu.milkosys

data class DairyModel(
    val uid: String = "",
    val fullName: String = "",
    val dairyName: String = "",
    val contactNumber: String = "",
    val address: String = "",
    var imageUrl: String? = null,  // add this
    var pinCode: String? = null,   // add this
    var email: String? = null
)
