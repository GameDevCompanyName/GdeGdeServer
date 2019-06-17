import org.jboss.netty.channel.Channel
import org.slf4j.LoggerFactory
import ru.gdcn.gdegde.Broadcaster
import ru.gdcn.gdegde.ServerMessage
import ru.gdcn.gdegde.ServerMethods

object Commands {

    private val logger = LoggerFactory.getLogger(Commands::class.java)

    fun executeServerCommand(commands: Array<String>) {
        when (commands[0]) {
            "clients" -> for (user in Broadcaster.users)
                print(user.login)
            "kick" -> if (commands.size == 2)
                ServerMethods.kickUser(commands[1])
            else
                print("Неверная команда.")
            "shutdown" -> {
                print("Сервер остановил свою работу.")
                System.exit(0)
            }
            else -> print("Нет такой команды.")
        }
    }

    fun executeUserCommand(userChannel: Channel, commands: Array<String>) {
        if (commands.size == 1)
            ServerMethods.sendMessageUser(
                userChannel,
                ServerMessage.serverMessage("Нет такой команды.")
            )
        when (commands[1]) {
            "clients" -> for (user in Broadcaster.users)
                ServerMethods.sendMessageUser(
                    userChannel,
                    ServerMessage.serverMessage(user.login + "\n")
                )
            "echo" -> ServerMethods.echoReceived(userChannel, commands)
            "help" -> ServerMethods.sendMessageUser(
                userChannel,
                ServerMessage.serverMessage(
                    "Доступные команды:\n" +
                            "/server clients\n" +
                            "/server echo <your text>\n" +
                            "/server help"
                )
            )
            "achievements" -> TODO() //Придумать как отправлять ачивки
            "kick" -> if (commands.size == 2) {
                //TODO добавить проверку роли видимо сюда
                ServerMethods.kickUser(commands[1])}
            else
                print("Неверная команда.")
            "shutdown" -> TODO() //Отключать сервер, если это сделал админ
            else -> ServerMethods.sendMessageUser(
                userChannel,
                ServerMessage.serverMessage("Нет такой команды.")
            )
        }
    }

    private fun print(text: String?) {
        println(text)
    }

}
