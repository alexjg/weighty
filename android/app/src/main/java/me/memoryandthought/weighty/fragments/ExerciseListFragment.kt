package me.memoryandthought.weighty.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.memoryandthought.weighty.*
import me.memoryandthought.weighty.viewmodels.Create
import me.memoryandthought.weighty.viewmodels.Edit
import me.memoryandthought.weighty.viewmodels.ExercisesViewModel
import org.jetbrains.anko.*
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.UI

class ExerciseListFragment: Fragment() {
    private lateinit var viewModel: ExercisesViewModel
    private lateinit var exercisesView: RecyclerView
    private var exercisesAdapter = ExerciseListAdapter({ exercise ->
        val action = ExerciseListFragmentDirections.actionExerciseListFragmentViewExerciseHistory(exercise)
        findNavController().navigate(action)
    }, { exercise ->
        val args = Bundle()
        args.putParcelable("mode", Edit(exercise))
        val fragment = EditExerciseDialog()
        fragment.arguments = args
        fragment.show(activity!!.supportFragmentManager, "edit_exercise")
    }, { exercise ->
        viewModel.archiveExercise(exercise)
        view?.snackbar(R.string.exercise_archived, R.string.undo){
            viewModel.unarchiveExercise(exercise)
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vmFactory = InjectorUtils.provideExercisesViewModelFactory(context!!)
        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(ExercisesViewModel::class.java)
        viewModel.loadExercises().observe(this, Observer { exercises ->
            exercisesAdapter.submitList(exercises)
        })

    }

    override fun onResume() {
        super.onResume()
        activity?.setTitle("Exercises")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return UI {
            frameLayout {

                view {
                    keepScreenOn = true
                }.lparams(width = matchParent, height = matchParent)
                frameLayout {
                    fitsSystemWindows = true

                    linearLayout { orientation = LinearLayout.HORIZONTAL
                        exercisesView = recyclerView {
                            layoutManager = LinearLayoutManager(context)
                            adapter = exercisesAdapter
                            addItemDecoration(MarginItemDecoration(dip(5)))
                        }.lparams(width = matchParent, height = matchParent)

                    }.lparams(width = matchParent, height = matchParent)
                    materialFloatingActionButton(R.style.Widget_MaterialComponents_FloatingActionButton) {
                        imageResource = android.R.drawable.ic_input_add
                        onClick {
                            val createExerciseFragment = EditExerciseDialog()
                            val args = Bundle()
                            args.putParcelable("mode", Create)
                            createExerciseFragment.arguments = args
                            createExerciseFragment.show(activity!!.supportFragmentManager, "create exercise")
                        }
                    }.lparams {
                        rightMargin = dip(16)
                        bottomMargin = dip(16)
                        gravity = Gravity.END or Gravity.BOTTOM
                    }
                }.lparams(width = matchParent, height = matchParent)
            }
        }.view
    }

}