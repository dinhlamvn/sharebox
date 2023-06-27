package com.dinhlam.sharebox.data.model

import com.google.gson.annotations.SerializedName

@JvmInline
value class HttpErrorMessage(@SerializedName("message") val errorMessage: String)