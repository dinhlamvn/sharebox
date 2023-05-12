package com.dinhlam.sharebox.database.option

import androidx.annotation.IntRange

sealed class SortOption(val sortField: String, @IntRange(from = -1, to = 1) val sort: Int = 1)
