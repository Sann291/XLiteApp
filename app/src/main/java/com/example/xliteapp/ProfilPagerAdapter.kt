package com.example.xliteapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ProfilePagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val userId: String
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 3 // Karena kita punya 3 Tab: Tweets, Replies, Media
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TweetsFragment.newInstance(userId)   // Tab 1: Tweets
            1 -> RepliesFragment.newInstance(userId) // Tab 2: Replies
            2 -> MediaFragment.newInstance(userId)   // Tab 3: Media
            else -> TweetsFragment.newInstance(userId)
        }
    }
}