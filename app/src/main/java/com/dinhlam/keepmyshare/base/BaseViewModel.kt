package com.dinhlam.keepmyshare.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseViewModel<T : BaseViewModel.BaseData>(initData: T) : ViewModel() {

    interface BaseData

    private val _data = MutableLiveData<T>().apply {
        value = initData
    }
    val data: LiveData<T> = _data

    protected fun setData(block: T.() -> T) {
        val newBaseData = data.value?.let(block) ?: return
        _data.postValue(newBaseData)
    }

    protected fun withData(block: (T) -> Unit) {
        data.value?.let(block)
    }

    protected fun executeWithData(block: suspend (T) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        data.value?.let { nonNullData ->
            block.invoke(nonNullData)
        }
    }
}