package com.dicoding.noviantisafitri.storyapp.responses

import com.google.gson.annotations.SerializedName

data class LoginResponse(

	@field:SerializedName("error")
	val error: Boolean,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("loginResult")
	val loginResult: LoginResult? = null
)

data class LoginResult(

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("token")
	val token: String
)
