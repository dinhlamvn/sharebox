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
            Box.All,
            Box.ComedyBox,
            Box.EntertainmentBox,
            Box.FilmsBox,
            Box.MusicBox,
            Box.FoodBox,
            Box.TravelBox,
            Box.SportBox,
            Box.PetsBox,
            Box.AnimalBox,
            Box.LifeStoryBox,
            Box.HealthBox,
            Box.IdolBox,
            Box.ReviewsBox,
            Box.TipsBox,
            Box.LearningBox,
        )
    }

    fun findBox(id: String) = getBoxes().plus(Box.PrivateBox).firstOrNull { box -> box.id == id }

    fun findBoxOrDefault(id: String, default: Box) =
        getBoxes().firstOrNull { box -> box.id == id } ?: default
}