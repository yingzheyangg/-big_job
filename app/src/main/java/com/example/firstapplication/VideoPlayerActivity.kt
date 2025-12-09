package com.example.firstapplication

import android.content.Intent
import android.os.Bundle
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.example.firstapplication.adapter.VideoPlayerAdapter
import com.example.firstapplication.model.Video
import com.example.firstapplication.viewmodel.VideoViewModel

class VideoPlayerActivity : AppCompatActivity() {
    
    private lateinit var videoViewModel: VideoViewModel
    private lateinit var viewPager2: ViewPager2
    private lateinit var ivBack: ImageView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    
    private lateinit var videoPlayerAdapter: VideoPlayerAdapter
    private var allVideos: List<Video> = emptyList()
    private var currentVideoIndex: Int = 0
    private var isPositionInitialized: Boolean = false
    private lateinit var exoPlayer: ExoPlayer
    
    private fun isAdapterInitialized(): Boolean {
        return ::videoPlayerAdapter.isInitialized
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_video_player)
            supportPostponeEnterTransition()
            
            initViews()
            setupRefresh()
            setupViewModel()
            setupClickListeners()
            
            val videoId = intent.getStringExtra("video_id")
            videoViewModel.loadAllVideos()
            if (videoId != null) {
                videoViewModel.setCurrentVideoId(videoId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun initViews() {
        try {
            viewPager2 = findViewById(R.id.viewPager2)
            ivBack = findViewById(R.id.iv_back)
            swipeRefreshLayout = findViewById(R.id.swipe_refresh_player)
            exoPlayer = ExoPlayer.Builder(this).build()
            
            // 设置ViewPager2为垂直滑动
            viewPager2.orientation = ViewPager2.ORIENTATION_VERTICAL
            
            // 初始化适配器
            videoPlayerAdapter = VideoPlayerAdapter(
                videos = emptyList(),
                onVideoInteractionListener = object : VideoPlayerAdapter.OnVideoInteractionListener {
                    override fun onLikeClick(video: Video) {
                        videoViewModel.toggleVideoLike(video)
                    }
                    
                    override fun onCommentClick(video: Video) {
                        showCommentPanel(video)
                    }
                    
                    override fun onShareClick(video: Video) {
                        shareVideo()
                    }
                    
                    override fun onBackClick() {
                        finish()
                    }
                },
                exoPlayer = exoPlayer
            )
            
            viewPager2.adapter = videoPlayerAdapter
            
            // 监听页面切换
            viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    currentVideoIndex = position
                    val currentVideo = allVideos.getOrNull(position)
                    if (currentVideo != null) {
                        videoViewModel.setCurrentVideo(currentVideo)
                        val uri = com.example.firstapplication.utils.ResourceMapper.getVideoUri(this@VideoPlayerActivity, currentVideo.videoUrl)
                        if (uri != null) {
                            val mediaItem = MediaItem.fromUri(uri)
                            exoPlayer.setMediaItem(mediaItem)
                            exoPlayer.prepare()
                            exoPlayer.playWhenReady = true
                        } else {
                            exoPlayer.pause()
                            exoPlayer.clearMediaItems()
                        }
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("初始化视图失败: ${e.message}")
        }
    }

    private fun setupRefresh() {
        swipeRefreshLayout.setColorSchemeColors(0xFFFE2C55.toInt(), 0xFF161823.toInt())
        swipeRefreshLayout.setOnChildScrollUpCallback { _, _ ->
            false
        }
        swipeRefreshLayout.setOnRefreshListener {
            videoViewModel.loadAllVideos()
        }
    }
    
    private fun setupViewModel() {
        try {
            videoViewModel = ViewModelProvider(this)[VideoViewModel::class.java]
            
            // 观察所有视频数据
            videoViewModel.allVideos.observe(this) { videos ->
                try {
                    if (videos.isNotEmpty()) {
                        allVideos = videos
                        videoPlayerAdapter.updateVideos(videos)
                        
                        // 只在首次加载时设置当前视频位置，避免点赞时重复跳转
                        if (!isPositionInitialized) {
                            val requestedId = intent.getStringExtra("video_id")
                            val position = if (requestedId != null) {
                                videos.indexOfFirst { it.id == requestedId }.takeIf { it >= 0 } ?: 0
                            } else 0

                            currentVideoIndex = position
                            viewPager2.setCurrentItem(position, false)
                            isPositionInitialized = true

                            viewPager2.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                                override fun onPreDraw(): Boolean {
                                    viewPager2.viewTreeObserver.removeOnPreDrawListener(this)
                                    supportStartPostponedEnterTransition()
                                    if (swipeRefreshLayout.isRefreshing) {
                                        swipeRefreshLayout.isRefreshing = false
                                    }
                                    return true
                                }
                            })
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "数据加载错误: ${e.message}", Toast.LENGTH_SHORT).show()
                }

                // 无论这次有没有新数据 / 列表是不是空，只要 allVideos 有回调，就统一关掉刷新圈
                if (swipeRefreshLayout.isRefreshing) {
                    swipeRefreshLayout.isRefreshing = false
                }
            }
            
            // 观察当前视频数据变化（用于点赞等状态更新）
            videoViewModel.currentVideo.observe(this) { currentVideo ->
                try {
                    if (currentVideo != null && isAdapterInitialized() && allVideos.isNotEmpty()) {
                        // 找到当前视频在列表中的位置
                        val position = allVideos.indexOfFirst { it.id == currentVideo.id }
                        if (position != -1 && position < allVideos.size) {
                            // 检查是否只有点赞状态发生变化
                            val oldVideo = allVideos[position]
                            val isLikeChanged = oldVideo.isLiked != currentVideo.isLiked || oldVideo.likeCount != currentVideo.likeCount
                            
                            if (isLikeChanged) {
                                // 点赞变化，使用精确更新
                                videoPlayerAdapter.updateVideoAt(position, currentVideo)
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("VideoPlayerActivity", "视频状态更新错误: ${e.message}", e)
                    e.printStackTrace()
                    Toast.makeText(this, "视频状态更新错误: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            
            // 观察错误信息
            videoViewModel.error.observe(this) { error ->
                if (error != null) {
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "ViewModel初始化失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        try {
            if (::exoPlayer.isInitialized) {
                exoPlayer.release()
            }
        } catch (_: Exception) {}
        super.onDestroy()
    }
    
    private fun setupClickListeners() {
        try {
            ivBack.setOnClickListener { finish() }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "按钮初始化失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun shareVideo() {
        try {
            val currentVideo = allVideos.getOrNull(currentVideoIndex)
            if (currentVideo != null) {
                val shareText = "我在抖音发现了一个有趣的视频，快来看看吧！"
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                startActivity(Intent.createChooser(shareIntent, "分享视频"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "分享失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showCommentPanel(video: Video) {
        try {
            val bottomSheet = CommentBottomSheet.newInstance(video.id)
            bottomSheet.show(supportFragmentManager, "comment_bottom_sheet")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "评论面板加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
