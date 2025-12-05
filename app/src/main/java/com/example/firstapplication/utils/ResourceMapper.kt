package com.example.firstapplication.utils

import android.content.Context
import android.net.Uri
import com.example.firstapplication.R
import com.google.android.exoplayer2.upstream.RawResourceDataSource

/**
 * 资源映射表工具类，用于优化getIdentifier性能问题
 * 避免在RecyclerView中频繁调用getIdentifier导致的性能开销
 */
object ResourceMapper {
    
    // 视频封面资源映射表
    private val videoCoverMap = mapOf(
        "video1" to R.drawable.video1,
        "video2" to R.drawable.video2,
        "video3" to R.drawable.video3,
        "video4" to R.drawable.video4,
        "video5" to R.drawable.video5
    )
    
    // 头像资源映射表
    private val avatarMap = mapOf(
        "avatar1" to R.drawable.avatar1,
        "avatar2" to R.drawable.avatar2,
        "avatar3" to R.drawable.avatar3,
        "avatar4" to R.drawable.avatar4,
        "avatar5" to R.drawable.avatar5
    )
    
    /**
     * 获取视频封面资源ID
     * @param resourceName 资源名称
     * @return 资源ID，如果找不到则返回默认背景图
     */
    fun getVideoCoverResId(resourceName: String): Int {
        val resId = videoCoverMap[resourceName]
        android.util.Log.d("ResourceMapper", "getVideoCoverResId: $resourceName -> $resId")
        return resId ?: R.drawable.ic_launcher_background
    }
    
    /**
     * 获取视频封面资源ID - 带上下文的后备方案
     * @param context 上下文
     * @param resourceName 资源名称
     * @return 资源ID，如果找不到则返回默认背景图
     */
    fun getVideoCoverResId(context: Context, resourceName: String): Int {
        val resId = videoCoverMap[resourceName]
        if (resId != null) {
            return resId
        }
        
        // 后备方案：使用getIdentifier
        val fallbackResId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
        android.util.Log.w("ResourceMapper", "Video resource not found in map: $resourceName, using getIdentifier: $fallbackResId")
        return if (fallbackResId != 0) fallbackResId else R.drawable.ic_launcher_background
    }
    
    /**
     * 获取头像资源ID
     * @param resourceName 资源名称
     * @return 资源ID，如果找不到则返回默认背景图
     */
    fun getAvatarResId(resourceName: String): Int {
        val resId = avatarMap[resourceName]
        android.util.Log.d("ResourceMapper", "getAvatarResId: $resourceName -> $resId")
        return resId ?: R.drawable.ic_launcher_background
    }
    
    /**
     * 获取头像资源ID - 带上下文的后备方案
     * @param context 上下文
     * @param resourceName 资源名称
     * @return 资源ID，如果找不到则返回默认背景图
     */
    fun getAvatarResId(context: Context, resourceName: String): Int {
        val resId = avatarMap[resourceName]
        if (resId != null) {
            return resId
        }
        
        // 后备方案：使用getIdentifier
        val fallbackResId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
        android.util.Log.w("ResourceMapper", "Avatar resource not found in map: $resourceName, using getIdentifier: $fallbackResId")
        return if (fallbackResId != 0) fallbackResId else R.drawable.ic_launcher_background
    }

    /**
     * 获取本地raw视频的Uri
     */
    fun getVideoUri(context: Context, resourceNameOrUrl: String): Uri? {
        val lower = resourceNameOrUrl.lowercase()
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return Uri.parse(resourceNameOrUrl)
        }
        val resId = context.resources.getIdentifier(resourceNameOrUrl, "raw", context.packageName)
        return if (resId != 0) RawResourceDataSource.buildRawResourceUri(resId) else null
    }
}
