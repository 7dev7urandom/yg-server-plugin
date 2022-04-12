package com.YGServer.main.modules.CircleOfDiscs;

import com.YGServer.main.YGServer;
import com.YGServer.main.tools.Laser;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class ImplantCommand implements CommandExecutor {
    YGServer main;
    public ImplantCommand(YGServer main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Location loc;
        if (sender instanceof BlockCommandSender) {
            loc = ((BlockCommandSender) sender).getBlock().getLocation();
            loc.add(.5, .5, .5);
        } else {
            loc = ((Player) sender).getLocation();
        }
        sender.sendMessage("test");
        Collection<Entity> nearby = loc.getWorld().getNearbyEntities(loc, 20, 20, 20);
        sender.sendMessage(nearby.toString());
        ItemFrame frame = (ItemFrame) nearby.stream().filter(e -> e.getType() == EntityType.ITEM_FRAME).findFirst().get();
        if(frame == null) return false;
//        List<Entity> markers = nearby.stream().filter(e -> {
//            return (e instanceof ArmorStand && "ImplantMARKER".equals(((ArmorStand) e).getName()));
//        }).toList();
//        sender.sendMessage(markers.toString());
//        if(markers.size() != 2) return false;
//        BoundingBox box = BoundingBox.of(markers.get(0).getLocation(), markers.get(1).getLocation());
//        Block block = loc.getWorld().getBlockAt((int) x, (int) y, (int) z);
        sender.sendMessage("test");
        Implant.startRunning(new Location(loc.getWorld(), -241.5, 79, -12.5), frame);
        return true;
    }

}
