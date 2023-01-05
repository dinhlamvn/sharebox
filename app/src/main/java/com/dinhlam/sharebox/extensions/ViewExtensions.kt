package com.dinhlam.sharebox.extensions

import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.dinhlam.sharebox.base.BaseListAdapter

fun EditText.getTrimmedText() = text.toString().trim()

fun RecyclerView.setupWith(
    adapter: BaseListAdapter<BaseListAdapter.BaseModelView>,
    modelViewsFactory: BaseListAdapter.ModelViewsFactory
) {
    this.adapter = adapter
    modelViewsFactory.attach(adapter)
}
