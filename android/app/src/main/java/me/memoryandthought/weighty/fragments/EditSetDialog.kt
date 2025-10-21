package me.memoryandthought.weighty.fragments

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.memoryandthought.weighty.InjectorUtils
import me.memoryandthought.weighty.R
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.domain.Set
import me.memoryandthought.weighty.viewmodels.Create
import me.memoryandthought.weighty.viewmodels.EditSetViewModel
import me.memoryandthought.weighty.viewmodels.FormDialogMode

class EditSetDialog : DialogFragment() {
    private lateinit var exercise: Exercise
    private lateinit var mode: FormDialogMode<Set>
    private var okButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exercise = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable("exercise", Exercise::class.java)
        } else {
            @Suppress("DEPRECATION")
            requireArguments().getParcelable("exercise")
        }!!

        mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments()
                .getParcelable("mode", FormDialogMode::class.java) as FormDialogMode<Set>
        } else {
            @Suppress("DEPRECATION")
            requireArguments().getParcelable("mode")
        } ?: (Create as FormDialogMode<Set>)
    }

    override fun onStart() {
        super.onStart()
        okButton = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val vmFactory = InjectorUtils.provideEditSetViewModelFactory(requireContext(), exercise, mode)

        val viewModel = ViewModelProvider(this, vmFactory)[EditSetViewModel::class.java]

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val composeView =
                ComposeView(requireContext()).apply {
                    setContent { EditSetForm(viewModel = viewModel) }
                }

            viewModel.isValid.observe(
                this,
                Observer { isValid -> okButton?.isEnabled = isValid }
            )
            val dialog =
                builder
                    .setTitle(viewModel.title)
                    .setView(composeView)
                    .setPositiveButton(viewModel.positiveButton, null)
                    .setNegativeButton(R.string.cancel, null)
                    .create()
            // This hack is necessary rather than doing it in the listener for setPositiveButton
            // because the dialog always dismisses when the handler completes but we actually need
            // wait until the viewModel.saveSet call is complete
            dialog.setOnShowListener {
                val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                button.setOnClickListener {
                    viewModel.viewModelScope.launch {
                        viewModel.saveSet()
                        withContext(Dispatchers.Main) { dialog.dismiss() }
                    }
                }
            }
            dialog
        }
            ?: throw IllegalStateException("Invalid activity")
    }
}

@Composable
fun EditSetForm(viewModel: EditSetViewModel) {
    val weight by viewModel.weight.observeAsState("")
    val reps by viewModel.reps.observeAsState("")
    val rpe by viewModel.rpe.observeAsState("")

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = weight,
            onValueChange = { viewModel.onWeightChange(it) },
            label = { Text(stringResource(R.string.add_set_weight)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = reps,
            onValueChange = { viewModel.onRepsChange(it) },
            label = { Text(stringResource(R.string.add_set_reps)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = rpe,
            onValueChange = { viewModel.onRpeChange(it) },
            label = { Text(stringResource(R.string.add_set_rpe)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
    }
}
