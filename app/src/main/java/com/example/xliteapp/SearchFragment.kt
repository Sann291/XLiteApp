package com.example.xliteapp

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
import com.google.firebase.database.*

class SearchFragment : Fragment() {

    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: UserAdapter
    private lateinit var dbRef: DatabaseReference
    private lateinit var rvSearch: RecyclerView // Simpan variabel ini di level class biar bisa diakses fungsi filter

    // Langkah 1: Siapkan Tampilan
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Sambungkan dengan fragment_search.xml
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    // Langkah 2: Jalankan Logika
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        rvSearch = view.findViewById(R.id.rvSearch) // Inisialisasi variabel global

        // Perhatikan: 'this' diganti 'requireContext()' kalau di Fragment
        rvSearch.layoutManager = LinearLayoutManager(requireContext())

        userList = arrayListOf()
        adapter = UserAdapter(userList)
        rvSearch.adapter = adapter

        dbRef = FirebaseDatabase.getInstance().getReference("users")

        fetchAllUsers()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun fetchAllUsers() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        // Pastikan logika ambil data ini sesuai struktur DB kamu
                        val displayName = snap.child("displayName").value?.toString() ?: "No Name"
                        val email = snap.child("email").value?.toString() ?: "-"
                        val bio = snap.child("bio").value?.toString() ?: ""

                        // Buat object User
                        val user = User(snap.key, displayName, email, bio)
                        userList.add(user)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun filter(text: String) {
        val filteredList = ArrayList<User>()
        for (user in userList) {
            val name = user.displayName ?: ""
            if (name.lowercase().contains(text.lowercase())) {
                filteredList.add(user)
            }
        }
        // Update adapter
        adapter = UserAdapter(filteredList)
        rvSearch.adapter = adapter
    }
}