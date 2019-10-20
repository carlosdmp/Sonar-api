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

data class UserLocationPair(
    val id: String,
    val geoPosition: String
)

data class UserLogin(
    val token: String
)


data class Message(
    val userId: String,
    val message: String,
    val geoPos: UserLocation?,
    val createdAt: String)

data class UserLocation(
    val latitude: Double,
    val longitude: Double
)

data class MessageResponse(
    val id: String,
    val userId: String,
    val message: String,
    val geoPos: UserLocation?,
    val createdAt: String
)