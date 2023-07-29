package com.dinhlam.sharebox.model

import com.google.gson.annotations.SerializedName

@JvmInline
value class HttpErrorMessage(@SerializedName("message") val errorMessage: String)