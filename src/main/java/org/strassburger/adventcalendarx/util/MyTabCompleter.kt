package org.strassburger.lifestealz.util

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class MyTabCompleter : TabCompleter {
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {

        // Fuck this is messy. sorry
        if (command.name.equals("adventcalendar", ignoreCase = true)) {
            if (args.size == 1) {
                val availableoptions = mutableListOf<String>()
                if (sender.hasPermission("adventcalendarx.admin")) availableoptions.add("settings")
                if (sender.hasPermission("adventcalendarx.open")) availableoptions.add("open")
                return availableoptions
            }
        }

        return mutableListOf<String>()
    }
}