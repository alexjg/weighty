package me.memoryandthought.weighty.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.memoryandthought.weighty.InjectorUtils
import me.memoryandthought.weighty.R
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.viewmodels.EditExerciseViewModel
import me.memoryandthought.weighty.viewmodels.FormDialogMode

@Composable
fun EditExerciseDialog(
    mode: FormDialogMode<Exercise>,
    onDismiss: () -> Unit
) {
    val viewModel: EditExerciseViewModel = viewModel(
        factory = InjectorUtils.provideEditExerciseViewModelFactory(
            context = LocalContext.current,
            mode = mode
        )
    )

    val nameState = remember { mutableStateOf(viewModel.name) }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(id = viewModel.titleResource),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = nameState.value,
                    onValueChange = { nameState.value = it },
                    label = { Text(stringResource(R.string.create_exercise_name_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.save(nameState.value)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(id = viewModel.positiveButtonResource))
                }
            }
        }
    }
}