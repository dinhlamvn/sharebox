package com.dinhlam.sharebox.data.model

import androidx.annotation.StringRes
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.utils.BoxUtils

sealed class Box(val id: String, @StringRes val name: Int) {
    object CommunityBox : Box(BoxUtils.createBoxId("community"), R.string.box_community)
    object ComedyBox : Box(BoxUtils.createBoxId("comedy"), R.string.box_comedy)
    object MusicBox : Box(BoxUtils.createBoxId("music"), R.string.box_music)
    object FoodBox : Box(BoxUtils.createBoxId("food"), R.string.box_food)
    object TravelBox : Box(BoxUtils.createBoxId("travel"), R.string.box_travel)
    object SportBox : Box(BoxUtils.createBoxId("sport"), R.string.box_sport)
    object AnimalBox : Box(BoxUtils.createBoxId("animals"), R.string.box_animal)
    object LifeStoryBox : Box(BoxUtils.createBoxId("life story"), R.string.box_life_story)
    object HealthBox : Box(BoxUtils.createBoxId("health"), R.string.box_health)
    object IdolBox : Box(BoxUtils.createBoxId("idol"), R.string.box_idol)
    object TipBox : Box(BoxUtils.createBoxId("tip"), R.string.box_tip)
    object PersonalBox : Box(BoxUtils.createBoxId("personal"), R.string.box_personal)
}
