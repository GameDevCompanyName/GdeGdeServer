import org.jboss.netty.channel.Channel
import org.slf4j.LoggerFactory
import java.util.HashMap

object Broadcaster {

    private val logger = LoggerFactory.getLogger(Broadcaster::class.java)

    private val loggedChannels = HashMap<Channel, User>()
    private val loggedUsers = HashMap<User, Channel>()

    val users: Collection<User>
        get() = loggedChannels.values

    fun checkIfUserOnline(login: String): Boolean {
        var userOnline = false
        val userToCheck = User(login)

        logger.info("Проверяю залогинен ли пользователь: $login")
        if (loggedUsers.containsKey(userToCheck))
            userOnline = true
        logger.info("Проверил онлайн ли пользователь: $login")

        return userOnline
    }

    fun userLoggedIn(newUser: User) {
        logger.info("Добавляю нового пользователя в список залогиненых: ${newUser.login}")
        loggedChannels[newUser.userChannel!!] = newUser
        loggedUsers[newUser] = newUser.userChannel!!

        sendMessageAll(
            ServerMessage.serverMessage(
                TextFormer.userConnected(newUser.login)
            )
        )
    }

    fun checkIfChannelLogged(userChannel: Channel): Boolean {
        var channelLogged = false

        logger.info("Проверяю залогинен ли канал.")
        if (loggedChannels.containsKey(userChannel))
            channelLogged = true

        return channelLogged
    }

    fun messageBroadcast(userChannel: Channel, text: String) {
        val sender = loggedChannels[userChannel]
        logger.info("Отправляю всем пользователям сообщение пользователя.")
        sendMessageAll(
            sender!!, ServerMessage.userMessage(
                sender.login,
                text,
                sender.color!!
            )
        )
    }

    fun userDisconnected(userChannel: Channel) {
        val channelOnline = checkIfChannelLogged(userChannel)

        if (!channelOnline)
            return
        val userToDelete = loggedChannels[userChannel]
        logger.info("Удаляю пользователя из списка залогиненых: " + userToDelete.login)
        loggedChannels.remove(userChannel)
        loggedUsers.remove(userToDelete)
        sendMessageAll(
            ServerMessage.serverMessage(
                TextFormer.userDisconnected(userToDelete.getLogin())
            )
        )
    }

    fun serverMessageBroadcast(text: String) {
        sendMessageAll(ServerMessage.serverMessage(text))
    }

    fun userKicked(login: String) {
        val kickUser = User(login)
        val userChannel = loggedUsers[kickUser]
        ServerMethods.disconnectReceived(userChannel!!)
    }

    private fun sendMessageAll(JSONMessage: String) {
        for (user in loggedChannels.values) {
            user.sendMessage(JSONMessage)
        }
    }

    private fun sendMessageAll(sender: User, JSONMessage: String) {
        for (user in loggedChannels.values) {
            if (user.login == sender.login)
                continue
            user.sendMessage(JSONMessage)
        }
    }

    fun getUser(userChannel: Channel): User {
        return loggedChannels[userChannel]!!
    }
}
