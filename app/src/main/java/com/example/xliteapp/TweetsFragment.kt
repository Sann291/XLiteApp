package com.example.xliteapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class TweetsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tweetList: ArrayList<Tweet>
    private lateinit var adapter: TweetAdapter
    private lateinit var dbRef: DatabaseReference

    // ID User yang profilnya sedang dilihat
    private var userId: String? = null

    companion object {
        // Fungsi untuk menerima kiriman ID User dari Activity
        fun newInstance(userId: String): TweetsFragment {
            val fragment = TweetsFragment()
            val args = Bundle()
            args.putString("userId", userId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getString("userId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Hubungkan dengan XML yang tadi dibuat
        val view = inflater.inflate(R.layout.fragment_list, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        tweetList = arrayListOf()
        adapter = TweetAdapter(tweetList)
        recyclerView.adapter = adapter

        loadUserTweets()

        return view
    }

    private fun loadUserTweets() {
        if (userId == null) return

        dbRef = FirebaseDatabase.getInstance().getReference("tweets")

        // QUERY: Ambil tweet yang UID-nya sama dengan User ID profil ini
        val query = dbRef.orderByChild("uid").equalTo(userId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tweetList.clear()
                if (snapshot.exists()) {
                    for (tweetSnap in snapshot.children) {
                        val tweet = tweetSnap.getValue(Tweet::class.java)
                        // Filter tambahan: Pastikan bukan Reply (jika kamu nanti pakai logika reply)
                        if (tweet != null) {
                            tweetList.add(tweet)
                        }
                    }
                    // Balik urutan biar yang terbaru di atas
                    tweetList.reverse()
                }
                adapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
            }
        })
    }
}