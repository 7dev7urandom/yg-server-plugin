package com.YGServer.main.modules.Decay;

import com.YGServer.main.PluginModule;
import com.YGServer.main.YGServer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

import java.util.*;

public class Decay extends PluginModule implements Listener {
    private final Material[] DECAYABLE_BLOCKS = {Material.GRASS_BLOCK, Material.STONE, Material.DIRT, Material.SAND, Material.GRAVEL, Material.GLASS};
    YGServer main;
    static NamespacedKey decayedBlocks;
    private final int MIN_DECAY_TIME = 60 * 5;
    private final int MAX_DECAY_TIME = 60 * 10;

    private BukkitRunnable pending;

    public Decay(YGServer main) {
        super(main);
        decayedBlocks = new NamespacedKey(main, "decayedBlocks");
        pending = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        };
        this.main.getCommand("undecay").setExecutor(new UndecayCommand(main));
        this.main.getCommand("decay").setExecutor(new DecayCommand(main));
    }

    @Override
    public void onEnable() {
        pending.runTaskLater(main, 20 * (int) (Math.random() * (MAX_DECAY_TIME - MIN_DECAY_TIME) + MIN_DECAY_TIME));
    }

    @Override
    public void onDisable() {
        pending.cancel();
    }

    void tick() {
        pending.runTaskLater(main, 20 * (int) (Math.random() * (MAX_DECAY_TIME - MIN_DECAY_TIME) + MIN_DECAY_TIME));
        if(!main.getConfig().getBoolean("doDecay", true)) return;
        Player[] players = main.getServer().getOnlinePlayers().toArray(new Player[0]);
        for (Player player : players) {
            if(main.essentials.getUser(player) != null && main.essentials.getUser(player).isAfk()) break;
//            if(player.getWorld().getName().startsWith("world2")) break;
            decayBlockInPlayerRange(player, 10);
//            if(Math.random() > 0.9) {
//                DecayedBlock[] decayed = getDecayedBlocksFromBox(player.getLocation(), 1);
//                if(decayed.length != 0) unDecay(decayed[(int) (Math.random() * decayed.length)]);
//            }
        }
    }
    public void decayBlockInPlayerRange(Player player, int range) {
        if(main.essentials.getUser(player) != null && main.essentials.getUser(player).isAfk()) return;
        ArrayList<Block> decayableRange = new ArrayList<>();
        Location location = player.getLocation();
        for (int x = location.getBlockX() - range; x <= location.getBlockX() + range; x++) {
            for (int y = location.getBlockY() - range / 3; y <= location.getBlockY() + range / 3; y++) {
                for (int z = location.getBlockZ() - range; z <= location.getBlockZ() + range; z++) {
                    for (Material block : DECAYABLE_BLOCKS) {
                        Block b = player.getWorld().getBlockAt(x, y, z);
                        if (block == b.getType()) {
                            decayableRange.add(b);
                            break;
                        }
                    }
                }
            }
        }
        if(decayableRange.size() == 0) return;
        decay(decayableRange.get((int)(Math.random() * decayableRange.size())));
    }
    public void unDecay(Chunk chunk) {
        for(DecayedBlock block : getDecayedBlocksFromChunk(chunk)) {
            chunk.getWorld().getBlockAt(block.block.getLocation()).setType(block.oldMaterial);
        }
        chunk.getPersistentDataContainer().remove(decayedBlocks);
    }
    public void unDecay(DecayedBlock block) {
        unDecay(new DecayedBlock[]{block}, block.block.getChunk());
    }
    public DecayedBlock[] unDecay(DecayedBlock[] blocksArr, Chunk chunk) {
        PersistentDataContainer container = chunk.getPersistentDataContainer();
        if(!container.has(decayedBlocks, PersistentDataType.STRING)) return blocksArr;
        List<DecayedBlock> blocks = new LinkedList<>(Arrays.asList(blocksArr));
        List<DecayedBlock> decayed = new LinkedList<>(Arrays.asList(getDecayedBlocksFromChunk(chunk)));
        for(int w = blocks.size() - 1; w >= 0; w--) {
            for(int i = decayed.size() - 1; i >= 0; i--) {
                if(decayed.get(i).block.getLocation().equals(blocks.get(w).block.getLocation())) {
                    chunk.getWorld().getBlockAt(blocks.get(w).block.getLocation()).setType(blocks.get(w).oldMaterial);
                    decayed.remove(i);
                    blocks.remove(w);
                    break;
                }
            }
        }
        setDecayedBlocksToChunk(chunk, decayed.toArray(new DecayedBlock[0]), true);
        return blocks.toArray(new DecayedBlock[0]);
    }
    public void decay(Block block) {
        Chunk chunk = block.getChunk();
        if(isBlockDecayed(block)) return;
        Material oldMaterial = block.getType();
        main.getLogger().info("Decaying block at " + block.getLocation());
        block.setType(Math.random() > 0.5 ? Material.OBSIDIAN : Material.CRYING_OBSIDIAN);
        DecayedBlock[] old = getDecayedBlocksFromChunk(chunk);
        DecayedBlock[] blocks = Arrays.copyOf(old, old.length + 1);
        blocks[old.length] = new DecayedBlock(block, oldMaterial);
//        DecayedBlock[] blocks = new DecayedBlock[]{new DecayedBlock(block, oldMaterial)};
        setDecayedBlocksToChunk(chunk, blocks, true);
    }

    private DecayedBlock[] getDecayedBlocksFromBox(Location location, int radius) {
        Collection<Chunk> chunks = Decay.around(location.getChunk(), radius);
        ArrayList<DecayedBlock> blocks = new ArrayList<>();
        for(Chunk chunk : chunks) {
            blocks.addAll(Arrays.asList(getDecayedBlocksFromChunk(chunk)));
        }
        return blocks.toArray(new DecayedBlock[0]);
    }

    public static Collection<Chunk> around(Chunk origin, int radius) {
        World world = origin.getWorld();

        int length = (radius * 2) + 1;
        Set<Chunk> chunks = new HashSet<>(length * length);

        int cX = origin.getX();
        int cZ = origin.getZ();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                chunks.add(world.getChunkAt(cX + x, cZ + z));
            }
        }
        return chunks;
    }


    private DecayedBlock[] getDecayedBlocksFromChunk(Chunk chunk) {
        if (!chunk.getPersistentDataContainer().has(decayedBlocks, PersistentDataType.STRING)) return new DecayedBlock[0];
        String[] data = chunk.getPersistentDataContainer().get(decayedBlocks, PersistentDataType.STRING).split("\n");
//        System.out.println("DATA: " + chunk.getPersistentDataContainer().get(decayedBlocks, PersistentDataType.STRING));
        if(data.length == 0 || data[0].equals("")) return new DecayedBlock[0];
        DecayedBlock[] ret = new DecayedBlock[data.length];
        for (int i = 0; i < data.length; i++) {
            String[] dat = data[i].split(":");
//            System.out.println(data[i]);
            ret[i] = new DecayedBlock(chunk.getWorld().getBlockAt(Integer.parseInt(dat[0]), Integer.parseInt(dat[1]), Integer.parseInt(dat[2])), Material.getMaterial(dat[3]));
        }
        return ret;
    }
    private void setDecayedBlocksToChunk(Chunk chunk, DecayedBlock[] blocks, boolean overwrite) {
        String[] datas = new String[blocks.length];
        for(int i = 0; i < datas.length; i++) {
            Location block = blocks[i].block.getLocation();
            datas[i] = block.getBlockX() + ":" + block.getBlockY() + ":" + block.getBlockZ() + ":" + blocks[i].oldMaterial.toString();
        }
        String finish = "";
        if(!overwrite && chunk.getPersistentDataContainer().has(decayedBlocks, PersistentDataType.STRING)) {
            finish = chunk.getPersistentDataContainer().get(decayedBlocks, PersistentDataType.STRING) + "\n";
        }
        finish = finish + String.join("\n", datas);
        chunk.getPersistentDataContainer().set(decayedBlocks, PersistentDataType.STRING, finish);
    }
    private boolean isBlockDecayed(Block block) {
        Chunk chunk = block.getChunk();
        for(DecayedBlock decayedBlock : getDecayedBlocksFromChunk(chunk)) {
            if(decayedBlock.block.getLocation().equals(block.getLocation())) return true;
        }
        return false;
    }
    public boolean unDecay(Block block) {
        DecayedBlock[] blocks = getDecayedBlocksFromChunk(block.getChunk());
        for(DecayedBlock decayedBlock : blocks) {
            if(decayedBlock.block.getLocation().equals(block.getLocation())) {
                unDecay(decayedBlock);
                return true;
            }
        }
        return false;
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.getBlock().getType() == Material.OBSIDIAN || event.getBlock().getType() == Material.CRYING_OBSIDIAN) {
            unDecay(event.getBlock());
        }
    }
}
