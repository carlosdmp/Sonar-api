package cdmp.app.model

data class User(
    val id: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val userStatus: Int
)

data class UserLogin(
    val token: String
)