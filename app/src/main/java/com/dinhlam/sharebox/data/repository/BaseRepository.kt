package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.logger.Logger

abstract class BaseRepository<E> {
    protected abstract suspend fun insertInternal(entity: E): E
    protected abstract suspend fun updateInternal(entity: E, willBeSync: Boolean): E
    abstract suspend fun count(): Int
    abstract suspend fun delete(entity: E): Boolean

    suspend fun insert(entity: E): E? {
        try {
            val record = insertInternal(entity)
            return record
        } catch (e: Exception) {
            Logger.error("Insert $entity to database failed.")
        }
        return null
    }

    suspend fun update(entity: E, willBeSync: Boolean = true): E? {
        try {
            val record = updateInternal(entity, willBeSync)
            return record
        } catch (e: Exception) {
            Logger.error("Update $entity to database failed.")
        }
        return null
    }
}