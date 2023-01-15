package com.dinhlam.sharebox.dialog.tag

import com.dinhlam.sharebox.base.BaseViewModel

class ChoiceTagViewModel : BaseViewModel<ChoiceTagState>(ChoiceTagState()) {

    fun setTitleAndSelectedTag(title: String?, selectedTag: Int) {
        setState { copy(title = title, selectedTagId = selectedTag) }
    }

    fun selectedTag(tagId: Int) = getState { state ->
        if (state.selectedTagId == -1 || state.selectedTagId != tagId) {
            setState { copy(selectedTagId = tagId) }
        } else {
            setState { copy(selectedTagId = -1) }
        }
    }
}
