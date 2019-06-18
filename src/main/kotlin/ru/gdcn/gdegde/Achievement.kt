package ru.gdcn.gdegde

import ru.gdcn.gdegde.database.DBEntity

data class Achievement(val id: Int, val title: String, val description: String) : DBEntity{

    override fun getValuesMap(): Map<String, Any> {
        val map = HashMap<String, Any>()
        map["id"] = id
        map["title"] = title
        map["description"] = description
        return map
    }

    override fun toString(): String {
        return "Достижение : $title\nОписание : $description"
    }
}

