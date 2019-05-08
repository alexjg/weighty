package me.memoryandthought.weighty.fragments

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import me.memoryandthought.weighty.R
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.viewmodels.Edit
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class ExerciseListAdapter(
    private val onClickExercise: (Exercise) -> Unit,
    private val onClickEditExercise:  (Exercise) -> Unit,
    private val onClickArchiveExercise: (Exercise) -> Unit
    ) : ListAdapter<Exercise, ExerciseListAdapter.ViewHolder>(
    DIFF_CALLBACK
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val lps = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        val itemView = ExerciseListItemView(parent.context, onClickEditExercise, onClickArchiveExercise)
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

class ExerciseListItemView(
    context: Context,
    private val onClickEditExercise: (Exercise) -> Unit,
    private val onClickArchiveExercise: (Exercise) -> Unit
    ) : MaterialCardView(context), PopupMenu.OnMenuItemClickListener {
    private lateinit var nameView: TextView
    private var exercise: Exercise? = null

    init {
        AnkoContext.createDelegate(this).apply {
            relativeLayout {
                nameView = textView().lparams{
                    alignParentLeft()
                }
                overflowImageButton{
                    onClick {
                        val popup = PopupMenu(context, this@overflowImageButton, Gravity.RIGHT)
                        popup.inflate(R.menu.exercise_menu)
                        popup.setOnMenuItemClickListener(this@ExerciseListItemView)
                        popup.show()
                    }

                }.lparams{
                    alignParentRight()
                    alignParentTop()
                    width = wrapContent
                    height = wrapContent
                }
            }.apply {
                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }
        nameView.padding = dip(16)
    }


    fun bindExercise(exercise: Exercise, onClickExercise: (Exercise) -> Unit) {
        this.exercise = exercise
        nameView.text = exercise.name
        this.onClick {
            onClickExercise(exercise)
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.exercise_menu_edit -> {
                onClickEditExercise(exercise!!)
                return true
            }
            R.id.exercise_menu_archive -> {
                onClickArchiveExercise(exercise!!)
                return true
            }
            else -> false
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

