package ru.gdcn.gdegde

import org.jboss.netty.channel.Channel
import org.slf4j.LoggerFactory
import ru.gdcn.gdegde.database.DBEntity

class User(var userChannel: Channel?, var login: String, val pass: String?, val color: String?, val role: Role?) : DBEntity {

    private val logger = LoggerFactory.getLogger(User::class.java)

    constructor(login: String):this(null, login, null,null, null)

    override fun getValuesMap(): Map<String, Any> {
        val map = HashMap<String, Any>()
        map["name"] = login
        map["password"] = pass!!
        map["color"] = color!!
        when (role){
            Role.USER -> map["fk_role"] = 1
            Role.ADMIN -> map["fk_role"] = 2
        }
        return map
    }

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

        return login == (other as User).login
    }

    override fun hashCode(): Int {
        return login.hashCode()
    }
}
