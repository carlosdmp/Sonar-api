package com.example

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import java.util.*
import io.ktor.swagger.experimental.*
import io.ktor.auth.*
import io.ktor.http.*

/**
 * Sonar API
 * 
 * Sonar REST api first version
 */
class SonarAPIServer(val myjwt: MyJWT) {
    /**
     * message
     */
    fun Routing.registerMessage() {
        authenticate("petstore_auth") {
            post("/message") {
                val body = call.getBodyParam<Message>("body") 

                if (false) httpException(HttpStatusCode.MethodNotAllowed)

                call.respond("")
            }
        }

        authenticate("petstore_auth") {
            put("/message") {
                val body = call.getBodyParam<Message>("body") 

                if (false) httpException(HttpStatusCode.BadRequest)
                if (false) httpException(HttpStatusCode.NotFound)
                if (false) httpException(HttpStatusCode.MethodNotAllowed)

                call.respond("")
            }
        }

//        authenticate("petstore_auth") {
//            get("/message/fetch") {
//                val body = call.getBodyParam<Message>("body")
//
//                if (false) httpException(HttpStatusCode.BadRequest)
//
//                call.respond(listOf())
//            }
//        }
    }

    /**
     * user
     */
    fun Routing.registerUser() {
        post("/user") {
            val body = call.getBodyParam<User>("body") 

            call.respond(Unit)
        }

        post("/user/createWithArray") {
            val body = call.getBodyParam<List<User>>("body") 

            call.respond(Unit)
        }

        post("/user/createWithList") {
            val body = call.getBodyParam<List<User>>("body") 

            call.respond(Unit)
        }

        get("/user/login") {
            val username = call.getQuery<String>("username") 
            val password = call.getQuery<String>("password") 

            if (false) httpException(HttpStatusCode.BadRequest)

            call.respond("")
        }

        get("/user/logout") {
            call.respond(Unit)
        }

        get("/user/{username}") {
            val username = call.getPath<String>("username") 

            if (false) httpException(HttpStatusCode.BadRequest)
            if (false) httpException(HttpStatusCode.NotFound)

            call.respond(User(
                id = 0,
                username = "username",
                firstName = "firstName",
                lastName = "lastName",
                email = "email",
                password = "password",
                phone = "phone",
                userStatus = 0
            ))
        }

        put("/user/{username}") {
            val username = call.getPath<String>("username") 
            val body = call.getBodyParam<User>("body") 

            if (false) httpException(HttpStatusCode.BadRequest)
            if (false) httpException(HttpStatusCode.NotFound)

            call.respond("")
        }

        delete("/user/{username}") {
            val username = call.getPath<String>("username") 

            if (false) httpException(HttpStatusCode.BadRequest)
            if (false) httpException(HttpStatusCode.NotFound)

            call.respond("")
        }
    }
}
