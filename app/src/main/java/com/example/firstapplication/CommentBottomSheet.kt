package com.example.firstapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapplication.adapter.CommentAdapter
import com.example.firstapplication.model.Comment
import com.example.firstapplication.model.User
import com.example.firstapplication.viewmodel.VideoViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CommentBottomSheet : BottomSheetDialogFragment() {
    
    private lateinit var videoViewModel: VideoViewModel
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var rvComments: RecyclerView
    private lateinit var etComment: EditText
    private lateinit var btnSend: TextView
    private lateinit var ivClose: ImageView
    private lateinit var tvCommentCount: TextView
    
    private var videoId: String? = null
    private var currentUser = User(
        id = "current_user",
        username = "当前用户",
        avatarUrl = "https://example.com/current_user.jpg",
        followersCount = 100,
        isFollowing = false
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_comments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            videoId = arguments?.getString(ARG_VIDEO_ID)
            if (videoId == null) {
                Toast.makeText(context, "视频ID无效", Toast.LENGTH_SHORT).show()
                dismiss()
                return
            }
            
            // 初始化视图
            initViews(view)
            
            // 先设置RecyclerView，再设置ViewModel，避免空指针
            setupRecyclerView()
            setupViewModel()
            setupClickListeners()
            loadComments()
            
            // 设置初始评论数显示
            if (::tvCommentCount.isInitialized) {
                tvCommentCount.text = "0"
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "初始化失败: ${e.message}", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }
    
    private fun initViews(view: View) {
        try {
            rvComments = view.findViewById(R.id.rv_comments)
            etComment = view.findViewById(R.id.et_comment)
            btnSend = view.findViewById(R.id.btn_send)
            ivClose = view.findViewById(R.id.iv_close)
            tvCommentCount = view.findViewById(R.id.tv_comment_count)
            
            // findViewById在Kotlin中返回非空类型，不需要空检查
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("初始化视图失败: ${e.message}")
        }
    }
    
    private fun setupViewModel() {
        try {
            videoViewModel = ViewModelProvider(requireActivity())[VideoViewModel::class.java]
            
            videoViewModel.comments.observe(viewLifecycleOwner) { comments ->
                try {
                    if (comments != null && ::tvCommentCount.isInitialized) {
                        commentAdapter = CommentAdapter(comments, { comment ->
                            onCommentLikeClick(comment)
                        }, { comment ->
                            onCommentReplyClick(comment)
                        })
                        rvComments.adapter = commentAdapter
                        tvCommentCount.text = "${comments.size}"
                    } else if (comments != null) {
                        // tvCommentCount还未初始化，只更新适配器
                        commentAdapter = CommentAdapter(comments, { comment ->
                            onCommentLikeClick(comment)
                        }, { comment ->
                            onCommentReplyClick(comment)
                        })
                        rvComments.adapter = commentAdapter
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "评论数据更新失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            
            videoViewModel.error.observe(viewLifecycleOwner) { error ->
                if (error != null) {
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "ViewModel初始化失败: ${e.message}", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }
    
    private fun setupRecyclerView() {
        try {
            commentAdapter = CommentAdapter(emptyList(), { comment ->
                onCommentLikeClick(comment)
            }, { comment ->
                onCommentReplyClick(comment)
            })
            
            rvComments.layoutManager = LinearLayoutManager(context)
            rvComments.adapter = commentAdapter
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "初始化评论列表失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupClickListeners() {
        try {
            ivClose.setOnClickListener {
                dismiss()
            }
            
            btnSend.setOnClickListener {
                sendComment()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "设置点击事件失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadComments() {
        try {
            videoId?.let { id ->
                videoViewModel.loadVideoComments(id)
            } ?: run {
                Toast.makeText(context, "视频ID为空，无法加载评论", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "加载评论失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun sendComment() {
        try {
            val content = etComment.text.toString().trim()
            if (content.isEmpty()) {
                Toast.makeText(context, "请输入评论内容", Toast.LENGTH_SHORT).show()
                return
            }
            
            videoId?.let { id ->
                videoViewModel.addComment(id, content, currentUser)
                etComment.text.clear()
            } ?: run {
                Toast.makeText(context, "视频ID为空，无法发布评论", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "发布评论失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun onCommentLikeClick(comment: Comment) {
        try {
            videoViewModel.toggleCommentLike(comment)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "点赞失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun onCommentReplyClick(comment: Comment) {
        try {
            // 设置回复的用户名到输入框
            val replyText = getString(R.string.reply_format, comment.user.username)
            etComment.setText(replyText)
            etComment.setSelection(etComment.text.length)
            etComment.requestFocus()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "回复失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    companion object {
        private const val ARG_VIDEO_ID = "video_id"
        
        fun newInstance(videoId: String): CommentBottomSheet {
            return CommentBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_VIDEO_ID, videoId)
                }
            }
        }
    }
}