package com.dinhlam.sharebox.model

sealed class Spacing(
    open val start: Int, open val top: Int, open val end: Int, open val bottom: Int
) {
    object None : Spacing(0, 0, 0, 0)
    data class All(
        override val start: Int = 0,
        override val top: Int = 0,
        override val end: Int = 0,
        override val bottom: Int = 0
    ) : Spacing(start, top, end, bottom)

    data class Vertical(override val top: Int = 0, override val bottom: Int = 0) :
        Spacing(0, top, 0, bottom)

    data class Horizontal(override val start: Int = 0, override val end: Int = 0) :
        Spacing(start, 0, end, 0)
}
