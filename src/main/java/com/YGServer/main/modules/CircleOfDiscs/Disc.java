package com.YGServer.main.modules.CircleOfDiscs;

import com.YGServer.main.YGServer;
import com.YGServer.main.modules.CircleOfDiscs.CircleOfDiscs;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class Disc implements Listener {

    protected ItemStack item;
    protected String id;

    public Disc(String id, ItemStack item, YGServer plugin) {
        this.item = item;
        this.id = id;
        ItemMeta meta = this.item.getItemMeta();
        meta.getPersistentDataContainer().set(CircleOfDiscs.discId, PersistentDataType.STRING, id);
//        meta.set
        this.item.setItemMeta(meta);
        new BukkitRunnable(){
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(plugin, 0, 20);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public ItemStack regenerateDiscToInventory(Inventory inventory) {
//        this.item = this.item.clone();
        if(inventory.addItem(item).isEmpty()) {
            return item;
        } else {
            return null;
        }
    }

    public void tick() {}

    public String getId() {
        return id;
    }

    public ItemStack getItem() {
        return item;
    }

    public boolean isThis(ItemStack item) {
        if(item == null) return false;
        return item.hasItemMeta()
                && item.getItemMeta().getPersistentDataContainer().has(CircleOfDiscs.discId, PersistentDataType.STRING)
                && item.getItemMeta().getPersistentDataContainer().get(CircleOfDiscs.discId, PersistentDataType.STRING).equals(this.getId());
    }
}
