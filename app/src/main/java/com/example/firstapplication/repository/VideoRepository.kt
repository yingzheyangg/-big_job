package com.example.firstapplication.repository

import com.example.firstapplication.model.Comment
import com.example.firstapplication.model.User
import com.example.firstapplication.model.Video

class VideoRepository {
    
    fun getRecommendedVideos(): List<Video> {
        return generateMockVideos()
    }
    
    fun getVideoById(videoId: String): Video? {
        val videos = generateMockVideos()
        return videos.find { it.id == videoId }
    }
    
    fun getVideoComments(videoId: String): List<Comment> {
        return generateMockComments(videoId)
    }
    
    fun addComment(videoId: String, content: String, user: User): Comment {
        return Comment(
            id = "comment_${System.currentTimeMillis()}",
            videoId = videoId,
            user = user,
            content = content,
            likeCount = 0,
            createdAt = System.currentTimeMillis(),
            isLiked = false
        )
    }
    
    fun toggleLike(videoId: String): Boolean {
        return true
    }
    
    /**
     * 关注功能已移除 - 此方法保留用于向后兼容
     * 关注按钮已从UI中删除
     */
    @Deprecated("关注功能已移除")
    fun toggleFollow(userId: String): Boolean {
        return true
    }
    
    fun toggleCommentLike(commentId: String): Boolean {
        return true
    }
    
    private fun generateMockVideos(): List<Video> {
        val mockUsers = listOf(
            User("1", "康", "avatar1", 12500, false),
            User("2", "乔", "avatar2", 8900, true),
            User("3", "九月的愿望", "avatar3", 25600, false),
            User("4", "Nehs", "avatar4", 18200, true),
            User("5", "嘿嘿youyou", "avatar5", 9800, false),
            User("6", "迎着阳光", "avatar5", 9800, false),
        )

        return listOf(
            Video(
                id = "video_1",
                title = "运动日常",
                description = "今天运动量达标，还看到了一只可可爱爱的小土松[色][色]",
                videoUrl = "demo4",  // 本地视频资源名
                coverUrl = "video7",  // 本地封面图片名
                author = mockUsers[0],
                playCount = 15600,
                likeCount = 1200,
                commentCount = 89,
                shareCount = 156,
                duration = 45,
                createdAt = System.currentTimeMillis() - 3600000,
                isLiked = false
            ),
            Video(
                id = "video_2",
                title = "生活日常",
                description = "ovo是微笑，o_o是警告！",
                videoUrl = "video2",
                coverUrl = "video2",
                author = mockUsers[1],
                playCount = 28900,
                likeCount = 2100,
                commentCount = 234,
                shareCount = 567,
                duration = 32,
                createdAt = System.currentTimeMillis() - 7200000,
                isLiked = true
            ),
            Video(
                id = "video_3",
                title = "旅游",
                description = "享受自然美景！",
                videoUrl = "video3",
                coverUrl = "video3",
                author = mockUsers[2],
                playCount = 45200,
                likeCount = 3400,
                commentCount = 567,
                shareCount = 890,
                duration = 28,
                createdAt = System.currentTimeMillis() - 10800000,
                isLiked = false
            ),
            Video(
                id = "video_4",
                title = "瞻仰伟人",
                description = "问苍茫大地，随主沉浮！",
                videoUrl = "video4",
                coverUrl = "video4",
                author = mockUsers[3],
                playCount = 32100,
                likeCount = 2800,
                commentCount = 445,
                shareCount = 723,
                duration = 38,
                createdAt = System.currentTimeMillis() - 14400000,
                isLiked = true
            ),
            Video(
                id = "video_5",
                title = "5分钟腹肌训练，坚持一个月见效",
                description = "每天5分钟，坚持一个月，轻松练出完美腹肌！",
                videoUrl = "video5",
                coverUrl = "video5",
                author = mockUsers[4],
                playCount = 19800,
                likeCount = 1500,
                commentCount = 234,
                shareCount = 345,
                duration = 42,
                createdAt = System.currentTimeMillis() - 18000000,
                isLiked = false
            ),
            Video(
                id = "video_6",
                title = "性能监控SDK",
                description = "本项目是一个Android移动资讯应用，为用户提供便捷的资讯浏览和个人管理服务。应用采用现代化的MVVM架构设计，提供流畅的用户体验和丰富的资讯内容展示。\n" +
                        "\n" +
                        "主要解决用户获取信息的需求，通过移动端提供随时随地的资讯访问服务，目标用户群体为需要获取各类资讯信息的移动端用户。",
                videoUrl = "demo2",
                coverUrl = "video6",
                author = mockUsers[5],
                playCount = 8900,
                likeCount = 678,
                commentCount = 89,
                shareCount = 123,
                duration = 55,
                createdAt = System.currentTimeMillis() - 21600000,
                isLiked = false
            )
        )
    }
    
    private fun generateMockComments(videoId: String): List<Comment> {
        val mockUsers = listOf(
            User("1", "康", "avatar1", 12500, false),
            User("2", "乔", "avatar2", 8900, true),
            User("3", "九月的愿望", "avatar3", 25600, false)
        )
        
        return listOf(
            Comment(
                id = "comment_1",
                videoId = videoId,
                user = mockUsers[0],
                content = "这个视频太棒了！学到了很多有用的知识，谢谢分享！",
                likeCount = 23,
                createdAt = System.currentTimeMillis() - 1800000,
                isLiked = false
            ),
            Comment(
                id = "comment_2",
                videoId = videoId,
                user = mockUsers[1],
                content = "博主讲得真清楚，新手也能看懂，已经收藏了！",
                likeCount = 15,
                createdAt = System.currentTimeMillis() - 3600000,
                isLiked = true
            ),
            Comment(
                id = "comment_3",
                videoId = videoId,
                user = mockUsers[2],
                content = "请问有详细的步骤图解吗？想跟着一起做！",
                likeCount = 8,
                createdAt = System.currentTimeMillis() - 7200000,
                isLiked = false
            )
        )
    }
}
