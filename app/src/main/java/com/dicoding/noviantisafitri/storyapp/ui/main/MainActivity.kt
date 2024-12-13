package com.dicoding.noviantisafitri.storyapp.ui.main

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.noviantisafitri.storyapp.R
import com.dicoding.noviantisafitri.storyapp.data.di.ViewModelFactory
import com.dicoding.noviantisafitri.storyapp.databinding.ActivityMainBinding
import com.dicoding.noviantisafitri.storyapp.ui.addStory.AddStoryActivity
import com.dicoding.noviantisafitri.storyapp.ui.welcome.WelcomeActivity
import androidx.paging.LoadState

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var factory: ViewModelFactory
    private lateinit var storyAdapter: ListStoryAdapter
    private var token = ""
    private val mainViewModel: MainViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewModel()

        mainViewModel.getSession().observe(this) { session ->
            if (!session.isLogin) {
                moveActivity()
            } else {
                token = session.token
                val userName = session.name
                val helloUserText = getString(R.string.username, userName)
                supportActionBar?.title = helloUserText

                setupView()
                setupAdapter()
                setupAction()
                setupSwipeRefreshLayout()
                setupData()
            }
        }
    }

    private fun setupSwipeRefreshLayout() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            if (isNetworkAvailable()) {
                setupData()
                binding.swipeRefreshLayout.isRefreshing = false
            } else {
                showToast("No internet connection")
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun setupAction() {
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddStoryActivity::class.java))
        }
    }

    private fun setupData() {
        mainViewModel.getListStories.observe(this@MainActivity) { pagingData ->
            storyAdapter.submitData(lifecycle, pagingData)
        }
    }

    private fun setupAdapter() {
        storyAdapter = ListStoryAdapter()
        binding.rvStories.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = storyAdapter.withLoadStateFooter(
                footer = StoryLoadStateAdapter {
                    storyAdapter.retry()
                }
            )
        }

        storyAdapter.addLoadStateListener { loadState ->
            showLoading(loadState.source.refresh is LoadState.Loading)
            binding.apply {
                tvEmpty.isVisible = loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached
            }
        }
    }

    private fun setupView() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
    }

    private fun setupViewModel() {
        factory = ViewModelFactory.getInstance(this)
    }

    private fun showLoading(loading: Boolean) {
        binding.apply {
            rvStories.visibility = if (loading) View.INVISIBLE else View.VISIBLE
            swipeRefreshLayout.isRefreshing = loading
        }
    }

    private fun showToast(message: String = "") {
        mainViewModel.toastText.observe(this@MainActivity) {
            it.getContentIfNotHandled()?.let { toastText ->
                Toast.makeText(
                    this@MainActivity, toastText, Toast.LENGTH_SHORT
                ).show()
            }
        }
        if (message.isNotEmpty()) {
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun moveActivity() {
        startActivity(Intent(this@MainActivity, WelcomeActivity::class.java))
        finish()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            val networks = connectivityManager.allNetworks
            for (network in networks) {
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                if (networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true) {
                    return true
                }
            }
        }
        return false
    }

    @Deprecated("onBackPressed() is deprecated, use onBackPressedDispatcher.onBackPressed() instead.", ReplaceWith("onBackPressedDispatcher.onBackPressed()"))
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btn_logout -> {
                mainViewModel.logoutApp()
                true
            }
            R.id.btn_language -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}