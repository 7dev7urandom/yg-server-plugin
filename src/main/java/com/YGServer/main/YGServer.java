package com.YGServer.main;

import com.YGServer.main.modules.CircleOfDiscs.CODCommand;
import com.YGServer.main.modules.CircleOfDiscs.CircleOfDiscs;
import com.YGServer.main.modules.Decay.Decay;
import com.YGServer.main.modules.ShulkerProtection.ShulkerProtection;
import com.earth2me.essentials.Essentials;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class YGServer extends JavaPlugin implements Listener {

    public ShulkerProtection shulkerProtectionModule;
    public CircleOfDiscs circleOfDiscsModule;
    public Decay decayModule;
    public Essentials essentials = (Essentials) getServer().getPluginManager().getPlugin("Essentials");

    @Override
    public void onDisable() {}

    @Override
    public void onEnable() {
        shulkerProtectionModule = new ShulkerProtection(this);
        circleOfDiscsModule = new CircleOfDiscs(this);
        decayModule = new Decay(this);
    }
}
