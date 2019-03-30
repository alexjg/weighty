package me.memoryandthought.weighty

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import me.memoryandthought.weighty.fragments.ExerciseListAdapter
import me.memoryandthought.weighty.fragments.ExerciseListFragment
import me.memoryandthought.weighty.viewmodels.ExercisesViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton as MaterialFloatingActionButton

/**
 * List of exercises
 */
class WeightyActivity : FragmentActivity() {

    private lateinit var viewModel: ExercisesViewModel
    private lateinit var exercisesView: RecyclerView
    private var exercisesAdapter = ExerciseListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ft = supportFragmentManager.beginTransaction()
        val fragment = NavHostFragment.create(R.navigation.nav_graph)
        ft.add(android.R.id.content, fragment)
        ft.commit()
    }

}

