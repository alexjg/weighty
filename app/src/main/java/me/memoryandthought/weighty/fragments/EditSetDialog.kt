package me.memoryandthought.weighty.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import me.memoryandthought.weighty.InjectorUtils
import me.memoryandthought.weighty.R
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.domain.Set
import me.memoryandthought.weighty.viewmodels.EditSetViewModel
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.textChangedListener

class EditSetDialog: DialogFragment() {
    private lateinit var exercise: Exercise
    private var okButton: Button? = null
    private var editingSet: Set? = null
    private var templateSet: Set? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exercise = arguments!!.getParcelable("exercise")!!
        editingSet = arguments?.getParcelable("editingSet")
        templateSet = arguments?.getParcelable("templateSet")
    }

    override fun onStart() {
        super.onStart()
        okButton = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val vmFactory = InjectorUtils.provideEditSetViewModelFactory(context!!, exercise, editingSet, templateSet)
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
                            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
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
                            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                            setText(viewModel.reps)
                            textChangedListener {
                                onTextChanged { charsequence, p1, p2, p3 ->
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
                                onTextChanged { charsequence, p1, p2, p3 ->
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
            viewModel.isValid.observe(this, Observer {
                okButton?.isEnabled = it
            })
            builder.setTitle(if (editingSet != null) (R.string.edit_set_title) else R.string.add_set_title)
                .setView(addSetView)
                .setPositiveButton(
                    if (editingSet != null) (R.string.edit_set_positive) else(R.string.add_set_positive)
                ) { dialog, id ->
                    viewModel.saveSet()
                    getDialog()?.dismiss()
                }
                .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, id ->
                    getDialog()?.cancel()
                }).create()
        } ?: throw IllegalStateException("Invalid activity")
    }

}