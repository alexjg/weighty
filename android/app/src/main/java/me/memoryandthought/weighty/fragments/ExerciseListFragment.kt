package me.memoryandthought.weighty.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import me.memoryandthought.weighty.InjectorUtils
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.viewmodels.Create
import me.memoryandthought.weighty.viewmodels.Edit
import me.memoryandthought.weighty.viewmodels.ExercisesViewModel
import me.memoryandthought.weighty.viewmodels.FormDialogMode

class ExerciseListFragment : Fragment() {
    private lateinit var viewModel: ExercisesViewModel

    override fun onResume() {
        super.onResume()
        activity?.setTitle("Exercises")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val vmFactory = InjectorUtils.provideExercisesViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, vmFactory)[ExercisesViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    var dialogMode by remember { mutableStateOf<FormDialogMode<Exercise>?>(null) }

                    val exercises by viewModel.loadExercises().observeAsState(initial = emptyList())
                    ExerciseList(
                        exercises = exercises,
                        onClickExercise = ::viewExerciseHistory,
                        onClickEditExercise = { exercise -> dialogMode = Edit(exercise) },
                        onClickArchiveExercise = ::archiveExercise,
                        onClickCreateExercise = { dialogMode = Create }
                    )

                    dialogMode?.let {
                        EditExerciseDialog(
                            mode = it,
                            onDismiss = { dialogMode = null }
                        )
                    }
                }
            }
        }
    }

    private fun viewExerciseHistory(exercise: Exercise) {
        val action =
            ExerciseListFragmentDirections.actionExerciseListFragmentViewExerciseHistory(exercise)
        findNavController().navigate(action)
    }

    private fun archiveExercise(exercise: Exercise) {
        viewModel.archiveExercise(exercise)
    }
}