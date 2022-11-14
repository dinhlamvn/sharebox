package com.dinhlam.sharesaver.dialog.tag

import com.dinhlam.sharesaver.base.BaseViewModel

class ChoiceTagViewModel : BaseViewModel<ChoiceTagState>(ChoiceTagState()) {

    fun setTitleAndSelectedTag(title: String?, selectedTag: Int) {
        setState { copy(title = title, selectedTagId = selectedTag) }
    }

    fun selectedTag(tagId: Int) {
        setState { copy(selectedTagId = tagId) }
    }
}