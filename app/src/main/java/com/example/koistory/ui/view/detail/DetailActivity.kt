package com.example.koistory.ui.view.detail

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.example.koistory.R
import com.example.koistory.data.response.Story
import com.example.koistory.databinding.ActivityDetailBinding
import com.example.koistory.ui.view.CombinedViewModel
import com.example.koistory.ui.view.ViewModelFactory

class DetailActivity : AppCompatActivity() {
    private val viewModel by viewModels<CombinedViewModel> {
        ViewModelFactory.getInstance(application,this)
    }
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storyId = intent.getStringExtra("story_id")
        if (storyId != null){
            showLoading(true)
            viewModel.getStoryById(storyId)
        }

        viewModel.story.observe(this) { story ->
            if (story != null) {
                setStoryData(story)
            } else {
                Toast.makeText(this, R.string.tidak_ada_data, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.errorMessage.observe(this) {
            message -> if (message != null) {
                Toast.makeText(this, "$message", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(this) {
                isLoading -> showLoading(isLoading)
        }
    }

    private fun setStoryData(story: Story) {
        Glide.with(this)
            .load(story.photoUrl)
            .into(binding.ivDetailPhoto)
        binding.tvDetailName.text = story.name
        binding.tvDateCreated.text = story.createdAt
        binding.tvDetailDescription.text = HtmlCompat.fromHtml(
            story.description ?: "",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
}