package com.example.pastebinapp.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pastebinapp.model.TextBlock
import com.example.pastebinapp.viewmodel.TextBlockViewModel
import kotlinx.coroutines.launch

@Composable
fun CreateTextBlockScreen(navController: NavController) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val viewModel: TextBlockViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Название") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Содержимое") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            maxLines = Int.MAX_VALUE
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val textBlock = TextBlock(
                    id = System.currentTimeMillis().toString(),
                    title = title,
                    contentUrl = ""
                )
                coroutineScope.launch {
                    val result = viewModel.uploadTextBlock(textBlock, content)
                    result.onSuccess {
                        Toast.makeText(context, "Текстовый блок сохранён", Toast.LENGTH_SHORT).show()
                        navController.navigate("textBlockList") // Переход к списку текстовых блоков
                    }.onFailure {
                        Toast.makeText(context, "Ошибка: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить")
        }
    }
}