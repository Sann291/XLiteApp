package com.example.xliteapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {

    // --- Deklarasi Variabel UI ---
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    private lateinit var tvName: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvBio: TextView
    private lateinit var tvInitial: TextView
    private lateinit var ivProfileImage: ImageView
    private lateinit var ivBanner: ImageView

    private lateinit var btnEditProfile: TextView
    private lateinit var btnMessage: TextView
    private lateinit var btnBack: ImageView

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile) // Pastikan layout XML sudah yang baru

        // 1. Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        dbRef = FirebaseDatabase.getInstance().getReference("users")

        // 2. Sambungkan ID dari XML (findViewById)
        tvName = findViewById(R.id.tvNameProfile)
        tvUsername = findViewById(R.id.tvUsernameProfile)
        tvBio = findViewById(R.id.tvBio)
        tvInitial = findViewById(R.id.tvInitial)
        ivProfileImage = findViewById(R.id.ivProfileImage) // Pastikan ID di XML ivProfileImage
        ivBanner = findViewById(R.id.ivBanner)

        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnMessage = findViewById(R.id.btnMessage)
        btnBack = findViewById(R.id.btnBack)

        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)

        // 3. Logika Cek User (Sendiri vs Orang Lain)
        val targetUid = intent.getStringExtra("TARGET_UID") ?: intent.getStringExtra("USER_ID")
        val uidToShow = targetUid ?: currentUser?.uid

        if (uidToShow == null) {
            Toast.makeText(this, "User error", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 4. Tombol Back
        btnBack.setOnClickListener { finish() }

        // 5. Atur Tombol Edit / Message
        if (uidToShow == currentUser?.uid) {
            // Profil Sendiri
            btnEditProfile.visibility = View.VISIBLE
            btnMessage.visibility = View.GONE

            btnEditProfile.setOnClickListener {
                startActivity(Intent(this, EditProfileActivity::class.java))
            }
        } else {
            // Profil Orang Lain
            btnEditProfile.visibility = View.GONE
            btnMessage.visibility = View.VISIBLE

            btnMessage.setOnClickListener {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("TARGET_UID", uidToShow)
                intent.putExtra("TARGET_NAME", tvName.text.toString())
                startActivity(intent)
            }
        }

        // 6. Load Data Profil
        loadProfileData(uidToShow)

        // 7. SETUP TABS & VIEWPAGER (Tweets, Replies, Media)
        val adapter = ProfilePagerAdapter(supportFragmentManager, lifecycle, uidToShow)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Tweets"
                1 -> "Replies"
                2 -> "Media"
                else -> "Tweets"
            }
        }.attach()
    }

    private fun loadProfileData(uid: String) {
        dbRef.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("displayName").value.toString()
                    val bio = snapshot.child("bio").value.toString()
                    val email = snapshot.child("email").value.toString()
                    val profileImageUrl = snapshot.child("profileImageUrl").value.toString()

                    // Update UI Teks
                    if (name != "null" && name.isNotEmpty()) tvName.text = name
                    if (bio != "null" && bio.isNotEmpty()) tvBio.text = bio

                    if (email != "null" && email.isNotEmpty()) {
                        val username = email.split("@")[0]
                        tvUsername.text = "@${username.lowercase()}"
                        tvInitial.text = username.first().uppercase()
                    }

                    // Update Foto Profil (Pakai Glide)
                    try {
                        if (profileImageUrl != "null" && profileImageUrl.isNotEmpty()) {
                            tvInitial.visibility = View.GONE
                            Glide.with(this@ProfileActivity)
                                .load(profileImageUrl)
                                .circleCrop()
                                .into(ivProfileImage)
                        } else {
                            ivProfileImage.setImageResource(R.drawable.circle_gray)
                            tvInitial.visibility = View.VISIBLE
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}