package me.memoryandthought.weighty.fragments

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
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.R
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.dip
import org.jetbrains.anko.padding
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.textView
import java.util.*

class ExerciseListAdapter(private val onClickExercise: (UUID) -> Unit) : ListAdapter<Exercise, ExerciseListAdapter.ViewHolder>(
    DIFF_CALLBACK
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val lps = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        val itemView = ExerciseListItemView(parent.context)
        itemView.layoutParams = lps
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.view.bindExercise(getItem(position), onClickExercise)
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

class ExerciseListItemView(context: Context) : MaterialCardView(context) {
    private lateinit var nameView: TextView

    init {
        AnkoContext.createDelegate(this).apply {
            nameView = textView()
        }
        nameView.padding = dip(16)
    }


    fun bindExercise(exercise: Exercise, onClickExercise: (UUID) -> Unit) {
        nameView.text = exercise.name
        nameView.onClick {
            onClickExercise(exercise.id)
        }
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

