package me.memoryandthought.weighty.fragments
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.viewModelScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import me.memoryandthought.weighty.*
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.viewmodels.EditExerciseViewModel
import me.memoryandthought.weighty.viewmodels.FormDialogMode
import org.jetbrains.anko.*
import java.lang.IllegalStateException

class EditExerciseDialog : DialogFragment() {
    private lateinit var viewModel: EditExerciseViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var mode: FormDialogMode<Exercise> = arguments?.getParcelable("mode")!!
        val vmFactory = InjectorUtils.provideEditExerciseViewModelFactory(context!!, mode)
        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(EditExerciseViewModel::class.java)
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
                            setText(viewModel.name)
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
            val dialog = builder.setTitle(viewModel.titleResource)
                .setView(createExerciseView)
                .setPositiveButton(viewModel.positiveButtonResource, null)
                .setNegativeButton(R.string.cancel, null)
                .create()

            // This hack is necessary rather than doing it in the listener for setPositiveButton
            // because the dialog always dismisses when the handler completes but we actually need
            // wait until the viewModel.save call is complete
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    viewModel.viewModelScope.launch {
                        viewModel.save(nameText.text.toString())
                        dialog?.dismiss()
                    }
                }
            }
            dialog
        } ?: throw IllegalStateException("Invalid activity")
    }

}