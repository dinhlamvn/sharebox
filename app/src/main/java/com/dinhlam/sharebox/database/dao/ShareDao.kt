package com.dinhlam.sharebox.database.dao

import androidx.room.*
import com.dinhlam.sharebox.database.entity.Share

@Dao
interface ShareDao {
    @Query("SELECT * FROM share")
    fun getAll(): List<Share>

    @Query(
        """
        SELECT s.* 
        FROM share s
        ORDER BY id DESC 
        LIMIT 10
    """
    )
    fun getRecentList(): List<Share>

    @Query("SELECT * FROM share ORDER BY id DESC")
    fun getByFolder(): List<Share>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg shares: Share)

    @Update
    fun update(share: Share): Int

    @Query("SELECT * FROM share WHERE id = :id")
    fun getById(id: Int): Share?

    @Query("SELECT COUNT(*) FROM share")
    fun countByFolder(): Int

    @Query("DELETE FROM share")
    fun deleteByFolder()

    @Query("SELECT * FROM share WHERE share_data LIKE '%' || :query || '%' OR share_note LIKE '%' || :query || '%'")
    fun search(query: String): List<Share>

    @Delete
    fun delete(share: Share): Int

    @Query("SELECT COUNT(*) FROM share")
    fun count(): Int

    @Query("SELECT * FROM share ORDER BY id DESC")
    fun find(): List<Share>

    @Query("SELECT * FROM share WHERE share_mode = :shareMode ORDER BY id DESC")
    fun find(shareMode: String): List<Share>
}
