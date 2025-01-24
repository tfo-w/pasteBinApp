package com.example.pastebinapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pastebinapp.viewmodel.TextBlockViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTextBlockScreen(navController: NavController, textBlockId: String) {
    val viewModel: TextBlockViewModel = viewModel()
    val textBlock by viewModel.currentTextBlock.collectAsState()

    // Состояния для редактирования названия и содержимого
    var title by remember { mutableStateOf(textBlock?.title ?: "") }
    var content by remember { mutableStateOf("") }

    // Загружаем содержимое текстового блока при первом отображении экрана
    LaunchedEffect(textBlockId) {
        viewModel.loadTextBlockById(textBlockId)
        textBlock?.let {
            title = it.title
            viewModel.loadTextContent(it.contentUrl)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактировать текстовый блок") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    val coroutineScope = rememberCoroutineScope() // хуйня, чтобы саспенд функция работала
                    IconButton(
                        onClick = {
                            textBlock?.let { block ->
                                val updatedTextBlock = block.copy(title = title)
                                coroutineScope.launch {
                                    viewModel.uploadTextBlock(updatedTextBlock, content)
                                    navController.popBackStack()
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Сохранить")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Поле для редактирования названия
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле для редактирования содержимого
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Содержимое") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                maxLines = Int.MAX_VALUE
            )
        }
    }
}