package com.example.firstapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.ViewCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.example.firstapplication.R
import com.example.firstapplication.model.Video
import com.example.firstapplication.utils.ResourceMapper

class VideoPlayerAdapter(
    private var videos: List<Video>,
    private val onVideoInteractionListener: OnVideoInteractionListener,
    private val exoPlayer: ExoPlayer
) : RecyclerView.Adapter<VideoPlayerAdapter.VideoPlayerViewHolder>() {
    
    private var recyclerView: RecyclerView? = null

    interface OnVideoInteractionListener {
        fun onLikeClick(video: Video)
        fun onCommentClick(video: Video)
        fun onShareClick(video: Video)
        fun onBackClick()
    }
    
    inner class VideoPlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivBack: ImageView = itemView.findViewById(R.id.iv_back)
        private val ivLike: ImageView = itemView.findViewById(R.id.iv_like)
        private val ivComment: ImageView = itemView.findViewById(R.id.iv_comment)
        private val ivShare: ImageView = itemView.findViewById(R.id.iv_share)
        private val ivAuthorAvatar: ImageView = itemView.findViewById(R.id.iv_author_avatar)
        private val ivVideoPlaceholder: ImageView = itemView.findViewById(R.id.iv_video_placeholder)
        private val playerView: PlayerView = itemView.findViewById(R.id.player_view)
        private val ivPlayPause: ImageView = itemView.findViewById(R.id.iv_play_pause)
        // 关注按钮已移除
        private val tvLikeCount: TextView = itemView.findViewById(R.id.tv_like_count)
        private val tvCommentCount: TextView = itemView.findViewById(R.id.tv_comment_count)
        private val tvShareCount: TextView = itemView.findViewById(R.id.tv_share_count)
        private val tvAuthorName: TextView = itemView.findViewById(R.id.tv_author_name)
        private val tvVideoDescription: TextView = itemView.findViewById(R.id.tv_video_description)
        private val tvMusicInfo: TextView = itemView.findViewById(R.id.tv_music_info)
        private val layoutInteraction: LinearLayout = itemView.findViewById(R.id.layout_interaction)
        private val layoutBottomInfo: LinearLayout = itemView.findViewById(R.id.layout_bottom_info)

        fun bind(video: Video, position: Int) {
            try {
                // 设置视频封面 - 使用资源映射表优化性能
                val coverResId = ResourceMapper.getVideoCoverResId(itemView.context, video.coverUrl)
                ivVideoPlaceholder.setImageResource(coverResId)
                ViewCompat.setTransitionName(ivVideoPlaceholder, "video_cover_${video.id}")
                
                // 设置作者头像 - 使用资源映射表优化性能
                val avatarResId = ResourceMapper.getAvatarResId(itemView.context, video.author.avatarUrl)
                ivAuthorAvatar.setImageResource(avatarResId)
                
                // 设置文本信息 - 添加@字符
                tvAuthorName.text = "@${video.author.username}"
                tvVideoDescription.text = video.description
                tvLikeCount.text = formatCount(video.likeCount)
                tvCommentCount.text = formatCount(video.commentCount)
                tvShareCount.text = formatCount(video.shareCount)
                tvMusicInfo.text = itemView.context.getString(R.string.original_music_format, video.author.username)
                
                // 更新点赞状态
                updateLikeButton(video.isLiked)

                // 根据内容类型切换显示 PlayerView 与占位图
                val hasPlayableVideo = com.example.firstapplication.utils.ResourceMapper.getVideoUri(itemView.context, video.videoUrl) != null
                if (hasPlayableVideo) {
                    playerView.visibility = View.VISIBLE
                    ivVideoPlaceholder.visibility = View.GONE
                    if (playerView.player !== exoPlayer) {
                        playerView.player = exoPlayer
                    }
                } else {
                    playerView.visibility = View.GONE
                    ivVideoPlaceholder.visibility = View.VISIBLE
                }

                // 播放区域点击切换播放/暂停，并在中间显示按钮
                val togglePlayPause: (View) -> Unit = toggle@{
                    if (!hasPlayableVideo) return@toggle
                    val isPlaying = exoPlayer.playWhenReady
                    exoPlayer.playWhenReady = !isPlaying
                    if (exoPlayer.playWhenReady) {
                        ivPlayPause.visibility = View.GONE
                    } else {
                        ivPlayPause.visibility = View.VISIBLE
                    }
                }
                playerView.setOnClickListener { togglePlayPause(it) }
                ivVideoPlaceholder.setOnClickListener { togglePlayPause(it) }

                ivPlayPause.visibility = View.GONE
                
                // 设置点击事件
                ivBack.setOnClickListener { 
                    animateClick(it)
                    onVideoInteractionListener.onBackClick() 
                }
                ivLike.setOnClickListener { 
                    animateClick(it)
                    onVideoInteractionListener.onLikeClick(video) 
                }
                ivComment.setOnClickListener { 
                    animateClick(it)
                    onVideoInteractionListener.onCommentClick(video) 
                }
                ivShare.setOnClickListener { 
                    animateClick(it)
                    onVideoInteractionListener.onShareClick(video) 
                }
                // 关注按钮已移除
                ivAuthorAvatar.setOnClickListener {
                    animateClick(it)
                    // 进入作者主页
                    android.widget.Toast.makeText(itemView.context, "作者主页功能开发中...", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(itemView.context, "绑定数据失败: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        
        private fun updateLikeButton(isLiked: Boolean) {
            // 清除之前的动画，避免状态干扰
            ivLike.clearAnimation()
            
            if (isLiked) {
                // 点赞时显示红色实心图标
                ivLike.setImageResource(R.drawable.like_filled_icon)
                // 添加点赞动画效果，只影响当前图标
                ivLike.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(150)
                    .withEndAction {
                        ivLike.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(150)
                            .start()
                    }
                    .start()
            } else {
                // 未点赞时显示白色空心图标
                ivLike.setImageResource(R.drawable.like_icon)
            }
        }
        
        // 只更新点赞状态，避免整个item重绘
        fun updateLikeStatusOnly(isLiked: Boolean, likeCount: Int) {
            updateLikeButton(isLiked)
            tvLikeCount.text = formatCount(likeCount)
        }
        
        private fun formatCount(count: Int): String {
            return when {
                count >= 10000 -> "${count / 10000}w"
                count >= 1000 -> "${count / 1000}k"
                else -> count.toString()
            }
        }
        
        private fun animateClick(view: View) {
            // 保存原始状态，避免影响其他UI元素
            val originalScaleX = view.scaleX
            val originalScaleY = view.scaleY
            
            view.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
                    view.animate()
                        .scaleX(originalScaleX)
                        .scaleY(originalScaleY)
                        .setDuration(100)
                        .start()
                }
                .start()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoPlayerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video_player, parent, false)
        return VideoPlayerViewHolder(view)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    override fun onBindViewHolder(holder: VideoPlayerViewHolder, position: Int) {
        holder.bind(videos[position], position)
    }

    override fun getItemCount(): Int = videos.size
    
    fun updateVideos(newVideos: List<Video>) {
        val diffResult = androidx.recyclerview.widget.DiffUtil.calculateDiff(
            VideoDiffCallback(videos, newVideos)
        )
        videos = newVideos
        diffResult.dispatchUpdatesTo(this)
    }
    
    fun updateVideoAt(position: Int, video: Video) {
        if (position >= 0 && position < videos.size) {
            val updatedVideos = videos.toMutableList()
            updatedVideos[position] = video
            videos = updatedVideos
            
            // 只更新特定ViewHolder的点赞状态，避免整个item重绘
            val viewHolder = recyclerView?.findViewHolderForAdapterPosition(position) as? VideoPlayerViewHolder
            viewHolder?.updateLikeStatusOnly(video.isLiked, video.likeCount)
        }
    }
}

class VideoDiffCallback(
    private val oldList: List<Video>,
    private val newList: List<Video>
) : androidx.recyclerview.widget.DiffUtil.Callback() {
    
    override fun getOldListSize(): Int = oldList.size
    
    override fun getNewListSize(): Int = newList.size
    
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }
    
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return oldItem == newItem
    }
}
