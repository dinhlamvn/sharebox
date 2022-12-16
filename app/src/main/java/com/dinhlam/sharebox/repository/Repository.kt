package com.dinhlam.sharebox.repository

interface Repository<T, in R> {
    fun insert(item: T)
    fun update(item: T): Boolean
    fun delete(item: T): Boolean
    fun insertMany(vararg items: T)
    fun updateMany(vararg items: T)
    fun updateById(id: R, block: (T) -> T)
    fun get(id: R): T
    fun getAll(): List<T>
}
