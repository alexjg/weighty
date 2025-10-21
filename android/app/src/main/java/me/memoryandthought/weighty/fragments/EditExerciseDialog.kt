package me.memoryandthought.weighty.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import me.memoryandthought.weighty.InjectorUtils
import me.memoryandthought.weighty.R
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.viewmodels.EditExerciseViewModel
import me.memoryandthought.weighty.viewmodels.FormDialogMode

class EditExerciseDialog : DialogFragment() {
    private lateinit var viewModel: EditExerciseViewModel
    private val nameState = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mode: FormDialogMode<Exercise> = requireArguments().getParcelable("mode")!!
        val vmFactory = InjectorUtils.provideEditExerciseViewModelFactory(requireContext(), mode)
        viewModel = ViewModelProvider(this, vmFactory)[EditExerciseViewModel::class.java]
        nameState.value = viewModel.name
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val composeView = ComposeView(requireContext()).apply {
            setContent {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = nameState.value,
                        onValueChange = { nameState.value = it },
                        label = { Text(stringResource(R.string.create_exercise_name_hint)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        val dialog = AlertDialog.Builder(requireActivity())
            .setTitle(viewModel.titleResource)
            .setView(composeView)
            .setPositiveButton(viewModel.positiveButtonResource, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                lifecycleScope.launch {
                    viewModel.save(nameState.value)
                    dialog.dismiss()
                }
            }
        }

        return dialog
    }
}
