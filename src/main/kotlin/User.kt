import org.jboss.netty.channel.Channel
import org.slf4j.LoggerFactory

class User(var userChannel: Channel?, var login: String, val pass: String?, val color: String?, val role: String?) {

    private val logger = LoggerFactory.getLogger(User::class.java)

    constructor(login: String):this(null, login, null,null, null)

    fun sendMessage(message: String) {
        if (userChannel == null) {
            logger.error("Нет соединения с пользователем $login!")
            return
        }
        userChannel!!.write(message)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        return login == other
    }

    override fun hashCode(): Int {
        return login.hashCode()
    }
}
