package com.example.pastebinapp.model


data class TextBlock(
    val id: String = "", // Уникальный идентификатор
    val title: String = "", // Название текстового блока
    val contentUrl: String = "", // Ссылка на файл в Firebase Storage
    val dynamicLink: String = "",// Убедитесь, что это поле существует
    val createdAt: Long = System.currentTimeMillis() // Время создания
)
