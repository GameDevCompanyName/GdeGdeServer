package ru.gdcn.gdegde.database

import ru.gdcn.gdegde.Achievement
import ru.gdcn.gdegde.Role
import ru.gdcn.gdegde.User

interface IDBConnector {

    fun initDBConnector()

    fun addNewUser(login: String, pass: String, color: String): User?

    fun getUser(login: String): User?

    fun getAchievements(login: String): Collection<Achievement>

    fun logError(text: String)

    fun changeRole(login: String, newRole: Role)
    //TODO изменение роли

    fun addAchievement(login: String, idAchievement: Int)
}