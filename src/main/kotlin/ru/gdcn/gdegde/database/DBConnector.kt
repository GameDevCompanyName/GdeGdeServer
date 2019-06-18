package ru.gdcn.gdegde.database

import org.postgresql.util.PSQLException
import org.slf4j.LoggerFactory
import ru.gdcn.gdegde.Achievement
import ru.gdcn.gdegde.Role
import ru.gdcn.gdegde.User
import ru.gdcn.gdegde.Utilities
import ru.gdcn.gdegde.database.DBParameters.DB_HOST_DEFAULT
import ru.gdcn.gdegde.database.DBParameters.DB_NAME_DEFAULT
import ru.gdcn.gdegde.database.DBParameters.DB_PASS_DEFAULT
import ru.gdcn.gdegde.database.DBParameters.DB_PORT_DEFAULT
import ru.gdcn.gdegde.database.DBParameters.DB_USER_DEFAULT
import java.sql.*
import kotlin.system.exitProcess

class DBConnector : IDBConnector {

    private val logger = LoggerFactory.getLogger(DBConnector::class.java)
    private var connection: Connection? = null

    override fun initDBConnector() {
        val dbName = DB_NAME_DEFAULT
        val dbUser = DB_USER_DEFAULT
        val dbPass = DB_PASS_DEFAULT
        val host = DB_HOST_DEFAULT
        val port = DB_PORT_DEFAULT

        logger.info("Creating new connection to $dbName via $dbUser user")
        if (connection != null) {
            try {
                logger.info("Closing old connection if present")
                connection?.close()
            } catch (e: PSQLException) {
                logger.warn("Couldn't carefully close old DB connection, but didn't crash")
            }
        } else {
            logger.info("Old connection was NULL, not even trying to close it...")
        }
        connection = DriverManager.getConnection("jdbc:postgresql://$host:$port/$dbName", dbUser, dbPass)
    }

    override fun addNewUser(login: String, pass: String, color: String): User? {
        val newUser = User(null, login, pass, color, Role.USER)
        return try {
            insertDataInTable("client", listOf(newUser))
            newUser
        } catch (e : PSQLException) {
            logger.error("Could not insert new user!")
            null
        }
    }

    override fun getUser(login: String): User? {
        val resultSet = getResultSetOfSelect(tableName = "client", limit = 1, whereCondition = "name = '$login'")
        var user : User? = null
        if (resultSet.next()){
            user = User(
                null,
                resultSet.getString("name"),
                resultSet.getString("password"),
                resultSet.getString("color"),
                Utilities.intToRole(resultSet.getInt("fk_role"))
            )
        }
        return user
    }

    override fun getAchievements(login: String): Collection<Achievement> {
        val resultSet = getResultSetOfProcedure("getClientAchievements('$login')")
        return Utilities.resultSetToAchievementCollection(resultSet)
    }

    override fun logError(text: String) {
        val error = Error(text)
        insertDataInTable("error", listOf(error))
    }

    override fun changeRole(login: String, newRole: Role) {
        val roleID = when (newRole){
            Role.USER -> 1
            Role.ADMIN -> 2
        }
        getResultSetOfProcedure("changeRole($login, $roleID)")
    }

    override fun getAchievement(idAchievement: Int): Achievement {
        val resultSet = getResultSetOfSelect(tableName = "achievement", whereCondition = "id = $idAchievement")
        resultSet.next()
        return Achievement(
            idAchievement,
            resultSet.getString("title"),
            resultSet.getString("description")
        )
    }

    override fun addAchievement(login: String, idAchievement: Int) {
        getResultSetOfProcedure("addClientAchievement($login, $idAchievement)")
    }

    override fun saveMessage(login: String, message: String) {
        getResultSetOfProcedure("addMessage($login, $message)")
    }

    override fun saveServerMessage(message: String) {
        getResultSetOfProcedure("addServerMessage($message)")
    }

    override fun getMessages(quantity: Int): Collection<String> {
        val resultSet = getResultSetOfSelect(
            tableName = "message",
            limit = quantity,
            orderBy = "date DESC"
        )
        val list = mutableListOf<String>()
        while (resultSet.next()){
            list.add(
                resultSet.getString("text")
            )
        }
        return list
    }

    private fun getResultSetOfProcedure(procedureCall: String): ResultSet {
        logger.info("Executing procedure: $procedureCall")
        if (connection == null) {
            logger.error("Cannot execute procedure, because there's no connection to DB!")
        }
        logger.info("Creating statement")
        val statement = connection!!.createStatement()
        try {
            logger.info("Executing query")
            return statement.executeQuery(
                StatementBuilder.procedureCall(
                    procedureCall
                )
            )
        } catch (e: PSQLException) {
            logger.error("Error while executing SELECT!")
            e.printStackTrace()
            exitProcess(1)
        }
    }

    fun getResultSetOfSelect(
        tableName: String,
        limit: Int = 0,
        orderBy: String = "",
        vararg fields: String,
        whereCondition: String = ""
    ): ResultSet {
        logger.info("Executing SELECT from table $tableName by fields")
        if (connection == null) {
            logger.error("Cannot execute SELECT, because there's no connection to DB")
        }
        logger.info("Creating statement")
        val statement = connection!!.createStatement()
        try {
            logger.info("Executing query")
            return statement.executeQuery(
                StatementBuilder.selectFieldsFromTable(
                    tableName = tableName,
                    limit = limit,
                    orderBy = orderBy,
                    whereCondition = whereCondition,
                    fields = *fields
                )
            )
        } catch (e: PSQLException) {
            logger.error("Error while executing SELECT!")
            e.printStackTrace()
            exitProcess(1)
        }
    }

    fun insertDataInTable(tableName: String, data: Collection<DBEntity>) {
        logger.info("Trying to insert data in table")
        val listOfFields = data.elementAt(0).getValuesMap().keys
        val statement = StatementBuilder.insertColumnsInTable(tableName, listOfFields)
        val preparedStatement: PreparedStatement
        try {
            logger.info("Preparing statement")
            preparedStatement = connection!!.prepareStatement(statement)
            logger.info("Prepared successfully!")
        } catch (e: PSQLException) {
            logger.error("Oops! Could not prepare statement - something wrong with connection!")
            e.printStackTrace()
            logError(e.toString())
            return
        }

        var count: Int
        logger.info("Adding data to statement...")
        for (element in data) {
            count = 1
            for (entry in element.getValuesMap().entries) {
                when (entry.value) {
                    is String -> preparedStatement.setString(count, entry.value as String)
                    is Int -> preparedStatement.setInt(count, entry.value as Int)
                    is Boolean -> preparedStatement.setBoolean(count, entry.value as Boolean)
                    is Timestamp -> preparedStatement.setTimestamp(count, entry.value as Timestamp)
                    is Double -> preparedStatement.setDouble(count, entry.value as Double)
                }
                count++
            }
            preparedStatement.addBatch()
        }
        logger.info("Successfully added " + data.size + " elements to statement")
        try {
            logger.info("Executing prepared statement")
            preparedStatement.executeBatch()
            logger.info("Success!")
        } catch (e: PSQLException) {
            logger.error("Oops! Could not execute statement! Something went wrong!")
            e.printStackTrace()
            logError(e.toString())
            return
        }
    }

    fun getListOfIds(tableName: String): Collection<Int> {
        logger.info("Getting IDs of table $tableName")
        val resultSet = getResultSetOfSelect(tableName, 0, "id")
        val list = mutableListOf<Int>()
        while (resultSet.next()) {
            list.add(resultSet.getInt("id"))
        }
        logger.info("Getting IDs - Success")
        return list
    }

    fun cleanTable(tableName: String, isCascade: Boolean = false) {
        logger.info("Cleaning table $tableName")
        try {
            val statement = connection!!.createStatement()
//            val statementText = StatementBuilder.deleteAll(tableName)
            val statementText = StatementBuilder.truncateTable(tableName, isCascade)
            statement.executeUpdate(statementText)
        } catch (e: PSQLException) {
            logger.error("Oops! Cannot clean table $tableName! Something went wrong!")
            e.printStackTrace()
            logError(e.toString())
            return
        }
        logger.info("Cleaned table successfully!")
    }

    fun setFieldToRandomId(tableName: String, tableFrom: String, fieldFk: String, whereCondition: String = "") {
        logger.info("Setting field $fieldFk in table $tableName to random id from $tableFrom")
        try {
            val statement = connection!!.createStatement()
            val statementText =
                StatementBuilder.setFieldToRandomId(
                    tableName,
                    tableFrom,
                    fieldFk,
                    whereCondition
                )
            statement.executeUpdate(statementText)
        } catch (e: PSQLException) {
            logger.error("Oops! Cannot do that! Something went wrong!")
            e.printStackTrace()
            logError(e.toString())
            return
        }
    }

    fun addLinks(
        table: String,
        fieldFirst: String,
        fieldSecond: String,
        tableFromFirst: String,
        tableFromSecond: String,
        quantity: Int
    ) {
        logger.info("Adding linking rows for $tableFromFirst and $tableFromSecond in table $table")
        try {
            val statementText = StatementBuilder.addLinks(
                table,
                fieldFirst,
                fieldSecond,
                tableFromFirst,
                tableFromSecond,
                quantity
            )
            val statement = connection!!.createStatement()
            statement.executeUpdate(statementText)
        } catch (e: PSQLException) {
            logger.error("Oops! Cannot do that! Something went wrong!")
            e.printStackTrace()
            logError(e.toString())
            return
        }
        logger.info("Success!")
    }

    fun closeConnection() {
        connection!!.close()
    }

}