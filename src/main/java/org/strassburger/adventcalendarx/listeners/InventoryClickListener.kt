package org.strassburger.adventcalendarx.listeners

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.strassburger.adventcalendarx.AdventCalendar
import java.time.ZoneId
import java.time.ZonedDateTime

class InventoryClickListener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return

        if (event.inventory == AdventCalendar.calendarGuiMap[player.uniqueId]) {
            val item = event.currentItem

            if (event.currentItem == null) {
                event.isCancelled = true;
                return
            }

            when(event.currentItem!!.type) {
                // When clicking on the close button
                Material.valueOf(AdventCalendar.instance.config.getString("closeBtnMaterial") ?: "BARRIER") -> {
                    player.playSound(player.location, Sound.ENTITY_CHICKEN_EGG, 400.0f, 1.0f)
                    event.inventory.close()
                }

                // When clicking on a present
                Material.PLAYER_HEAD -> {
                    if (event.currentItem!!.itemMeta.customModelData - 2412 > 0) {
                        // Get present number
                        val i = event.currentItem!!.itemMeta.customModelData - 2412

                        val commands = AdventCalendar.instance.config.getList("days.$i.commands")

                        val desiredTimeZone = ZoneId.of(AdventCalendar.instance.config.getString("timeZone") ?: "America/New_York")
                        val currentDateTime = ZonedDateTime.now(desiredTimeZone)
                        val currentMonth = currentDateTime.month
                        val currentMonthVal = currentDateTime.monthValue
                        val currentDay = currentDateTime.dayOfMonth

                        val adventMonth = (AdventCalendar.instance.config.getString("calendarMonth") ?: "12").toIntOrNull()

                        if (currentMonthVal != adventMonth) {
                            val errMessage = (AdventCalendar.instance.config.getString("messages.wrongMonthMsg") ?: "<gray>You can only open the Advent Calendar in <red>%month%<gray>!").replace("%month%", currentMonth.toString())
                            player.sendMessage(AdventCalendar.formatMsg(errMessage))
                            event.isCancelled = true
                            return
                        }

                        if (currentDay < i) {
                            player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 400.0f, 1.0f)
                            val errMessage = (AdventCalendar.instance.config.getString("messages.cannotOpenYet") ?: "<red>You cannot open this gift yet!")
                            player.sendMessage(AdventCalendar.formatMsg(errMessage))
                            event.isCancelled = true
                            return
                        }

                        val allowOpenExpiredPresents = AdventCalendar.instance.config.getBoolean("allowExpiredClaims")

                        if (currentDay > i && !allowOpenExpiredPresents) {
                            player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, 400.0f, 1.0f)
                            val errMessage = (AdventCalendar.instance.config.getString("messages.cannotOpenExpired") ?: "<red>You cannot open this present anymore!")
                            player.sendMessage(AdventCalendar.formatMsg(errMessage))
                            event.isCancelled = true
                            return
                        }

                        // Execute commands for day
                        if (commands != null) {
                            for (command in commands) {
                                if (command !is String) continue
                                val console = Bukkit.getServer().consoleSender
                                Bukkit.dispatchCommand(console, command.replace("%player%", player.name))
                            }
                        }

                        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 400.0f, 1.0f)
                        val confirmMsg = (AdventCalendar.instance.config.getString("messages.openedPresent") ?: "<gray>You opened <red>Present %num%<gray>!").replace("%num%", i.toString())
                        player.sendMessage(AdventCalendar.formatMsg(confirmMsg))

                    }
                }

                else -> {}
            }

            event.isCancelled = true
        }
    }
}