package me.memoryandthought.weighty.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.util.UUID

@Composable
fun WeightyApp() {
    val navController = rememberNavController()

    MaterialTheme {
        NavHost(
            navController = navController,
            startDestination = "exercises"
        ) {
            composable("exercises") {
                ExerciseListScreen(
                    onExerciseClick = { exercise ->
                        navController.navigate("exercise/${exercise.id}")
                    }
                )
            }

            composable(
                route = "exercise/{exerciseId}",
                arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
            ) { backStackEntry ->
                val exerciseId = UUID.fromString(
                    requireNotNull(backStackEntry.arguments?.getString("exerciseId"))
                )
                ExerciseHistoryScreen(
                    exerciseId = exerciseId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}