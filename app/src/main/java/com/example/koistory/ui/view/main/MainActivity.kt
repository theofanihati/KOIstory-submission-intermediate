package com.example.koistory.ui.view.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koistory.data.response.ListStoryItem
import com.example.koistory.databinding.ActivityMainBinding
import com.example.koistory.ui.view.CombinedViewModel
import com.example.koistory.ui.view.detail.DetailActivity
import com.example.koistory.ui.view.adapter.StoryAdapter
import com.example.koistory.ui.view.ViewModelFactory
import com.example.koistory.ui.view.add_story.AddStoryActivity
import com.example.koistory.ui.view.welcome.WelcomeActivity
import android.provider.Settings
import android.util.Log
import com.example.koistory.R
import com.example.koistory.ui.view.adapter.LoadingStateAdapter
import com.example.koistory.ui.view.maps.MapsActivity

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<CombinedViewModel> {
        ViewModelFactory.getInstance(application,this)
    }
    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManagerUpcoming = LinearLayoutManager(this)
        binding.rvStory.layoutManager = layoutManagerUpcoming

        setupView()
        setupAction()

        viewModel.errorMessage.observe(this) {
            message -> if (message != null) {
                Toast.makeText(this, "$message", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(this) {
            isLoading -> showLoading(isLoading)
        }

        viewModel.stories.observe(this) {
            stories -> setStory(stories)
        }

        viewModel.getStories()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupAction() {
        binding.actionLogout.setOnClickListener {
            viewModel.logout()
            AlertDialog.Builder(this).apply {
                setTitle("Yeah!")
                setMessage(R.string.alert_logout_success)
                setPositiveButton("OK") { _, _ ->
                    val intent = Intent(context, WelcomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                create()
                show()
            }
        }
        binding.ivSettings.setOnClickListener{
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
        }
        binding.ivMaps.setOnClickListener{
            startActivity(Intent(this, MapsActivity::class.java))
        }
        binding.fabAdd.setOnClickListener{
            startActivity(Intent(this, AddStoryActivity::class.java))
        }
    }

    private fun setStory(listStories: List<ListStoryItem>) {
        if(listStories != null){
            val adapter = StoryAdapter()
            Log.d("MainActivity", "dapet ni $listStories")
//            adapter.submitList(listStories)
            binding.rvStory.adapter = adapter.withLoadStateFooter(
                footer = LoadingStateAdapter {
                    adapter.retry()
                }
            )

            viewModel.pagedStory.observe(this, {
                adapter.submitData(lifecycle, it)
            })

            adapter.setOnItemClickCallback(object : StoryAdapter.OnItemClickCallback {
                override fun onItemClicked(data: ListStoryItem) {
                    showSelectedStory(data)
                }
            })
        } else {
            Toast.makeText(this, R.string.tidak_ada_data, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSelectedStory(story: ListStoryItem) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra("story_id", story.id)
        }
        startActivity(intent)
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
}