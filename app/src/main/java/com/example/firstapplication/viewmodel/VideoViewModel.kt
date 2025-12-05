package com.example.firstapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstapplication.model.Comment
import com.example.firstapplication.model.User
import com.example.firstapplication.model.Video
import com.example.firstapplication.repository.VideoRepository
import kotlinx.coroutines.launch

class VideoViewModel : ViewModel() {
    
    private val repository = VideoRepository()
    
    private val _videos = MutableLiveData<List<Video>>()
    val videos: LiveData<List<Video>> = _videos
    
    private val _currentVideo = MutableLiveData<Video?>()
    val currentVideo: LiveData<Video?> = _currentVideo
    
    private val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>> = _comments
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _allVideos = MutableLiveData<List<Video>>()
    val allVideos: LiveData<List<Video>> = _allVideos
    
    init {
        loadRecommendedVideos()
    }
    
    fun loadRecommendedVideos() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val videoList = repository.getRecommendedVideos()
                _videos.value = videoList
                _allVideos.value = videoList
                _error.value = null
            } catch (e: Exception) {
                _error.value = "加载视频失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadAllVideos() {
        viewModelScope.launch {
            try {
                val videoList = repository.getRecommendedVideos()
                _allVideos.value = videoList
                _error.value = null
            } catch (e: Exception) {
                _error.value = "加载视频失败: ${e.message}"
            }
        }
    }
    
    fun setCurrentVideoId(videoId: String) {
        viewModelScope.launch {
            try {
                val video = repository.getVideoById(videoId)
                _currentVideo.value = video
                _error.value = null
            } catch (e: Exception) {
                _error.value = "加载视频失败: ${e.message}"
            }
        }
    }
    
    fun setCurrentVideo(video: Video) {
        _currentVideo.value = video
    }
    
    /**
     * 关注功能已移除 - 此方法保留用于向后兼容
     * 关注按钮已从UI中删除
     */
    @Deprecated("关注功能已移除")
    fun toggleFollow(user: User) {
        viewModelScope.launch {
            try {
                val success = repository.toggleFollow(user.id)
                if (success) {
                    // 找到当前显示的视频并更新其关注状态
                    val currentVideos = _allVideos.value.orEmpty().toMutableList()
                    val videoIndex = currentVideos.indexOfFirst { it.author.id == user.id }
                    
                    if (videoIndex != -1) {
                        val video = currentVideos[videoIndex]
                        val updatedAuthor = video.author.copy(isFollowing = !video.author.isFollowing)
                        val updatedVideo = video.copy(author = updatedAuthor)
                        currentVideos[videoIndex] = updatedVideo
                        _allVideos.value = currentVideos
                        
                        // 如果当前视频就是这一个，也更新currentVideo
                        if (_currentVideo.value?.id == video.id) {
                            _currentVideo.value = updatedVideo
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "操作失败: ${e.message}"
            }
        }
    }
    
    fun loadVideoById(videoId: String) {
        viewModelScope.launch {
            try {
                val video = repository.getVideoById(videoId)
                _currentVideo.value = video
                _error.value = null
            } catch (e: Exception) {
                _error.value = "加载视频失败: ${e.message}"
            }
        }
    }
    
    fun loadVideoComments(videoId: String) {
        viewModelScope.launch {
            try {
                val commentList = repository.getVideoComments(videoId)
                _comments.value = commentList
                _error.value = null
            } catch (e: Exception) {
                _error.value = "加载评论失败: ${e.message}"
            }
        }
    }

    //视频点赞
    fun toggleVideoLike(video: Video) {
        viewModelScope.launch {
            try {
                android.util.Log.d("VideoViewModel", "开始点赞操作 - 视频ID: ${video.id}, 当前状态: ${video.isLiked}")
                val success = repository.toggleLike(video.id)
                android.util.Log.d("VideoViewModel", "点赞操作结果: $success")
                
                if (success) {
                    // 创建更新后的视频对象
                    val updatedVideo = video.copy(
                        isLiked = !video.isLiked,
                        likeCount = if (video.isLiked) video.likeCount - 1 else video.likeCount + 1
                    )
                    android.util.Log.d("VideoViewModel", "更新后的视频状态 - isLiked: ${updatedVideo.isLiked}, likeCount: ${updatedVideo.likeCount}")
                    
                    _currentVideo.value = updatedVideo
                    
                    // 更新所有视频列表（VideoPlayerActivity使用这个）
                    val currentAllVideos = _allVideos.value.orEmpty().toMutableList()
                    val allVideosIndex = currentAllVideos.indexOfFirst { it.id == video.id }
                    if (allVideosIndex != -1) {
                        currentAllVideos[allVideosIndex] = updatedVideo
                        _allVideos.value = currentAllVideos
                        android.util.Log.d("VideoViewModel", "已更新所有视频列表，位置: $allVideosIndex")
                    }
                    
                    // 更新首页视频列表
                    val currentVideos = _videos.value.orEmpty().toMutableList()
                    val index = currentVideos.indexOfFirst { it.id == video.id }
                    if (index != -1) {
                        currentVideos[index] = updatedVideo
                        _videos.value = currentVideos
                        android.util.Log.d("VideoViewModel", "已更新首页视频列表，位置: $index")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("VideoViewModel", "点赞操作失败: ${e.message}", e)
                _error.value = "操作失败: ${e.message}"
            }
        }
    }
    
    fun addComment(videoId: String, content: String, user: User) {
        viewModelScope.launch {
            try {
                val newComment = repository.addComment(videoId, content, user)
                val currentComments = _comments.value.orEmpty().toMutableList()
                currentComments.add(0, newComment)
                _comments.value = currentComments
                
                // 更新评论数
                _currentVideo.value?.let { video ->
                    val updatedVideo = video.copy(commentCount = video.commentCount + 1)
                    _currentVideo.value = updatedVideo
                    
                    // 同时更新所有视频列表中的评论数
                    val currentAllVideos = _allVideos.value.orEmpty().toMutableList()
                    val index = currentAllVideos.indexOfFirst { it.id == video.id }
                    if (index != -1) {
                        currentAllVideos[index] = updatedVideo
                        _allVideos.value = currentAllVideos
                    }
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "发布评论失败: ${e.message}"
            }
        }
    }
    
    fun toggleCommentLike(comment: Comment) {
        viewModelScope.launch {
            try {
                val success = repository.toggleCommentLike(comment.id)
                if (success) {
                    val currentComments = _comments.value.orEmpty().toMutableList()
                    val index = currentComments.indexOfFirst { it.id == comment.id }
                    if (index != -1) {
                        val updatedComment = comment.copy(
                            isLiked = !comment.isLiked,
                            likeCount = if (comment.isLiked) comment.likeCount - 1 else comment.likeCount + 1
                        )
                        currentComments[index] = updatedComment
                        _comments.value = currentComments
                    }
                }
            } catch (e: Exception) {
                _error.value = "操作失败: ${e.message}"
            }
        }
    }
}