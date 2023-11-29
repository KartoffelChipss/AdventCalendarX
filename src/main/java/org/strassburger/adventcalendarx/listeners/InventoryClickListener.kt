package org.strassburger.adventcalendarx.listeners

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.strassburger.adventcalendarx.AdventCalendar

class InventoryClickListener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return

        if (event.inventory == AdventCalendar.calendarGuiMap[player.uniqueId]) {
            val item = event.currentItem

            when(event.currentItem!!.type) {
                Material.valueOf(AdventCalendar.instance.config.getString("closeBtnMaterial") ?: "BARRIER") -> {
                    player.playSound(player.location, Sound.ENTITY_CHICKEN_EGG, 400.0f, 1.0f)
                    event.inventory.close()
                }

                else -> {}
            }

            event.isCancelled = true
        }
    }
}