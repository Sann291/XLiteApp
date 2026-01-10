package com.example.xliteapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // -----------------------------------------------------------
        // 1. SETUP GOOGLE SIGN IN CLIENT
        // -----------------------------------------------------------
        // R.string.default_web_client_id itu otomatis dibuat oleh google-services.json
        // Kalau merah, coba Build -> Rebuild Project
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // -----------------------------------------------------------
        // 2. DEFINISI TOMBOL & INPUT (Sesuai ID di XML X Lite)
        // -----------------------------------------------------------
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoogle = findViewById<CardView>(R.id.btnGoogleLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        // -----------------------------------------------------------
        // 3. LOGIKA TOMBOL GOOGLE
        // -----------------------------------------------------------
        btnGoogle.setOnClickListener {
            signInGoogle()
        }

        // -----------------------------------------------------------
        // 4. LOGIKA TOMBOL LOGIN BIASA (EMAIL)
        // -----------------------------------------------------------
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Email & Password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Proses Login ke Firebase
            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                        goToHome()
                    } else {
                        Toast.makeText(this, "Gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // -----------------------------------------------------------
        // 5. LOGIKA REGISTER (DAFTAR AKUN BARU)
        // -----------------------------------------------------------
        tvRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Isi Email & Password baru untuk mendaftar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Buat Akun Baru
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Akun Berhasil Dibuat!", Toast.LENGTH_SHORT).show()
                        goToHome()
                    } else {
                        Toast.makeText(this, "Gagal Daftar: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    // --- FUNGSI MEMANGGIL GOOGLE ---
    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    // --- MENANGKAP HASIL LOGIN GOOGLE ---
    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- TUKAR TOKEN GOOGLE KE FIREBASE ---
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Masuk sebagai ${auth.currentUser?.email}", Toast.LENGTH_SHORT).show()
                    goToHome()
                } else {
                    Toast.makeText(this, "Gagal Autentikasi Firebase", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // --- PINDAH KE HALAMAN UTAMA ---
    private fun goToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    // --- CEK APAKAH SUDAH LOGIN SEBELUMNYA ---
    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            goToHome()
        }
    }
}