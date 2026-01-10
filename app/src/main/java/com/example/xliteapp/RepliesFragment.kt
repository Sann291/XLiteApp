package com.example.xliteapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RepliesFragment : Fragment() {

    // Nanti kita isi logika khusus Reply disini
    // Untuk sekarang kosongkan dulu atau copy logika TweetsFragment
    // Agar aplikasi tidak error saat dijalankan

    companion object {
        fun newInstance(userId: String): RepliesFragment {
            val fragment = RepliesFragment()
            val args = Bundle()
            args.putString("userId", userId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }
}