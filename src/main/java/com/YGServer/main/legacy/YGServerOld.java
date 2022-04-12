package com.YGServer.main.legacy;

import org.bukkit.*;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.time.Instant;


public class YGServerOld extends JavaPlugin implements Listener {

    static NamespacedKey powerKey;
    @Override
    public void onEnable() {
        getLogger().info("YGServer plugin enabled");
        this.getConfig().addDefault("powerDistance", 100);
        this.getConfig().options().copyDefaults(true);
        saveConfig();
        powerKey = new NamespacedKey(this, "power");
        ItemStack shulker = new ItemStack(Material.SHULKER_BOX);
        ShapedRecipe shulkerBox = new ShapedRecipe(new NamespacedKey(this, "shulkerBox"), shulker);
        shulkerBox.shape(" * ", "#@#", " * ");
        shulkerBox.setIngredient('*', Material.DIAMOND);
        shulkerBox.setIngredient('#', Material.GOLD_BLOCK);
        shulkerBox.setIngredient('@', Material.CHEST);
        getServer().addRecipe(shulkerBox);
        getServer().getPluginManager().registerEvents(this, this);
        YGServerOld plugin = this;
        new BukkitRunnable(){
            @Override
            public void run() {
                plugin.getServer().getOnlinePlayers().forEach(p -> updateScoreboard(p));
            }
        }.runTaskTimer(this, 0, 1);

        PluginCommand powerCommand = this.getCommand("power");
        powerCommand.setExecutor(new PowerCommand(this));
        PluginCommand homeCommand = this.getCommand("home");
        homeCommand.setExecutor(new HomeCommand(this));
//        if(CommodoreProvider.isSupported()) {
//            Commodore commodore = CommodoreProvider.getCommodore(this);
//            commodore.register(command, LiteralArgumentBuilder.literal("power")
//                    .then(LiteralArgumentBuilder.literal("get")
//                            .then(RequiredArgumentBuilder.argument("player", StringArgumentType.word())))
//                    .then(LiteralArgumentBuilder.literal("set")
//                            .then(RequiredArgumentBuilder.argument("player", StringArgumentType.word())
//                                    .then(RequiredArgumentBuilder.argument("value", IntegerArgumentType.integer(0))))).build());
//        } else {
//            System.out.println("CommodoreProvider not supported");
//        }
    }
    @Override
    public void onDisable() {
        getLogger().info("YGServer plugin disabled");
    }

    @EventHandler
    public void onPlayerExpChangeEvent(PlayerExpChangeEvent event) {
        getLogger().info(event.getPlayer().getName() + " has " + event.getPlayer().getStatistic(Statistic.PLAY_ONE_MINUTE) + " minutes played");
    }

    @EventHandler
    public void onPlayerJoined(PlayerJoinEvent event) {
        updateScoreboard(event.getPlayer());
    }

    public void updateScoreboard(Player p) {
        Scoreboard ret = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = ret.registerNewObjective("stats", "dummy", "Your stats");
        int power = getPlayerPower(p);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        Score first = obj.getScore(p.getName());
        Score second = obj.getScore(ChatColor.RED + "Power: " + ChatColor.RESET + power);
        Score third = obj.getScore(ChatColor.RED + "Applied power: " + ChatColor.RESET + getPlayerAppliedPower(p));

        first.setScore(2);
        second.setScore(1);
        third.setScore(0);

        p.setScoreboard(ret);
    }
    public int getPlayerPower(Player p) {
        PersistentDataContainer pData = p.getPersistentDataContainer();
        int power = 0;
        if(pData.has(powerKey, PersistentDataType.INTEGER)) {
            power = pData.get(powerKey, PersistentDataType.INTEGER);
        }
        return power;
    }
    public void setPlayerPower(Player p, int power) {
        PersistentDataContainer pData = p.getPersistentDataContainer();
        pData.set(powerKey, PersistentDataType.INTEGER, power);
    }
    public int getPlayerAppliedPower(Player p) {
        int power = getPlayerPower(p);
        int[] pos = p.getPersistentDataContainer().get(new NamespacedKey(this, "home"), PersistentDataType.INTEGER_ARRAY);
        if(pos == null) return 0;
        String dim = p.getPersistentDataContainer().get(new NamespacedKey(this, "home_dimension"), PersistentDataType.STRING);
        if(dim == null) return 0;
        if(!p.getPersistentDataContainer().has(new NamespacedKey(this, "home_creation"), PersistentDataType.LONG)) return 0;
        long time = (Instant.now().getEpochSecond() - p.getPersistentDataContainer().get(new NamespacedKey(this, "home_creation"), PersistentDataType.LONG)) / 24 / 60 /60;
        Location pPos = p.getLocation();
        double distance = 0;
        if(dim.equals(pPos.getWorld().getName())) {
            distance = Math.sqrt(Math.pow(pos[0] - pPos.getX(), 2) + Math.pow(pos[1] - pPos.getY(), 2) + Math.pow(pos[2] - pPos.getZ(), 2));
        }
        return (int) Math.ceil(power / (distance + this.getConfig().getInt("powerDistance", 0)) * this.getConfig().getInt("powerDistance", 0) + Math.sqrt(time));
    }
}
