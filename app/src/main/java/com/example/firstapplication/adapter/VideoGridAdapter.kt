package com.example.firstapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapplication.R
import com.example.firstapplication.model.Video
import com.example.firstapplication.utils.ResourceMapper

class VideoGridAdapter(
    private val videos: List<Video>,
    private val onVideoClick: (Video, View) -> Unit
) : RecyclerView.Adapter<VideoGridAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.card_video)
        private val coverImage: ImageView = itemView.findViewById(R.id.iv_video_cover)

        private val likeCount: TextView = itemView.findViewById(R.id.tv_like_count)
        private val title: TextView = itemView.findViewById(R.id.tv_video_title)
        private val authorAvatar: ImageView = itemView.findViewById(R.id.iv_author_avatar)
        private val authorName: TextView = itemView.findViewById(R.id.tv_author_name)

        fun bind(video: Video) {
            // 设置视频封面 - 使用资源映射表优化性能
            val coverResId = ResourceMapper.getVideoCoverResId(itemView.context, video.coverUrl)
            coverImage.setImageResource(coverResId)

            ViewCompat.setTransitionName(coverImage, "video_cover_${video.id}")

            // 设置作者头像 - 使用资源映射表优化性能
            val avatarResId =
                ResourceMapper.getAvatarResId(itemView.context, video.author.avatarUrl)
            authorAvatar.setImageResource(avatarResId)

            // 设置文本信息
            title.text = video.title
            authorName.text = video.author.username

            // 设置点击事件
            cardView.setOnClickListener { onVideoClick(video, coverImage) }

            // 设置点赞数
            likeCount.text = formatCount(video.likeCount)
        }

        private fun formatCount(count: Int): String {
            return when {
                count >= 10000 -> "${count / 10000}w"
                count >= 1000 -> "${count / 1000}k"
                else -> count.toString()
            }
        }
    }

    // Adapter 负责将数据与 Item 视图绑定，必须实现 3 个抽象方法，同时定义 ViewHolder 复用视图：
    // 第一步：item创建ViewHolder（加载item布局）
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video_grid, parent, false)
        return VideoViewHolder(view)
    }

    // 第二步：绑定数据到ViewHolder
    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videos[position])
    }

    // 列表数据量大小
    override fun getItemCount(): Int = videos.size
}
