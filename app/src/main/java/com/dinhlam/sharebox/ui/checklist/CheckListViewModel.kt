package com.dinhlam.sharebox.ui.checklist

import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.model.ShareData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CheckListViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val boxRepository: BoxRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<CheckListState>(CheckListState(savedStateHandle[AppExtras.EXTRA_SHARE_ID])) {

    init {
        onChange(CheckListState::shareId) { shareId ->
            getShareDetail(shareId)
        }
        getBoxToArchiveContent()
    }

    private fun getShareDetail(shareId: String?) {
        val id = shareId ?: return
        suspend {
            shareRepository.findOne(id)
        }.execute { asyncLoad ->
            val shareDetail = asyncLoad.data
            copy(
                shareDetail = shareDetail,
                currentBox = shareDetail?.boxDetail,
                checkListDataList = shareDetail?.shareData?.cast<ShareData.ShareCheckList>()?.checkListDataList.orEmpty()
            )
        }
    }

    private fun getBoxToArchiveContent() {
        suspend {
            boxRepository.findLastActiveBox()
        }.execute { asyncLoad ->
            copy(currentBox = asyncLoad.data)
        }
    }


    fun saveCheckListData(
        oldCheckList: ShareData.ShareCheckList.CheckListData?,
        checkListData: ShareData.ShareCheckList.CheckListData
    ) = getState { state ->
        val checkListList = if (state.checkListDataList.contains(oldCheckList)) {
            state.checkListDataList.map { item ->
                if (item == oldCheckList) {
                    checkListData
                } else {
                    item
                }
            }
        } else {
            state.checkListDataList.plus(checkListData)
        }

        setState { copy(checkListDataList = checkListList) }
    }

    fun saveCheckList(shareNote: String?) = getState { state ->
        suspend {
            val shareData = ShareData.ShareCheckList(state.checkListDataList)
            if (state.shareDetail != null) {
                val share = shareRepository.findOneRaw(state.shareDetail.shareId)!!
                shareRepository.update(
                    share.copy(
                        shareData = shareData,
                        shareNote = shareNote,
                        shareBoxId = state.currentBox!!.boxId
                    )
                )!!
            } else {
                shareRepository.insert(shareData, shareNote, state.currentBox!!.boxId)!!
            }
        }.execute { asyncLoad ->
            copy(asyncArchive = asyncLoad)
        }
    }

    fun setCurrentBoxId(boxId: String) {
        suspend { boxRepository.findOne(boxId) }.execute { asyncLoad ->
            copy(currentBox = asyncLoad.data)
        }
    }

    fun markTaskDone(checkListData: ShareData.ShareCheckList.CheckListData) = setState {
        copy(checkListDataList = checkListDataList.map { data ->
            if (data == checkListData) {
                data.copy(done = true)
            } else {
                data
            }
        })
    }

    fun setCheckListDataReminder(
        checkListData: ShareData.ShareCheckList.CheckListData,
        timeInMillis: Long
    ) = setState {
        copy(checkListDataList = checkListDataList.map { data ->
            if (data == checkListData) {
                data.copy(reminder = timeInMillis)
            } else {
                data
            }
        })
    }
}