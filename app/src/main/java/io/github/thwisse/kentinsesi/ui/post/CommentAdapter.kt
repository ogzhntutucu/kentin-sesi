package io.github.thwisse.kentinsesi.ui.post

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.thwisse.kentinsesi.data.model.Comment
import io.github.thwisse.kentinsesi.databinding.ItemCommentBinding
import io.github.thwisse.kentinsesi.util.Constants
import java.text.SimpleDateFormat
import java.util.Locale

class CommentAdapter(
    private val onReplyClick: ((Comment) -> Unit)? = null
) : ListAdapter<Comment, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CommentViewHolder(private val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            binding.tvAuthorName.text = comment.authorName
            binding.tvCommentText.text = comment.text

            binding.tvReplyingTo.isVisible = !comment.replyToAuthorName.isNullOrBlank()
            binding.tvReplyingTo.text = if (!comment.replyToAuthorName.isNullOrBlank()) {
                "${comment.replyToAuthorName} kişisine yanıt"
            } else {
                ""
            }

            val canReply = comment.depth < Constants.MAX_COMMENT_DEPTH
            binding.btnReply.isVisible = canReply
            binding.btnReply.setOnClickListener {
                if (canReply) onReplyClick?.invoke(comment)
            }

            // Tarihi formatla (Örn: 12 May, 14:30)
            val date = comment.createdAt
            if (date != null) {
                val format = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                binding.tvCommentDate.text = format.format(date)
            } else {
                binding.tvCommentDate.text = "Az önce"
            }

            val density = binding.root.resources.displayMetrics.density
            val basePadding = (16 * density).toInt()
            val indent = (comment.depth.coerceIn(0, Constants.MAX_COMMENT_DEPTH) * 16 * density).toInt()
            binding.root.setPaddingRelative(
                basePadding + indent,
                binding.root.paddingTop,
                basePadding,
                binding.root.paddingBottom
            )
        }
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.id.isNotBlank() && newItem.id.isNotBlank() && oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean = oldItem == newItem
    }
}