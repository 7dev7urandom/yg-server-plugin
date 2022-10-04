package com.YGServer.main.modules.Decay;

import com.YGServer.main.YGServer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class UndecayCommand implements CommandExecutor {

    Decay decay;

    public UndecayCommand(Decay decay) {
        this.decay = decay;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!sender.hasPermission("ygserver.decay.command.undecay")) return true;
        if(!(sender instanceof Player)) return false;
        decay.unDecay(((Player)sender).getLocation().getChunk());
        sender.sendMessage("Undecayed the chunk you are in");
        return true;
    }
}
