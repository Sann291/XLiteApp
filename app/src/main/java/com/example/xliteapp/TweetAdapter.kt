package com.example.xliteapp

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class TweetAdapter(private val tweetList: ArrayList<Tweet>) :
    RecyclerView.Adapter<TweetAdapter.TweetViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private val dbRef = FirebaseDatabase.getInstance().getReference("tweets")

    class TweetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNameDisplay: TextView = itemView.findViewById(R.id.tvNameDisplay)
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)

        // Edit & Delete
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
        val btnEdit: ImageView = itemView.findViewById(R.id.btnEdit)

        // Like Area (Perhatikan tipe datanya berubah jadi ImageView)
        val btnLikeArea: LinearLayout = itemView.findViewById(R.id.btnLikeArea)
        val ivLikeIcon: ImageView = itemView.findViewById(R.id.btnLikeIcon) // Ganti TextView jadi ImageView
        val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)

        // Reply Area
        val btnReplyArea: LinearLayout = itemView.findViewById(R.id.btnReplyArea) // Area untuk diklik
        val tvReplyCount: TextView = itemView.findViewById(R.id.tvReplyCount)     // Text untuk angkanya

        // Retweet Area
        val btnRetweetArea: LinearLayout = itemView.findViewById(R.id.btnRetweetArea) // Area untuk diklik
        val tvRetweetCount: TextView = itemView.findViewById(R.id.tvRetweetCount)     // Text untuk angkanya

        // Share Area
        val btnShareArea: LinearLayout = itemView.findViewById(R.id.btnShareArea)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tweet, parent, false)
        return TweetViewHolder(view)
    }

    override fun onBindViewHolder(holder: TweetViewHolder, position: Int) {
        val currentTweet = tweetList[position]
        val tweetId = currentTweet.id

        // --- TAMPILAN DATA UTAMA ---
        holder.tvNameDisplay.text = (currentTweet.username ?: "User").replaceFirstChar { it.uppercase() }
        holder.tvUsername.text = "@${currentTweet.username?.lowercase()}"
        holder.tvContent.text = currentTweet.content

        // --- TAMPILKAN JUMLAH COUNTER ---
        holder.tvLikeCount.text = currentTweet.likeCount.toString()
        holder.tvReplyCount.text = currentTweet.replyCount.toString()       // Ubah btnReply jadi tvReplyCount
        holder.tvRetweetCount.text = currentTweet.retweetCount.toString()   // Ubah btnRetweet jadi tvRetweetCount

        // --- 1. LOGIKA WAKTU ---
        val timestamp = currentTweet.timestamp ?: 0L
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        holder.tvTime.text = when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "${minutes}m"
            hours < 24 -> "${hours}h"
            else -> "${days}d"
        }

        // --- 2. LOGIKA EDIT & DELETE ---
        if (currentTweet.uid == currentUserId) {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnEdit.visibility = View.VISIBLE

            holder.btnDelete.setOnClickListener {
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Hapus Tweet?")
                    .setMessage("Yakin mau hapus tweet ini?")
                    .setPositiveButton("Hapus") { _, _ ->
                        if (tweetId != null) dbRef.child(tweetId).removeValue()
                    }
                    .setNegativeButton("Batal", null).show()
            }

            holder.btnEdit.setOnClickListener {
                val context = holder.itemView.context
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Edit Tweet")
                val input = EditText(context)
                input.setText(currentTweet.content)
                builder.setView(input)

                builder.setPositiveButton("Update") { _, _ ->
                    val textBaru = input.text.toString()
                    if (tweetId != null && textBaru.isNotEmpty()) {
                        dbRef.child(tweetId).child("content").setValue(textBaru)
                        Toast.makeText(context, "Tweet diupdate!", Toast.LENGTH_SHORT).show()
                    }
                }
                builder.setNegativeButton("Batal", null).show()
            }
        } else {
            holder.btnDelete.visibility = View.GONE
            holder.btnEdit.visibility = View.GONE
        }

        // --- 3. LOGIKA LIKE (Counter +1 / -1 & Ganti Icon) ---
        var isLikedLocal = false // Nanti idealnya status ini diambil dari database

        // Set tampilan awal (Default belum di-like/Outline)
        holder.ivLikeIcon.setImageResource(R.drawable.ic_like)
        holder.ivLikeIcon.clearColorFilter() // Hapus filter warna sisa
        holder.tvLikeCount.setTextColor(Color.parseColor("#536471"))

        holder.btnLikeArea.setOnClickListener {
            if (tweetId != null) {
                if (!isLikedLocal) {
                    // --- KONDISI SAAT TOMBOL DI-KLIK (JADI LIKE) ---
                    val newCount = currentTweet.likeCount + 1
                    dbRef.child(tweetId).child("likeCount").setValue(newCount)

                    // 1. Ganti Icon jadi yang Penuh (Filled)
                    holder.ivLikeIcon.setImageResource(R.drawable.ic_like_filled)

                    // 2. Beri Warna Merah/Pink Twitter
                    holder.ivLikeIcon.setColorFilter(Color.parseColor("#F91880"))
                    holder.tvLikeCount.setTextColor(Color.parseColor("#F91880"))

                    holder.tvLikeCount.text = newCount.toString()
                    currentTweet.likeCount = newCount
                    isLikedLocal = true

                } else {
                    // --- KONDISI SAAT TOMBOL DI-KLIK LAGI (JADI UNLIKE) ---
                    val newCount = if (currentTweet.likeCount > 0) currentTweet.likeCount - 1 else 0
                    dbRef.child(tweetId).child("likeCount").setValue(newCount)

                    // 1. Ganti Icon balik ke Garis (Outline)
                    holder.ivLikeIcon.setImageResource(R.drawable.ic_like)

                    // 2. Ubah warna jadi Abu-abu (bisa pakai clearColorFilter atau set warna abu)
                    holder.ivLikeIcon.setColorFilter(Color.parseColor("#536471"))
                    holder.tvLikeCount.setTextColor(Color.parseColor("#536471"))

                    holder.tvLikeCount.text = newCount.toString()
                    currentTweet.likeCount = newCount
                    isLikedLocal = false
                }
            }
        }

        // --- 4. LOGIKA RETWEET ---
        // Ganti listener ke Area, update teks ke tvRetweetCount
        holder.btnRetweetArea.setOnClickListener {
            val context = holder.itemView.context
            AlertDialog.Builder(context)
                .setTitle("Retweet")
                .setMessage("Posting ulang tweet ini ke profilmu?")
                .setPositiveButton("Retweet") { _, _ ->
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val myUid = currentUser?.uid
                    val myUsername = currentUser?.email?.split("@")?.get(0) ?: "me"

                    if (myUid != null && tweetId != null) {
                        val retweetContent = "RT @${currentTweet.username}: ${currentTweet.content}"
                        val newTweetId = dbRef.push().key
                        val retweetData = Tweet(newTweetId, myUid, myUsername, retweetContent, System.currentTimeMillis())

                        if (newTweetId != null) {
                            dbRef.child(newTweetId).setValue(retweetData)

                            val newCount = currentTweet.retweetCount + 1
                            dbRef.child(tweetId).child("retweetCount").setValue(newCount)

                            // Update UI
                            holder.tvRetweetCount.text = newCount.toString()
                            currentTweet.retweetCount = newCount

                            Toast.makeText(context, "Berhasil Retweet!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Batal", null).show()
        }

        // --- 5. LOGIKA REPLY ---
        // Ganti listener ke Area, update teks ke tvReplyCount
        holder.btnReplyArea.setOnClickListener {
            val context = holder.itemView.context
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Balas ke @${currentTweet.username}")

            val input = EditText(context)
            input.setText("@${currentTweet.username} ")
            input.setSelection(input.text.length)
            builder.setView(input)

            builder.setPositiveButton("Kirim") { _, _ ->
                val replyContent = input.text.toString()
                val currentUser = FirebaseAuth.getInstance().currentUser
                val myUid = currentUser?.uid
                val myUsername = currentUser?.email?.split("@")?.get(0) ?: "me"

                if (myUid != null && replyContent.isNotEmpty() && tweetId != null) {
                    val newTweetId = dbRef.push().key
                    val replyTweet = Tweet(newTweetId, myUid, myUsername, replyContent, System.currentTimeMillis())

                    if (newTweetId != null) {
                        dbRef.child(newTweetId).setValue(replyTweet)

                        val newCount = currentTweet.replyCount + 1
                        dbRef.child(tweetId).child("replyCount").setValue(newCount)

                        // Update UI
                        holder.tvReplyCount.text = newCount.toString()
                        currentTweet.replyCount = newCount

                        Toast.makeText(context, "Balasan terkirim!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
                .setNegativeButton("Batal", null).show()
        }

        // --- 6. LOGIKA SHARE ---
        holder.btnShareArea.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Link dicopy!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return tweetList.size
    }
}