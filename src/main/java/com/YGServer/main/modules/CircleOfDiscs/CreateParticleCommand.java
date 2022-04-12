package com.YGServer.main.modules.CircleOfDiscs;

import com.YGServer.main.YGServer;
import com.YGServer.main.tools.Laser;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class CreateParticleCommand implements CommandExecutor {

    YGServer main;
    public CreateParticleCommand(YGServer main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof BlockCommandSender || sender instanceof Player)) return false;
        Location loc;
        if (sender instanceof BlockCommandSender) {
            loc = ((BlockCommandSender) sender).getBlock().getLocation();
            loc.add(.5, .5, .5);
        } else {
            loc = ((Player) sender).getLocation();
        }
        Implant.runCorner(loc);
        return true;
    }
}
