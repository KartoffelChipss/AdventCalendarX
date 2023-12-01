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
import org.strassburger.adventcalendarx.util.data.ManagePlayerData
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
            val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

            //sender.sendMessage("$currentMonthVal $adventMonth")
            if (currentMonthVal != adventMonth) {
                val errMessage = (AdventCalendar.instance.config.getString("messages.wrongMonthMsg") ?: "<gray>You can only open the Advent Calendar in <red>%month%<gray>!").replace("%month%", months[(adventMonth ?: 12) - 1])
                sender.sendMessage(AdventCalendar.formatMsg(errMessage))
                return false
            }

            val inventory : Inventory = Bukkit.createInventory(null, 6 * 9, AdventCalendar.getAndFormatMsg(false, "inventoryname", "<dark_gray>Advent Calendar"))

            fillBackground(inventory)

            addCloseBtn(inventory)

            addPresents(inventory, sender)

            sender.openInventory(inventory)
            AdventCalendar.calendarGuiMap[sender.uniqueId] = inventory

            return false
        }

        if (optionOne == "testmode") {
            if (!sender.hasPermission("adventcalendarx.testmode")) {
                sender.sendMessage(AdventCalendar.getAndFormatMsg(false, "messages.noPermissionError", "<red>You don't have permission to use this!"))
                return false
            }

            val optionTwo = args.getOrNull(1)

            var targetPlayer : Player = sender
            if (optionTwo != null && Bukkit.getPlayer(optionTwo) != null) targetPlayer = Bukkit.getPlayer(optionTwo)!!

            if (AdventCalendar.testModePlayers.contains(targetPlayer.uniqueId)) {
                AdventCalendar.testModePlayers.remove(targetPlayer.uniqueId)
                targetPlayer.sendMessage(AdventCalendar.getAndFormatMsg(true, "messages.leftTestmode", "<gray>You are no longer in <red>testmode<gray>!"))
            } else {
                AdventCalendar.testModePlayers.add(targetPlayer.uniqueId)
                targetPlayer.sendMessage(AdventCalendar.getAndFormatMsg(true, "messages.enteredTestmode", "<gray>You are now in <red>testmode<gray>. You can now claim any present to test if the rewards work."))
            }
        }

        if (optionOne == "reload") {
            if (!sender.hasPermission("adventcalendarx.reload")) {
                sender.sendMessage(AdventCalendar.getAndFormatMsg(false, "messages.noPermissionError", "<red>You don't have permission to use this!"))
                return false
            }

            AdventCalendar.instance.reloadConfig()

            Bukkit.resetRecipes()
            sender.sendMessage(AdventCalendar.getAndFormatMsg(true, "messages.reloadMsg", "<gray>Successfully reloaded the plugin!"))
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

    private fun addPresents(inventory: Inventory, player: Player) {
        val presentSlots : List<Int> = listOf(10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,39,40,41)

        val claimedGifts = ManagePlayerData().getOpenedGifts(player.uniqueId)

        val desiredTimeZone = ZoneId.of(AdventCalendar.instance.config.getString("timeZone") ?: "America/New_York")
        val currentDateTime = ZonedDateTime.now(desiredTimeZone)
        val currentDay = currentDateTime.dayOfMonth

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

            val allowOpenExpiredPresents = AdventCalendar.instance.config.getBoolean("allowExpiredClaims")
            if (currentDay > i && !allowOpenExpiredPresents) {
                skullTexture = AdventCalendar.instance.config.getString("skulls.disabled") ?: "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWYyYjNmN2NkYjU5Mzk0YTBjODJmZTY3ZTY3Mjg0MmQ3NTVjZThlOGVkZGFlZWI5YjVjYzE3M2I1NTAxZThkNiJ9fX0="
            }

            if (claimedGifts.contains(i)) skullTexture = AdventCalendar.instance.config.getString("skulls.opened") ?: "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTQzMTAwYWJmMDFhMWZlMDVlYWY3ZTA5MDRhMGFjZDliYzMzOGIwZWU2N2I4OTBhYTY3MDUzZWU2NzJlNjY3OSJ9fX0="

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

            if (currentDay > i && !allowOpenExpiredPresents) skullLore.add(AdventCalendar.getAndFormatMsg(false, "messages.cannotOpenExpired", "<red>You cannot open this present anymore!"))

            if (claimedGifts.contains(i)) skullLore.add(AdventCalendar.getAndFormatMsg(false, "messages.alreadyOpened", "<red>You have already claimed this present!"))

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