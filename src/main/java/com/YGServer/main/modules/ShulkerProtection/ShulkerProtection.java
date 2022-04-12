package com.YGServer.main.modules.ShulkerProtection;

import com.YGServer.main.YGServer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;

public class ShulkerProtection implements Listener {

    YGServer main;
    static NamespacedKey shulkerId;

    public ShulkerProtection(YGServer pluginMain) {
        main = pluginMain;
        shulkerId = new NamespacedKey(main, "shulkerOwner");
        ItemStack shulker = new ItemStack(Material.SHULKER_BOX);
        ShapelessRecipe shulkerBox = new ShapelessRecipe(new NamespacedKey(pluginMain, "shulkerBox"), shulker);
        shulkerBox.addIngredient(1, Material.DIAMOND_BLOCK);
        shulkerBox.addIngredient(4, Material.EMERALD);
        shulkerBox.addIngredient(1, Material.CHEST);
        pluginMain.getServer().addRecipe(shulkerBox);
        ItemStack shulkerNamed = new ItemStack(Material.SHULKER_BOX);
        ItemMeta meta = shulkerNamed.getItemMeta();
        meta.setLore(Arrays.asList("LOCKED"));
        shulkerNamed.setItemMeta(meta);
        addShulkerNamedRecipe(1, shulkerNamed);
        addShulkerNamedRecipe(2, shulkerNamed);
        addShulkerNamedRecipe(3, shulkerNamed);
        pluginMain.getServer().getPluginManager().registerEvents(this, pluginMain);
    }

    private void addShulkerNamedRecipe(int numOfNametags, ItemStack result) {
        ShapelessRecipe shulkerBoxNamed = new ShapelessRecipe(new NamespacedKey(main, "shulkerBoxNamed" + numOfNametags), result);
        shulkerBoxNamed.addIngredient(Material.DIAMOND_BLOCK);
        shulkerBoxNamed.addIngredient(4, Material.EMERALD);
        shulkerBoxNamed.addIngredient(Material.CHEST);
        for(int i = 0; i < numOfNametags; i++) {
            shulkerBoxNamed.addIngredient(Material.NAME_TAG);
        }
        main.getServer().addRecipe(shulkerBoxNamed);
    }
    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {
        if(event.getRecipe() == null) return;
        ItemStack item = event.getRecipe().getResult();
        if(item.getItemMeta() == null) return;
        CraftingInventory inventory = event.getInventory();
        if(item.getItemMeta().hasLore() && item.getItemMeta().getLore().get(0).equals("LOCKED")) {
            ItemMeta meta = item.getItemMeta();
            ItemStack[] nameTags = (inventory.all(Material.NAME_TAG)).values().toArray(new ItemStack[0]);
//            UUID uuid = Bukkit.getOfflinePlayer(signedBookMeta.getAuthor()).getUniqueId();
            ArrayList<String> lore = new ArrayList<>();
            lore.add("Owned by");
            for(ItemStack nameTag : nameTags) {
                if(!nameTag.getItemMeta().hasDisplayName()) {
                    event.getInventory().setResult(new ItemStack(Material.AIR));
                    return;
                }
                lore.add(nameTag.getItemMeta().getDisplayName());
            }
            meta.setLore(lore);
            lore.remove(0);
            meta.getPersistentDataContainer().set(shulkerId, PersistentDataType.STRING, String.join("\n", lore));
            item.setItemMeta(meta);
            event.getInventory().setResult(item);
        }
    }
    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        if(item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(shulkerId, PersistentDataType.STRING)) {
            if(!(event.getEntity() instanceof Player)) {
                event.setCancelled(true);
                return;
            }
            // It's a locked shulker box
            if (!Arrays.asList(item.getItemMeta().getPersistentDataContainer().get(shulkerId, PersistentDataType.STRING).split("\n")).contains(event.getEntity().getName()) || !(event.getEntity() instanceof Player)) {
                // The entity is not named correctly or the entity is not a player
                if(event.getEntity().hasPermission("ygserver.shulkers.pickup_any")) return;
                if(main.circleOfDiscsModule.playerHasDisc((Player) event.getEntity(), "shulkerbox")) return;
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {
        if(event.getItemInHand().hasItemMeta()) {
            ItemMeta meta = event.getItemInHand().getItemMeta();
            if(meta.getPersistentDataContainer().has(shulkerId, PersistentDataType.STRING)) {
                ShulkerBox shulker = (ShulkerBox) event.getBlockPlaced().getState();
                shulker.getPersistentDataContainer().set(shulkerId, PersistentDataType.STRING, meta.getPersistentDataContainer().get(shulkerId, PersistentDataType.STRING));
                shulker.update();
            }
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        if(event.getBlock().getState() instanceof ShulkerBox) {
            ShulkerBox box = (ShulkerBox) event.getBlock().getState();
            PersistentDataContainer container = box.getPersistentDataContainer();
            if(container.has(shulkerId, PersistentDataType.STRING)) {
//                System.out.println(event.getBlock().getDrops());
                ItemStack drop = event.getBlock().getDrops().toArray(new ItemStack[]{})[0];
                ItemMeta meta = drop.getItemMeta();
                meta.getPersistentDataContainer().set(shulkerId, PersistentDataType.STRING, container.get(shulkerId, PersistentDataType.STRING));
                ArrayList<String> lore = new ArrayList<>();
                lore.add("Owned by");
                for(String name : container.get(shulkerId, PersistentDataType.STRING).split("\n")) {
                    lore.add(name);
                }
                meta.setLore(lore);
                drop.setItemMeta(meta);
                event.setDropItems(false);
                event.getPlayer().getWorld().dropItemNaturally(event.getBlock().getLocation(), drop);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if(block == null) return;
        if(block.getState() instanceof ShulkerBox) {
            ShulkerBox box = (ShulkerBox) block.getState();
            if(box.getPersistentDataContainer().has(shulkerId, PersistentDataType.STRING)) {
                if(!Arrays.asList(box.getPersistentDataContainer().get(shulkerId, PersistentDataType.STRING).split("\n")).contains(event.getPlayer().getName())) {
                    if(event.getPlayer().hasPermission("ygserver.shulkers.open_any")) return;
                    if(main.circleOfDiscsModule.playerHasDisc(event.getPlayer(), "shulkerbox")) return;
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "You do not have access to this block!");
                }
            }
        }
    }

    @EventHandler
    public void onItemMove(InventoryClickEvent event) {
//        System.out.println("Moved item");
        if(event.getCurrentItem() == null) return;
        if(event.getCurrentItem().getType() != Material.SHULKER_BOX) return;
        if(event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(shulkerId, PersistentDataType.STRING)) {
            main.getLogger().info(event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(shulkerId, PersistentDataType.STRING));
            if(event.getWhoClicked() instanceof Player && !Arrays.asList(event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(shulkerId, PersistentDataType.STRING).split("\n")).contains(event.getWhoClicked().getName())) {
                if(event.getClickedInventory().getType() == InventoryType.PLAYER) return;
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void itemDespawn(ItemDespawnEvent event) {
        ItemStack item = event.getEntity().getItemStack();
//        if(item.getType() != Material.SHULKER_BOX) return;
        if(item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(shulkerId, PersistentDataType.STRING)) event.setCancelled(true);
    }
}
