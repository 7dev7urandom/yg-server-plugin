package com.YGServer.main.modules.CircleOfDiscs;

import com.YGServer.main.YGServer;
import com.YGServer.main.modules.CircleOfDiscs.discs.Disc;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class CODCommand implements CommandExecutor {

    public YGServer main;

    public CODCommand(YGServer main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("ygserver.cod.command.cod")) return true;
        if(args.length != 2) return false;
        switch (args[0]) {
            case "give":
                main.circleOfDiscsModule.giveDisc(((Player)sender).getInventory(), args[1]);
                return true;
        }
        return false;
    }
}
