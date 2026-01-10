package com.example.xliteapp

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton // Pastikan ini ada
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {

    private lateinit var chatList: ArrayList<ChatMessage>
    private lateinit var adapter: ChatAdapter
    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var dbRef: DatabaseReference

    // ID Unik Room Chat
    var chatRoomId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // 1. Terima Data dari Profil/Search
        val targetUid = intent.getStringExtra("TARGET_UID")
        val targetName = intent.getStringExtra("TARGET_NAME")
        val myUid = FirebaseAuth.getInstance().currentUser?.uid

        findViewById<TextView>(R.id.tvChatName).text = targetName ?: "Chat"

        // 2. LOGIKA ROOM ID
        if (myUid != null && targetUid != null) {
            // Urutkan ID biar A->B dan B->A ketemu di room yang sama
            chatRoomId = if (myUid < targetUid) {
                myUid + "_" + targetUid
            } else {
                targetUid + "_" + myUid
            }
        }

        // 3. Setup RecyclerView & Komponen
        rvChat = findViewById(R.id.rvChat)
        etMessage = findViewById(R.id.etChatMessage)

        // --- INI SOLUSI ERORNYA ---
        // Kita definisikan dulu btnSend itu barang yang mana di layout
        val btnSend = findViewById<ImageButton>(R.id.btnSendChat)
        // ---------------------------

        rvChat.layoutManager = LinearLayoutManager(this)
        chatList = arrayListOf()
        adapter = ChatAdapter(chatList)
        rvChat.adapter = adapter

        // 4. Logic Kirim Pesan
        // Sekarang btnSend sudah dikenali, jadi tidak akan merah lagi
        btnSend.setOnClickListener {
            val msg = etMessage.text.toString()
            if (msg.isNotEmpty() && chatRoomId != null) {
                val chatObj = ChatMessage(myUid, targetUid, msg, System.currentTimeMillis())

                // Masukkan ke Database
                FirebaseDatabase.getInstance().getReference("chats")
                    .child(chatRoomId!!).push().setValue(chatObj)

                etMessage.setText("") // Kosongkan kolom ketik
            }
        }

        // 5. Logic Terima Pesan (Realtime)
        readMessages()
    }

    private fun readMessages() {
        if (chatRoomId == null) return

        dbRef = FirebaseDatabase.getInstance().getReference("chats").child(chatRoomId!!)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (snap in snapshot.children) {
                    val chat = snap.getValue(ChatMessage::class.java)
                    if (chat != null) chatList.add(chat)
                }
                adapter.notifyDataSetChanged()
                // Scroll ke bawah otomatis pas ada pesan baru
                if (chatList.isNotEmpty()) {
                    rvChat.scrollToPosition(chatList.size - 1)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}