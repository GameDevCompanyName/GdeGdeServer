package ru.gdcn.gdegde

data class Achievement(val title: String, val description: String){

    override fun toString(): String {
        return "Достижение : $title\nОписание : $description"
    }
}

