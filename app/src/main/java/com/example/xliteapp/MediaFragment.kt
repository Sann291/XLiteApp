package com.example.xliteapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class MediaFragment : Fragment() {

    companion object {
        fun newInstance(userId: String): MediaFragment {
            val fragment = MediaFragment()
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
        // Tampilkan layout kosong dulu
        return inflater.inflate(R.layout.fragment_list, container, false)
    }
}