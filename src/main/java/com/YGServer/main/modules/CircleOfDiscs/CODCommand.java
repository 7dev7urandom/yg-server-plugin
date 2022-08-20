package com.YGServer.main.modules.CircleOfDiscs;

import com.YGServer.main.YGServer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CODCommand implements CommandExecutor {

    public YGServer main;

    public CODCommand(YGServer main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!sender.hasPermission("ygserver.cod.command.cod")) return true;
        if(args.length != 2) return false;
        switch (args[0]) {
            case "give":
                if(!CircleOfDiscs.giveDisc(((Player)sender).getInventory(), args[1])) {
                    sender.sendMessage("Disc does not exist. All discs: ", String.join(", ", CircleOfDiscs.discs.stream().map(d -> d.id).toList()));
                }
                return true;
        }
        return false;
    }
}
