package com.YGServer.main.tools;

import com.YGServer.main.YGServer;
import org.bukkit.Location;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;

public class Laser {
    public EnderCrystal crystal;
    public Laser(Location start, Location end, YGServer main) {
        crystal = (EnderCrystal) start.getWorld().spawnEntity(start, EntityType.ENDER_CRYSTAL);
        crystal.setBeamTarget(end);
        crystal.setShowingBottom(false);
    }
}
