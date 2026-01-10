package com.example.xliteapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(private val userList: ArrayList<User>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvSearchName)
        val tvUsername: TextView = itemView.findViewById(R.id.tvSearchUsername)
        val tvInitial: TextView = itemView.findViewById(R.id.tvSearchInitial)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]

        val name = currentUser.displayName ?: "User"
        val email = currentUser.email ?: "anon"

        holder.tvName.text = name
        holder.tvUsername.text = "@${email.split("@")[0]}"
        holder.tvInitial.text = name.first().toString().uppercase()

        // PAS DIKLIK -> PINDAH KE PROFIL ORANG ITU
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ProfileActivity::class.java)
            // KITA BAWA OLEH-OLEH (UID ORANG ITU)
            intent.putExtra("TARGET_UID", currentUser.uid)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}