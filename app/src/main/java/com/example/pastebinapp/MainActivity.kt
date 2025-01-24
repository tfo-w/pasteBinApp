package com.example.pastebinapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pastebinapp.ui.CreateTextBlockScreen
import com.example.pastebinapp.ui.EditTextBlockScreen
import com.example.pastebinapp.ui.TextBlockDetailScreen
import com.example.pastebinapp.ui.TextBlockListScreen
import com.example.pastebinapp.ui.theme.PasteBinAppTheme
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.dynamiclinks.dynamicLinks


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            PasteBinAppTheme {
                val navController = rememberNavController()
                PasteBinApp(navController)
                LaunchedEffect(Unit) {
                    handleDynamicLinks(intent, navController)
                }
            }
        }
    }

    private fun handleDynamicLinks(intent: Intent?, navController: NavController) {
        Firebase.dynamicLinks.getDynamicLink(intent).addOnSuccessListener { pendingData ->
            pendingData?.link?.lastPathSegment?.let { textBlockId ->
                navController.navigate("textBlockDetail/$textBlockId")
            }
        }.addOnFailureListener { e ->
            Log.e("DynamicLinks", "Ошибка обработки ссылки", e)
        }
    }
}

@Composable
fun PasteBinApp(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "createTextBlock")
    {
        composable("createTextBlock") {
            CreateTextBlockScreen(navController)
        }
        composable("textBlockList") {
            TextBlockListScreen(navController)
        }
        composable(
            route = "textBlockDetail/{textBlockId}",
            arguments = listOf(navArgument("textBlockId") { type = NavType.StringType })
        ) { backStackEntry ->
            val textBlockId = backStackEntry.arguments?.getString("textBlockId") ?: ""
            TextBlockDetailScreen(navController, textBlockId)
        }
        composable(
            route = "editTextBlock/{textBlockId}",
            arguments = listOf(navArgument("textBlockId") { type = NavType.StringType })
        ) { backStackEntry ->
            val textBlockId = backStackEntry.arguments?.getString("textBlockId") ?: ""
            EditTextBlockScreen(navController, textBlockId)
        }
    }
}