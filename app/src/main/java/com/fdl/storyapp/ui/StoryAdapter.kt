package com.fdl.storyapp.ui

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.navigation.Navigation
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.fdl.storyapp.R
import com.fdl.storyapp.databinding.StoryListItemBinding
import com.fdl.storyapp.model.StoryModel
import com.fdl.storyapp.ui.home.HomeFragmentDirections
import com.fdl.storyapp.utilities.withDateFormat

class StoryAdapter : PagingDataAdapter<StoryModel, StoryAdapter.ListViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListViewHolder {
        val binding =
            StoryListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val story = getItem(position)
        story?.let { holder.bind(it) }
    }

    inner class ListViewHolder(private var binding: StoryListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(story: StoryModel) {
            Glide.with(binding.root)
                .load(story.imageUrl)
                .placeholder(R.drawable.image_placeholder)
                .signature(ObjectKey(story.imageUrl ?: story.id))
                .into(binding.ivImage)

            binding.apply {
                tvName.text = story.name
                tvDate.text = story.createdAt?.withDateFormat()
                root.setOnClickListener {
                    Navigation.findNavController(root).navigate(
                        HomeFragmentDirections.actionHomeFragmentToStoryDetailFragment(
                            story
                        )
                    )
                }
            }
        }
    }

    companion object {
        val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<StoryModel>() {
                override fun areItemsTheSame(oldItem: StoryModel, newItem: StoryModel): Boolean =
                    oldItem == newItem

                override fun areContentsTheSame(oldItem: StoryModel, newItem: StoryModel): Boolean =
                    oldItem.id == newItem.id
            }
    }
}