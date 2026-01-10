package com.example.xliteapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NotificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification) // Buat layout activity_notification.xml isinya cuma RecyclerView

        val rv = findViewById<RecyclerView>(R.id.rvNotification)
        rv.layoutManager = LinearLayoutManager(this)

        // DATA DUMMY TAPI KELIHATAN ASLI
        // (Biar gak ribet setup database notifikasi yang kompleks)
        val notifList = arrayListOf<Notification>()

        notifList.add(Notification("System", "Selamat datang di X Lite App! Mulailah membuat tweet pertamamu.", System.currentTimeMillis()))
        notifList.add(Notification("Update", "Fitur Chat sekarang sudah tersedia. Coba kirim pesan ke temanmu!", System.currentTimeMillis()))
        notifList.add(Notification("Security", "Login berhasil dari perangkat Android.", System.currentTimeMillis()))

        val adapter = NotificationAdapter(notifList)
        rv.adapter = adapter
    }
}