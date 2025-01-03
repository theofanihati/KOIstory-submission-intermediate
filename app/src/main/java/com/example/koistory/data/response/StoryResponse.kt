package com.example.koistory.data.response

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class StoryResponse(
	@field:SerializedName("listStory")
	val listStory: List<ListStoryItem> = emptyList(),
	@field:SerializedName("error")
	val error: Boolean? = null,
	@field:SerializedName("message")
	val message: String? = null
)

@Entity(tableName = "story")
data class ListStoryItem(
	@field:SerializedName("photoUrl")
	val photoUrl: String? = null,
	@field:SerializedName("createdAt")
	val createdAt: String? = null,
	@field:SerializedName("name")
	val name: String? = null,
	@field:SerializedName("description")
	val description: String? = null,
	@field:SerializedName("lon")
	val lon: Double,
	@PrimaryKey
	@field:SerializedName("id")
	val id: String,
	@field:SerializedName("lat")
	val lat: Double
)

data class DetailStoryResponse(
	@field:SerializedName("story")
	val story: Story? = null,
	@field:SerializedName("error")
	val error: Boolean? = null,
	@field:SerializedName("message")
	val message: String? = null
)

data class FileUploadResponse(
	@field:SerializedName("error")
	val error: Boolean,
	@field:SerializedName("message")
	val message: String
)

data class Story(
	@field:SerializedName("photoUrl")
	val photoUrl: String? = null,
	@field:SerializedName("createdAt")
	val createdAt: String? = null,
	@field:SerializedName("name")
	val name: String? = null,
	@field:SerializedName("description")
	val description: String? = null,
	@field:SerializedName("lon")
	val lon: Double? = null,
	@field:SerializedName("id")
	val id: String? = null,
	@field:SerializedName("lat")
	val lat: Double? = null
)