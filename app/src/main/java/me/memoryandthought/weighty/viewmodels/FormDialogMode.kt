package me.memoryandthought.weighty.viewmodels

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class FormDialogMode<out T: Parcelable> : Parcelable
@Parcelize
data class Edit<T: Parcelable>(val data: T) : FormDialogMode<T>(), Parcelable
@Parcelize
data class CreateFromTemplate<T: Parcelable>(val template: T): FormDialogMode<T>(), Parcelable
@Parcelize
object Create: FormDialogMode<Nothing>(), Parcelable
