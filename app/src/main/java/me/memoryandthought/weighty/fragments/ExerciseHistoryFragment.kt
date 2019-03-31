package me.memoryandthought.weighty.fragments

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.BundleCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.*
import com.google.android.material.card.MaterialCardView
import me.memoryandthought.weighty.InjectorUtils
import me.memoryandthought.weighty.R
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.viewmodels.ExerciseHistoryItem
import me.memoryandthought.weighty.viewmodels.ExerciseHistoryViewModel
import me.memoryandthought.weighty.viewmodels.SetRow
import me.memoryandthought.weighty.viewmodels.WorkoutHeader
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.UI
import java.lang.IllegalStateException
import java.util.UUID

class ExerciseHistoryFragment : Fragment() {

    private lateinit var viewModel: ExerciseHistoryViewModel
    private lateinit var historyView: RecyclerView
    private var historyAdapter = ExerciseHistoryAdapter()
    private var exercise: Exercise? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args: ExerciseHistoryFragmentArgs by navArgs()
        val vmFactory = InjectorUtils.provideExerciseHistoryViewModelFactory(
            context!!,
            UUID.fromString(args.exerciseId)
        )
        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(ExerciseHistoryViewModel::class.java)
        viewModel.historyItems().observe(this, Observer {
            historyAdapter.submitList(it)
        })

    }

    override fun onResume() {
        super.onResume()
        viewModel.exercise().observe(this, Observer { exercise ->
            exercise?.let {
                activity?.setTitle(exercise.name)
                this.exercise = it
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return UI {
            frameLayout {
                historyView = recyclerView {
                    layoutManager = LinearLayoutManager(context)
                    adapter = historyAdapter
                }.lparams {
                    width = matchParent
                    height = wrapContent
                }
                materialFloatingActionButton(R.style.Widget_MaterialComponents_FloatingActionButton) {
                    imageResource = android.R.drawable.ic_input_add
                    onClick {
                        val dialog = EditSetDialog()
                        val args = Bundle()
                        args.putParcelable("exercise", this@ExerciseHistoryFragment.exercise!!)
                        dialog.arguments = args
                        dialog.show(activity!!.supportFragmentManager, "add_set")
                    }
                }.lparams {
                    rightMargin = dip(16)
                    bottomMargin = dip(16)
                    gravity = Gravity.END or Gravity.BOTTOM
                }
            }

        }.view
    }

}

class ExerciseHistoryAdapter: ListAdapter<ExerciseHistoryItem, ExerciseHistoryAdapter.ViewHolder>(DIFF_CALLBACK) {

    private val HEADER_ITEM: Int = 1
    private val SET_ROW_ITEM: Int = 2

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is  WorkoutHeader -> HEADER_ITEM
            is SetRow -> SET_ROW_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = when (viewType) {
            HEADER_ITEM -> WorkoutHeaderView(parent.context)
            SET_ROW_ITEM -> SetRowView(parent.context)
            else -> throw IllegalStateException("Unknown viewType $viewType")
        }
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        when (item) {
            is WorkoutHeader -> {
                val itemView = holder.view as WorkoutHeaderView
                itemView.bindWorkoutHeader(item)
            }
            is SetRow -> {
                val itemView = holder.view as SetRowView
                itemView.bindSetRow(item)
            }
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    object DIFF_CALLBACK : DiffUtil.ItemCallback<ExerciseHistoryItem>() {

        override fun areContentsTheSame(oldItem: ExerciseHistoryItem, newItem: ExerciseHistoryItem): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: ExerciseHistoryItem, newItem: ExerciseHistoryItem): Boolean {
            return oldItem == newItem
        }

    }

}

class SetRowView(context: Context) : MaterialCardView(context) {
    private lateinit var weightView: SetRowColumnView
    private lateinit var repsView: SetRowColumnView
    private lateinit var rpeView: SetRowColumnView
    private var container: LinearLayout

    init {
        AnkoContext.createDelegate(this).apply {
            container = linearLayout(){
                weightView = setRowDataItem() {
                    headerText = "Weight"
                    textGravity = Gravity.LEFT
                }.lparams{
                    width = 0
                    weight = 1.0f
                }
                repsView = setRowDataItem() {
                    headerText = "Reps"
                    textGravity = Gravity.CENTER_HORIZONTAL
                }.lparams{
                    width = 0
                    weight = 1.0f
                }
                rpeView = setRowDataItem() {
                    headerText = "RPE"
                    textGravity = Gravity.RIGHT
                }.lparams {
                    width = 0
                    weight = 1.0f
                }
            }
        }
        layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, dip(70))
        container.leftPadding = dip(16)
        container.rightPadding = dip(16)
        container.topPadding = dip(8)
        weightView.headerText = "Weight"
    }

    fun bindSetRow(row: SetRow) {
        weightView.valueText = row.weight
        repsView.valueText = row.reps
        rpeView.valueText = row.rpe
    }
}


class SetRowColumnView(context: Context): _LinearLayout(context) {
    private val headerView: TextView
    private val valueView: TextView
    var headerText: String = ""
        set(value)  {
            headerView.text = value
            field = value
        }
    var valueText: String = ""
        set(value) {
            valueView.text = value
            field = value
        }
    var textGravity: Int = Gravity.CENTER
        set(value) {
            field = value
            headerView.gravity = value
            valueView.gravity = value
            requestLayout()
        }


    init {
        AnkoContext.createDelegate(this).apply {
            headerView = textView() {
                textAppearance = com.google.android.material.R.style.TextAppearance_MaterialComponents_Subtitle1
                gravity = Gravity.CENTER_HORIZONTAL
            }.lparams {
                height = 0
                weight = 1.0f
                width = matchParent
            }
            valueView = textView {
                textAppearance = com.google.android.material.R.style.TextAppearance_MaterialComponents_Headline6
                gravity = Gravity.CENTER_HORIZONTAL
            }.lparams {
                height = 0
                weight = 1.0f
                width = matchParent
            }
            lparams {
                height = matchParent
                width = matchParent
            }
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
    }
}

class WorkoutHeaderView(context: Context): LinearLayout(context) {
    private var dateTextView = TextView(context)

    init {
        this.addView(dateTextView)
        dateTextView.textAppearance = com.google.android.material.R.style.TextAppearance_MaterialComponents_Subtitle2
        layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        padding = dip(16)
    }

    fun bindWorkoutHeader(header: WorkoutHeader) {
        dateTextView.text = header.date
    }
}
