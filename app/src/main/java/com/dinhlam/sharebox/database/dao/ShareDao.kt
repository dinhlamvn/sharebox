package com.dinhlam.sharebox.database.dao

import androidx.room.*
import com.dinhlam.sharebox.database.entity.Share
import com.dinhlam.sharebox.model.FolderShareCount

@Dao
interface ShareDao {
    @Query("SELECT * FROM share")
    fun getAll(): List<Share>

    @Query(
        """
        SELECT s.* 
        FROM share s 
        JOIN folder f on f.id = s.folder_id
        WHERE f.password IS NULL OR f.password == ''
        ORDER BY id DESC 
        LIMIT 10
    """
    )
    fun getRecentList(): List<Share>

    @Query("SELECT * FROM share WHERE folder_id = :folderId ORDER BY id DESC")
    fun getByFolder(folderId: String): List<Share>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg shares: Share)

    @Update
    fun update(share: Share): Int

    @Query("SELECT * FROM share WHERE id = :id")
    fun getById(id: Int): Share?

    @Query("SELECT COUNT(*) FROM share WHERE folder_id = :folderId")
    fun countByFolder(folderId: String): Int

    @Query("DELETE FROM share WHERE folder_id = :folderId")
    fun deleteByFolder(folderId: String)

    @Query("SELECT * FROM share WHERE share_info LIKE '%' || :query || '%' OR share_note LIKE '%' || :query || '%'")
    fun search(query: String): List<Share>

    @Query("SELECT folder_id as id, COUNT(*) as shareCount FROM share GROUP BY folder_id")
    fun countShareByFolder(): List<FolderShareCount>
}
