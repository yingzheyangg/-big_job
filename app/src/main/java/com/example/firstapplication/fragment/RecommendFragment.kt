package com.example.firstapplication.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.app.ActivityOptionsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.firstapplication.R
import com.example.firstapplication.VideoPlayerActivity
import com.example.firstapplication.adapter.VideoGridAdapter
import com.example.firstapplication.model.Video
import com.example.firstapplication.viewmodel.VideoViewModel



class RecommendFragment : Fragment() {
    
    private lateinit var videoViewModel: VideoViewModel
    private lateinit var videoGridAdapter: VideoGridAdapter
    private lateinit var rvVideoGrid: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var refreshHeader: View
    private lateinit var refreshAnimView: View
    private lateinit var refreshTextView: TextView
    private var isRefreshingAnim = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recommend, container, false)
        
        initViews(view)
        setupViewModel()
        setupRecyclerView()
        setupRefresh()
        observeViewModel()
        
        return view
    }
    
    private fun initViews(view: View) {
        rvVideoGrid = view.findViewById(R.id.rv_video_grid)
        progressBar = view.findViewById(R.id.progress_bar)
        tvError = view.findViewById(R.id.tv_error)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_recommend)
        refreshHeader = view.findViewById(R.id.refresh_header_recommend)
        refreshAnimView = view.findViewById(R.id.iv_refresh_anim_recommend)
        refreshTextView = view.findViewById(R.id.tv_refresh_text_recommend)
    }
    
    private fun setupViewModel() {
        videoViewModel = ViewModelProvider(requireActivity())[VideoViewModel::class.java]
        videoViewModel.loadRecommendedVideos()
    }
    
    private fun setupRecyclerView() {
        videoGridAdapter = VideoGridAdapter(emptyList()) { video, sharedView ->
            onVideoClick(video, sharedView)
        }
        // 使用瀑布流布局
        val layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)

        layoutManager.gapStrategy =
            StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

        rvVideoGrid.layoutManager = layoutManager
        rvVideoGrid.setHasFixedSize(false)
        rvVideoGrid.adapter = videoGridAdapter
    }

    private fun setupRefresh() {
        swipeRefreshLayout.setColorSchemeColors(0xFFFE2C55.toInt(), 0xFF161823.toInt())
        swipeRefreshLayout.setOnRefreshListener {
            refreshTextView.text = "正在刷新..."
            refreshHeader.visibility = View.VISIBLE
            startRefreshAnimation()
            videoViewModel.loadRecommendedVideos()
        }
    }
    
    private fun observeViewModel() {
        videoViewModel.videos.observe(viewLifecycleOwner) { videos ->
            if (videos.isNotEmpty()) {
                videoGridAdapter = VideoGridAdapter(videos) { video, sharedView ->
                    onVideoClick(video, sharedView)
                }
                rvVideoGrid.adapter = videoGridAdapter
                showContent()
                swipeRefreshLayout.isRefreshing = false
                stopRefreshAnimation()
            }
        }
        
        videoViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (!isLoading) {
                swipeRefreshLayout.isRefreshing = false
                stopRefreshAnimation()
            }
        }
        
        videoViewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                showError(error)
                swipeRefreshLayout.isRefreshing = false
                stopRefreshAnimation()
            } else {
                tvError.visibility = View.GONE
            }
        }
    }

    private fun startRefreshAnimation() {
        if (isRefreshingAnim) return
        isRefreshingAnim = true
        val view = refreshAnimView
        fun loop() {
            if (!isRefreshingAnim) return
            view.animate().rotationBy(360f).setDuration(600).withEndAction { loop() }.start()
        }
        loop()
    }

    private fun stopRefreshAnimation() {
        if (!isRefreshingAnim) return
        isRefreshingAnim = false
        refreshAnimView.clearAnimation()
        refreshHeader.visibility = View.GONE
    }
    
    private fun onVideoClick(video: Video, sharedView: View) {
        val intent = Intent(requireContext(), VideoPlayerActivity::class.java).apply {
            putExtra("video_id", video.id)
            putExtra("transition_name", sharedView.transitionName)
        }
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            requireActivity(),
            sharedView,
            sharedView.transitionName
        )
        startActivity(intent, options.toBundle())
    }
    
    private fun showContent() {
        rvVideoGrid.visibility = View.VISIBLE
        tvError.visibility = View.GONE
    }
    
    private fun showError(message: String) {
        rvVideoGrid.visibility = View.GONE
        tvError.visibility = View.VISIBLE
        tvError.text = message
        
        tvError.setOnClickListener {
            videoViewModel.loadRecommendedVideos()
        }
    }
}
