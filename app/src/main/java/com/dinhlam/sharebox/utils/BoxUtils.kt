package com.dinhlam.sharebox.utils

import com.dinhlam.sharebox.data.model.Box
import java.util.UUID

object BoxUtils {

    fun createBoxId(name: String): String {
        val lowerCase = name.lowercase().replace(" ", "-")
        return UUID.nameUUIDFromBytes(lowerCase.toByteArray()).toString()
    }

    fun getBoxes(): List<Box> {
        return listOf(
            Box.ComedyBox,
            Box.MusicBox,
            Box.FoodBox,
            Box.TravelBox,
            Box.SportBox,
            Box.AnimalBox,
            Box.LifeStoryBox,
            Box.HealthBox,
            Box.IdolBox,
            Box.TipBox,
            Box.All,
        )
    }

    fun findBox(id: String) = getBoxes().plus(Box.PersonalBox).firstOrNull { box -> box.id == id }

    fun findBoxOrDefault(id: String, default: Box) =
        getBoxes().firstOrNull { box -> box.id == id } ?: default
}