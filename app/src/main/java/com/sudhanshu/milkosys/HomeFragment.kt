package com.sudhanshu.milkosys

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DairyAdapter
    private lateinit var etSearch: EditText

    private val dairyList = mutableListOf<DairyModel>()
    private val filteredList = mutableListOf<DairyModel>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        etSearch = view.findViewById(R.id.etSearch)
        recyclerView = view.findViewById(R.id.recyclerViewDairy)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = DairyAdapter(filteredList) { dairy ->
            val intent = Intent(requireContext(), ProductListActivity::class.java)
            intent.putExtra("uid", dairy.uid)
            intent.putExtra("dairyName", dairy.dairyName)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        fetchDairies()
        setupSearch()

        return view
    }

    private fun fetchDairies() {
        db.collection("dairyProfiles")
            .get()
            .addOnSuccessListener { result ->
                dairyList.clear()
                for (doc in result) {
                    val dairy = doc.toObject(DairyModel::class.java)
                    dairyList.add(dairy)
                }
                // initially show all data
                filteredList.clear()
                filteredList.addAll(dairyList)
                adapter.notifyDataSetChanged()
            }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterList(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun filterList(query: String) {
        val lowerCaseQuery = query.lowercase()
        filteredList.clear()

        if (lowerCaseQuery.isEmpty()) {
            filteredList.addAll(dairyList)
        } else {
            for (dairy in dairyList) {
                if (dairy.fullName.lowercase().contains(lowerCaseQuery) ||
                    dairy.dairyName.lowercase().contains(lowerCaseQuery) ||
                    dairy.address.lowercase().contains(lowerCaseQuery)
                ) {
                    filteredList.add(dairy)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }
}
