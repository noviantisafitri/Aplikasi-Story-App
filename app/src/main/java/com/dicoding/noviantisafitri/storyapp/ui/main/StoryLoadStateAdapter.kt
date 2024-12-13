package com.dicoding.noviantisafitri.storyapp.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.noviantisafitri.storyapp.databinding.ItemLoadingStateBinding

class StoryLoadStateAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<StoryLoadStateAdapter.LoadingStateViewHolder>() {

    inner class LoadingStateViewHolder(private val binding: ItemLoadingStateBinding, retry: () -> Unit) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnRetry.setOnClickListener { retry.invoke() }
        }

        fun bind(loadState: LoadState) {
            binding.apply {
                pbLoad.isVisible = loadState is LoadState.Loading
                btnRetry.isVisible = loadState is LoadState.Error
                tvEmpty.isVisible = loadState is LoadState.NotLoading && loadState.endOfPaginationReached
            }
        }
    }

    override fun onBindViewHolder(holder: LoadingStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): LoadingStateViewHolder {
        val binding = ItemLoadingStateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LoadingStateViewHolder(binding, retry)
    }
}