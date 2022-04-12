package com.YGServer.main.modules.Decay;

import com.YGServer.main.YGServer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DecayCommand implements CommandExecutor {

    YGServer main;

    public DecayCommand(YGServer main) {
        this.main = main;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!sender.hasPermission("ygserver.decay.command.decay")) return true;
        if(!(sender instanceof Player)) return false;
        main.decayModule.decayBlockInPlayerRange(((Player)sender), 10);
        return true;
    }
}