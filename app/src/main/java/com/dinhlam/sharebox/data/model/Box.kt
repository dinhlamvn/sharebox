package com.dinhlam.sharebox.data.model

import androidx.annotation.StringRes
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.utils.BoxUtils

sealed class Box(val id: String, @StringRes val name: Int) {
    object All : Box(BoxUtils.createBoxId("all"), R.string.box_all)
    object ComedyBox : Box(BoxUtils.createBoxId("comedy"), R.string.box_comedy)
    object EntertainmentBox : Box(BoxUtils.createBoxId("entertainment"), R.string.box_entertainment)
    object FilmsBox : Box(BoxUtils.createBoxId("films"), R.string.box_film)
    object MusicBox : Box(BoxUtils.createBoxId("music"), R.string.box_music)
    object FoodBox : Box(BoxUtils.createBoxId("food"), R.string.box_food)
    object TravelBox : Box(BoxUtils.createBoxId("travel"), R.string.box_travel)
    object SportBox : Box(BoxUtils.createBoxId("sport"), R.string.box_sport)
    object PetsBox : Box(BoxUtils.createBoxId("pets"), R.string.box_pet)
    object AnimalBox : Box(BoxUtils.createBoxId("animals"), R.string.box_animal)
    object LifeStoryBox : Box(BoxUtils.createBoxId("life story"), R.string.box_life_story)
    object HealthBox : Box(BoxUtils.createBoxId("health"), R.string.box_health)
    object IdolBox : Box(BoxUtils.createBoxId("idol"), R.string.box_idol)
    object ReviewsBox : Box(BoxUtils.createBoxId("reviews"), R.string.box_review)
    object TipsBox : Box(BoxUtils.createBoxId("tip"), R.string.box_tip)
    object LearningBox : Box(BoxUtils.createBoxId("learning"), R.string.box_learning)
    object PrivateBox : Box(BoxUtils.createBoxId("private"), R.string.box_private)
}
