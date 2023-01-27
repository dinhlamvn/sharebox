package com.dinhlam.sharebox.repository

import com.dinhlam.sharebox.model.SortType

interface Repository<T, in R> {
    fun insert(item: T)
    fun update(item: T): Boolean
    fun delete(item: T): Boolean
    fun insertMany(vararg items: T)
    fun updateMany(vararg items: T)
    fun updateById(id: R, block: (T) -> T)
    fun get(id: R): T?
    fun getAll(sortType: SortType = SortType.NONE): List<T>

    fun search(query: String): List<T>
}
