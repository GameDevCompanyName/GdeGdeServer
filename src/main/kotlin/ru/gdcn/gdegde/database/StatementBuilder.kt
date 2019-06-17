package ru.gdcn.gdegde.database

import java.lang.StringBuilder

object StatementBuilder {

    fun selectFieldsFromTable(tableName: String, limit: Int = 0, orderBy : String = "", whereCondition: String = "", vararg fields: String): String {
        val statementText = StringBuilder()
        statementText.append("SELECT ")
        if (fields.isEmpty()) {
            statementText.append("* ")
        } else {
            statementText.append(fields.joinToString()).append(' ')
        }
        statementText.append("FROM $tableName ")
        if (whereCondition.isNotEmpty())
            statementText.append("WHERE $whereCondition ")
        if (orderBy.isNotEmpty())
            statementText.append("ORDER BY $orderBy ")
        if (limit > 0) {
            statementText.append("LIMIT $limit")
        }
        statementText.append(";")
        return statementText.toString()
    }

    fun insertColumnsInTable(tableName: String, fields: Collection<String>): String {
        val statementText = StringBuilder()
        statementText.append("INSERT INTO $tableName (" + fields.joinToString() + ") VALUES (?" + ",?".repeat(fields.size - 1) + ")")
        return statementText.toString()
    }

    fun deleteAll(tableName: String): String {
        return ("DELETE FROM $tableName WHERE id > 0;")
    }

    fun truncateTable(tableName: String, isCascade : Boolean = false): String? {
        val statementText = StringBuilder()
        statementText.append("TRUNCATE $tableName")
        if (isCascade)
            statementText.append(" CASCADE")
        statementText.append(";")
        return statementText.toString()
    }

    fun setFieldToRandomId(tableName: String, tableFrom: String, fieldFk: String, whereCondition : String = "") : String {
        val statementText = StringBuilder()
        statementText.append("UPDATE $tableName SET $fieldFk = (SELECT id FROM $tableFrom")
        if (whereCondition.isNotEmpty())
            statementText.append(" WHERE $whereCondition")
        statementText.append(" ORDER BY random() LIMIT 1);")
        return statementText.toString()
    }

    fun addLinks(table: String, fieldFirst: String, fieldSecond: String, tableFromFirst: String, tableFromSecond: String, quantity: Int): String {
        return "DO \$\$\n" +
                "BEGIN\n" +
                "   FOR counter IN 1..$quantity LOOP\n" +
                "\t\tINSERT INTO $table ($fieldFirst, $fieldSecond) VALUES\n" +
                "\t\t(\n" +
                "\t\t\t(\n" +
                "\t\t\t\tSELECT id\n" +
                "\t\t\t\tFROM $tableFromFirst\n" +
                "\t\t\t\tORDER BY random()\n" +
                "\t\t\t\tLIMIT 1\n" +
                "\t\t\t),\n" +
                "\t\t\t(\n" +
                "\t\t\t\tSELECT id\n" +
                "\t\t\t\tFROM $tableFromSecond\n" +
                "\t\t\t\tORDER BY random()\n" +
                "\t\t\t\tLIMIT 1\n" +
                "\t\t\t)\n" +
                "\t\t);\n" +
                "   END LOOP;\n" +
                "END; \n" +
                "\$\$;"
    }

    fun procedureCall(procedureCall: String): String {
        return "SELECT * FROM $procedureCall;"
    }

}