package me.memoryandthought.weighty.fragments

import android.view.ViewManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.jetbrains.anko.custom.ankoView

fun ViewManager.materialFloatingActionButton(theme: Int = 0, init: FloatingActionButton.() -> Unit = {}) = ankoView({
    FloatingActionButton(it)
}, theme, init)

fun ViewManager.materialTextInputLayout(theme: Int = 0, init: TextInputLayout.() -> Unit = {}) = ankoView({
    TextInputLayout(it)
}, theme, init)

fun ViewManager.materialTextInputEditText(theme: Int = 0, init: TextInputEditText.() -> Unit = {}) = ankoView({
    TextInputEditText(it)
}, theme, init)