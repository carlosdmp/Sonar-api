package cdmp.app

import cdmp.app.db.Database
import cdmp.app.model.Message
import cdmp.app.model.MessageResponse
import cdmp.app.model.UserLocationPair
import com.google.gson.Gson
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.channels.*
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.*

/**
 * Class in charge of the logic of the chat server.
 * It contains handlers to events and commands to send messages to specific users in the server.
 */
class ChatServer {
    /**
     * Atomic counter used to get unique user-names based on the maxiumum users the server had.
     */
    val usersCounter = AtomicInteger()

    /**
     * A concurrent map associating session IDs to user names.
     */
    val memberNames = ConcurrentHashMap<String, String>()


    val members = ConcurrentHashMap<String, WebSocketSession>()

    /**
     * A list of the lastest messages sent to the server, so new members can have a bit context of what
     * other people was talking about before joining.
     */
    val lastMessages = LinkedList<String>()

    /**
     * Handles that a member identified with a session id and a socket joined.
     */
    suspend fun memberJoin(member: String, socket: WebSocketSession) {
        // Checks if this user is already registered in the server and gives him/her a temporal name if required.
        val name = memberNames.computeIfAbsent(member) { "user${usersCounter.incrementAndGet()}" }

        members[member] = socket

        broadcast("server", "Member joined: $name.")

        // Sends the user the latest messages from this server to let the member have a bit context.
//        val messages = synchronized(lastMessages) { lastMessages.toList() }
//        for (message in messages) {
//            socket.send(Frame.Text(message))
//        }
    }

    /**
     * Handles a [member] idenitified by its session id renaming [to] a specific name.
     */
    suspend fun memberRenamed(member: String, to: String) {
        // Re-sets the member name.
        val oldName = memberNames.put(member, to) ?: member
        // Notifies everyone in the server about this change.
        broadcast("server", "Member renamed from $oldName to $to")
    }

    /**
     * Handles that a [member] with a specific [socket] left the server.
     */
    suspend fun memberLeft(member: String) {
        // Removes the socket connection for this member
        members.remove(member)
        val name = memberNames.remove(member) ?: member
        broadcast("server", "Member left: $name.")

    }

    /**
     * Handles the 'who' command by sending the member a list of all all members names in the server.
     */
    suspend fun who(sender: String) {
        members[sender]?.send(Frame.Text(memberNames.values.joinToString(prefix = "[server::who] ")))
    }

    /**
     * Handles the 'help' command by sending the member a list of available commands.
     */
    suspend fun help(sender: String) {
        members[sender]?.send(Frame.Text("[server::help] Possible commands are: /user, /help and /who"))
    }

    /**
     * Handles sending to a [recipient] from a [sender] a [message].
     *
     * Both [recipient] and [sender] are identified by its session-id.
     */
    suspend fun sendTo(recipient: String, sender: String, message: String) {
        members[recipient]?.send(Frame.Text("[$sender] $message"))
    }

    /**
     * Handles a [message] sent from a [sender] by notifying the rest of the users.
     */
    suspend fun message(user: UserLocationPair, message: Message) {
        // Pre-format the message to be send, to prevent doing it for all the users or connected sockets.
        val formatted = "[${user.id}] $message"
        Database.registerMessage(message)
        // Sends this pre-formatted message to all the members in the server.
        broadcast(formatted)

        // Appends the message to the list of [lastMessages] and caps that collection to 100 items to prevent
        // growing too much.
//        synchronized(lastMessages) {
//            lastMessages.add(formatted)
//            if (lastMessages.size > 100) {
//                lastMessages.removeFirst()
//            }
//        }
    }

    /**
     * Sends a [message] to all the members in the server, including all the connections per member.
     */
    private suspend fun broadcast(message: String) {
        members.values.forEach { socket ->
            socket.send(Frame.Text(
                Gson().toJson(
                    MessageResponse(
                        message = message,
                        id = "",
                        createdAt = "",
                        geoPos = null,
                        userId = ""
                    )
                )
            ))
        }
    }

    /**
     * Sends a [message] coming from a [sender] to all the members in the server, including all the connections per member.
     */
    private suspend fun broadcast(sender: String, message: String) {
        val name = memberNames[sender] ?: sender
        broadcast("[$name] $message")
    }

    /**
     * Sends a [message] to a list of [this] [WebSocketSession].
     */
    suspend fun List<WebSocketSession>.send(frame: Frame) {
        forEach {
            try {
                it.send(frame.copy())
            } catch (t: Throwable) {
                try {
                    it.close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, ""))
                } catch (ignore: ClosedSendChannelException) {
                    // at some point it will get closed
                }
            }
        }
    }
}