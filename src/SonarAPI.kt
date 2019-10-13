package com.example

import java.util.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.swagger.experimental.*

data class Message(
    val id: Long,
    val sender: Long,
    val text: String
)

data class User(
    val id: Long,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phone: String,
    val userStatus: Int
)
