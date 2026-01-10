package com.example.xliteapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(private val chatList: ArrayList<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    val myUid = FirebaseAuth.getInstance().currentUser?.uid

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRight: TextView = itemView.findViewById(R.id.tvRightChat)
        val tvLeft: TextView = itemView.findViewById(R.id.tvLeftChat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]

        if (chat.senderId == myUid) {
            // Ini pesan SAYA -> Tampilkan Kanan, Sembunyikan Kiri
            holder.tvRight.visibility = View.VISIBLE
            holder.tvLeft.visibility = View.GONE
            holder.tvRight.text = chat.message
        } else {
            // Ini pesan TEMAN -> Tampilkan Kiri, Sembunyikan Kanan
            holder.tvRight.visibility = View.GONE
            holder.tvLeft.visibility = View.VISIBLE
            holder.tvLeft.text = chat.message
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }
}