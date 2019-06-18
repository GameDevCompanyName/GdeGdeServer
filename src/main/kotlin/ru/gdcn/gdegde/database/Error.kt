package ru.gdcn.gdegde.database

data class Error(val text: String) : DBEntity {

    override fun getValuesMap(): Map<String, Any> {
        val map = HashMap<String, Any>()
        map["text"] = text
        return map
    }

}