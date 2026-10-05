package com.dinhlam.sharebox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dinhlam.sharebox.data.local.entity.BoxTransferState

@Dao
interface BoxTransferStateDao {
    @Query("SELECT * FROM box_transfer_state WHERE boxId = :boxId")
    suspend fun find(boxId: String): BoxTransferState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(state: BoxTransferState)
}
