package org.strassburger.adventcalendarx

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.NamespacedKey
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.java.JavaPlugin
import org.strassburger.adventcalendarx.commands.CalendarCommand
import org.strassburger.adventcalendarx.listeners.InventoryClickListener
import org.strassburger.adventcalendarx.listeners.InventoryCloseListener
import org.strassburger.lifestealz.util.MyTabCompleter
import java.util.*

class AdventCalendar : JavaPlugin() {

    companion object {
        lateinit var instance: AdventCalendar

        // Maps to check if a player has opened a custom gui
        val calendarGuiMap: MutableMap<UUID, Inventory> = mutableMapOf()

        val testModePlayers: MutableList<UUID> = mutableListOf()

        fun formatMsg(msg: String): Component {
            val mm = MiniMessage.miniMessage()
            return mm.deserialize("<!i>$msg")
        }

        fun getAndFormatMsg(addPrefix: Boolean, path: String, fallback: String): Component {
            val mm = MiniMessage.miniMessage()
            var msg = "<!i>${instance.config.getString(path) ?: fallback}"
            val prefix = "<dark_gray>[<red>AdventCalendarX<dark_gray>]"
            if (addPrefix) msg = "$prefix $msg"
            return mm.deserialize(msg)
        }
    }

    init {
        instance = this
    }

    override fun onEnable() {
        config.options().copyDefaults(true)
        saveDefaultConfig()

        registerCommands()
        registerEvents()

        logger.info("AdventClalendarX has been loaded!")
    }

    override fun onDisable() {
        logger.info("AdventClalendarX has been disabled!")
    }

    private fun registerEvents() {
        server.pluginManager.registerEvents(InventoryClickListener(), this)
        server.pluginManager.registerEvents(InventoryCloseListener(), this)

        logger.info("Events have been registered!")
    }

    private fun registerCommands() {
        val adventcalendarCommand = getCommand("adventcalendar")
        adventcalendarCommand!!.setExecutor(CalendarCommand())
        adventcalendarCommand.tabCompleter = MyTabCompleter()
        adventcalendarCommand.permissionMessage(getAndFormatMsg(false,"messages.noPermissionError","<red>You don't have permission to use this!"))

        logger.info("Commands have been registered!")
    }
}
