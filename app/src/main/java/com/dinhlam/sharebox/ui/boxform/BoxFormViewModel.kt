package com.dinhlam.sharebox.ui.boxform

import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.extensions.ifTrue
import com.dinhlam.sharebox.extensions.md5
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.UserHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BoxFormViewModel @Inject constructor(
    private val boxRepository: BoxRepository,
    private val userHelper: UserHelper,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<BoxFormState>(BoxFormState()) {

    init {
        loadBoxDetail(savedStateHandle[AppExtras.EXTRA_BOX_ID])
    }

    private fun loadBoxDetail(boxId: String?) {
        suspend {
            boxRepository.findOne(boxId!!)
        }.execute { asyncLoad -> copy(currentBoxDetail = asyncLoad.data) }
    }

    fun saveBox(name: String, desc: String, passcode: String) = getState { state ->
        suspend {
            if (state.currentBoxDetail != null) {
                val currentBox = boxRepository.findOneRaw(state.currentBoxDetail.boxId)!!
                val newBox = if (state.isUsePasscode) {
                    currentBox.copy(
                        boxName = name,
                        boxDesc = desc,
                        passcode = passcode.takeIfNotNullOrBlank()?.md5()
                    )
                } else {
                    currentBox.copy(
                        boxName = name,
                        boxDesc = desc
                    )
                }
                boxRepository.update(newBox)!!
            } else {
                boxRepository.insert(
                    name,
                    desc,
                    userHelper.getCurrentUserId(),
                    state.isUsePasscode.ifTrue(passcode.takeIfNotNullOrBlank()?.md5(), null)
                )!!
            }
        }.execute { asyncLoad -> copy(asyncLoadSave = asyncLoad) }
    }

    fun toggleUsePasscode(checked: Boolean) = setState {
        copy(isUsePasscode = checked)
    }

    fun togglePasscodeVisibility() = setState {
        copy(isPasscodeVisible = !isPasscodeVisible)
    }
}