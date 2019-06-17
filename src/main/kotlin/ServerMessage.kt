import org.jboss.netty.channel.Channel
import org.json.simple.JSONObject
import org.json.simple.JSONValue
import org.slf4j.LoggerFactory

/*
Объект для упаковки и распаковки сообщений.
 */
object ServerMessage {

    private val logger = LoggerFactory.getLogger(ServerMessage::class.java)
    
    fun read(message: String, userChannel: Channel) {
        logger.info("Читаю сообщение...")
        val incomingMessage = JSONValue.parse(message) as JSONObject
        val type = incomingMessage["type"].toString()
        when (type) {
            "loginAttempt" -> {
                logger.info("Получил попытку логина.")
                ServerMethods.loginAttemptReceived(
                    userChannel,
                    incomingMessage["login"].toString(),
                    incomingMessage["password"].toString()
                )
            }
            "message" -> {
                logger.info("Получил обычное сообщение.")
                ServerMethods.messageReceived(
                    userChannel,
                    incomingMessage["text"].toString()
                )
            }
            "disconnect" -> {
                logger.info("Получил сообщение об отключении.")
                ServerMethods.disconnectReceived(userChannel)
            }
            "ping" -> {
                logger.info("Получил пинг-запрос.")
                ServerMethods.pingReceived(userChannel)
            }
            "command" -> {
                logger.info("Получил команду от пользователя: $message")
                ServerMethods.commandReceived(
                    userChannel,
                    incomingMessage["text"].toString()
                )
            }
            else -> logger.error("Неизвестный тип сообщения: $type")
        }
    }

    fun loginSuccess(): String {
        val `object` = JSONObject()
        `object`["type"] = "loginSuccess"
        return `object`.toJSONString()
    }

    fun loginWrongError(): String {
        val `object` = JSONObject()
        `object`["type"] = "loginWrongError"
        return `object`.toJSONString()
    }

    fun loginAlreadyError(): String {
        val `object` = JSONObject()
        `object`["type"] = "loginAlreadyError"
        return `object`.toJSONString()
    }

    fun userColor(login: String, color: String): String {
        val `object` = JSONObject()
        `object`["type"] = "userColor"
        `object`["login"] = login
        `object`["color"] = color
        return `object`.toJSONString()
    }

    fun userMessage(login: String, message: String, color: String): String {
        val `object` = JSONObject()
        `object`["type"] = "userMessage"
        `object`["login"] = login
        `object`["text"] = message
        `object`["color"] = color
        return `object`.toJSONString()
    }

    fun serverMessage(text: String): String {
        val `object` = JSONObject()
        `object`["type"] = "serverMessage"
        `object`["text"] = text
        return `object`.toJSONString()
    }

    fun serverPong(): String {
        val `object` = JSONObject()
        `object`["type"] = "pong"
        return `object`.toJSONString()
    }
}
