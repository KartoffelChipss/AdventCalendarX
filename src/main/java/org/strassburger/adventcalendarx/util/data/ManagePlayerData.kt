package org.strassburger.adventcalendarx.util.data

import com.google.gson.GsonBuilder
import org.bukkit.entity.Player
import java.io.File
import java.io.IOException
import java.util.UUID

class ManagePlayerData {
    fun getPlayerData(uuid: UUID) : PlayerData {
        val playerdata = PlayerData(uuid = uuid.toString())

        val file: File
        val gson = GsonBuilder().setPrettyPrinting().create()
        val json = gson.toJson(playerdata)

        val dir = File("./plugins/AdventCalendarX/userData")
        if (!dir.exists()) {
            dir.mkdirs()
        }

        file = File(dir, "${uuid}.json")
        if (!file.exists()) {
            try {
                file.createNewFile()
                file.writeText(json)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        val newJson = file.readText()
        return gson.fromJson(newJson, PlayerData::class.java)
    }

    fun checkForPlayer(uuid: String) : Boolean {
        val dir = File("./plugins/AdventCalendarX/userData")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file: File = File(dir, "${uuid}.json")

        return file.exists()
    }

    private fun savePlayerData(playerData: PlayerData) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val json = gson.toJson(playerData)
        File("./plugins/AdventCalendarX/userData/${playerData.uuid}.json").writeText(json)
    }

    fun getOpenedGifts(uuid: UUID) : MutableList<Int> {
        return getPlayerData(uuid = uuid).claimedGifts
    }

    fun addOpenedPresent(uuid: UUID, giftNum: Int) : PlayerData {
        val playerdata = getPlayerData(uuid = uuid)

        playerdata.claimedGifts.add(giftNum)

        savePlayerData(playerData = playerdata)
        return playerdata
    }
}