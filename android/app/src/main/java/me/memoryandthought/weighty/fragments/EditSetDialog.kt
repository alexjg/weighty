package me.memoryandthought.weighty.fragments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.memoryandthought.weighty.InjectorUtils
import me.memoryandthought.weighty.R
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.domain.Set
import me.memoryandthought.weighty.viewmodels.EditSetViewModel
import me.memoryandthought.weighty.viewmodels.FormDialogMode

@Composable
fun EditSetDialog(
    exercise: Exercise,
    mode: FormDialogMode<Set>,
    onDismiss: () -> Unit
) {
    val viewModel: EditSetViewModel = viewModel(
        factory = InjectorUtils.provideEditSetViewModelFactory(
            context = LocalContext.current,
            exercise = exercise,
            mode = mode
        )
    )

    val weight by viewModel.weight.collectAsState()
    val reps by viewModel.reps.collectAsState()
    val rpe by viewModel.rpe.collectAsState()
    val isValid by viewModel.isValid.collectAsState()
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = viewModel.title,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = weight,
                    onValueChange = { viewModel.onWeightChange(it) },
                    label = { Text(stringResource(R.string.add_set_weight)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reps,
                    onValueChange = { viewModel.onRepsChange(it) },
                    label = { Text(stringResource(R.string.add_set_reps)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rpe,
                    onValueChange = { viewModel.onRpeChange(it) },
                    label = { Text(stringResource(R.string.add_set_rpe)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(id = R.string.cancel))
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    TextButton(
                        enabled = isValid,
                        onClick = {
                            scope.launch {
                                viewModel.saveSet()
                                onDismiss()
                            }
                        }
                    ) {
                        Text(viewModel.positiveButton)
                    }
                }
            }
        }
    }
}
