package com.example.pastebinapp.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pastebinapp.model.TextBlock
import com.example.pastebinapp.repository.TextBlockRepository
import com.google.firebase.Firebase
import com.google.firebase.dynamiclinks.androidParameters
import com.google.firebase.dynamiclinks.dynamicLink
import com.google.firebase.dynamiclinks.dynamicLinks
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TextBlockViewModel : ViewModel() {

    private val repository = TextBlockRepository()

    // StateFlow для хранения списка текстовых блоков
    private val _textBlocks = MutableStateFlow<List<TextBlock>>(emptyList())
    val textBlocks: StateFlow<List<TextBlock>> get() = _textBlocks

    // StateFlow для хранения содержимого текстового блока
    private val _textContent = MutableStateFlow<String?>(null)
    val textContent: StateFlow<String?> get() = _textContent

    // StateFlow для хранения текущего текстового блока (для деталей)
    private val _currentTextBlock = MutableStateFlow<TextBlock?>(null)
    val currentTextBlock: StateFlow<TextBlock?> get() = _currentTextBlock

    // загружаем текстовые блоки при инициализации ViewModel
    init {
        loadTextBlocks()
    }

    // метод для загрузки списка текстовых блоков
    private fun loadTextBlocks() {
        viewModelScope.launch {
            try {
                val blocks = repository.getTextBlocks()
                _textBlocks.value = blocks
            } catch (e: Exception) {
                Log.e("TextBlockViewModel", "Ошибка при загрузке списка текстовых блоков", e)
            }
        }
    }

    // метод для загрузки содержимого текстового блока
    fun loadTextContent(fileUrl: String) = viewModelScope.launch {
        try {
            val content = repository.downloadTextContent(fileUrl)
            _textContent.value = content
        } catch (e: Exception) {
            Log.e("TextBlockViewModel", "Ошибка при загрузке содержимого", e)
        }
    }

    // метод для загрузки текстового блока по его ID
    fun loadTextBlockById(id: String) = viewModelScope.launch {
        try {
            val textBlock = repository.getTextBlockById(id)
            _currentTextBlock.value = textBlock
        } catch (e: Exception) {
            Log.e("TextBlockViewModel", "Ошибка при загрузке текстового блока", e)
        }
    }

    // метод для загрузки текстового блока в Storage и сохранения метаданных в Firestore
    suspend fun uploadTextBlock(textBlock: TextBlock, content: String): Result<TextBlock> {
        return try {
            Log.d("UploadTextBlock", "Начало загрузки текстового блока")
            val fileUrl = repository.uploadTextBlock(content, textBlock.id)
            Log.d("UploadTextBlock", "Текст загружен в Storage: $fileUrl")

            val dynamicLink = repository.createDynamicLink(textBlock.id)
            Log.d("UploadTextBlock", "Dynamic Link создан: $dynamicLink")

            val updatedTextBlock = textBlock.copy(contentUrl = fileUrl, dynamicLink = dynamicLink.toString())
            Firebase.firestore.collection("textblocks")
                .document(updatedTextBlock.id)
                .set(updatedTextBlock)
                .await()

            Log.d("UploadTextBlock", "Текстовый блок сохранён в Firestore")
            Result.success(updatedTextBlock)
        } catch (e: Exception) {
            Log.e("UploadTextBlock", "Ошибка при загрузке текстового блока", e)
            Result.failure(e)
        }
    }

    // метод для удаления текстового блока
    fun deleteTextBlock(textBlock: TextBlock) = viewModelScope.launch {
        try {
            repository.deleteTextBlock(textBlock)
            loadTextBlocks() // обновляем список текстовых блоков после удаления
        } catch (e: Exception) {
            Log.e("TextBlockViewModel", "Ошибка при удалении текстового блока", e)
        }
    }

    fun createDynamicLink(textBlockId: String, onLinkCreated: (Uri) -> Unit) = viewModelScope.launch {
        try {
            val dynamicLink = Firebase.dynamicLinks.dynamicLink {
                link = Uri.parse("https://pastebinapp.page.link/textBlock/$textBlockId")
                domainUriPrefix = "https://pastebinapp.page.link"
                androidParameters("com.example.pastebinapp") {
                    minimumVersion = 1
                }
            }
            onLinkCreated(dynamicLink.uri) // Используйте dynamicLink.uri
        } catch (e: Exception) {
            Log.e("TextBlockViewModel", "Ошибка при создании Dynamic Link", e)
        }
    }
}