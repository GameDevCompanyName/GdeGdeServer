package ru.gdcn.gdegde

import Commands
import org.jboss.netty.channel.Channel
import org.slf4j.LoggerFactory

import java.util.regex.Pattern

object ServerMethods {
    private val logger = LoggerFactory.getLogger(ServerMethods::class.java)

    fun loginAttemptReceived(userChannel: Channel, login: String, password: String) {
        logger.info("Обрабатываю попытку залогиниться по логину: $login")

        val userOnline = Broadcaster.checkIfUserOnline(login)
        if (userOnline) {
            logger.info("Юзер с таким именем уже онлайн: $login")
            sendMessageUserAlreadyOnline(userChannel)
            return
        }

        val userExists = DBConnector.searchForUser(login)
        if (userExists) {
            logger.info("Такой пользователь уже есть в базе првоеряю пароль для: $login")
            val passIsCorrect = DBConnector.checkLoginAttempt(login, password)
            if (passIsCorrect) {
                logger.info("Верный пароль для: $login")
                val newUser = initUser(userChannel, login)
                sendMesageUserLoginSuccess(userChannel)
                sendMessageUserColor(userChannel, login, newUser.color!!)
                Broadcaster.userLoggedIn(userChannel, newUser)
            } else {
                logger.info("Неверный пароль для: $login")
                sendMesssageUserWrongPassword(userChannel)
            }
        }

        if (!userExists) {
            logger.info("Такого пользователя в базе ещё нет, создаю нового для: $login")
            DBConnector.insertNewUser(login, password)
            logger.info("Пользователь создан: $login")
            val newUser = initUser(userChannel, login)
            sendMesageUserLoginSuccess(userChannel)
            sendMessageUserColor(userChannel, login, newUser.color!!)
            Broadcaster.userLoggedIn(userChannel, newUser)
        }
    }

    fun messageReceived(userChannel: Channel, text: String) {
        logger.info("Проверяю залогинен ли канал, пытающийся отправить сообщение.")
        val userIsLogged = Broadcaster.checkIfChannelLogged(userChannel)
        if (userIsLogged) {
            logger.info("Канал залогинен, передаю сообщение в ru.gdcn.gdegde.Broadcaster.")
            Broadcaster.messageBroadcast(userChannel, text)
        } else {
            logger.info("Канал НЕ залогинен и не может отправлять сообщения.")
        }
    }

    fun pingReceived(userChannel: Channel) {
        logger.info("Пришёл запрос пинга.")
        sendMessageUserPong(userChannel)
    }

    fun disconnectReceived(userChannel: Channel) {
        logger.info("Канал отключается.")
        Broadcaster.userDisconnected(userChannel)
        userChannel.close()
        logger.info("Сообщение об отключении обработано.")
    }

    fun commandReceived(userChannel: Channel, text: String) {
        var command = text
        logger.info("Проверяю залогинен ли канал, пытающийся выполнить команду.")
        val userIsLogged = Broadcaster.checkIfChannelLogged(userChannel)
        if (userIsLogged) {
            logger.info("Канал залогинен, передаю команду в Commands.")
            val pattern = Pattern.compile("^/[a-zA-Z0-9\\s]+$")
            val m = pattern.matcher(command)
            if (m.matches()) {
                logger.info("Команда корректна.")
                command = command.substring(1)
                val commands = command.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                Commands.executeUserCommand(userChannel, commands)
            } else {
                logger.info("Команда некорректна.")
                sendMessageUser(
                    userChannel,
                    ServerMessage.serverMessage("Некорретная команда.")
                )
            }
        } else {
            logger.info("Канал НЕ залогинен и не может выполнять команды.")
        }
    }

    fun echoReceived(userChannel: Channel, commands: Array<String>) {
        logger.info("Отправляю эхо-запрос.")
        if (commands.size >= 3) {
            val stringBuilder = StringBuilder()
            for (i in 2 until commands.size - 1)
                stringBuilder.append(commands[i]).append(" ")
            stringBuilder.append(commands[commands.size - 1])
            sendMessageUser(
                userChannel,
                ServerMessage.serverMessage(stringBuilder.toString())
            )
        } else {
            sendMessageUser(
                userChannel,
                ServerMessage.serverMessage("Hello, World!")
            )
        }
    }

    fun kickUser(login: String) {
        Broadcaster.userKicked(login)
        sendServerMessageAll("$login был исключен.") // TODO может стои твызвать метод из Broadcast?
    }

    fun sendServerMessageAll(text: String) {
        logger.info("Отправляю сервеное сообщение.")
        Broadcaster.serverMessageBroadcast(text) // TODO может стои твызвать метод из Broadcast?
    }

    fun sendMessageUser(userChannel: Channel, message: String) {
        logger.info("Пишу сообщение в канал: $message")
        userChannel.write(message)
    }

    private fun initUser(userChannel: Channel, login: String): User {
        logger.info("Инициализирую нового пользователя: $login")
        val userColor = DBConnector.getUserColor(login)
        val userRole = DBConnector.getUserRole(login)
        val newUser = User(userChannel, login, userColor, userRole)
        logger.info("Пользователь проинициализирован.")
        return newUser
    }

    private fun sendMessageUserAlreadyOnline(userChannel: Channel) {
        logger.info("Отправляю сообщение о том, что такой пользователь уже залогинен.")
        sendMessageUser(userChannel, ServerMessage.loginAlreadyError())
    }

    private fun sendMesageUserLoginSuccess(userChannel: Channel) {
        logger.info("Отправляю сообщение об удачном логине.")
        sendMessageUser(userChannel, ServerMessage.loginSuccess())
    }

    private fun sendMesssageUserWrongPassword(userChannel: Channel) {
        logger.info("Отправляю сообщение о неверном пароле.")
        sendMessageUser(userChannel, ServerMessage.loginWrongError())
    }

    private fun sendMessageUserColor(userChannel: Channel, login: String, color: String) {
        logger.info("Отправляю пользователю его цвет.")
        sendMessageUser(userChannel, ServerMessage.userColor(login, color))
    }

    private fun sendMessageUserPong(userChannel: Channel) {
        logger.info("Отправляю ответ на эхо-запрос.")
        sendMessageUser(userChannel, ServerMessage.serverPong())
    }
}
