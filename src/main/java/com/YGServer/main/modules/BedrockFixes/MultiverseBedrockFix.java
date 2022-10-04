package com.YGServer.main.modules.BedrockFixes;

import com.YGServer.main.YGServer;
import com.YGServer.main.modules.world2.Portal2World2Opener;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.destination.CannonDestination;
import com.onarandombox.MultiverseCore.event.MVTeleportEvent;
import com.onarandombox.MultiverseCore.utils.WorldManager;
import com.onarandombox.MultiversePortals.event.MVPortalEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


public class MultiverseBedrockFix {
    private YGServer main;
    public MultiverseBedrockFix(YGServer main) {
        this.main = main;
        main.getLogger().warning("Registering listeners");
        tryRegister("com.onarandombox.MultiverseCore.event.MVTeleportEvent", new TeleportListener(main));
        tryRegister("com.onarandombox.MultiversePortals.event.MVPortalEvent", new PortalListener(main));

        // Fix players stuck in portal2world2
        new BukkitRunnable(){
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if(p.getLocation().subtract(new Vector(88, 51, -385)).lengthSquared() < 4) {
                        p.setVelocity(new Vector(0, 15, 0));
                        p.sendMessage("Looks like you might have gotten stuck in the portal. Here's a boost!");
                    }
                }
            }
        }.runTaskTimer(main, 0, 20 * 2);
    }

    public void tryRegister(String eventClass, AbstractListener listener) {
        try {
            Class.forName(eventClass);
            Bukkit.getPluginManager().registerEvents(listener, listener.main);
        } catch (ClassNotFoundException e) {
            // Ignore
            main.getLogger().warning("Couldn't register event " + eventClass + " (" + e.getMessage() + ")");
        }
    }
//    private static Map<Player, Instant> recentBedrockTps = new HashMap();

    public static abstract class AbstractListener implements Listener {
        public YGServer main;
        public AbstractListener(YGServer main) {
            super();
            this.main = main;
        }
    }

    class TeleportListener extends AbstractListener {

        public TeleportListener(YGServer main) {
            super(main);
        }

        @EventHandler
        public void onTeleport(MVTeleportEvent event) {
            main.getLogger().warning("onTeleport: " + event.getTeleporter().getName() + " with display name " + event.getTeleportee().getDisplayName() + ". Destination is " + event.getDestination() + " (is Cannon: " + (event.getDestination() instanceof CannonDestination) + ")");

            if(event.getDestination() instanceof CannonDestination) {
                if(event.getTeleportee().getDisplayName().charAt(0) == '.') {
                    main.getLogger().warning("Cancelling and teleporting...");
                    // Bedrock player in a cannon
//                        event.getTeleportee().setFlying(true);
//                        recentBedrockTps.put(event.getTeleportee(), Instant.now());
                    event.setCancelled(true);
                    event.getTeleportee().teleport(new Location(main.multiverse.getMVWorldManager().getMVWorld("world").getCBWorld(), 89.0, 102.0, -388.0));
                }
            }
        }
    }

    class PortalListener extends AbstractListener {
        public PortalListener(YGServer main) {
            super(main);
        }
        @EventHandler
        public void onPortal(MVPortalEvent event) {
            main.getLogger().warning("onPortal: " + event.getSendingPortal().getName() + " with display name " + event.getTeleportee().getDisplayName() + ". Destination is " + event.getDestination() + " (is Cannon: " + (event.getDestination() instanceof CannonDestination) + ")");
            event.getTeleportee().setInvulnerable(true);
            new BukkitRunnable(){
                @Override
                public void run() {
                    event.getTeleportee().setInvulnerable(false);
                }
            }.runTaskLater(main, 20 * 3);
//            if(event.getSendingPortal().getName() == "portal2world2") {
//                if(event.getTeleportee().getDisplayName().charAt(0) == '.') {
//                    Instant lastTp = recentBedrockTps.get(event.getTeleportee());
//                    if(lastTp != null && Instant.now().toEpochMilli() - lastTp.toEpochMilli() < 10000) {
//                        event.setCancelled(true);
//                        event.getTeleportee().setVelocity(new Vector(0, 20, 0));
//                    }
//                }
//            }
            if(event.getDestination() instanceof CannonDestination) {
                if(event.getTeleportee().getDisplayName().charAt(0) == '.') {
                    main.getLogger().warning("Cancelling and teleporting...");
                    // Bedrock player in a cannon
//                    event.teleportee.isFlying = true
//                    recentBedrockTps[event.teleportee] = Instant.now()
                    event.setCancelled(true);
                    event.getTeleportee().teleport(new Location(main.multiverse.getMVWorldManager().getMVWorld("world").getCBWorld(), 89.0, 102.0, -388.0));
                }
            }
        }
    }
}