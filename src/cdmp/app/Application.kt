package cdmp.app

import cdmp.app.datatype.safeCall
import cdmp.app.db.Database
import cdmp.app.model.User
import cdmp.app.model.UserLogin
import cdmp.app.swagger.experimental.HttpException
import cdmp.app.swagger.experimental.getBodyParam
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.request.receiveOrNull
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.css.*
import kotlinx.html.*
import java.text.DateFormat

fun main(args: Array<String>) {
    val server = embeddedServer(Netty, 8888) {
        module(false)
    }
    server.start(wait = true)
}

fun Application.module(testing: Boolean = false) {
    val myjwt = MyJWT(secret = "my-super-secret-for-jwt")

    val client = HttpClient(Apache) {
    }

    install(ContentNegotiation) {

        gson {
            setDateFormat(DateFormat.LONG)
            setPrettyPrinting()
        }
    }

    install(Authentication) {
        // ---------------
        // @TODO: Please, edit the application.conf # jwt.secret property and provide a secure random value for it
        // ---------------
        // null
        jwt("sonar_auth") {
            authSchemes("Bearer", "Token")
            verifier(myjwt.verifier)
            validate {
                UserIdPrincipal(it.payload.getClaim("name").asString())
            }
        }
        // ---------------
        // @TODO: Please, edit the application.conf # jwt.secret property and provide a secure random value for it
        // ---------------
        // null
        jwt("api_key") {
            authSchemes("Bearer", "Token")
            verifier(myjwt.verifier)
            validate {
                UserIdPrincipal(it.payload.getClaim("name").asString())
            }
        }
    }

    routing {

        post("/user") {
            safeCall {
                checkNotNull(call.receiveOrNull(UserLogin::class))
            }.fold({
                call.respond(HttpStatusCode.BadRequest)
            }, {
                safeCall {  Database.logUser(it.token) }.fold(
                    {
                        call.respond(HttpStatusCode.OK, body)
                    },{
                        call.respond(HttpStatusCode.InternalServerError, it.whenLeft { it.message })
                    }
                )

            })
        }

        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        get("/html-dsl") {
            call.respondHtml {
                body {
                    h1 { +"HTML" }
                    ul {
                        for (n in 1..10) {
                            li { +"$n" }
                        }
                    }
                }
            }
        }

        get("/styles.css") {
            call.respondCss {
                body {
                    backgroundColor = Color.red
                }
                p {
                    fontSize = 2.em
                }
                rule("p.myclass") {
                    color = Color.blue
                }
            }
        }

        install(StatusPages) {
            exception<AuthenticationException> { cause ->
                call.respond(HttpStatusCode.Unauthorized)
            }
            exception<AuthorizationException> { cause ->
                call.respond(HttpStatusCode.Forbidden)
            }

            exception<HttpException> { cause ->
                call.respond(cause.code, cause.description)
            }
            exception<Throwable> { cause ->
                call.respond(cause.message ?: "unknown error")

            }
        }

        get("/json/jackson") {
            call.respond(mapOf("hello" to "world"))
        }
//
//        SonarAPIServer(myjwt).apply {
//            messageModule()
//            userModule()
//        }
    }
}

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()

open class MyJWT(val secret: String) {
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier = JWT.require(algorithm).build()
    fun sign(name: String): String = JWT.create().withClaim("name", name).sign(algorithm)
}

fun FlowOrMetaDataContent.styleCss(builder: CSSBuilder.() -> Unit) {
    style(type = ContentType.Text.CSS.toString()) {
        +CSSBuilder().apply(builder).toString()
    }
}

fun CommonAttributeGroupFacade.style(builder: CSSBuilder.() -> Unit) {
    this.style = CSSBuilder().apply(builder).toString().trim()
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}
