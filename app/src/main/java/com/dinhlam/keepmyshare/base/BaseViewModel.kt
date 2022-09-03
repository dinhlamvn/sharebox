package com.dinhlam.keepmyshare.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinhlam.keepmyshare.extensions.asThe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty

abstract class BaseViewModel<T : BaseViewModel.BaseData>(initData: T) : ViewModel() {

    interface BaseData

    data class Consumer(
        val consumeField: String,
        val block: (Any?, Boolean) -> Unit
    )

    private val consumers = mutableSetOf<Consumer>()

    private val _data = OneTimeLiveData<T>()
    val data: LiveData<T> = _data

    init {
        _data.postValue(initData)

    }

    protected fun setData(block: T.() -> T) {
        val before = data.value
        val newBaseData = data.value?.let(block) ?: return
        _data.postValue(newBaseData)
        if (before == null) {
            return
        }

        consumers.forEach { consumer ->
            val beforeField = before::class.java.getDeclaredField(consumer.consumeField)
            beforeField.isAccessible = true
            val beforeValue = beforeField.get(before)
            val afterField = newBaseData::class.java.getDeclaredField(consumer.consumeField)
            afterField.isAccessible = true
            val afterValue = afterField.get(newBaseData)
            consumer.block.invoke(afterValue, beforeValue != afterValue)
        }
    }

    protected fun withData(block: (T) -> Unit) {
        data.value?.let(block)
    }

    protected fun executeWithData(block: suspend (T) -> Unit) =
        viewModelScope.launch(Dispatchers.IO) {
            data.value?.let { nonNullData ->
                block.invoke(nonNullData)
            }
        }

    fun <T> listen(property: KProperty<T>, block: (T, Boolean) -> Unit) {
        consumers.add(Consumer(property.name, block.asThe()!!))
    }

    fun onClearConsumers() {
        consumers.clear()
    }
}