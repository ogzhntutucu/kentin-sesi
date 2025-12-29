package io.github.thwisse.kentinsesi.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import io.github.thwisse.kentinsesi.data.model.Post
import io.github.thwisse.kentinsesi.databinding.ItemPostBinding

class PostAdapter(
    private val onItemClick: (Post) -> Unit
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            val context = binding.root.context
            binding.apply {
                tvTitle.text = post.title
                tvCategory.text = post.category
                tvUpvoteCount.text = context.getString(io.github.thwisse.kentinsesi.R.string.post_support_count, post.upvoteCount)
                tvCommentCount.text = context.getString(io.github.thwisse.kentinsesi.R.string.post_comment_count, post.commentCount)
                tvUpdateCount.text = "${post.updateCount} Güncelleme"

                tvLocation.text = context.getString(io.github.thwisse.kentinsesi.R.string.post_location_city_district, post.district ?: "-")

                // Durum metni
                tvStatus.text = when(post.status) {
                    "new" -> context.getString(io.github.thwisse.kentinsesi.R.string.post_status_new)
                    "in_progress" -> context.getString(io.github.thwisse.kentinsesi.R.string.post_status_in_progress)
                    "resolved" -> context.getString(io.github.thwisse.kentinsesi.R.string.post_status_resolved)
                    else -> post.status
                }

                // Resim yükleme
                ivPostImage.load(post.imageUrl) {
                    crossfade(true)
                    placeholder(android.R.drawable.progress_indeterminate_horizontal)
                }
            }

            // Karta (Root View) tıklayınca detay aç
            binding.root.setOnClickListener {
                onItemClick(post)
            }
        }
    }

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
    }
}