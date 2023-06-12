package com.dinhlam.sharebox.dialog.box

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.repository.BoxRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class BoxSelectionDialogViewModel @Inject constructor(private val boxRepository: BoxRepository) :
    BaseViewModel<BoxSelectionDialogState>(BoxSelectionDialogState()) {

    init {
        getListBoxes()
    }

    private fun getListBoxes() {
        setState { copy(isLoading = true) }
        backgroundTask {
            val boxes = boxRepository.findLatestBox()
            delay(1000)
            setState { copy(boxes = boxes, isLoading = false) }
        }
    }
}