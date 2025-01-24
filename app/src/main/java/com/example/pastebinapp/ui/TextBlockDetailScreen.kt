package com.example.pastebinapp.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pastebinapp.viewmodel.TextBlockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextBlockDetailScreen(navController: NavController, textBlockId: String) {
    // создаем экземпляр ViewModel для работы с данными
    val viewModel: TextBlockViewModel = viewModel()

    // получаем текстовый блок по его ID и отслеживаем изменения с помощью StateFlow
    val textBlock by viewModel.currentTextBlock.collectAsState()

    // получаем содержимое текстового блока и отслеживаем изменения с помощью StateFlow
    val textContent by viewModel.textContent.collectAsState()

    // получаем контекст для использования системных сервисов (например, ClipboardManager)
    val context = LocalContext.current

    // Загружаем текстовый блок и его содержимое при первом отображении экрана
    LaunchedEffect(textBlockId) {
        viewModel.loadTextBlockById(textBlockId)
        textBlock?.let { block ->
            viewModel.loadTextContent(block.contentUrl)
        }
    }

    // Scaffold — это базовый макет для экрана, который включает AppBar, FloatingActionButton и т.д.
    Scaffold(
        topBar = {
            // TopAppBar — верхняя панель с заголовком, кнопкой "Назад" и действиями
            TopAppBar(
                title = { Text("Детали текстового блока") }, // заголовок экрана
                navigationIcon = {
                    // кнопка "Назад" для возврата на предыдущий экран
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    // кнопка для удаления текстового блока
                    IconButton(
                        onClick = {
                            textBlock?.let { block ->
                                // вызываем метод для удаления текстового блока
                                viewModel.deleteTextBlock(block)
                                // возвращаемся на предыдущий экран после удаления
                                navController.popBackStack()
                            }
                        }
                    ) {
                        // иконка для кнопки удаления
                        Icon(Icons.Default.Delete, contentDescription = "Удалить")
                    }

                    // Кнопка для редактирования
                    IconButton(
                        onClick = {
                            textBlock?.let {
                                navController.navigate("editTextBlock/${it.id}")
                            }
                        }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                    }
                }
            )
        },
        floatingActionButton = {
            // FloatingActionButton — кнопка для копирования ссылки
            FloatingActionButton(
                onClick = {
                    textBlock?.let { block ->
                        viewModel.createDynamicLink(block.id) { link ->
                            val clipboardManager =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipData = ClipData.newPlainText("Ссылка", link.toString())
                            clipboardManager.setPrimaryClip(clipData)
                            Toast.makeText(context, "Ссылка скопирована", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            ) {
                // иконка для кнопки копирования
                Icon(Icons.Default.ContentCopy, contentDescription = "Копировать ссылку")
            }
        }
    ) { innerPadding ->
        // если текстовый блок загружен, отображаем его детали
        if (textBlock != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                // отображаем название текстового блока
                Text(
                    text = "Название: ${textBlock!!.title}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                // заголовок для содержимого
                Text(
                    text = "Содержимое:",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                // если содержимое загружено, отображаем его
                if (textContent != null) {
                    Text(
                        text = textContent!!,
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    // если содержимое еще не загружено, показываем индикатор загрузки
                    CircularProgressIndicator()
                }
            }
        } else {
            // если текстовый блок еще не загружен, показываем сообщение о загрузке
            Text("Данные загружаются...")
        }
    }
}