package com.example.firstapplication.adapter

import android.animation.ObjectAnimator
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapplication.R
import com.example.firstapplication.model.Video
import com.example.firstapplication.utils.ResourceMapper
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ui.PlayerView

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
        // View 初始化
        private val ivBack: ImageView = itemView.findViewById(R.id.iv_back)
        private val ivLike: ImageView = itemView.findViewById(R.id.iv_like)
        private val ivComment: ImageView = itemView.findViewById(R.id.iv_comment)
        private val ivShare: ImageView = itemView.findViewById(R.id.iv_share)
        private val ivAuthorAvatar: ImageView = itemView.findViewById(R.id.iv_author_avatar)
        private val ivVideoPlaceholder: ImageView = itemView.findViewById(R.id.iv_video_placeholder)
        private val playerView: PlayerView = itemView.findViewById(R.id.player_view)
        private val ivPlayPause: ImageView = itemView.findViewById(R.id.iv_play_pause)
        private val tvLikeCount: TextView = itemView.findViewById(R.id.tv_like_count)
        private val tvCommentCount: TextView = itemView.findViewById(R.id.tv_comment_count)
        private val tvShareCount: TextView = itemView.findViewById(R.id.tv_share_count)
        private val tvAuthorName: TextView = itemView.findViewById(R.id.tv_author_name)
        private val tvVideoDescription: TextView = itemView.findViewById(R.id.tv_video_description)
        private val tvMusicInfo: TextView = itemView.findViewById(R.id.tv_music_info)

        // 音乐转盘相关
        private val ivMusicDisc: ImageView = itemView.findViewById(R.id.iv_music_disc)
        private var discAnimator: ObjectAnimator? = null

        fun bind(video: Video, position: Int) {
            try {
                // 1. 设置基础信息
                val coverResId = ResourceMapper.getVideoCoverResId(itemView.context, video.coverUrl)
                ivVideoPlaceholder.setImageResource(coverResId)
                ViewCompat.setTransitionName(ivVideoPlaceholder, "video_cover_${video.id}")

                val avatarResId = ResourceMapper.getAvatarResId(itemView.context, video.author.avatarUrl)
                ivAuthorAvatar.setImageResource(avatarResId)

                tvAuthorName.text = "@${video.author.username}"
                tvVideoDescription.text = video.description
                tvLikeCount.text = formatCount(video.likeCount)
                tvCommentCount.text = formatCount(video.commentCount)
                tvShareCount.text = formatCount(video.shareCount)
                tvMusicInfo.text = itemView.context.getString(
                    R.string.original_music_format,
                    video.author.username
                )

                // 初始化时禁止动画 (animate = false)
                // 解决滑动列表时图标乱跳的问题
                updateLikeButton(video.isLiked, animate = false)

                // 2. 播放器与封面逻辑
                val hasPlayableVideo = ResourceMapper.getVideoUri(itemView.context, video.videoUrl) != null
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

                // 3. 处理转盘动画（初始化）
                handleDiscAnimation()

                // 4. 设置手势监听 (处理单击/双击)
                setupGestures(hasPlayableVideo)

                // 初始状态下隐藏暂停图标
                ivPlayPause.visibility = View.GONE

                // 5. 设置普通按钮点击事件
                ivBack.setOnClickListener {
                    animateClick(it)
                    onVideoInteractionListener.onBackClick()
                }

                // 点赞按钮点击（这里是用户主动点击，所以 animate = true）
                ivLike.setOnClickListener {
                    animateClick(it)
                    handleInteraction { currentVideo ->
                        onVideoInteractionListener.onLikeClick(currentVideo)
                    }
                }

                ivComment.setOnClickListener {
                    animateClick(it)
                    handleInteraction { currentVideo ->
                        onVideoInteractionListener.onCommentClick(currentVideo)
                    }
                }

                ivShare.setOnClickListener {
                    animateClick(it)
                    handleInteraction { currentVideo ->
                        onVideoInteractionListener.onShareClick(currentVideo)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // 实际生产中建议使用 Log 而不是 Toast，避免刷屏
            }
        }

        // --- 核心逻辑封装 ---

        private fun setupGestures(hasPlayableVideo: Boolean) {
            val gestureDetector = GestureDetector(itemView.context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean = true

                // 【单击】：播放/暂停
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    if (hasPlayableVideo) {
                        val isPlaying = exoPlayer.playWhenReady
                        exoPlayer.playWhenReady = !isPlaying

                        // 更新UI状态
                        ivPlayPause.visibility = if (exoPlayer.playWhenReady) View.GONE else View.VISIBLE
                        handleDiscAnimation() // 更新转盘状态
                    }
                    return true
                }

                // 【双击】：点赞
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    handleInteraction { currentVideo ->
                        // 1. 触发业务逻辑
                        onVideoInteractionListener.onLikeClick(currentVideo)

                        // 2. 显示屏幕中间的大爱心
                        showTapHeart(e.x, e.y)

                        // 3. 【立即反馈】强制让图标变红并播放动画
                        // 这样用户双击时感觉非常跟手，不用等数据回调
                        ivLike.setImageResource(R.drawable.like_filled_icon)
                        updateLikeButton(true, animate = true)
                    }
                    return true
                }
            })

            val touchListener = View.OnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
            }

            // 绑定到播放区域
            playerView.setOnTouchListener(touchListener)
            ivVideoPlaceholder.setOnTouchListener(touchListener)
        }

        // 转盘动画逻辑
        private fun handleDiscAnimation() {
            if (discAnimator == null) {
                discAnimator = ObjectAnimator.ofFloat(ivMusicDisc, "rotation", 0f, 360f).apply {
                    duration = 6000
                    repeatCount = ObjectAnimator.INFINITE
                    interpolator = LinearInterpolator()
                }
            }

            // 只要是在播放状态，就转动；否则暂停
            if (exoPlayer.playWhenReady) {
                if (discAnimator?.isPaused == true) discAnimator?.resume() else discAnimator?.start()
            } else {
                if (discAnimator?.isRunning == true) discAnimator?.pause()
            }
        }

        // 【核心修改】增加 animate 参数
        private fun updateLikeButton(isLiked: Boolean, animate: Boolean) {
            ivLike.clearAnimation() // 清除之前的动画

            if (isLiked) {
                ivLike.setImageResource(R.drawable.like_filled_icon)
                // 只有 animate 为 true 时才播放缩放动画
                if (animate) {
                    ivLike.animate()
                        .scaleX(1.2f).scaleY(1.2f)
                        .setDuration(150)
                        .withEndAction {
                            ivLike.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start()
                        }.start()
                }
            } else {
                ivLike.setImageResource(R.drawable.like_icon)
                // 取消点赞一般不需要复杂的缩放动画，变回白色即可
            }
        }

        // 外部更新调用（例如 ViewModel 回调）
        fun updateLikeStatusOnly(isLiked: Boolean, likeCount: Int) {
            // 这里是数据变化导致的更新，通常也视为一种“动态”，或者你可以根据需求决定是否 false
            // 但如果是用户点击触发的回调，传 true 体验更好
            updateLikeButton(isLiked, animate = true)
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
            val originalScaleX = view.scaleX
            val originalScaleY = view.scaleY
            view.animate()
                .scaleX(0.9f).scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
                    view.animate().scaleX(originalScaleX).scaleY(originalScaleY).setDuration(100).start()
                }.start()
        }

        private fun handleInteraction(action: (Video) -> Unit) {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION && position < videos.size) {
                action(videos[position])
            }
        }

        // 屏幕点击的大爱心动画
        private fun showTapHeart(x: Float, y: Float) {
            val heartView = ImageView(itemView.context)
            heartView.setImageResource(R.drawable.like_filled_icon)
            val layoutParams = android.widget.FrameLayout.LayoutParams(200, 200)
            heartView.x = x - 100
            heartView.y = y - 100
            heartView.layoutParams = layoutParams
            heartView.rotation = (-30..30).random().toFloat()
            (itemView as? ViewGroup)?.addView(heartView)

            heartView.alpha = 0f
            heartView.scaleX = 0f
            heartView.scaleY = 0f

            heartView.animate()
                .alpha(1f).scaleX(1.2f).scaleY(1.2f)
                .setDuration(200)
                .withEndAction {
                    heartView.animate()
                        .alpha(0f).translationYBy(-150f)
                        .setDuration(400).setStartDelay(200)
                        .withEndAction { (itemView as? ViewGroup)?.removeView(heartView) }
                        .start()
                }.start()
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
        val diffResult = DiffUtil.calculateDiff(VideoDiffCallback(videos, newVideos))
        videos = newVideos
        diffResult.dispatchUpdatesTo(this)
    }

    fun updateVideoAt(position: Int, video: Video) {
        if (position >= 0 && position < videos.size) {
            val updatedVideos = videos.toMutableList()
            updatedVideos[position] = video
            videos = updatedVideos // 更新数据源

            val viewHolder = recyclerView?.findViewHolderForAdapterPosition(position) as? VideoPlayerViewHolder
            // 这里调用 updateLikeStatusOnly，它内部会开启动画
            viewHolder?.updateLikeStatusOnly(video.isLiked, video.likeCount)
        }
    }
}

class VideoDiffCallback(
    private val oldList: List<Video>,
    private val newList: List<Video>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].id == newList[newItemPosition].id
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition] == newList[newItemPosition]
}