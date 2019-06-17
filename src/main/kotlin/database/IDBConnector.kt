package database

import Achievement
import User

interface IDBConnector {

    fun initDBConnector()

    fun addNewUser(login: String, pass: String, color: String): User?

    fun getUser(login: String): User?

    fun getAchievements(login: String): Collection<Achievement>

}