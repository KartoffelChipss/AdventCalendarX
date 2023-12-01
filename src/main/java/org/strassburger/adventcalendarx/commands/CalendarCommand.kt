package org.strassburger.adventcalendarx.commands

import com.destroystokyo.paper.profile.PlayerProfile
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.strassburger.adventcalendarx.AdventCalendar
import java.lang.reflect.Field
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.math.log

class CalendarCommand() : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) return false

        val optionOne = args?.getOrNull(0)

        if (optionOne == null || optionOne == "open") {
            val desiredTimeZone = ZoneId.of(AdventCalendar.instance.config.getString("timeZone") ?: "America/New_York")
            val currentDateTime = ZonedDateTime.now(desiredTimeZone)
            val currentMonth = currentDateTime.month
            val currentMonthVal = currentDateTime.monthValue
            val adventMonth = (AdventCalendar.instance.config.getString("calendarMonth") ?: "12").toIntOrNull()

            //sender.sendMessage("$currentMonthVal $adventMonth")
            if (currentMonthVal != adventMonth) {
                val errMessage = (AdventCalendar.instance.config.getString("messages.wrongMonthMsg") ?: "<gray>You can only open the Advent Calendar in <red>%month%<gray>!").replace("%month%", currentMonth.toString())
                sender.sendMessage(AdventCalendar.formatMsg(errMessage))
                return false
            }

            val inventory : Inventory = Bukkit.createInventory(null, 6 * 9, AdventCalendar.getAndFormatMsg(false, "inventoryname", "<dark_gray>Advent Calendar"))

            fillBackground(inventory)

            addCloseBtn(inventory)

            addPresents(inventory)

            sender.openInventory(inventory)
            AdventCalendar.calendarGuiMap[sender.uniqueId] = inventory

            return false
        }

        return false
    }

    private fun fillBackground(inventory: Inventory) {
        val fillslots : List<Int> = listOf(0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,50,51,52,53)

        for (slotNum in fillslots) {
            val fillItem = ItemStack(Material.valueOf(AdventCalendar.instance.config.getString("fillMaterial") ?: "GRAY_STAINED_GLASS_PANE"), 1)
            val fillItemMeta = fillItem.itemMeta
            fillItemMeta.displayName(AdventCalendar.formatMsg("<reset>"))
            fillItem.itemMeta = fillItemMeta

            inventory.setItem(slotNum, fillItem)
        }
    }

    private fun addCloseBtn(inventory: Inventory) {
        val btn = ItemStack(Material.valueOf(AdventCalendar.instance.config.getString("closeBtnMaterial") ?: "BARRIER"), 1)
        val btnMeta = btn.itemMeta
        btnMeta.displayName(AdventCalendar.getAndFormatMsg(false, "closeBtnName", "<red>Close"))
        btn.itemMeta = btnMeta

        inventory.setItem(49, btn)
    }

    private fun addPresents(inventory: Inventory) {
        val presentSlots : List<Int> = listOf(10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,39,40,41)

        var i = 1
        while (i <= 24) {
            val validSkulltextures : List<String> = listOf("red", "yellow", "purple", "green", "blue", "white", "numbered")
            var skulltextureName = AdventCalendar.instance.config.getString("presentType") ?: "red"
            if (!validSkulltextures.contains(skulltextureName)) skulltextureName = "red"

            var skullTexture : String = ""
            if (skulltextureName == "numbered") {

            } else {
               skullTexture = AdventCalendar.instance.config.getString("skulls.${skulltextureName}") ?: "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDUzNjhmNTYzNWZmNmMzNDA3ZjBmMzU2YzViNmUwOTQ3YmNkNWUzODQ5MGM5YWE4YjhiNTgyYTRmMjFhZTNjYiJ9fX0="
            }

            if (AdventCalendar.instance.config.getList("days.$i.skull") != null) {
                skullTexture = AdventCalendar.instance.config.getString("days.$i.skull")!!
                println("Found custom skull for present $i")
            }

            var skullName : Component
            val specificName = AdventCalendar.instance.config.getString("days.$i.name")

            skullName =
                if (specificName == null) AdventCalendar.formatMsg((AdventCalendar.instance.config.getString("presentName") ?: "<red>%num%. day").replace("%num%", i.toString()))
                else AdventCalendar.formatMsg(specificName)


            val skullLore : MutableList<Component> = mutableListOf()
            var specifiedLore = AdventCalendar.instance.config.getList("presentLore") ?: mutableListOf()
            if (AdventCalendar.instance.config.getList("days.$i.lore") != null) specifiedLore = AdventCalendar.instance.config.getList("days.$i.lore")!!
            for (loreLine in specifiedLore) {
                if (loreLine is String) skullLore.add(AdventCalendar.formatMsg(loreLine))
            }

            val head = getCustomSkull(skullTexture, skullName, skullLore)
            val headMeta = head.itemMeta
            headMeta.setCustomModelData(2412 + i)
            head.itemMeta = headMeta

            inventory.setItem(presentSlots[i - 1], head)
            i++
        }
    }

    private fun getCustomSkull(url: String, name : Component ?= null, lore : MutableList<Component> = mutableListOf()) : ItemStack {
        val skull = ItemStack(Material.PLAYER_HEAD, 1)
        val skullMeta: SkullMeta = skull.itemMeta as SkullMeta

        if (name != null) skullMeta.displayName(name)
        skullMeta.lore(lore)

        val profile: GameProfile = GameProfile(UUID.randomUUID(), "")
        profile.properties.put("textures", Property("textures", url))

        try {
            val profileField = skullMeta.javaClass.getDeclaredField("profile")
            profileField.trySetAccessible()
            profileField.set(skullMeta, profile)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        skull.itemMeta = skullMeta

        return skull
    }
}