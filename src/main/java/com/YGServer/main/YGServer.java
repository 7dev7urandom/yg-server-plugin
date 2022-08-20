package com.YGServer.main;

import com.YGServer.main.modules.BedrockFixes.MultiverseBedrockFix;
import com.YGServer.main.modules.CircleOfDiscs.CODCommand;
import com.YGServer.main.modules.CircleOfDiscs.CircleOfDiscs;
import com.YGServer.main.modules.Decay.Decay;
import com.YGServer.main.modules.ShulkerProtection.ShulkerProtection;
import com.YGServer.main.modules.world2.Portal2World2Opener;
import com.earth2me.essentials.Essentials;
import com.onarandombox.MultiverseCore.MultiverseCore;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class YGServer extends JavaPlugin implements Listener {

    public Decay decayModule;
    public MultiverseBedrockFix multiverseBedrockFix;
    public Essentials essentials = (Essentials) getServer().getPluginManager().getPlugin("Essentials");
    public MultiverseCore multiverse = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");

    public Portal2World2Opener portalManager;

    public ArrayList<PluginModule> modules;
    @Override
    public void onDisable() {}

    @Override
    public void onEnable() {
        multiverseBedrockFix = new MultiverseBedrockFix(this);

        modules.add(new ShulkerProtection(this));
        modules.add(new CircleOfDiscs(this));
        modules.add(new Decay(this));
        modules.add(new Portal2World2Opener(this));

        modules.forEach(module -> module.enable());
        getCommand("ygmod").setExecutor(new ModuleCommand(this));
        getCommand("ygmod").setTabCompleter((sender, command, alias, args) -> {
            List<String> defaults = new ArrayList<>();
            if(args.length < 2) {
                defaults.add("enable");
                defaults.add("disable");
                defaults.add("list");
            }
            for (PluginModule m : modules) {
                defaults.add(m.getClass().getName());
            }
            List<String> ret = new ArrayList<>();
            if (args.length == 0) ret = defaults;
            else if (args.length == 1) StringUtil.copyPartialMatches(args[0], defaults, ret);
            else if (args.length == 2) {
                if(args[0].equals("enable") || args[0].equals("disable")) ret = defaults;
            }
            return ret;
        });
    }
}
