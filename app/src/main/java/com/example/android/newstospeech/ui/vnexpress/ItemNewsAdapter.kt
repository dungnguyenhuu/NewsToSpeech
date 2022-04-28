package com.example.android.newstospeech.ui.vnexpress

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ExpandableListView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.newstospeech.databinding.ItemNewsBinding
import com.example.android.newstospeech.data.model.ItemNews

class ItemNewsAdapter(val itemNewsListener: ItemNewsListener) : ListAdapter<ItemNews, ItemNewsAdapter.ItemNewsViewHolder>(DiffCallback) {
    class ItemNewsViewHolder(val binding: ItemNewsBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ItemNews, itemListener: ItemNewsListener) {
            binding.itemNews = item
            binding.itemListener = itemListener
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ItemNews>() {
        override fun areItemsTheSame(oldItem: ItemNews, newItem: ItemNews): Boolean {
            return oldItem.link == newItem.link
        }

        override fun areContentsTheSame(oldItem: ItemNews, newItem: ItemNews): Boolean {
            return oldItem.content == newItem.content
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemNewsViewHolder {
        return ItemNewsViewHolder(ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ItemNewsViewHolder, position: Int) {
        holder.bind(getItem(position), itemNewsListener)
    }
}

class ItemNewsListener(
    val clickListener: (item: ItemNews) -> Unit
) {
    fun onClick(item: ItemNews) = clickListener(item)
}