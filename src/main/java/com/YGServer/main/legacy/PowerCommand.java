package com.YGServer.main.legacy;

import com.YGServer.main.legacy.YGServerOld;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PowerCommand implements CommandExecutor {

    YGServerOld main;

    public PowerCommand(YGServerOld main) {
        this.main = main;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if(sender.isOp()) {
            if(args.length <= 1) return false;
            if(args[0].equals("admin")) {
                if(args[1].equals("distance")) {
                    if(args.length > 2) {
                        try {
                            main.getConfig().set("powerDistance", Integer.valueOf(args[2]));
                            return true;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    } else {
                        sender.sendMessage("Distance factor is " + main.getConfig().getInt("powerDistance", 0));
                        return true;
                    }
                }
            }
            Player p = Bukkit.getPlayerExact(args[1]);
            if(p == null) throw new CommandException("Player " + args[1] + " does not exist");
            switch (args[0]) {
                case "set":
                    try {
                        main.setPlayerPower(p, Integer.valueOf(args[2]));
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    Command.broadcastCommandMessage(sender, "Set power for " + p.getName() + " to " + args[2]);
                    if(main != null) main.updateScoreboard(p);
                    return true;
                case "get":
                    if(args.length > 2) {
                        if(args[2].startsWith("a")) {
                            int power = main.getPlayerAppliedPower(p);
                            sender.sendMessage(p.getName() + " has " + power + " applied power");
                            return true;
                        }
                    }
                    int power = main.getPlayerPower(p);
                    sender.sendMessage(p.getName() + " has " + power + " power");
                    return true;
                default:
                    return false;
            }
        }
        sender.sendMessage("You don't have permission to run this command");
        return true;
    }
}
