package ru.gdcn.gdegde.database

import ru.gdcn.gdegde.Achievement
import ru.gdcn.gdegde.Role
import ru.gdcn.gdegde.User
import java.util.*

interface IDBConnector {

    fun initDBConnector()

    fun addNewUser(login: String, pass: String, color: String): User?

    fun getUser(login: String): User?

    fun getAchievements(login: String): Collection<Achievement>

    fun getAchievement(idAchievement: Int): Achievement

    fun logError(text: String)

    fun changeRole(login: String, newRole: Role)

    fun addAchievement(login: String, idAchievement: Int)

    fun saveMessage(login: String, message: String)

    fun saveServerMessage(message: String)

    fun getMessages(quantity: Int): Collection<String>

    fun lookForMessages(text: String): Collection<String>

}