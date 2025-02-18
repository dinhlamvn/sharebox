package com.dinhlam.sharebox.extensions

import androidx.core.database.getIntOrNull
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dinhlam.sharebox.logger.Logger

fun SupportSQLiteDatabase.insertDefaultTags() {
    val queries = arrayOf(
        "INSERT INTO tag('tagColor','created_at') VALUES(0xFF0000,?)",
        "INSERT INTO tag('tagColor','created_at') VALUES(0x00FF00,?)",
        "INSERT INTO tag('tagColor','created_at') VALUES(0x0000FF,?)",
        "INSERT INTO tag('tagColor','created_at') VALUES(0xFFA500,?)",
        "INSERT INTO tag('tagColor','created_at') VALUES(0xFFFF00,?)",
        "INSERT INTO tag('tagColor','created_at') VALUES(0xA020F0,?)",
        "INSERT INTO tag('tagColor','created_at') VALUES(0x808080,?)",
    )
    queries.forEach { query ->
        this.execSQL(query, arrayOf(nowUTCTimeInMillis()))
    }

    val selectQuery = "SELECT * FROM `tag`"
    val cursor = this.query(selectQuery)
    while (cursor.moveToNext()) {
        val data = cursor.getIntOrNull(cursor.getColumnIndex("tagColor"))
        Logger.debug("Tag data: $data")
    }
}