package com.dinhlam.sharebox.ui.boxlist

import com.dinhlam.sharebox.base.BaseViewModel.AsyncLoad
import com.dinhlam.sharebox.base.UpdateStateEvent
import com.dinhlam.sharebox.base.UpdateStateHandler
import com.dinhlam.sharebox.base.handlerOf
import com.dinhlam.sharebox.model.BoxDetail
import kotlin.collections.orEmpty
import kotlin.reflect.KClass

data class SetBoxListEvent(
    val asyncLoad: AsyncLoad<List<BoxDetail>>,
    val append: Boolean = false
) : UpdateStateEvent

data object ResetBoxListEvent : UpdateStateEvent

data class SetTotalBoxEvent(val totalBox: Int) : UpdateStateEvent

data class SetSearchBoxesEvent(
    val boxes: List<BoxDetail>,
    val isSearching: Boolean
) : UpdateStateEvent

class BoxListUpdateStateHandler : UpdateStateHandler<BoxListState, UpdateStateEvent>() {

    override val eventHandlers: Map<KClass<UpdateStateEvent>, (BoxListState, UpdateStateEvent) -> BoxListState>
        get() = mapOf(
            handlerOf<BoxListState, SetBoxListEvent> { state, event ->
                state.copy(
                    asyncLoadBoxes = event.asyncLoad,
                    boxes = if (event.append) {
                        state.boxes + event.asyncLoad.data.orEmpty()
                    } else {
                        event.asyncLoad.data.orEmpty()
                    },
                    currentPage = if (event.asyncLoad is AsyncLoad.Success) state.currentPage + 1 else state.currentPage
                )
            },
            handlerOf<BoxListState, ResetBoxListEvent> { _, _ -> BoxListState() },
            handlerOf<BoxListState, SetTotalBoxEvent> { state, event ->
                state.copy(totalBox = event.totalBox)
            },
            handlerOf<BoxListState, SetSearchBoxesEvent> { state, event ->
                state.copy(searchBoxes = event.boxes, isSearching = event.isSearching)
            }
        )
}
