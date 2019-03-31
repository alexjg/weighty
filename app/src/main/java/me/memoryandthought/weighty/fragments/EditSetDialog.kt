package me.memoryandthought.weighty.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.textfield.TextInputEditText
import me.memoryandthought.weighty.InjectorUtils
import me.memoryandthought.weighty.R
import me.memoryandthought.weighty.domain.Exercise
import me.memoryandthought.weighty.viewmodels.EditSetViewModel
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.textChangedListener

class EditSetDialog: DialogFragment() {
    private lateinit var exercise: Exercise
    private var okButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exercise = arguments!!.getParcelable("exercise")
    }

    override fun onStart() {
        super.onStart()
        okButton = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var repsInput: TextInputEditText
        lateinit var spacer1: View
        lateinit var rpeInput: TextInputEditText
        lateinit var spacer2: View
        lateinit var weightInput: TextInputEditText
        val vmFactory = InjectorUtils.provideEditSetViewModelFactory(context!!, exercise)
        val viewModel = ViewModelProviders.of(this, vmFactory)
            .get(EditSetViewModel::class.java)
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val addSetView = AnkoContext.createReusable(activity!!).apply {
                linearLayout(){
                    orientation = LinearLayout.HORIZONTAL
                    materialTextInputLayout {
                        weightInput = materialTextInputEditText {
                            hint = context.getString(R.string.add_set_weight)
                            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
                            textChangedListener {
                                onTextChanged { charsequence, p1, p2, p3 ->
                                    viewModel.weight = charsequence.toString()
                                }
                            }
                        }
                    }.lparams{
                        width = 0
                        weight = 1.0f
                    }
                    spacer1 = view{}.lparams{
                        width = dip(8)
                        height = dip(1)
                    }
                    materialTextInputLayout {
                        hint = context.getString(R.string.add_set_reps)
                        repsInput = materialTextInputEditText {
                            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
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
                    spacer2 = view{}.lparams{
                        width = dip(8)
                        height = dip(1)
                    }
                    materialTextInputLayout {
                        hint = context.getString(R.string.add_set_rpe)
                        rpeInput = materialTextInputEditText {
                            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
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
            builder.setTitle(R.string.add_set_title)
                .setView(addSetView)
                .setPositiveButton(R.string.add_set_positive, DialogInterface.OnClickListener { dialog, id ->
                    viewModel.saveSet()
                    getDialog()?.dismiss()
                })
                .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, id ->
                    getDialog()?.cancel()
                }).create()
        } ?: throw IllegalStateException("Invalid activity")
    }

}