package com.dinhlam.sharebox.listmodel

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.view.CardViewIconView

data class CardIconViewListModel(
    val id: String,
    val icon: Drawable
) : BaseListAdapter.BaseListModel(id) {
    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*> {
        return object :
            BaseListAdapter.BaseViewHolderCustomView<CardIconViewListModel, CardViewIconView>(
                CardViewIconView(container.context),
                RecyclerView.LayoutParams(
                    100.dp(),
                    100.dp()
                )
            ) {
            override fun onBind(model: CardIconViewListModel, position: Int) {
                view.setIcon(model.icon)
            }

            override fun onUnBind() {

            }
        }
    }
}
