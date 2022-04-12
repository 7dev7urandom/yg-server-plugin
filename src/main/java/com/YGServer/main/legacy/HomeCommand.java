package com.YGServer.main.legacy;

import com.YGServer.main.legacy.YGServerOld;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.time.Instant;

public class HomeCommand implements CommandExecutor {

    YGServerOld main;

    public HomeCommand(YGServerOld main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p;
        Location location;
        if(args.length > 0) {
            p = sender.getServer().getPlayer(args[0]);
            if(p == null || (args.length != 1 || args.length != 4)) return false;
            if(args.length == 4) {
                location = new Location(p.getWorld(), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
            } else {
                location = p.getLocation();
            }
        } else {
            if(sender instanceof Player) {
                p = (Player) sender;
                location = p.getLocation();
            } else {
                sender.sendMessage("Run this command as a player");
                return true;
            }
        }
        p.getPersistentDataContainer().set(new NamespacedKey(main, "home"), PersistentDataType.INTEGER_ARRAY, new int[]{(int) location.getX(), (int) location.getY(), (int) location.getZ()});
        p.getPersistentDataContainer().set(new NamespacedKey(main, "home_dimension"), PersistentDataType.STRING, location.getWorld().getName());
        p.getPersistentDataContainer().set(new NamespacedKey(main, "home_creation"), PersistentDataType.LONG, Instant.now().getEpochSecond());
        Command.broadcastCommandMessage(sender, "Set home to " + (int) location.getX() + " " + (int) location.getY() + " " + (int) location.getZ() + " (" + location.getWorld().getName() + ")");
        return true;
    }
}
