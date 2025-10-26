package me.memoryandthought.weighty.fragments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.memoryandthought.weighty.domain.Exercise

@Composable
fun ExerciseList(
    exercises: List<Exercise>,
    onClickExercise: (Exercise) -> Unit,
    onClickEditExercise: (Exercise) -> Unit,
    onClickArchiveExercise: (Exercise) -> Unit,
    onClickCreateExercise: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onClickCreateExercise) {
                Icon(Icons.Filled.Add, contentDescription = "Add exercise")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(exercises) { exercise ->
                ExerciseListItem(
                    exercise = exercise,
                    onClickExercise = onClickExercise,
                    onClickEditExercise = onClickEditExercise,
                    onClickArchiveExercise = onClickArchiveExercise
                )
            }
        }
    }
}

@Composable
fun ExerciseListItem(
    exercise: Exercise,
    onClickExercise: (Exercise) -> Unit,
    onClickEditExercise: (Exercise) -> Unit,
    onClickArchiveExercise: (Exercise) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClickExercise(exercise) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = exercise.name, modifier = Modifier.weight(1f))
            var showMenu by remember { mutableStateOf(false) }
            IconButton(onClick = { showMenu = !showMenu }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options")
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            onClickEditExercise(exercise)
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Archive") },
                        onClick = {
                            onClickArchiveExercise(exercise)
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}
