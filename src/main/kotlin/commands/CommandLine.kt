import org.slf4j.LoggerFactory

import java.util.Scanner
import java.util.regex.Matcher
import java.util.regex.Pattern

class CommandLine : Thread() {

    private val logger = LoggerFactory.getLogger(CommandLine::class.java)

    override fun run() {
        val scanner = Scanner(System.`in`)
        val pattern = Pattern.compile("^/[a-zA-Z0-9\\s]+$") //Регулярное выражение для команд
        var m: Matcher
        var message: String
        while (true) {
            message = scanner.nextLine()
            m = pattern.matcher(message)
            //Если команда, то пытаемся вызвать ее
            if (m.matches()) {
                logger.info("Получил команду от сервера: $message")
                message = message.substring(1)
                val command = message.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                Commands.executeServerCommand(command)
            } else {
                ServerMethods.sendServerMessageAll(message)
                println(" SERVER: $message")
            }//Иначе отправляем как сообщение в чат
        }
    }
}
