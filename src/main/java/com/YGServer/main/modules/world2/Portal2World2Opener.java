package com.YGServer.main.modules.world2;

import com.YGServer.main.PluginModule;
import com.YGServer.main.YGServer;
import com.YGServer.main.modules.CircleOfDiscs.CircleOfDiscs;
import com.onarandombox.MultiverseCore.utils.WorldManager;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.Arrays;

public class Portal2World2Opener extends PluginModule implements Listener {
    public static BoundingBox portalArea;
    public static Location[] discFinalLocations;
    public static Item[] discsAtLocations;
    public static boolean[] discsFinished;
    public static World world;
    public static boolean isAnimating = false;

    BukkitRunnable pending;

    public Portal2World2Opener(YGServer pluginMain) {
        super(pluginMain);
        world = Bukkit.getWorld("world");
        portalArea = BoundingBox.of(new Location(world, 95, 90, -375), new Location(world, 77, 110, -400));
        discFinalLocations = new Location[]{new Location(world, 87.5, 100.5, -385.5), new Location(world, 87.5, 100.5, -384.5), new Location(world, 88.5, 100.5, -385.5), new Location(world, 88.5, 100.5, -384.5)};
    }

    @Override
    public void onEnable() {
        main.getServer().getPluginManager().registerEvents(this, main);
        reset();
    }
    public void reset() {
        discsAtLocations = new Item[discFinalLocations.length];
        discsFinished = new boolean[discFinalLocations.length];
        Arrays.fill(discsFinished, false);
        if(pending != null) pending.cancel();
        isAnimating = false;
    }
    @Override
    public void onDisable() {
        if(pending != null) pending.cancel();
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if(isPortalOpen()) return;
        if(CircleOfDiscs.itemIsDisc(event.getItemDrop().getItemStack())) {
            if(portalArea.contains(event.getItemDrop().getLocation().toVector())) {
                boolean found = false;
                for(int i = 0; i < discsAtLocations.length; i++) {
                    if(discsAtLocations[i] != null) continue;
                    found = true;
                    discsAtLocations[i] = event.getItemDrop();
                    long startTime = System.currentTimeMillis();
                    final int lerpTime = 2000;
                    int finalI = i;
                    Vector original = event.getItemDrop().getVelocity().clone();
                    main.getLogger().info("Setting gravity");
                    event.getItemDrop().setGravity(false);
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            if(discsAtLocations[finalI] != event.getItemDrop()) {
                                this.cancel();
                                return;
                            }
                            long currentTime = System.currentTimeMillis();
                            Vector vecToFinalDestination = discFinalLocations[finalI].toVector().subtract(event.getItemDrop().getLocation().toVector());
                            if(vecToFinalDestination.lengthSquared() < .01) {
                                event.getItemDrop().setVelocity(new Vector());
                                discsFinished[finalI] = true;
                                this.cancel();
                                onDiscArrived();
                                return;
                            }
                            double time = (((double) currentTime - (double) startTime)) / ((double) lerpTime);
                            if(time > 1) time = 1;
//                            main.getLogger().info("Current time: " + currentTime + "; Start time: " + startTime + "; Time: " + time);
                            if(vecToFinalDestination.length() > .2) vecToFinalDestination.normalize().multiply(.2);
                            else vecToFinalDestination.multiply(.1);
                            event.getItemDrop().setVelocity(lerp(original, vecToFinalDestination, (float) time));
                        }
                    }.runTaskTimer(main, 0, 1);
                    break;
                }
                if(!found) return;
            }
        }
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if(CircleOfDiscs.itemIsDisc(event.getItem().getItemStack())) {
            for(int i = 0; i < discsAtLocations.length; i++) {
                if(discsAtLocations[i] == event.getItem()) {
                    if(isAnimating || event.getItem().getThrower() != event.getEntity().getUniqueId()) {
                        event.setCancelled(true);
                        return;
                    }
                    discsAtLocations[i] = null;
                    discsFinished[i] = false;
                }
            }
        }
    }

    private void onDiscArrived() {
        if(!isEnabled()) return;
        if(isPortalOpen()) return;
        main.getLogger().info("DISC ARRIVED");
        for(boolean val : discsFinished) {
            if (!val) return;
        }
        // All discs arrived, run animation
        isAnimating = true;
        Location center = new Location(world, 88, 102, -385);
        world.playSound(center, Sound.AMBIENT_CAVE, 15, 1);
        pending = new BukkitRunnable(){
            int i = 0;
            @Override
            public void run() {
                Arrays.stream(discFinalLocations).forEach(loc -> world.playEffect(loc, Effect.MOBSPAWNER_FLAMES, 0));
                world.playSound(center, Sound.BLOCK_AMETHYST_BLOCK_STEP, 20, 1);
                if(i++ >= 4) {
                    isAnimating = false;
                    this.cancel();
                    world.createExplosion(center, 2, false, false);
                    Arrays.stream(discsAtLocations).forEach(item -> item.setVelocity(new Vector()));
                    main.getServer().dispatchCommand(Bukkit.getConsoleSender(), "fill 87 99 -385 88 98 -386 air");
                }
            }
        };
        pending.runTaskTimer(main, 0, 20);
        main.getLogger().info("RUNNING ALL DISCS");
    }
    public static Vector lerp(Vector a, Vector b, float t) {
        Vector ret = b.clone().subtract(a).multiply(t).add(a);
//        main.getLogger().info("A: " + a + "; b: " + b + "; ret: " + ret);
        return ret;
    }
    public static boolean isPortalOpen() {
        return world.getBlockAt(new Location(world, 87, 99, -385)).getType().isAir();
    }

    @Override
    public boolean command(CommandSender sender, String command, String[] args) {
        if(args.length > 0) {
            if(args[0].equals("reset")) {
                reset();
                return true;
            } else if (args[0].equals("giveDiscs")) {
                if(args.length >= 2) {
                    try {
                        int id = Integer.parseInt(args[1]);

                        if(id >= discFinalLocations.length) return false;
                        if(discsAtLocations[id] == null) return true;
                        if(!(sender instanceof Player)) return false;
                        ((Player) sender).getInventory().addItem(discsAtLocations[id].getItemStack());
                        discsAtLocations[id].remove();
                        discsAtLocations[id] = null;
                        discsFinished[id] = false;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                } else {
                    if(!(sender instanceof Player)) return false;
                    for(Item disc : discsAtLocations) {
                        if(disc != null) {
                            ((Player) sender).getInventory().addItem(disc.getItemStack());
                            disc.remove();
                        }
                    }
                    Arrays.fill(discsFinished, false);
                    Arrays.fill(discsAtLocations, null);
                }
                if(pending != null) pending.cancel();
                isAnimating = false;
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
}
