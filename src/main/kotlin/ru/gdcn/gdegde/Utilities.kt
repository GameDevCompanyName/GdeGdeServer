package ru.gdcn.gdegde

import java.sql.ResultSet

object Utilities {

    private const val HEX_SOURCE = "0123456789ABDCEF"

    fun generateRandomHex(): String = generateSequence { HEX_SOURCE.random() }.take(6).toString()

    fun intToRole(roleId: Int): Role {
        when (roleId) {
            1 -> return Role.USER
            2 -> return Role.ADMIN
            else -> return Role.USER
        }
    }

    fun resultSetToAchievementCollection(resultSet: ResultSet): Collection<Achievement> {
        val result = mutableListOf<Achievement>()
        while (resultSet.next()) {
            result.add(
                Achievement(resultSet.getString("title"), resultSet.getString("description"))
            )
        }
        return result
    }

}