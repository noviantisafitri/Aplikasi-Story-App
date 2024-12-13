package com.dicoding.noviantisafitri.storyapp.ui.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.dicoding.noviantisafitri.storyapp.R
import com.dicoding.noviantisafitri.storyapp.data.di.timeStamptoString
import com.dicoding.noviantisafitri.storyapp.databinding.ActivityDetailBinding
import com.dicoding.noviantisafitri.storyapp.responses.ListStoryItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        setupData()
    }

    private fun setupData() {
        binding.apply {
            progressBar.visibility = View.VISIBLE
            tvErrorMessage.visibility = View.GONE
            tvDetailName.visibility = View.GONE
            tvDetailDescription.visibility = View.GONE
            ivDetailPhoto.visibility = View.GONE
            tvDivider.visibility = View.GONE
        }

        lifecycleScope.launch {
            delay(2000)
            val data = intent.getParcelableExtra<ListStoryItem>(EXTRA_DATA)

            if (data != null) {
                binding.apply {
                    tvDetailName.text = data.name
                    tvDetailDescription.text = data.description
                    tvDetailDate.text = data.createdAt.timeStamptoString()
                    Glide.with(this@DetailActivity)
                        .load(data.photo)
                        .fitCenter()
                        .apply(RequestOptions
                            .placeholderOf(R.drawable.ic_loading)
                            .error(R.drawable.ic_error))
                        .into(ivDetailPhoto)

                    progressBar.visibility = View.GONE
                    tvDetailName.visibility = View.VISIBLE
                    tvDetailDescription.visibility = View.VISIBLE
                    tvDetailDate.visibility = View.VISIBLE
                    ivDetailPhoto.visibility = View.VISIBLE
                    tvDivider.visibility = View.VISIBLE
                }
            } else {
                binding.apply {
                    progressBar.visibility = View.GONE
                    tvErrorMessage.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupView() {
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val data = intent.getParcelableExtra<ListStoryItem>(EXTRA_DATA)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = data?.name ?: "Detail Story"
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    companion object {
        const val EXTRA_DATA = "extra_data"
    }
}