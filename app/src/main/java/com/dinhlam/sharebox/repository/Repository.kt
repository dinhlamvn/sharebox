package com.dinhlam.sharebox.repository

import com.dinhlam.sharebox.model.SortType

interface Repository<T, R> {
    fun insert(data: T)
    fun update(data: T): Boolean
    fun delete(data: T): Boolean
    fun insertMany(vararg items: T)
    fun updateMany(vararg items: T)
    fun updateById(id: R, block: (T) -> T)
    fun find(id: R): T?
    fun findAll(sortType: SortType = SortType.NONE): List<T>

    fun search(query: String): List<T>
}
