package com.YGServer.main.modules.Decay;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class DecayedBlock {
    Block block;
    Material oldMaterial;
    public DecayedBlock(Block block, Material oldMaterial) {
        this.block = block;
        this.oldMaterial = oldMaterial;
    }
}
