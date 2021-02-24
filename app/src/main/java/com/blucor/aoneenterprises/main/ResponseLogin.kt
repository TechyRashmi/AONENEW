package com.blucor.aoneenterprises.main

import com.blucor.aoneenterprises.User

data class ResponseLogin(
    val Success: Boolean,
    val message: String,
    val user: MutableList<User>
)