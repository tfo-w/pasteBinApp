package com.example.pastebinapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pastebinapp.model.TextBlock
import com.example.pastebinapp.viewmodel.TextBlockViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextBlockListScreen(navController: NavController) {
    val viewModel = TextBlockViewModel()
    val textBlocks by viewModel.textBlocks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Список текстовых блоков") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("createTextBlock") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            items(textBlocks) { textBlock ->
                TextBlockItem(textBlock, onClick = {
                    // переход к экрану деталей текстового блока
                    navController.navigate("textBlockDetail/${textBlock.id}")
                })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TextBlockItem(textBlock: TextBlock, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = textBlock.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Создано: ${textBlock.createdAt}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


