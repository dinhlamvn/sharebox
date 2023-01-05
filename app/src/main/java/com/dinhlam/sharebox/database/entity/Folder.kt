package com.dinhlam.sharebox.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Folder(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name", index = true) val name: String,
    @ColumnInfo(name = "desc") val desc: String? = null,
    @ColumnInfo(name = "password") val password: String? = null,
    @ColumnInfo(name = "password_alias") val passwordAlias: String? = null,
    @ColumnInfo(name = "tag") val tag: Int? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)
