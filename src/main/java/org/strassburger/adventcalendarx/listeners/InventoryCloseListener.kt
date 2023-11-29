package org.strassburger.adventcalendarx.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.strassburger.adventcalendarx.AdventCalendar

class InventoryCloseListener : Listener {
    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val playerUUID = event.player.uniqueId

        if (AdventCalendar.calendarGuiMap.containsKey(playerUUID)) AdventCalendar.calendarGuiMap.remove(playerUUID)
    }
}