package com.blucor.aoneenterprises

import com.google.gson.annotations.SerializedName

data class LoginResponse(

//	@field:SerializedName("user")
//	val user: MutableList<User>,
	@field:SerializedName("Success")
	val success: Boolean? = null,

	val message:String
)
data class User(

	@field:SerializedName("role")
	val role: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("mobileno")
	val mobileno: String? = null,

	@field:SerializedName("fullname")
	val fullname: String? = null,

	@field:SerializedName("email")
	val email: String? = null
)
