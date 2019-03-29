package me.memoryandthought.weighty

import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.dip
import org.jetbrains.anko.textView

class ExerciseListAdapter : ListAdapter<Exercise, ExerciseListAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val lps = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, parent.context.dip(40));
        val itemView = ExerciseListItemView(parent.context)
        itemView.layoutParams = lps
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.view.bindExercise(getItem(position))
    }

    class ViewHolder(val view: ExerciseListItemView) : RecyclerView.ViewHolder(view)

    object DIFF_CALLBACK : DiffUtil.ItemCallback<Exercise>() {

        override fun areContentsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
            return oldItem == newItem
        }

    }

}

class ExerciseListItemView : MaterialCardView {
    private lateinit var nameView: TextView

    constructor(context: Context?) : super(context, null, R.style.Widget_MaterialComponents_CardView) {
        init()
    }

    private fun init() = AnkoContext.createDelegate(this).apply {
        nameView = textView()
    }


    fun bindExercise(exercise: Exercise) {
        nameView.text = exercise.name
    }

}

class MarginItemDecoration(private val margin: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        with(outRect) {
            if (parent.getChildAdapterPosition(view) == 0){
                top = margin
            }
            left = margin
            right = margin
            bottom = margin
        }
    }

}

