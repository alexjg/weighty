package me.memoryandthought.weighty.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.launch
import me.memoryandthought.weighty.InjectorUtils
import me.memoryandthought.weighty.R
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.domain.Set
import me.memoryandthought.weighty.viewmodels.Create
import me.memoryandthought.weighty.viewmodels.CreateFromTemplate
import me.memoryandthought.weighty.viewmodels.Edit
import me.memoryandthought.weighty.viewmodels.ExerciseHistoryViewModel
import me.memoryandthought.weighty.viewmodels.FormDialogMode
import me.memoryandthought.weighty.viewmodels.SetRow
import me.memoryandthought.weighty.viewmodels.WorkoutHeader

class ExerciseHistoryFragment : Fragment() {

    private lateinit var viewModel: ExerciseHistoryViewModel
    private lateinit var exercise: Exercise

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args: ExerciseHistoryFragmentArgs by navArgs()
        exercise = args.exercise
        val vmFactory = InjectorUtils.provideExerciseHistoryViewModelFactory(
            requireContext(),
            args.exercise
        )
        viewModel = ViewModelProvider(this, vmFactory)[ExerciseHistoryViewModel::class.java]
    }

    override fun onResume() {
        super.onResume()
        activity?.title = exercise.name
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ExerciseHistoryScreen(
                        viewModel = viewModel,
                        exercise = exercise
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseHistoryScreen(
    viewModel: ExerciseHistoryViewModel,
    exercise: Exercise
) {
    val historyItems by viewModel.historyItems().observeAsState(initial = emptyList())
    val mostRecentSet by viewModel.mostRecentSet().observeAsState()
    var dialogMode by remember { mutableStateOf<FormDialogMode<Set>?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val addSetString = stringResource(id = R.string.add_set)
    val setArchivedString = stringResource(id = R.string.set_archived)
    val undoString = stringResource(id = R.string.undo)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val mode = mostRecentSet?.let { CreateFromTemplate(it) } ?: Create
                dialogMode = mode
            }) {
                Icon(Icons.Filled.Add, contentDescription = addSetString)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            items(
                items = historyItems,
                key = { item ->
                    when (item) {
                        is WorkoutHeader -> item.date
                        is SetRow -> item.set.id
                    }
                }
            ) { item ->
                when (item) {
                    is WorkoutHeader -> WorkoutHeaderComposable(item)
                    is SetRow -> {
                        val currentSet = item.set
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.archiveSet(currentSet)
                                    scope.launch {
                                        val snackbarResult = snackbarHostState.showSnackbar(
                                            message = setArchivedString,
                                            actionLabel = undoString
                                        )
                                        if (snackbarResult == SnackbarResult.ActionPerformed) {
                                            viewModel.unarchiveSet(currentSet)
                                        }
                                    }
                                    return@rememberSwipeToDismissBoxState true
                                }
                                false
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromEndToStart = true,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                val color by animateColorAsState(
                                    when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.EndToStart -> Color.Red
                                        else -> Color.Transparent
                                    }, label = "background color"
                                )
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(color)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete Icon",
                                        tint = Color.White
                                    )
                                }
                            },
                        ) {
                            SetRowComposable(row = item, onClickSet = { dialogMode = Edit(it) })
                        }
                    }
                }
            }
        }
    }
    dialogMode?.let { mode ->
        EditSetDialog(
            exercise = exercise,
            mode = mode,
            onDismiss = { dialogMode = null }
        )
    }
}

@Composable
fun WorkoutHeaderComposable(header: WorkoutHeader) {
    Text(
        text = header.date,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@Composable
fun SetRowComposable(row: SetRow, onClickSet: (Set) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickSet(row.set) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SetRowDataItemComposable(
                header = stringResource(id = R.string.add_set_weight),
                value = row.weight,
                modifier = Modifier.weight(1f)
            )
            SetRowDataItemComposable(
                header = stringResource(id = R.string.add_set_reps),
                value = row.reps,
                modifier = Modifier.weight(1f)
            )
            SetRowDataItemComposable(
                header = stringResource(id = R.string.add_set_rpe),
                value = row.rpe,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SetRowDataItemComposable(header: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = header, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(text = value, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
    }
}
