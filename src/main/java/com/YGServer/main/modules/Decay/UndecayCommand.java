package com.YGServer.main.modules.Decay;

import com.YGServer.main.YGServer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class UndecayCommand implements CommandExecutor {

    YGServer main;

    public UndecayCommand(YGServer main) {
        this.main = main;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!sender.hasPermission("ygserver.decay.command.undecay")) return true;
        if(!(sender instanceof Player)) return false;
        main.decayModule.unDecay(((Player)sender).getLocation().getChunk());
        return true;
    }
}
