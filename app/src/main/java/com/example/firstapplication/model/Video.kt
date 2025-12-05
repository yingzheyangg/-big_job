package com.example.firstapplication.model

data class Video(
    val id: String,
    val title: String,
    val description: String,
    val videoUrl: String,  // 本地视频资源名
    val coverUrl: String,  // 本地封面图片名
    val author: User,
    val playCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val shareCount: Int,
    val duration: Int, // 视频时间
    val createdAt: Long, // 发布时间
    val isLiked: Boolean = false
)

data class User(
    val id: String,
    val username: String,
    val avatarUrl: String, //头像资源名
    val followersCount: Int, //粉丝数量
    val isFollowing: Boolean = false
)

data class Comment(
    val id: String,
    val videoId: String,
    val user: User,
    val content: String,
    val likeCount: Int,
    val createdAt: Long,
    val isLiked: Boolean = false
)