package com.example

import cdmp.app.model.User
import io.ktor.client.*
import io.ktor.client.request.*

/**
 * Sonar API Client
 * 
 * Sonar REST api first version
 */
open class SonarAPIClient(val endpoint: String, val client: HttpClient = HttpClient()) {
    /**
     * Send a message to the world
     * 
     * @param body Message object
     * 
     * @return OK
     */
    suspend fun sendMessage(
        body: Message // BODY
    ): String {
        return client.post<String>("$endpoint/message") {
            this.body = mutableMapOf<String, Any?>().apply {
                this["body"] = body
            }
        }
    }

    /**
     * Edit an existing message
     * 
     * @param body Message object
     * 
     * @return OK
     */
    suspend fun editMessage(
        body: Message // BODY
    ): String {
        return client.put<String>("$endpoint/message") {
            this.body = mutableMapOf<String, Any?>().apply {
                this["body"] = body
            }
        }
    }

    /**
     * Finds messages to show
     * 
     * Fetch messages by location and timestamp
     * 
     * @param body Message object
     * 
     * @return successful operation
     */
    suspend fun fetchMessages(
        body: Message // BODY
    ): List<Message> {
        return client.get<List<Message>>("$endpoint/message/fetch") {
            this.body = mutableMapOf<String, Any?>().apply {
                this["body"] = body
            }
        }
    }

    /**
     * Create user
     * 
     * This can only be done by the logged in user.
     * 
     * @param body Created user object
     * 
     * @return successful operation
     */
    suspend fun createUser(
        body: User // BODY
    ): Unit {
        return client.post<Unit>("$endpoint/user") {
            this.body = mutableMapOf<String, Any?>().apply {
                this["body"] = body
            }
        }
    }

    /**
     * Creates list of users with given input array
     * 
     * @param body List of user object
     * 
     * @return successful operation
     */
    suspend fun createUsersWithArrayInput(
        body: List<User> // BODY
    ): Unit {
        return client.post<Unit>("$endpoint/user/createWithArray") {
            this.body = mutableMapOf<String, Any?>().apply {
                this["body"] = body
            }
        }
    }

    /**
     * Creates list of users with given input array
     * 
     * @param body List of user object
     * 
     * @return successful operation
     */
    suspend fun createUsersWithListInput(
        body: List<User> // BODY
    ): Unit {
        return client.post<Unit>("$endpoint/user/createWithList") {
            this.body = mutableMapOf<String, Any?>().apply {
                this["body"] = body
            }
        }
    }

    /**
     * Logs user into the system
     * 
     * @param username The user name for login
     * @param password The password for login in clear text
     * 
     * @return successful operation
     */
    suspend fun loginUser(
        username: String, // QUERY
        password: String // QUERY
    ): String {
        return client.get<String>("$endpoint/user/login") {
            this.url {
                this.parameters.apply {
                    this.append("username", "$username")
                    this.append("password", "$password")
                }
            }
        }
    }

    /**
     * Logs out current logged in user session
     * 
     * @return successful operation
     */
    suspend fun logoutUser(
    ): Unit {
        return client.get<Unit>("$endpoint/user/logout") {
        }
    }

    /**
     * Get user by user name
     * 
     * @param username The name that needs to be fetched. Use user1 for testing.
     * 
     * @return successful operation
     */
    suspend fun getUserByName(
        username: String // PATH
    ): User {
        return client.get<User>("$endpoint/user/$username") {
        }
    }

    /**
     * Updated user
     * 
     * This can only be done by the logged in user.
     * 
     * @param username name that need to be updated
     * @param body Updated user object
     * 
     * @return OK
     */
    suspend fun updateUser(
        username: String, // PATH
        body: User // BODY
    ): String {
        return client.put<String>("$endpoint/user/$username") {
            this.body = mutableMapOf<String, Any?>().apply {
                this["body"] = body
            }
        }
    }

    /**
     * Delete user
     * 
     * This can only be done by the logged in user.
     * 
     * @param username The name that needs to be deleted
     * 
     * @return OK
     */
    suspend fun deleteUser(
        username: String // PATH
    ): String {
        return client.delete<String>("$endpoint/user/$username") {
        }
    }
}
