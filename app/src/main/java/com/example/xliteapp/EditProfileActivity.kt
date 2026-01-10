package com.example.xliteapp

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class EditProfileActivity : AppCompatActivity() {

    // Deklarasi Variabel UI
    private lateinit var ivProfile: ImageView
    private lateinit var etName: EditText
    private lateinit var etBio: EditText
    private lateinit var btnSave: Button

    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val dbRef = FirebaseDatabase.getInstance().getReference("users")
    private val storageRef = FirebaseStorage.getInstance().reference

    // Variabel untuk menampung gambar yang dipilih
    private var selectedImageUri: Uri? = null

    // 1. SIAPKAN PELUNCUR GALERI
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            // Tampilkan langsung di layar biar user lihat preview-nya
            Glide.with(this).load(uri).circleCrop().into(ivProfile)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // 2. SAMBUNGKAN ID (Sesuai XML kamu)
        ivProfile = findViewById(R.id.ivEditProfileImage) // Pastikan ID ini ada di XML
        etName = findViewById(R.id.etEditName)
        etBio = findViewById(R.id.etEditBio)
        btnSave = findViewById(R.id.btnSaveProfile)

        // 3. LOAD DATA LAMA (Biar kolom gak kosong pas dibuka)
        loadCurrentData()

        // 4. KLIK GAMBAR -> BUKA GALERI
        ivProfile.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // 5. KLIK SAVE
        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val bio = etBio.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Nama wajib diisi"
                return@setOnClickListener
            }

            // Logika Penyimpanan:
            if (selectedImageUri != null) {
                // KASUS A: User Ganti Foto -> Upload dulu, baru simpan data
                uploadImageAndSave(name, bio)
            } else {
                // KASUS B: User Cuma Ganti Teks -> Langsung simpan data
                saveDataToDatabase(name, bio, null)
            }
        }
    }

    private fun loadCurrentData() {
        val uid = auth.currentUser?.uid ?: return
        dbRef.child(uid).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val name = snapshot.child("displayName").value.toString()
                val bio = snapshot.child("bio").value.toString()
                val imgUrl = snapshot.child("profileImageUrl").value.toString()

                if (name != "null") etName.setText(name)
                if (bio != "null") etBio.setText(bio)

                // Load gambar lama pakai Glide
                if (imgUrl != "null" && imgUrl.isNotEmpty()) {
                    Glide.with(this).load(imgUrl).circleCrop().into(ivProfile)
                }
            }
        }
    }

    private fun uploadImageAndSave(name: String, bio: String) {
        val uid = auth.currentUser?.uid ?: return
        val imageRef = storageRef.child("profile_images/$uid.jpg")

        Toast.makeText(this, "Mengupload foto...", Toast.LENGTH_SHORT).show()
        btnSave.isEnabled = false // Matikan tombol biar gak dipencet berkali-kali

        // Upload ke Firebase Storage
        imageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                // Sukses Upload -> Ambil Link Download (URL)
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveDataToDatabase(name, bio, uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal upload gambar", Toast.LENGTH_SHORT).show()
                btnSave.isEnabled = true
            }
    }

    private fun saveDataToDatabase(name: String, bio: String, imageUrl: String?) {
        val currentUser = auth.currentUser ?: return

        // Siapkan data yang mau diupdate
        val updates = mutableMapOf<String, Any>(
            "displayName" to name,
            "bio" to bio,
            "email" to (currentUser.email ?: "")
        )

        // Kalau ada URL gambar baru, tambahkan ke update
        if (imageUrl != null) {
            updates["profileImageUrl"] = imageUrl
        }

        // Simpan ke Database
        dbRef.child(currentUser.uid).updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profil berhasil diupdate!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal update database", Toast.LENGTH_SHORT).show()
                btnSave.isEnabled = true
            }
    }
}