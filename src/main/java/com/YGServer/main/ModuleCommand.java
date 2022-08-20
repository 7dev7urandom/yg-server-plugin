package com.YGServer.main;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ModuleCommand implements CommandExecutor {

    YGServer main;

    public ModuleCommand(YGServer main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 0 || args[0].equals("list")) {
            sender.sendMessage("Modules: ", String.join(", ", main.modules.stream().map(m -> (m.isEnabled() ? ChatColor.GREEN : ChatColor.RED) + m.getClass().getName() ).toList()));
            return true;
        } else if(args[0].equals("enable")) {
            if(args.length < 2) return false;
            for(PluginModule m : main.modules) {
                if(m.getClass().getName().equals(args[1])) {
                    m.enable();
                    sender.sendMessage(args[1] + " has been enabled");
                    return true;
                }
            }
            sender.sendMessage(args[1] + " could not be found");
            return true;
        } else if (args[0].equals("disable")) {
            if(args.length < 2) return false;
            for(PluginModule m : main.modules) {
                if(m.getClass().getName().equals(args[1])) {
                    m.disable();
                    sender.sendMessage(args[1] + " has been disabled");
                    return true;
                }
            }
            sender.sendMessage(args[1] + " could not be found");
            return true;
        } else {
            for (PluginModule m : main.modules) {
                if (m.getClass().getName().equals(args[0])) {
                    return m.command(sender, String.join(" ", args), Arrays.copyOfRange(args, 1, args.length));
                }
            }
            return false;
        }
    }
}
