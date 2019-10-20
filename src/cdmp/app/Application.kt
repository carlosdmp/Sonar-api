package cdmp.app

import cdmp.app.datatype.safeCall
import cdmp.app.db.Database
import cdmp.app.model.Message
import cdmp.app.model.UserLocationPair
import cdmp.app.model.UserLogin
import cdmp.app.swagger.experimental.HttpException
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.gson.Gson
import io.ktor.application.*
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
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.sessions.*
import io.ktor.util.generateNonce
import io.ktor.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.css.*
import kotlinx.html.*
import java.lang.Exception
import java.text.DateFormat

private val server = ChatServer()

fun main(args: Array<String>) {
    val server = embeddedServer(Netty, 8888) {
        module(false)
    }
    server.start(wait = true)
}

fun Application.module(testing: Boolean = false) {
    val currentUsers = mutableMapOf<UserLocationPair, DefaultWebSocketServerSession>()
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

    install(WebSockets)

    install(Sessions) {
        cookie<ChatSession>("SESSION")
    }

    intercept(ApplicationCallPipeline.Features) {
        if (call.sessions.get<ChatSession>() == null) {
            call.sessions.set(ChatSession(generateNonce()))
        }
    }
    routing {

        post("/user") {
            safeCall {
                checkNotNull(call.receiveOrNull(UserLogin::class))
            }.fold({
                call.respond(HttpStatusCode.BadRequest)
            }, {
                safeCall { Database.logUser(it.token) }.fold(
                    {
                        call.respond(HttpStatusCode.InternalServerError, it.message ?: "Unknown error")
                    }, {
                        call.respond(HttpStatusCode.OK, body)
                    }
                )
            })
        }

        webSocket("/channel-session") {
            val session = call.sessions.get<ChatSession>()
            if (session == null) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
                return@webSocket
            }

            // We notify that a member joined by calling the server handler [memberJoin]
            // This allows to associate the session id to a specific WebSocket connection.
            server.memberJoin(session.id, this)

            try {
                // We starts receiving messages (frames).
                // Since this is a coroutine. This coroutine is suspended until receiving frames.
                // Once the connection is closed, this consumeEach will finish and the code will continue.
                for (frame in incoming) {
                    // Frames can be [Text], [Binary], [Ping], [Pong], [Close].
                    // We are only interested in textual messages, so we filter it.
                    if (frame is Frame.Text) {
                        // Now it is time to process the text sent from the user.
                        // At this point we have context about this connection, the session, the text and the server.
                        // So we have everything we need.
                        receivedMessage(session.id, frame.readText())
                    }
                }
            } catch (e: Exception) {
                print(e)
            } finally {
                // Either if there was an error, of it the connection was closed gracefully.
                // We notify the server that the member left.
                server.memberLeft(session.id)
            }
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

/**
 * We received a message. Let's process it.
 */
private suspend fun receivedMessage(id: String, rawFrameText: String) {
    // We are going to handle commands (text starting with '/') and normal messages
    val frameText = rawFrameText.trim()
    when {
        // The frameText `who` responds the user about all the member names connected to the user.
        frameText.startsWith("/who") -> server.who(id)
        // The frameText `user` allows the user to set its name.
        frameText.startsWith("/user") -> {
            // We strip the frameText part to get the rest of the parameters.
            // In this case the only parameter is the user's newName.
            val newName = frameText.removePrefix("/user").trim()
            // We verify that it is a valid name (in terms of length) to prevent abusing
            when {
                newName.isEmpty() -> server.sendTo(id, "server::help", "/user [newName]")
                newName.length > 50 -> server.sendTo(
                    id,
                    "server::help",
                    "new name is too long: 50 characters limit"
                )
                else -> server.memberRenamed(id, newName)
            }
        }
        // The frameText 'help' allows users to get a list of available commands.
        frameText.startsWith("/help") -> server.help(id)
        // If no commands matched at this point, we notify about it.
        frameText.startsWith("/") -> server.sendTo(
            id,
            "server::help",
            "Unknown frameText ${frameText.takeWhile { !it.isWhitespace() }}"
        )
        else -> {
            val message = Gson().fromJson(frameText, Message::class.java)
            val userLocation = UserLocationPair(message.userId, message.geoPos.toString())
            if (message.message.isNotEmpty()) {
                server.message(userLocation, message)
            }
        }
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

data class ChatSession(val id: String)
