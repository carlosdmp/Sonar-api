package com.example

import java.util.*
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.server.testing.*
import io.ktor.swagger.experimental.*
import kotlin.test.*

class SwaggerRoutesTest {
    /**
     * @see SonarAPIServer.sendMessage
     */
    @Test
    fun testSendMessage() {
        withTestApplication {
            // @TODO: Adjust path as required
            handleRequest(HttpMethod.Post, "/message") {
                // @TODO: Your body here
                setBodyJson(mapOf<String, Any?>())
            }.apply {
                // @TODO: Your test here
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    /**
     * @see SonarAPIServer.editMessage
     */
    @Test
    fun testEditMessage() {
        withTestApplication {
            // @TODO: Adjust path as required
            handleRequest(HttpMethod.Put, "/message") {
                // @TODO: Your body here
                setBodyJson(mapOf<String, Any?>())
            }.apply {
                // @TODO: Your test here
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    /**
     * @see SonarAPIServer.fetchMessages
     */
    @Test
    fun testFetchMessages() {
        withTestApplication {
            // @TODO: Adjust path as required
            handleRequest(HttpMethod.Get, "/message/fetch") {
            }.apply {
                // @TODO: Your test here
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    /**
     * @see SonarAPIServer.createUser
     */
    @Test
    fun testCreateUser() {
        withTestApplication {
            // @TODO: Adjust path as required
            handleRequest(HttpMethod.Post, "/user") {
                // @TODO: Your body here
                setBodyJson(mapOf<String, Any?>())
            }.apply {
                // @TODO: Your test here
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    /**
     * @see SonarAPIServer.createUsersWithArrayInput
     */
    @Test
    fun testCreateUsersWithArrayInput() {
        withTestApplication {
            // @TODO: Adjust path as required
            handleRequest(HttpMethod.Post, "/user/createWithArray") {
                // @TODO: Your body here
                setBodyJson(mapOf<String, Any?>())
            }.apply {
                // @TODO: Your test here
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    /**
     * @see SonarAPIServer.createUsersWithListInput
     */
    @Test
    fun testCreateUsersWithListInput() {
        withTestApplication {
            // @TODO: Adjust path as required
            handleRequest(HttpMethod.Post, "/user/createWithList") {
                // @TODO: Your body here
                setBodyJson(mapOf<String, Any?>())
            }.apply {
                // @TODO: Your test here
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    /**
     * @see SonarAPIServer.loginUser
     */
    @Test
    fun testLoginUser() {
        withTestApplication {
            // @TODO: Adjust path as required
            handleRequest(HttpMethod.Get, "/user/login") {
            }.apply {
                // @TODO: Your test here
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    /**
     * @see SonarAPIServer.logoutUser
     */
    @Test
    fun testLogoutUser() {
        withTestApplication {
            // @TODO: Adjust path as required
            handleRequest(HttpMethod.Get, "/user/logout") {
            }.apply {
                // @TODO: Your test here
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    /**
     * @see SonarAPIServer.getUserByName
     */
    @Test
    fun testGetUserByName() {
        withTestApplication {
            // @TODO: Adjust path as required
            handleRequest(HttpMethod.Get, "/user/{username}") {
            }.apply {
                // @TODO: Your test here
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    /**
     * @see SonarAPIServer.updateUser
     */
    @Test
    fun testUpdateUser() {
        withTestApplication {
            // @TODO: Adjust path as required
            handleRequest(HttpMethod.Put, "/user/{username}") {
                // @TODO: Your body here
                setBodyJson(mapOf<String, Any?>())
            }.apply {
                // @TODO: Your test here
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    /**
     * @see SonarAPIServer.deleteUser
     */
    @Test
    fun testDeleteUser() {
        withTestApplication {
            // @TODO: Adjust path as required
            handleRequest(HttpMethod.Delete, "/user/{username}") {
            }.apply {
                // @TODO: Your test here
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    fun <R> withTestApplication(test: TestApplicationEngine.() -> R): R {
        return withApplication(createTestEnvironment()) {
            (environment.config as MapApplicationConfig).apply {
                put("jwt.secret", "TODO-change-this-supersecret-or-use-SECRET-env")
            }
            application.module()
            test()
        }
    }

    fun TestApplicationRequest.setBodyJson(value: Any?) = setBody(Json.stringify(value))
}
