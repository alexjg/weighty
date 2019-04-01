package me.memoryandthought.weighty.fragments

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.*
import com.google.android.material.card.MaterialCardView
import me.memoryandthought.weighty.InjectorUtils
import me.memoryandthought.weighty.R
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.domain.Set
import me.memoryandthought.weighty.viewmodels.ExerciseHistoryItem
import me.memoryandthought.weighty.viewmodels.ExerciseHistoryViewModel
import me.memoryandthought.weighty.viewmodels.SetRow
import me.memoryandthought.weighty.viewmodels.WorkoutHeader
import org.jetbrains.anko.*
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.UI
import java.lang.IllegalStateException

class ExerciseHistoryFragment : Fragment() {

    private lateinit var viewModel: ExerciseHistoryViewModel
    private lateinit var historyView: RecyclerView
    private var historyAdapter = ExerciseHistoryAdapter(::onClickSet)
    private lateinit var exercise: Exercise
    private var mostRecentSet: Set? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args: ExerciseHistoryFragmentArgs by navArgs()
        exercise = args.exercise
        val vmFactory = InjectorUtils.provideExerciseHistoryViewModelFactory(
            context!!,
            args.exercise
        )
        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(ExerciseHistoryViewModel::class.java)
        viewModel.historyItems().observe(this, Observer {
            historyAdapter.submitList(it)
        })
        viewModel.mostRecentSet().observe(this, Observer {
            mostRecentSet = it
        })

    }

    override fun onResume() {
        super.onResume()
        activity?.setTitle(exercise.name)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  UI {
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
                        mostRecentSet?.let {
                            args.putParcelable("templateSet", it)
                        }
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
        val swipeHandler = object : SwipeToDeleteCallback(context!!) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (viewHolder is ExerciseHistoryAdapter.ViewHolder) {
                    val vh = viewHolder.view as SetRowView
                    vh.set?.let { set ->
                        viewModel.archiveSet(set)
                        view.snackbar(R.string.set_archived, R.string.undo) {
                            viewModel.unarchiveSet(set)
                        }
                    }
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(historyView)
        return view
    }

    fun onClickSet(set: Set): Unit {
        val dialog = EditSetDialog()
        val args = Bundle()
        args.putParcelable("exercise", this@ExerciseHistoryFragment.exercise!!)
        args.putParcelable("editingSet", set)
        dialog.arguments = args
        dialog.show(activity!!.supportFragmentManager, "edit_set")
    }

}

class ExerciseHistoryAdapter(private val onClickSet: (Set) -> Unit): ListAdapter<ExerciseHistoryItem, ExerciseHistoryAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        val HEADER_ITEM: Int = 1
        val SET_ROW_ITEM: Int = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is  WorkoutHeader -> HEADER_ITEM
            is SetRow -> SET_ROW_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = when (viewType) {
            HEADER_ITEM -> WorkoutHeaderView(parent.context)
            SET_ROW_ITEM -> SetRowView(parent.context, onClickSet)
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

class SetRowView(context: Context, private val onClickSet: (Set) -> Unit ) : MaterialCardView(context) {
    private lateinit var weightView: SetRowColumnView
    private lateinit var repsView: SetRowColumnView
    private lateinit var rpeView: SetRowColumnView
    var set: Set? = null
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
            onClick {
                set?.let {
                    onClickSet(it)
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
        set = row.set
        weightView.valueText = row.weight
        repsView.valueText = row.reps
        rpeView.valueText = row.rpe
    }
}


class SetRowColumnView(context: Context): _LinearLayout(context) {
    private val headerView: TextView
    private val valueView: TextView
    private var set: Set? = null
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


abstract class SwipeToDeleteCallback(private val context: Context) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
    private val background = ColorDrawable()
    private val deleteIcon = ContextCompat.getDrawable(context, android.R.drawable.ic_delete)!!
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }


    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        if (viewHolder.itemViewType == ExerciseHistoryAdapter.HEADER_ITEM) {
            return 0
        }
        return super.getMovementFlags(recyclerView, viewHolder)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int, isCurrentlyActive: Boolean ) { val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = (dX == 0f) and !isCurrentlyActive

        if (isCanceled) {
            clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
        }

        background.color = context.getColor(R.color.primary_dark_material_dark)
        background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
        background.draw(c)

        val deleteIconTop = itemView.top + (itemHeight - deleteIcon.intrinsicHeight) / 2
        val deleteIconMargin = (itemHeight - deleteIcon.intrinsicHeight) / 2
        val deleteIconLeft = itemView.right - deleteIconMargin - deleteIcon.intrinsicHeight
        val deleteIconRight = itemView.right - deleteIconMargin
        val deleteIconBottom = deleteIconTop + deleteIcon.intrinsicHeight

        deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
        deleteIcon.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float){
        c?.drawRect(left, top, right, bottom, clearPaint)
    }


}