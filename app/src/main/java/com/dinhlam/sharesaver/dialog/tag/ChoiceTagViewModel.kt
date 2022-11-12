package com.dinhlam.sharesaver.dialog.tag

import com.dinhlam.sharesaver.base.BaseViewModel

class ChoiceTagViewModel : BaseViewModel<ChoiceTagState>(ChoiceTagState()) {

    fun setTitleAndSelectedPosition(title: String?, position: Int) {
        setState { copy(title = title, selectedPosition = position) }
    }

    fun selectedPosition(position: Int) {
        setState { copy(selectedPosition = position) }
    }
}