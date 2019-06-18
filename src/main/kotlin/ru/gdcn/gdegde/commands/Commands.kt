import org.jboss.netty.channel.Channel
import org.slf4j.LoggerFactory
import ru.gdcn.gdegde.Broadcaster
import ru.gdcn.gdegde.ServerMessage
import ru.gdcn.gdegde.ServerMethods
import java.lang.ClassCastException

object Commands {

    private val logger = LoggerFactory.getLogger(Commands::class.java)

    fun executeServerCommand(commands: Array<String>) {
        when (commands[0]) {
            "clients" -> for (user in Broadcaster.users)
                print(user.login)
            "kick" -> if (commands.size == 2)
                ServerMethods.kickUser(null, commands[1])
            else
                print("Неверная команда.")
            "changerole" -> if (commands.size == 3) {
                try {
                    ServerMethods.changeRole(commands[1], commands[2].toInt())
                } catch (e: ClassCastException){
                    print("Кажись команда введена неверно.")
                }
            }
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
        if (commands.size == 1) {
            userChannel.write(ServerMessage.serverMessage("Нет такой команды."))
            return
        }
        when (commands[1]) {
            "clients" -> for (user in Broadcaster.users)
                userChannel.write(ServerMessage.serverMessage(user.login + "\n"))
            "echo" -> ServerMethods.echoReceived(userChannel, commands)
            "help" ->
                userChannel.write(
                    ServerMessage.serverMessage(
                        "Доступные команды:\n" +
                                "/server clients\n" +
                                "/server echo <your text>\n" +
                                "/server achievements\n" +
                                "/server kick <user name>\n" +
                                "/server shutdown\n" +
                                "/server help"
                    )
                )
            "achievements" -> userChannel.write(
                ServerMessage.serverMessage(
                    ServerMethods.getAchievements(userChannel)
                )
            )
            "kick" -> if (commands.size == 3) {
                ServerMethods.kickUser(userChannel, commands[2])
            } else
                userChannel.write(ServerMessage.serverMessage("Неверная команда!"))
            "shutdown" -> ServerMethods.doomsDay(userChannel)
            else -> userChannel.write(ServerMessage.serverMessage("Нет такой команды."))
        }
    }

    private fun print(text: String?) {
        println(text)
    }

}
