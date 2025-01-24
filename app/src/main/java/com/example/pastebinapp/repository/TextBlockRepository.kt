package com.example.pastebinapp.repository

import android.net.Uri
import android.util.Log
import com.example.pastebinapp.model.TextBlock
import com.google.firebase.Firebase
import com.google.firebase.dynamiclinks.androidParameters
import com.google.firebase.dynamiclinks.dynamicLink
import com.google.firebase.dynamiclinks.dynamicLinks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TextBlockRepository {

    // Инициализация Firebase Storage
    private val storage = FirebaseStorage.getInstance()
    private val storageRef: StorageReference = storage.reference

    // Инициализация Firestore
    private val db = FirebaseFirestore.getInstance()

    // Метод для загрузки текстового блока
    suspend fun uploadTextBlock(text: String, fileName: String): String {
        return try {
            val fileRef = storageRef.child("textblocks/$fileName.txt")
            val uploadTask = fileRef.putBytes(text.toByteArray())
            uploadTask.await()
            fileRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("TextBlockRepository", "Ошибка при загрузке текстового блока", e)
            throw e // или вернуть null, если нужно
        }
    }

    // метод для сохранения метаданных текстового блока в Firestore
    suspend fun saveTextBlockMetadata(textBlock: TextBlock) {
        db.collection("textblocks")
            .document(textBlock.id)
            .set(textBlock)
            .await()
    }

    suspend fun getTextBlocks(): List<TextBlock> {
        return db.collection("textblocks")
            .get()
            .await()
            .toObjects(TextBlock::class.java)
    }

    suspend fun getTextBlockById(id: String): TextBlock? {
        return db.collection("textblocks")
            .document(id)
            .get()
            .await()
            .toObject(TextBlock::class.java)
    }

    // метод для загрузки содержимого текстового блока
    suspend fun downloadTextContent(fileUrl: String): String? {
        return try {
            // получаем ссылку на файл в Firebase Storage
            val fileRef = storage.getReferenceFromUrl(fileUrl)
            // скачиваем содержимое файла в виде байтов
            val bytes = fileRef.getBytes(Long.MAX_VALUE).await()
            // преобразуем байты в строку
            String(bytes, Charsets.UTF_8)
        } catch (e: Exception) {
            // логируем ошибку, если что-то пошло не так
            Log.e("TextBlockRepository", "Ошибка при загрузке содержимого", e)
            null
        }
    }

    // метод для удаления текстового блока
    suspend fun deleteTextBlock(textBlock: TextBlock) {
        try {
            // удаляем файл из Firebase Storage
            val fileRef = storage.getReferenceFromUrl(textBlock.contentUrl)
            fileRef.delete().await()

            // удаляем метаданные из Firestore
            db.collection("textblocks")
                .document(textBlock.id)
                .delete()
                .await()
        } catch (e: Exception) {
            // логируем ошибку, если что-то пошло не так
            Log.e("TextBlockRepository", "Ошибка при удалении текстового блока", e)
        }
    }

    suspend fun createDynamicLink(textBlockId: String): Uri {
        val dynamicLink = Firebase.dynamicLinks.dynamicLink {
            link = Uri.parse("https://yourapp.page.link/textBlock/$textBlockId")
            domainUriPrefix = "https://pastebinapp.page.link"
            androidParameters("com.example.pastebinapp") {
                minimumVersion = 1
            }
        }
        return dynamicLink.uri
    }
}