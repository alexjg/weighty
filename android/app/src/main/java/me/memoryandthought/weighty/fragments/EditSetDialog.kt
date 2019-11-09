package me.memoryandthought.weighty.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.viewModelScope
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.memoryandthought.weighty.InjectorUtils
import me.memoryandthought.weighty.R
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.domain.Set
import me.memoryandthought.weighty.viewmodels.Create
import me.memoryandthought.weighty.viewmodels.EditSetViewModel
import me.memoryandthought.weighty.viewmodels.FormDialogMode
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.textChangedListener

class EditSetDialog: DialogFragment() {
    private lateinit var exercise: Exercise
    private lateinit var mode: FormDialogMode<Set>
    private var okButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exercise = arguments!!.getParcelable("exercise")!!
        mode = arguments?.getParcelable<FormDialogMode<Set>>("mode") ?: (Create as FormDialogMode<Set>)
    }

    override fun onStart() {
        super.onStart()
        okButton = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val vmFactory = InjectorUtils.provideEditSetViewModelFactory(context!!, exercise, mode)
        val viewModel = ViewModelProviders.of(this, vmFactory)
            .get(EditSetViewModel::class.java)
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val addSetView = AnkoContext.createReusable(activity!!).apply {
                linearLayout {
                    orientation = LinearLayout.HORIZONTAL
                    materialTextInputLayout {
                        materialTextInputEditText {
                            hint = context.getString(R.string.add_set_weight)
                            setText(viewModel.weight)
                            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                            textChangedListener {
                                onTextChanged { charsequence, _, _, _ ->
                                    viewModel.weight = charsequence.toString()
                                }
                            }
                        }
                    }.lparams{
                        width = 0
                        weight = 1.0f
                    }
                    view{}.lparams{
                        width = dip(8)
                        height = dip(1)
                    }
                    materialTextInputLayout {
                        hint = context.getString(R.string.add_set_reps)
                        materialTextInputEditText {
                            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
                            setText(viewModel.reps)
                            textChangedListener {
                                onTextChanged { charsequence, _, _, _ ->
                                    viewModel.reps = charsequence.toString()
                                }
                            }
                        }
                    }.lparams{
                        width = 0
                        weight = 1.0f
                    }
                    view{}.lparams{
                        width = dip(8)
                        height = dip(1)
                    }
                    materialTextInputLayout {
                        hint = context.getString(R.string.add_set_rpe)
                        materialTextInputEditText {
                            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                            setText(viewModel.rpe)
                            textChangedListener {
                                onTextChanged { charsequence, _, _, _ ->
                                    viewModel.rpe = charsequence.toString()
                                }
                            }
                        }
                    }.lparams {
                        width = 0
                        weight = 1.0f
                    }
                    leftPadding = dip(24)
                    rightPadding = dip(24)
                    bottomPadding= dip(24)
                    topPadding = dip(8)
                    lparams{
                        width = matchParent
                        height = wrapContent
                    }
                }
            }.view
            viewModel.isValid.observe(this, Observer { isValid ->
                okButton?.isEnabled = isValid
            })
            val dialog = builder.setTitle(viewModel.title)
                .setView(addSetView)
                .setPositiveButton(viewModel.positiveButton, null)
                .setNegativeButton(R.string.cancel, null)
                .create()
            // This hack is necessary rather than doing it in the listener for setPositiveButton
            // because the dialog always dismisses when the handler completes but we actually need
            // wait until the viewModel.saveSet call is complete
            dialog.setOnShowListener {
                val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                button.setOnClickListener {
                    viewModel.viewModelScope.launch {
                        viewModel.saveSet()
                        withContext(Dispatchers.Main){
                            dialog?.dismiss()
                        }
                    }
                }
            }
            dialog

        } ?: throw IllegalStateException("Invalid activity")
    }

}