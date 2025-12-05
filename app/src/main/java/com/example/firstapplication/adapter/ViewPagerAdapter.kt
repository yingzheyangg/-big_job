package com.example.firstapplication.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.firstapplication.fragment.RecommendFragment
import com.example.firstapplication.fragment.ShopFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    
    override fun getItemCount(): Int = 2
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RecommendFragment()
            1 -> ShopFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}