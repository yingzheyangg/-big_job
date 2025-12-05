package com.example.firstapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.firstapplication.adapter.VideoGridAdapter
import com.example.firstapplication.adapter.ViewPagerAdapter
import com.example.firstapplication.model.Video
import com.example.firstapplication.viewmodel.VideoViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    
    private lateinit var videoViewModel: VideoViewModel
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        initViews()
        setupViewPager()
        setupTabLayout()
        setupBottomNavigation()
    }
    
    private fun initViews() {
        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)
        bottomNavigation = findViewById(R.id.bottom_navigation)
    }
    
    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter
    }
    
    private fun setupTabLayout() {
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "推荐"
                1 -> tab.text = "商城"
            }
        }.attach()
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_home
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, VideoPlayerActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_profile -> {
                    Toast.makeText(this, "个人中心开发中...", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }
    

}
