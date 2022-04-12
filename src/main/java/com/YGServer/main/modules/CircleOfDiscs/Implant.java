package com.YGServer.main.modules.CircleOfDiscs;

import com.YGServer.main.YGServer;
import com.YGServer.main.tools.Laser;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Implant {
    public static List<Entity> entites = new ArrayList<>();
    public static YGServer main;
    public static Location centerImplantZone;
    public static boolean running;
    public static int crystals = 0;
    public static ItemFrame discContainer;
    public static ItemStack item;
    public static NamespacedKey animatingItem;

    public static void cleanup() {
        entites.forEach(e -> e.remove());
        entites.clear();
        crystals = 0;
        running = false;
        centerImplantZone.getWorld().playSound(centerImplantZone, Sound.BLOCK_BEACON_DEACTIVATE, 1, 1);
    }
    public static void particleOnLine(Location loc1, Location loc2, Particle effect, double time, boolean persist) {
        Vector vector = loc1.toVector().subtract(loc2.toVector()).normalize();
        for (double i = 0; i <= loc1.distance(loc2); i += 0.2) {
            final double x = i;
            new BukkitRunnable(){
                @Override
                public void run() {
                    if(persist) {
                        for (double b = 0; b <= x; b += 0.2) {
                            loc1.getWorld().spawnParticle(effect, loc2.clone().add(vector.clone().multiply(b)), 1, new Particle.DustOptions(Color.RED, 1));
                        }
                    } else {
                        loc1.getWorld().spawnParticle(effect, loc1.clone().subtract(vector.clone().multiply(x)), 1, new Particle.DustOptions(Color.RED, 1));
                    }
                }
            }.runTaskLater(main, (long) (i / loc1.distance(loc2) * time * 20));
        }
    }
    public static void runCorner(Location loc) {
        if(!running) return;
        particleOnLine(loc, loc.clone().add(0, 4, 0), Particle.REDSTONE, 2, false);
        new BukkitRunnable() {
            @Override
            public void run() {
                entites.add(new Laser(loc.clone().add(0, 3, 0), new Location(loc.getWorld(), -242, 80, -13), main).crystal);
                if(++crystals == 4) {
                    startInfusion();
                }
            }
        }.runTaskLater(main, 40);
    }
    public static void startInfusion() {
        new BukkitRunnable(){
            @Override
            public void run() {
                BukkitRunnable wait = new BukkitRunnable() {
                    @Override
                    public void run() {
                        particleOnLine(centerImplantZone.clone().add(0, -1, 0), centerImplantZone.clone().add(0, 4, 0), Particle.REDSTONE, 5, true);
                    }
                };
                wait.runTaskTimer(main, 0, 20 * 2);
                new BukkitRunnable(){
                    public void run() {
                        // Laser finished
                        wait.cancel();
                        Collection<Entity> ps = (Collection<Entity>) centerImplantZone.getWorld().getNearbyEntities(centerImplantZone, 1, 1, 1, e -> e.getType() == EntityType.PLAYER);
                        if(ps.size() != 1) {
                            centerImplantZone.getWorld().playSound(centerImplantZone, Sound.ENTITY_ITEM_BREAK, 1, 1);
                            cleanup();
                            return;
                        }
                        Player p = (Player) ps.toArray()[0];
                        cleanup();
                    }
                }.runTaskLater(main, 20*20);

            }
        }.runTaskLater(main, 40);
    }
    public static void startRunning(Location center, ItemFrame itemFrame) {
        if(running) return;
        main.getServer().broadcastMessage("Starting");
        discContainer = itemFrame;
        item = discContainer.getItem();
        if(item == null) return;
        if(!(item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(CircleOfDiscs.instance.discId, PersistentDataType.STRING))) return;
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Implant.animatingItem, PersistentDataType.BYTE, (byte)1);
        item.setItemMeta(meta);
        running = true;
        centerImplantZone = center;

    }
}
