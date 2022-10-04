package com.YGServer.main;

import org.bukkit.command.CommandSender;

public abstract class PluginModule {
    private boolean isEnabled;
    protected YGServer main;

    public PluginModule(YGServer main) {
        this.main = main;
    }

    public abstract void onEnable();
    public abstract void onDisable();
    void enable() {
        if(isEnabled) return;
        main.getLogger().info("Enabling module " + this.getClass().getSimpleName());
        isEnabled = true;
        onEnable();
    }
    void disable() {
        if(!isEnabled) return;
        main.getLogger().info("Disabling module " + this.getClass().getSimpleName());
        isEnabled = false;
        onDisable();
    }
    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean command(CommandSender sender, String command, String[] args) {
        return false;
    }
}
