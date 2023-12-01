package org.strassburger.lifestealz.util

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class MyTabCompleter : TabCompleter {
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String>? {

        // Fuck this is messy. sorry
        if (command.name.equals("adventcalendar", ignoreCase = true)) {
            if (args.size == 1) {
                val availableoptions = mutableListOf<String>()
                if (sender.hasPermission("adventcalendarx.testmode")) availableoptions.add("testmode")
                if (sender.hasPermission("adventcalendarx.reload")) availableoptions.add("reload")
                if (sender.hasPermission("adventcalendarx.open")) availableoptions.add("open")
                return availableoptions
            } else if (args.size == 2) {
                if (args[0] == "testmode") return null
            }
        }

        return mutableListOf<String>()
    }
}