package com.dinhlam.sharebox.extensions

import com.dinhlam.sharebox.data.local.entity.Folder

fun Folder.isHasPassword() = password.isNullOrBlank().not()