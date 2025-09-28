package com.sudhanshu.milkosys

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProductListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private val productList = mutableListOf<ProductModel>()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var dairyUid: String
    private lateinit var dairyName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        recyclerView = findViewById(R.id.recyclerViewProducts)
        recyclerView.layoutManager = LinearLayoutManager(this)

        dairyUid = intent.getStringExtra("uid") ?: ""
        dairyName = intent.getStringExtra("dairyName") ?: ""

        adapter = ProductAdapter(productList) { product, qty ->
            addToCart(product, qty)
        }
        recyclerView.adapter = adapter

        fetchProducts()
    }

    private fun fetchProducts() {
        db.collection("users").document(dairyUid)
            .collection("DairyProducts")
            .get()
            .addOnSuccessListener { result ->
                productList.clear()
                for (doc in result) {
                    val product = doc.toObject(ProductModel::class.java)
                    product.productId = doc.id // ✅ docId save
                    productList.add(product)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch products", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addToCart(product: ProductModel, qty: Int) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val cartRef = db.collection("users").document(currentUserUid).collection("cart")

        cartRef
            .whereEqualTo("productId", product.productId)
            .whereEqualTo("dairyUid", dairyUid)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    for (doc in result) {
                        val oldQty = doc.getLong("quantity")?.toInt() ?: 0
                        val newQty = oldQty + qty
                        val newTotal = product.price * newQty

                        cartRef.document(doc.id).update(
                            mapOf(
                                "quantity" to newQty,
                                "totalPrice" to newTotal
                            )
                        ).addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Updated ${product.name} to $newQty items",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    val cartItem = hashMapOf(
                        "uid" to currentUserUid,
                        "productId" to product.productId,   // ✅ productId save
                        "productName" to product.name,
                        "price" to product.price,
                        "quantity" to qty,
                        "dairyName" to dairyName,
                        "dairyUid" to dairyUid,
                        "totalPrice" to product.price * qty
                    )

                    cartRef.add(cartItem)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Added $qty ${product.name} to cart",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error checking cart", Toast.LENGTH_SHORT).show()
            }
    }
}
