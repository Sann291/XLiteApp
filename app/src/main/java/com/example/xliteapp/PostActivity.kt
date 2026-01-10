package com.example.xliteapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PostActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var etContent: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference("tweets")

        val btnClose = findViewById<ImageButton>(R.id.btnClose)
        val btnPost = findViewById<Button>(R.id.btnPost)
        etContent = findViewById(R.id.etPostContent)

        btnClose.setOnClickListener { finish() }

        btnPost.setOnClickListener {
            val isiPostingan = etContent.text.toString().trim()
            if (isiPostingan.isNotEmpty()) {
                saveTweetToFirebase(isiPostingan)
            } else {
                Toast.makeText(this, "Tulisan tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveTweetToFirebase(content: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) return

        val tweetId = dbRef.push().key ?: return
        val username = currentUser.email?.split("@")?.get(0) ?: "Anonymous"
        val uid = currentUser.uid

        val tweet = Tweet(
            id = tweetId,
            uid = uid,
            username = username,
            content = content,
            timestamp = System.currentTimeMillis(),
            likeCount = 0,
            replyCount = 0,   // <-- Tambahan baru
            retweetCount = 0  // <-- Tambahan baru
        )

        dbRef.child(tweetId).setValue(tweet)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 1. Munculkan Notifikasi di Status Bar (SYARAT UJIAN)
                    showNotification("Tweet Terkirim", "Isi: $content")

                    Toast.makeText(this, "Berhasil!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // --- FUNGSI NOTIFIKASI ---
    private fun showNotification(title: String, message: String) {
        val channelId = "tweet_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Untuk Android Oreo ke atas, wajib buat Channel dulu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Tweet Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_edit) // Pastikan ikon ini ada (atau ganti ikon lain)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(1, builder.build())
    }
}