package me.memoryandthought.weighty.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.textfield.TextInputEditText
import me.memoryandthought.weighty.*
import me.memoryandthought.weighty.viewmodels.ExercisesViewModel
import org.jetbrains.anko.*
import java.lang.IllegalStateException

class CreateExerciseDialog : DialogFragment() {
    private lateinit var viewModel: ExercisesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vmFactory = InjectorUtils.provideExercisesViewModelFactory(context!!)
        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(ExercisesViewModel::class.java)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var nameText: TextInputEditText
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val createExerciseView: View = AnkoContext.createReusable(
                activity!!,
                false
            ).apply {
                frameLayout {
                    materialTextInputLayout {
                        hint = context.getString(R.string.create_exercise_name_hint)
                        nameText = materialTextInputEditText {

                        }.lparams{
                            width = matchParent
                            height = wrapContent
                            topMargin = dip(16)
                            leftMargin = dip(16)
                            rightMargin = dip(16)
                            bottomMargin = dip(16)
                        }
                    }
                }
            }.view
            createExerciseView.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            builder.setTitle(R.string.create_exercise_title)
                .setView(createExerciseView)
                .setPositiveButton(R.string.create_exercise_positive, DialogInterface.OnClickListener { dialog, id ->
                    viewModel.addExercise(nameText.text.toString())
                    getDialog()?.dismiss()
                })
                .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, id ->
                    getDialog()?.cancel()
                })
            builder.create()
        } ?: throw IllegalStateException("Invalid activity")
    }

}