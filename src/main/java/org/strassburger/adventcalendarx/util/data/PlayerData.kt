package org.strassburger.adventcalendarx.util.data

data class PlayerData (
    var uuid: String,
    var claimedGifts: MutableList<Int> = mutableListOf(),
)