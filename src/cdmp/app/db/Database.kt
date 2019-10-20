package cdmp.app.db

import cdmp.app.datatype.*
import cdmp.app.model.Message
import cdmp.app.model.User
import com.mongodb.ConnectionString
import com.mongodb.MongoURI.MONGODB_PREFIX
import com.mongodb.ServerAddress
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.*

object Database {
    private val client = KMongo.createClient(ConnectionString(MONGODB_PREFIX + "127.0.0.1:27017"))
    private val database = client.getDatabase("sonar")
    val users = database.getCollection<User>()
    val messages = database.getCollection<Message>()

    fun findUser(token: String): Either<Throwable, User> = users.findOne(User::id eq token).getOrThrowable()

    fun registerUser(token: String): Either<Throwable, User> {
        val user = User(
            id = token,
            email = "",
            firstName = "",
            lastName = "",
            phone = "",
            username = "",
            userStatus = 1
        )
        return safeCall {
            users.save(user)
            user
        }
    }

    fun registerMessage(message: Message): Either<Throwable, Message> {
        return safeCall {
            messages.save(message)
            message
        }
    }

    fun logUser(token: String): Either<Throwable, User> = when (val user = findUser(token)) {
        is Either.Right -> user
        is Either.Left -> registerUser(token)
    }

}

