package com.YGServer.main.modules.CircleOfDiscs;

import com.YGServer.main.YGServer;
import com.YGServer.main.modules.CircleOfDiscs.discs.Disc;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import javax.naming.Name;
import java.util.*;

public class CircleOfDiscs implements Listener {

    YGServer main;
    ArrayList<Disc> discs = new ArrayList<>();
    public NamespacedKey discId;
    public static CircleOfDiscs instance;

    private HashMap<Player, Boolean> isInvis = new HashMap<>();
    private HashSet<Player> isXray = new HashSet<>();

    public CircleOfDiscs(YGServer mainPlugin) {
        if(instance != null) return;
        instance = this;
        main = mainPlugin;
        Implant.main = main;
        Implant.animatingItem = new NamespacedKey(main, "codInfuseAnimating");
        main.getCommand("cod").setExecutor(new CODCommand(main));
//        main.getCommand("implant").setExecutor(new ImplantCommand(main));
//        main.getCommand("runcorner").setExecutor(new CreateParticleCommand(main));
        discId = new NamespacedKey(main, "discId");
        main.getServer().getPluginManager().registerEvents(this, main);
        registerDiscs();
    }
    public void registerDiscs() {
        ItemStack item = new ItemStack(Material.MUSIC_DISC_13);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Shulker Disc");
        List<String> lore = Arrays.asList("Ancient Disc from the Circle of Discs");
        meta.setLore(lore);
        item.setItemMeta(meta);
        discs.add(new Disc("shulkerbox", item, main){
            @EventHandler
            public void onInteract(PlayerInteractEntityEvent event) {
                if(isThis(event.getPlayer().getInventory().getItem(event.getHand()))) {
                    if(event.getRightClicked() instanceof LivingEntity) {
                        ((LivingEntity)event.getRightClicked()).addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 10 * 20, 0, false, true));
                        event.setCancelled(true);
                    }
                }
            }
        });
        item = new ItemStack(Material.MUSIC_DISC_WARD);
        meta = item.getItemMeta();
        meta.setDisplayName("Strength Disc");
        meta.setLore(lore);
        item.setItemMeta(meta);
        discs.add(new Disc("strength", item, main) {
            private int ticks = 0;

            @Override
            public void tick() {
                if(ticks++ >= 4) ticks = 0;
                else return;
                main.getServer().getOnlinePlayers().forEach(player -> {
                    if(main.circleOfDiscsModule.playerHasDisc(player, this.id)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 5 * 20, 0, false, false));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 5 * 20, 0, false, false));
                        return;
                    }
                });
            }
        });
        item = new ItemStack(Material.MUSIC_DISC_CAT);
        meta = item.getItemMeta();
        meta.setDisplayName("Fly Disc");
        meta.setLore(lore);
        item.setItemMeta(meta);
        HashMap<Player, Integer> pFlyTime = new HashMap<>();
        discs.add(new Disc("fly", item, main) {
            @Override
            public void tick() {
                main.getServer().getOnlinePlayers().forEach(p -> {
                    // TODO: Cooldown for flying to prevent simply flying again after being stopped from timeout
                    // TODO: just rewrite this it's bad and doesn't even account properly for when players should be able to fly
                    // TODO: the timeout on flight should reset after touching ground
                    if(playerHasDisc(p, this.getId()) && p.getPotionEffect(PotionEffectType.LEVITATION) == null && (!pFlyTime.containsKey(p) || pFlyTime.get(p) != -1)) p.setAllowFlight(true);
                    else if(p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) p.setAllowFlight(false);

                    if(p.isFlying() && !pFlyTime.containsKey(p)) {
                        pFlyTime.put(p, 0);
                    } else if (p.isFlying()) {
                        pFlyTime.put(p, pFlyTime.get(p) + 1);
                    }
                    if(pFlyTime.containsKey(p) && pFlyTime.get(p) >= 9) {
                        pFlyTime.put(p, -1);
//                        if(p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) p.setFlying(false);
                    }
                });
            }

            @EventHandler
            public void playerMoveEvent(PlayerMoveEvent event) {
                if(event.getPlayer().isOnGround()) {
                    pFlyTime.remove(event.getPlayer());
                    if(playerHasDisc(event.getPlayer(), this)) {
                        event.getPlayer().setAllowFlight(true);
                    }
                }
            }
            @EventHandler
            public void playerFall(EntityDamageEvent event) {
                if(event.getCause() == EntityDamageEvent.DamageCause.FALL && event.getEntity() instanceof Player && playerHasDisc((Player) event.getEntity(), this)) event.setCancelled(true);
            }
        });
        item = new ItemStack(Material.MUSIC_DISC_CHIRP);
        meta = item.getItemMeta();
        meta.setDisplayName("Invisibility Disc");
        meta.setLore(lore);
        item.setItemMeta(meta);
        discs.add(new Disc("invisibility", item, main) {
            @Override
            public void tick() {
                isInvis.forEach((p, v) -> {
                    if(playerHasDisc(p, this)) {
                        if(main.essentials.getUser(p) == null) return;
                        if(v != main.essentials.getUser(p).isVanished()) main.essentials.getUser(p).setVanished(v);
                    } else {
                        if(v) {
                            isInvis.put(p, false);
                            if(main.essentials.getUser(p) == null) return;
                            main.essentials.getUser(p).setVanished(false);
                        }
                    }
                });
            }
            @EventHandler
            public void onClick(PlayerInteractEvent event) {
                if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if(event.getClickedBlock() != null && event.getClickedBlock().getState() != null) return;
                    if(isThis(event.getItem())) {
                        boolean toggle;
                        if(!isInvis.containsKey(event.getPlayer())) toggle = true;
                        else toggle = !isInvis.get(event.getPlayer());
                        isInvis.put(event.getPlayer(), toggle);
                        if(main.essentials.getUser(event.getPlayer()) == null) return;
                        main.essentials.getUser(event.getPlayer()).setVanished(toggle);
                        event.getPlayer().sendMessage(ChatColor.RED + "You are now " + (toggle ? "invisible" : "visible"));
                    }
                }
            }
            @EventHandler
            public void onAttack(EntityDamageByEntityEvent event) {
                if(event.getDamager() instanceof Player) {
                    Player d = (Player) event.getDamager();
                    if(playerHasDisc(d, this) && isInvis.containsKey(d) && isInvis.get(d)) event.setCancelled(true);
                }
            }
        });
        item = new ItemStack(Material.MUSIC_DISC_11);
        meta = item.getItemMeta();
        meta.setDisplayName("Immortality Disc");
        meta.setLore(lore);
        item.setItemMeta(meta);
        discs.add(new Disc("immortality_broken", item, main));
        item = new ItemStack(Material.MUSIC_DISC_STAL);
        meta = item.getItemMeta();
        meta.setDisplayName("Immortality Disc");
        meta.setLore(lore);
        item.setItemMeta(meta);
        discs.add(new Disc("immortality", item, main){
            @EventHandler
            public void playerDie(EntityDamageEvent event) {
                if(event.getEntity() instanceof Player && playerHasDisc((Player) event.getEntity(), this)) {
                    Player p = (Player) event.getEntity();
                    if(p.getHealth() - event.getDamage() < 1) {
                        event.setCancelled(true);
                        p.setHealth(2);
//                        p.getWorld().spawnParticle(Particle.TOTEM, p.getLocation(), 1);
                        p.playEffect(EntityEffect.TOTEM_RESURRECT);
                        for (PotionEffect effect : p.getActivePotionEffects()) p.removePotionEffect(effect.getType());
                        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 45 * 20, 1, false, true));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 5 * 20, 1, false, true));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40 * 20, 0, false, true));
                        // Maybe disc breaks?
//                        for (ItemStack item : event.getEntity().getInventory()) {
//                            if(isThis(item)) {
//                                ItemMeta meta = item.getItemMeta();
//                                meta.
//                            }
//                        }
                    }
                }
            }
        });
        item = new ItemStack(Material.MUSIC_DISC_MALL);
        meta = item.getItemMeta();
        meta.setDisplayName("X-ray Disc");
        meta.setLore(lore);
        item.setItemMeta(meta);
        discs.add(new Disc("xray", item, main){
            @EventHandler
            public void inventoryClick(InventoryClickEvent event) {
                if(!(event.getWhoClicked() instanceof Player)) return;
                reloadOffhand((Player) event.getWhoClicked());
            }
            @EventHandler
            public void offhandEvent(PlayerSwapHandItemsEvent event) {
                reloadOffhand(event.getPlayer());
            }
            private void reloadOffhand(Player p) {
                if(isThis(p.getInventory().getItemInOffHand()) && !isXray.contains(p)) {
                    p.setResourcePack("mc.micahhenney.com/xray.zip");
                    isXray.add(p);
                } else if (!isThis(p.getInventory().getItemInOffHand()) && isXray.contains(p)) {
                    p.setResourcePack("mc.micahhenney.com/blank.zip");
                    isXray.remove(p);
                }
            }
        });
        item = new ItemStack(Material.MUSIC_DISC_BLOCKS);
        meta = item.getItemMeta();
        meta.setDisplayName("Pearl Disc");
        meta.setLore(lore);
        item.setItemMeta(meta);
        discs.add(new Disc("pearl", item, main){
            @EventHandler
            public void onClick(PlayerInteractEvent event) {
                if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if(event.getClickedBlock() != null && event.getClickedBlock().getState() != null) return;
                    if(isThis(event.getItem())) {
                        if(event.getPlayer().getFoodLevel() <= 6) {
                            event.getPlayer().sendMessage(ChatColor.RED + "You don't have enough energy!");
                            return;
                        }
                        event.getPlayer().launchProjectile(EnderPearl.class, event.getPlayer().getLocation().getDirection().multiply(3));
                         event.getPlayer().setFoodLevel(event.getPlayer().getFoodLevel() - 1);
//                        event.getPlayer().setExhaustion(4);
                    }
                }
            }
            @EventHandler
            public void teleportEvent(PlayerTeleportEvent event){
                Player p = event.getPlayer();
                if(event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL && playerHasDisc(p, this)){
                    event.setCancelled(true);
                    p.setNoDamageTicks(1);
                    p.teleport(event.getTo());
                }
            }
        });

        new BukkitRunnable(){
            @Override
            public void run() {
                main.getServer().getOnlinePlayers().forEach(p -> {
                    int numOfDiscs = 0;
                    for(ItemStack item : p.getInventory()) {
                        if(item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(discId, PersistentDataType.STRING)) {
                            numOfDiscs++;
                        }
                    }
                    if(numOfDiscs >= 3) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, false, false));
                    }
                    if(numOfDiscs >= 2) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40, 0, false, false));
                    }
                });
            }
        }.runTaskTimer(main, 20, 20);
    }
    public boolean playerHasDisc(Player player) {
        for(ItemStack item : player.getInventory())
            if(item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(discId, PersistentDataType.STRING))
                return true;
        return false;
    }
    public boolean playerHasDisc(Player player, String id) {
        for(ItemStack item : player.getInventory()) {
            if(item == null || !item.hasItemMeta()) continue;
            PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
            if (data.has(discId, PersistentDataType.STRING) && data.get(discId, PersistentDataType.STRING).equals(id))
                    return true;
        }
        return false;
    }
    public boolean playerHasDisc(Player player, Disc disc) {
        return playerHasDisc(player, disc.getId());
    }

    public void giveDisc(Inventory inventory, String id) {
        for(Disc disc : discs) {
            if(disc.getId().equals(id)) {
                giveDisc(inventory, disc);
                return;
            }
        }
    }

    public void giveDisc(Inventory inventory, Disc disc) {
        inventory.addItem(disc.getItem());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Player dropping disc on death is currently disabled
//        for (ItemStack item : event.getEntity().getInventory()) {
//            if(item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(discId, PersistentDataType.STRING)) {
//                event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), item);
//                event.getEntity().getInventory().remove(item);
//            }
//        }
    }
    @EventHandler
    public void onEntityHurt(EntityDamageEvent event) {
        if(event.getEntity().getType().equals(EntityType.ENDER_CRYSTAL)) event.setCancelled(true);
    }
    @EventHandler
    public void onItemClick(InventoryClickEvent event) {
        if(event.getAction() == InventoryAction.NOTHING) return;
        ItemStack item = event.getCursor().getType() == Material.AIR ? event.getCurrentItem() : event.getCursor();
        if(item == null) {
            main.getLogger().warning("InventoryClickEvent item was null. Other info:");
            main.getLogger().warning("Current item: " + event.getCurrentItem());
            main.getLogger().warning("Cursor item: " + event.getCursor());
            main.getLogger().warning("Action: " + event.getAction());
            main.getLogger().warning("Click: " + event.getClick());
            return;
        }
        if(event.getClick() == ClickType.CREATIVE) return;
        if(item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(discId, PersistentDataType.STRING)) {
            String discType = item.getItemMeta().getPersistentDataContainer().get(discId, PersistentDataType.STRING);
            Player p = (Player) event.getWhoClicked();
            main.getLogger().info("Disc '" + discType + "' was moved by " + p.getName() + " at " + p.getLocation() + " in inventory type " + event.getWhoClicked().getOpenInventory().getType().getDefaultTitle() + " from " + event.getClickedInventory().getType().getDefaultTitle());
        }
    }
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if(event.getItem().getItemStack().hasItemMeta() && event.getItem().getItemStack().getItemMeta().getPersistentDataContainer().has(discId, PersistentDataType.STRING)) {
            if(!(event.getEntity() instanceof Player)) {
                event.setCancelled(true);
                return;
            }
            String discName = event.getItem().getItemStack().getItemMeta().getPersistentDataContainer().get(discId, PersistentDataType.STRING);
            main.getLogger().info("Disc '" + discName + "' picked up by " + event.getEntity().getName() + " at " + event.getEntity().getLocation());
        }
    }
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if(event.getItemDrop().getItemStack().hasItemMeta() && event.getItemDrop().getItemStack().getItemMeta().getPersistentDataContainer().has(discId, PersistentDataType.STRING)) {
            String discName = event.getItemDrop().getItemStack().getItemMeta().getPersistentDataContainer().get(discId, PersistentDataType.STRING);
            main.getLogger().info("Disc '" + discName + "' dropped by " + event.getPlayer().getName() + " at " + event.getPlayer().getLocation());
        }
    }
    @EventHandler
    public void itemDieEvent(ItemDespawnEvent event) {
        ItemStack item = event.getEntity().getItemStack();
        if(item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(discId, PersistentDataType.STRING)) event.setCancelled(true);
    }
    @EventHandler
    public void entityCombust(EntityCombustByBlockEvent event) {
//        System.out.println("Combust block");
        if(!(event.getEntity() instanceof Item)) return;
        ItemStack item = ((Item)event.getEntity()).getItemStack();
        if(item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(discId, PersistentDataType.STRING)) event.setCancelled(true);
//        System.out.println(event.isCancelled());
    }
    @EventHandler
    public void entityCombust(EntityCombustEvent event) {
//        System.out.println("Combust");
        if(!(event.getEntity() instanceof Item)) return;
        ItemStack item = ((Item)event.getEntity()).getItemStack();
        if(item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(discId, PersistentDataType.STRING)) event.setCancelled(true);
//        System.out.println(event.isCancelled());
    }
    @EventHandler
    public void entityCombust(EntityCombustByEntityEvent event) {
//        System.out.println("Combust entity");
        if(!(event.getEntity() instanceof Item)) return;
        ItemStack item = ((Item)event.getEntity()).getItemStack();
        if(item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(discId, PersistentDataType.STRING)) event.setCancelled(true);
//        System.out.println(event.isCancelled());
    }
    @EventHandler
    public void entityPricked(EntityDamageByEntityEvent event) {
        if(!(event.getEntity() instanceof Item)) return;
        ItemStack item = ((Item)event.getEntity()).getItemStack();
        if(item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(discId, PersistentDataType.STRING)) event.setCancelled(true);
    }
    @EventHandler
    public void entityExploded(EntityExplodeEvent event) {
        if(!(event.getEntity() instanceof Item)) return;
        ItemStack item = ((Item)event.getEntity()).getItemStack();
        if(item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(discId, PersistentDataType.STRING)) event.setCancelled(true);
    }
    @EventHandler
    public void entityDamage(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Item)) return;
        ItemStack item = ((Item)event.getEntity()).getItemStack();
        if(item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(discId, PersistentDataType.STRING)) event.setCancelled(true);
    }
}
