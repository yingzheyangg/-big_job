package com.example.firstapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapplication.R
import com.example.firstapplication.model.Comment
import com.example.firstapplication.utils.ResourceMapper

class CommentAdapter(
    private val comments: List<Comment>,
    private val onCommentLikeClick: (Comment) -> Unit,
    private val onCommentReplyClick: (Comment) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userAvatar: ImageView = itemView.findViewById(R.id.iv_user_avatar)
        private val username: TextView = itemView.findViewById(R.id.tv_username)
        private val commentContent: TextView = itemView.findViewById(R.id.tv_comment_content)
        private val likeCount: TextView = itemView.findViewById(R.id.tv_like_count)
        private val likeIcon: ImageView = itemView.findViewById(R.id.iv_like_comment)
        private val timeText: TextView = itemView.findViewById(R.id.tv_time)
        private val replyText: TextView = itemView.findViewById(R.id.tv_reply)

        fun bind(comment: Comment) {
            try {
                // 设置用户头像 - 使用资源映射表优化性能
                val avatarResId = ResourceMapper.getAvatarResId(itemView.context, comment.user.avatarUrl)
                userAvatar.setImageResource(avatarResId)
                
                // 设置用户信息
                username.text = comment.user.username
                commentContent.text = comment.content
                likeCount.text = formatCount(comment.likeCount)
                timeText.text = formatTime(comment.createdAt)
                
                // 更新点赞状态
                updateLikeStatus(comment.isLiked)
                
                // 设置点击事件
                likeIcon.setOnClickListener { onCommentLikeClick(comment) }
                replyText.setOnClickListener { onCommentReplyClick(comment) }
            } catch (e: Exception) {
                e.printStackTrace()
                // 防止个别评论数据错误导致整个列表崩溃
                username.text = "用户"
                commentContent.text = "评论内容加载失败"
                likeCount.text = "0"
                timeText.text = "刚刚"
            }
        }
        
        private fun updateLikeStatus(isLiked: Boolean) {
            likeIcon.isSelected = isLiked
            if (isLiked) {
                likeIcon.setColorFilter(itemView.context.getColor(android.R.color.holo_red_light))
            } else {
                likeIcon.setColorFilter(itemView.context.getColor(android.R.color.darker_gray))
            }
        }
        
        private fun formatCount(count: Int): String {
            return when {
                count >= 10000 -> "${count / 10000}w"
                count >= 1000 -> "${count / 1000}k"
                else -> count.toString()
            }
        }
        
        private fun formatTime(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            
            return when {
                diff < 60000 -> "刚刚"
                diff < 3600000 -> "${diff / 60000}分钟前"
                diff < 86400000 -> "${diff / 3600000}小时前"
                else -> "${diff / 86400000}天前"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    override fun getItemCount(): Int = comments.size
}