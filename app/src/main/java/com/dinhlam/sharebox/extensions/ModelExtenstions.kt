package com.dinhlam.sharebox.extensions

import com.dinhlam.sharebox.database.entity.Folder

fun Folder.isHasPassword() = password.isNullOrBlank().not()