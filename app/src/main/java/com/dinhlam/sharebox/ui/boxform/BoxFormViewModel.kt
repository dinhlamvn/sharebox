package com.dinhlam.sharebox.ui.boxform

import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.extensions.md5
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.utils.BoxUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BoxFormViewModel @Inject constructor(
    private val boxRepository: BoxRepository,
    private val userHelper: UserHelper,
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<BoxFormState>(BoxFormState(savedStateHandle[AppExtras.EXTRA_BOX_ID])) {

    init {
        loadBoxDetail()
    }

    private fun loadBoxDetail() = getState { state ->
        state.boxId?.let { boxId ->
            suspend {
                boxRepository.findOne(boxId)
            }.execute { asyncLoad -> copy(boxDetail = asyncLoad.data) }
        }
    }


    fun saveBox(name: String, desc: String, passcode: String) = getState { state ->
        suspend {
            val createdBox =
                state.boxDetail?.boxId?.let { boxId -> boxRepository.findOneRaw(boxId) }
            val box = createdBox?.let { box ->
                val newBox = if (state.isChangePasscode) {
                    box.copy(
                        boxName = name,
                        boxDesc = desc,
                        passcode = passcode.takeIfNotNullOrBlank()?.md5()
                    )
                } else {
                    box.copy(
                        boxName = name,
                        boxDesc = desc
                    )
                }
                boxRepository.update(newBox)
                newBox
            } ?: boxRepository.insert(
                BoxUtils.createBoxId("${userHelper.getCurrentUserId()}-$name"),
                name,
                desc,
                userHelper.getCurrentUserId(),
                nowUTCTimeInMillis(),
                passcode.takeIfNotNullOrBlank()?.md5()
            )!!
            realtimeDatabaseRepository.push(box)
            box
        }.execute { asyncLoad -> copy(asyncLoadSave = asyncLoad) }
    }

    fun setChangePasscodeChecked(checked: Boolean) = setState {
        copy(isChangePasscode = checked)
    }
}