package com.example.koistory.data.response

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
	@field:SerializedName("error")
	val error: Boolean? = null,
	@field:SerializedName("message")
	val message: String? = null
)

data class ErrorResponse(
	@field:SerializedName("error")
	val error: Boolean? = null,
	@field:SerializedName("message")
	val message: String? = null
)