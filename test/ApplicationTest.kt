package com.example

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.html.*
import kotlinx.html.*
import kotlinx.css.*
import io.ktor.features.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import com.fasterxml.jackson.databind.*
import io.ktor.jackson.*
import io.ktor.auth.*
import kotlin.reflect.*
import java.util.*
import io.ktor.auth.jwt.*
import com.auth0.jwt.*
import com.auth0.jwt.algorithms.*
import io.ktor.swagger.experimental.*
import kotlin.test.*
import io.ktor.server.testing.*

class ApplicationTest {
    @Test
    fun testRoot() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("HELLO WORLD!", response.content)
            }
        }
    }
}
