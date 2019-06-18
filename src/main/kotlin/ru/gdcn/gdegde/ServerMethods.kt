package ru.gdcn.gdegde

import Commands
import org.jboss.netty.channel.Channel
import org.slf4j.LoggerFactory
import ru.gdcn.gdegde.database.DBConnector
import ru.gdcn.gdegde.database.IDBConnector

import java.util.regex.Pattern

object ServerMethods {
    private val logger = LoggerFactory.getLogger(ServerMethods::class.java)
    private val dbConnector: IDBConnector = DBConnector()

    init {
        dbConnector.initDBConnector()
    }

    fun loginAttemptReceived(userChannel: Channel, login: String, password: String) {
        logger.info("Обрабатываю попытку залогиниться по логину: $login")

        if (Broadcaster.checkIfUserOnline(login)) {
            logger.info("Юзер с таким именем уже онлайн: $login")
            userChannel.write(ServerMessage.loginAlreadyError())
            return
        }

        //Авторизация пользователя
        var user = dbConnector.getUser(login)
        if (user != null) {
            logger.info("Такой пользователь уже есть в базе првоеряю пароль для: $login")
            if (password.equals(user.pass)) {
                logger.info("Верный пароль для: $login")
                user.userChannel = userChannel
                user.sendMessage(ServerMessage.loginSuccess())
                user.sendMessage(ServerMessage.userColor(user.login, user.color!!))
                Broadcaster.userLoggedIn(user)
            } else {
                logger.info("Неверный пароль для: $login")
                user.sendMessage(ServerMessage.loginWrongError())
            }
        }

        //Регистрация пользователя
        //TODO давать ачивку за регистрацию
        if (user == null) {
            logger.info("Такого пользователя в базе ещё нет, создаю нового для: $login")
            user = dbConnector.addNewUser(login, password, Utilities.generateRandomHex())//TODO добавить цвет из утилит
            logger.info("Пользователь создан: $login")
            if (user != null) {
                user.userChannel = userChannel
                user.sendMessage(ServerMessage.loginSuccess())
                Broadcaster.userLoggedIn(user)
            } else {
                logger.info("Не удалось создать пользователя: $login")
                userChannel.write(ServerMessage.serverMessage("Не удалось зарегистрировать пользователя!"))
            }
        }
    }

    fun messageReceived(userChannel: Channel, text: String) {
        logger.info("Проверяю залогинен ли канал, пытающийся отправить сообщение.")
        if (Broadcaster.checkIfChannelLogged(userChannel)) {
            logger.info("Канал залогинен, передаю сообщение в Broadcaster.")
            Broadcaster.messageBroadcast(userChannel, text)
        } else {
            logger.info("Канал НЕ залогинен и не может отправлять сообщения.")
        }
    }

    fun pingReceived(userChannel: Channel) {
        logger.info("Пришёл запрос пинга.")
        logger.info("Отвечаю понг...")
        userChannel.write(ServerMessage.serverPong())
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
        if (Broadcaster.checkIfChannelLogged(userChannel)) {
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
                userChannel.write(ServerMessage.serverMessage("Некорретная команда."))
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
            userChannel.write(ServerMessage.serverMessage(stringBuilder.toString()))
        } else {
            userChannel.write(ServerMessage.serverMessage("Hello, World!"))
        }
    }

    fun kickUser(userChannel: Channel?, login: String) {
        if (userChannel == null || Broadcaster.getUser(userChannel).role!!.equals("admin")) {
            Broadcaster.userKicked(login)
            Broadcaster.serverMessageBroadcast("$login был исключен.")
        }
    }

    fun getAchievements(userChannel: Channel): String{
        val user = Broadcaster.getUser(userChannel)
        return dbConnector.getAchievements(user.login).joinToString("\n") //TODO сделать красивый вывод ачивок
    }
}
