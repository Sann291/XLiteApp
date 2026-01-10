package com.example.xliteapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tweetList: ArrayList<Tweet>
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference("tweets")

        // 2. Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        tweetList = arrayListOf()

        // 3. Ambil Data dari Firebase (Logic read tetap di sini)
        getTweetData()

        // --- SETUP TAMPILAN BARU ---

        // A. Tombol Logout
        findViewById<View>(R.id.btnLogoutIcon).setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // B. Setup Profil Icon
        findViewById<View>(R.id.profileIcon).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // C. Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    true
                }
                R.id.nav_notif -> {
                    startActivity(Intent(this, NotificationActivity::class.java))
                    true
                }
                R.id.nav_chat -> {
                    Toast.makeText(this, "Cari teman untuk mulai Chat", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, SearchActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // D. Tombol Tambah Tweet (FAB) - BAGIAN YANG DIUBAH
        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            // Sekarang tombol ini membuka halaman PostActivity
            val intent = Intent(this, PostActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getTweetData() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tweetList.clear()
                if (snapshot.exists()) {
                    for (tweetSnap in snapshot.children) {
                        val tweetData = tweetSnap.getValue(Tweet::class.java)
                        if (tweetData != null) {
                            tweetList.add(tweetData)
                        }
                    }
                    // Balik urutan (Tweet terbaru di atas)
                    tweetList.reverse()
                    recyclerView.adapter = TweetAdapter(tweetList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}