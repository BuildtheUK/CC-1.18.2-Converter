package org.btuk.converter.plugin;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.lib.PaperLib;
import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.data.*;
import org.bukkit.block.data.type.*;
import org.bukkit.block.data.type.Observer;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import org.btuk.converter.plugin.utils.Utils;
import org.btuk.converter.plugin.utils.blocks.stairs.StairData;
import org.btuk.converter.plugin.utils.entities.ArmorStandHelper;
import org.btuk.converter.plugin.utils.entities.MinecartHelper;
import org.btuk.converter.plugin.utils.exceptions.BlockNotFoundException;
import org.btuk.converter.plugin.utils.items.ItemsHelper;

public class Converter {

    public final Plugin instance;
    private final World world;

    private boolean isRunning;

    public Converter(Plugin instance, World world) {
        this.instance = instance;
        this.world = world;

        //Set the helpers to use the logger in the Plugin
        ItemsHelper.setHelper(instance);
        ArmorStandHelper.setHelper(instance);

        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean value) {
        isRunning = value;
    }

    public CompletableFuture<Void> convert(JSONObject jsonObject) {

        List<CompletableFuture<Void>> completableFutures = new ArrayList<>();

        JSONArray blocksObjects = (JSONArray) jsonObject.get("block");

        HashMap<String, List<JSONObject>> objectsPerChunk = new HashMap<>();

        //Iterate through array of blocks.
        //Add them to the objects per chunk hash map
        for(Object object : blocksObjects){
            JSONObject jObject = (JSONObject) object;
            Integer blockX = Utils.readInteger(jObject, "x");
            Integer blockY = Utils.readInteger(jObject, "y");
            Integer blockZ = Utils.readInteger(jObject, "z");
            if(blockX != null && blockY != null && blockZ != null) {
                int chunkX = blockX >> 4;
                int chunkY = blockY >> 4;
                int chunkZ = blockZ >> 4;
                String chunkIndex = String.format("%1$d,%2$d,%3$d", chunkX, chunkY, chunkZ);

                if (objectsPerChunk.containsKey(chunkIndex))
                    objectsPerChunk.get(chunkIndex).add(jObject);
                else {
                    List<JSONObject> jObjects = new ArrayList<>();
                    jObjects.add(jObject);
                    objectsPerChunk.put(chunkIndex, jObjects);
                }
            }
        }

        //Check if region contains entities to add
        if(jsonObject.containsKey("entity")){

            JSONArray entityObjects = (JSONArray) jsonObject.get("entity");

            for(Object object : entityObjects){
                JSONObject jObject = (JSONObject) object;
                Double locX = Utils.readDouble(jObject, "x");
                Double locY = Utils.readDouble(jObject, "y");
                Double locZ = Utils.readDouble(jObject, "z");
                if(locX != null && locY != null && locZ != null) {
                    int chunkX = Utils.floor(locX) >> 4;
                    int chunkY = Utils.floor(locY) >> 4;
                    int chunkZ = Utils.floor(locZ) >> 4;
                    String chunkIndex = String.format("%1$d,%2$d,%3$d", chunkX, chunkY, chunkZ);

                    if (objectsPerChunk.containsKey(chunkIndex))
                        objectsPerChunk.get(chunkIndex).add(jObject);
                    else {
                        List<JSONObject> jObjects = new ArrayList<>();
                        jObjects.add(jObject);
                        objectsPerChunk.put(chunkIndex, jObjects);
                    }
                }
            }

        }

        for(String chunkIndex : objectsPerChunk.keySet()){
            List<JSONObject> chunkObjects = objectsPerChunk.get(chunkIndex);

            int chunkX = Integer.parseInt(chunkIndex.substring(0, chunkIndex.indexOf(',')));
            int chunkY = Integer.parseInt(chunkIndex.substring(chunkIndex.indexOf(',') + 1, chunkIndex.indexOf(',', chunkIndex.indexOf(',') + 1)));
            int chunkZ = Integer.parseInt(chunkIndex.substring(chunkIndex.indexOf(',', chunkIndex.indexOf(',') + 1) + 1));

            Location chunkL = new Location(world, (chunkX << 4) + 1 , (chunkY << 4) + 1, (chunkZ << 4) + 1);
            Location chunkLPlusX = chunkL.clone();
            chunkLPlusX.setX(chunkL.getX() + 1);
            Location chunkLPlusZ = chunkL.clone();
            chunkLPlusZ.setZ(chunkL.getZ() + 1);
            Location chunkLMinX = chunkL.clone();
            chunkLMinX.setX(chunkL.getX() - 1);
            Location chunkLMinZ = chunkL.clone();
            chunkLMinZ.setZ(chunkL.getZ() - 1);

            CompletableFuture<Chunk>[] chunks = new CompletableFuture[]{
                    PaperLib.getChunkAtAsync(chunkL),
                    PaperLib.getChunkAtAsync(chunkLPlusX),
                    PaperLib.getChunkAtAsync(chunkLPlusZ),
                    PaperLib.getChunkAtAsync(chunkLMinX),
                    PaperLib.getChunkAtAsync(chunkLMinZ)
            };

            completableFutures.add(CompletableFuture.allOf(chunks).thenRunAsync(() -> Bukkit.getScheduler().runTask(instance, () -> {
                //Iterate through array of blocks.
                //Convert them, depending on the type
                //Catch any exceptions that may occur.
                for(JSONObject jObject : chunkObjects){
                    //Check if jObject is blocks
                    if(jObject.containsKey("block")){
                        Integer blockX = Utils.readInteger(jObject, "x");
                        Integer blockY = Utils.readInteger(jObject, "y");
                        Integer blockZ = Utils.readInteger(jObject, "z");
                        if(blockX != null && blockY != null && blockZ != null) {
                            Location blockLocation = new Location(world, blockX, blockY, blockZ);

                            try {
                                setBlockData(jObject, blockLocation);
                            } catch (Exception e) {
                                instance.getLogger().warning(e.getMessage());
                            }
                        }
                    }else {
                        //Else jObject is entity
                        String entityNamespace = (String) jObject.get("entity");

                        //Get the location of the entity.
                        Double locX = Utils.readDouble(jObject, "x");
                        Double locY = Utils.readDouble(jObject, "y");
                        Double locZ = Utils.readDouble(jObject, "z");

                        if(locX != null && locY != null && locZ != null) {
                            Location entityLocation = new Location(world, locX, locY, locZ);

                            try {
                                setEntity(entityNamespace, jObject, entityLocation);
                            } catch (Exception e) {
                                instance.getLogger().warning(String.format("setEntity (%1$s) error: \n%2$s", entityNamespace, e.getMessage()));
                            }
                        }
                    }
                }
            })));
        }

        return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]));
    }

    //Set the blockData of the block.
    private void setBlockData(JSONObject object, Location l) throws Exception {

        Block block = world.getBlockAt(l);

        String blockName = (String) object.get("block");

        switch (blockName) {

            case "minecraft:sunflower" -> {

                //Check the block below:
                Location lYMin = new Location(world, l.getX(), l.getY() - 1, l.getZ());
                BlockData bYMin = world.getBlockData(lYMin);

                if (bYMin.getMaterial() == Material.SUNFLOWER) {
                    setTopFlower(Material.SUNFLOWER, block);
                } else if (bYMin.getMaterial() == Material.LILAC) {
                    setTopFlower(Material.LILAC, block);
                } else if (bYMin.getMaterial() == Material.ROSE_BUSH) {
                    setTopFlower(Material.ROSE_BUSH, block);
                } else if (bYMin.getMaterial() == Material.PEONY) {
                    setTopFlower(Material.PEONY, block);
                } else if (bYMin.getMaterial() == Material.TALL_GRASS) {
                    setTopFlower(Material.TALL_GRASS, block);
                } else if (bYMin.getMaterial() == Material.LARGE_FERN) {
                    setTopFlower(Material.LARGE_FERN, block);
                }
            }

            case "minecraft:oak_stairs", "minecraft:cobblestone_stairs", "minecraft:brick_stairs", "minecraft:stone_brick_stairs", "minecraft:nether_brick_stairs",
                 "minecraft:sandstone_stairs", "minecraft:spruce_stairs", "minecraft:birch_stairs", "minecraft:jungle_stairs", "minecraft:quartz_stairs", "minecraft:acacia_stairs",
                 "minecraft:dark_oak_stairs", "minecraft:red_sandstone_stairs", "minecraft:purpur_stairs" -> {

                BlockData bd = block.getBlockData();
                if (!(bd instanceof Stairs)) {
                    throw new BlockNotFoundException("Found " + bd.getMaterial().name() + " expected " + object.get("block") + " at " + l.getX() + "," + l.getY() + "," + l.getZ());
                }

                Stairs stair = getStair(l);

                //Update the block.
                block.setBlockData(stair, false);

            }

            //Fences, Iron bar and Glass panes.
            case "minecraft:oak_fence", "minecraft:birch_fence", "minecraft:spruce_fence", "minecraft:jungle_fence", "minecraft:acacia_fence", "minecraft:dark_oak_fence",
                 "minecraft:nether_brick_fence", "minecraft:glass_pane", "minecraft:iron_bars" -> {

                //Check if the fence can connect to adjacent blocks.
                BlockData bd = block.getBlockData();
                if (!(bd instanceof Fence fence)) {
                    throw new BlockNotFoundException("Found " + bd.getMaterial().name() + " expected " + object.get("block") + " at " + l.getX() + "," + l.getY() + "," + l.getZ());
                }

                //North (Negative Z)
                Location lZMin = new Location(world, l.getX(), l.getY(), l.getZ() - 1);
                if (canConnect(bd.getMaterial(), lZMin, BlockFace.NORTH)) {
                    fence.setFace(BlockFace.NORTH, true);
                }

                //East (Positive X)
                Location lXMax = new Location(world, l.getX() + 1, l.getY(), l.getZ());
                if (canConnect(bd.getMaterial(), lXMax, BlockFace.EAST)) {
                    fence.setFace(BlockFace.EAST, true);
                }

                //South (Positive Z)
                Location lZMax = new Location(world, l.getX(), l.getY(), l.getZ() + 1);
                if (canConnect(bd.getMaterial(), lZMax, BlockFace.SOUTH)) {
                    fence.setFace(BlockFace.SOUTH, true);
                }

                //West (Negative X)
                Location lXMin = new Location(world, l.getX() - 1, l.getY(), l.getZ());
                if (canConnect(bd.getMaterial(), lXMin, BlockFace.WEST)) {
                    fence.setFace(BlockFace.WEST, true);
                }

                block.setBlockData(fence, false);
            }

            //Stained glass (for some reason it's a different data type from normal glass panes, even though the data is the exact same)
            case "minecraft:red_stained_glass_pane", "minecraft:lime_stained_glass_pane",
                 "minecraft:pink_stained_glass_pane", "minecraft:gray_stained_glass_pane", "minecraft:cyan_stained_glass_pane", "minecraft:blue_stained_glass_pane",
                 "minecraft:white_stained_glass_pane", "minecraft:brown_stained_glass_pane", "minecraft:green_stained_glass_pane", "minecraft:black_stained_glass_pane",
                 "minecraft:orange_stained_glass_pane", "minecraft:yellow_stained_glass_pane", "minecraft:purple_stained_glass_pane",
                 "minecraft:magenta_stained_glass_pane", "minecraft:light_blue_stained_glass_pane", "minecraft:light_gray_stained_glass_pane" -> {

                //Check if the fence can connect to adjacent blocks.
                BlockData bd = block.getBlockData();
                if (!(bd instanceof GlassPane fence)) {
                    throw new BlockNotFoundException("Found " + bd.getMaterial().name() + " expected " + object.get("block") + " at " + l.getX() + "," + l.getY() + "," + l.getZ());
                }

                //North (Negative Z)
                Location lZMin = new Location(world, l.getX(), l.getY(), l.getZ() - 1);
                if (canConnect(bd.getMaterial(), lZMin, BlockFace.NORTH)) {
                    fence.setFace(BlockFace.NORTH, true);
                }

                //East (Positive X)
                Location lXMax = new Location(world, l.getX() + 1, l.getY(), l.getZ());
                if (canConnect(bd.getMaterial(), lXMax, BlockFace.EAST)) {
                    fence.setFace(BlockFace.EAST, true);
                }

                //South (Positive Z)
                Location lZMax = new Location(world, l.getX(), l.getY(), l.getZ() + 1);
                if (canConnect(bd.getMaterial(), lZMax, BlockFace.SOUTH)) {
                    fence.setFace(BlockFace.SOUTH, true);
                }

                //West (Negative X)
                Location lXMin = new Location(world, l.getX() - 1, l.getY(), l.getZ());
                if (canConnect(bd.getMaterial(), lXMin, BlockFace.WEST)) {
                    fence.setFace(BlockFace.WEST, true);
                }

                block.setBlockData(fence, false);

            }

            case "minecraft:cobblestone_wall", "minecraft:mossy_cobblestone_wall" -> {

                //Check if the fence can connect to adjacent blocks.
                BlockData bd = world.getBlockData(l);
                if (!(bd instanceof Wall)) {
                    throw new BlockNotFoundException("Found " + bd.getMaterial().name() + " expected " + object.get("block") + " at " + l.getX() + "," + l.getY() + "," + l.getZ());
                }

                block.setBlockData(getWall(l), false);

            }

            case "minecraft:chest", "minecraft:trapped_chest" -> {

                BlockData blockData = world.getBlockData(l);

                //Check adjacent blocks.
                //North (Negative Z)
                Location lZMin = new Location(world, l.getX(), l.getY(), l.getZ() - 1);

                //East (Positive X)
                Location lXMax = new Location(world, l.getX() + 1, l.getY(), l.getZ());

                //South (Positive Z)
                Location lZMax = new Location(world, l.getX(), l.getY(), l.getZ() + 1);

                //West (Negative X)
                Location lXMin = new Location(world, l.getX() - 1, l.getY(), l.getZ());

                //Check if types are the same and they face the same direction.
                BlockData bZMin = world.getBlockData(lZMin);
                BlockData bXMax = world.getBlockData(lXMax);
                BlockData bZMax = world.getBlockData(lZMax);
                BlockData bXMin = world.getBlockData(lXMin);

                if (!(blockData instanceof Chest chest)) {
                    throw new BlockNotFoundException("Found " + blockData.getMaterial().name() + " expected " + object.get("block") + " at " + l.getX() + "," + l.getY() + "," + l.getZ());
                }

                if (bZMin.getMaterial() == blockData.getMaterial()) {
                    //Check if directions are the same.
                    Chest oChest = (Chest) bZMin;
                    if (chest.getFacing() == oChest.getFacing()) {
                        //Must be West of East otherwise they can't connect.
                        if (chest.getFacing() == BlockFace.WEST) {
                            chest.setType(Chest.Type.LEFT);
                        } else if (chest.getFacing() == BlockFace.EAST) {
                            chest.setType(Chest.Type.RIGHT);
                        }
                    }
                } else if (bXMax.getMaterial() == blockData.getMaterial()) {
                    //Check if directions are the same.
                    Chest oChest = (Chest) bXMax;
                    if (chest.getFacing() == oChest.getFacing()) {
                        //Must be North or South otherwise they can't connect.
                        if (chest.getFacing() == BlockFace.NORTH) {
                            chest.setType(Chest.Type.LEFT);
                        } else if (chest.getFacing() == BlockFace.SOUTH) {
                            chest.setType(Chest.Type.RIGHT);
                        }
                    }
                } else if (bZMax.getMaterial() == blockData.getMaterial()) {
                    //Check if directions are the same.
                    Chest oChest = (Chest) bZMax;
                    if (chest.getFacing() == oChest.getFacing()) {
                        //Must be West of East otherwise they can't connect.
                        if (chest.getFacing() == BlockFace.EAST) {
                            chest.setType(Chest.Type.LEFT);
                        } else if (chest.getFacing() == BlockFace.WEST) {
                            chest.setType(Chest.Type.RIGHT);
                        }
                    }
                } else if (bXMin.getMaterial() == blockData.getMaterial()) {
                    //Check if directions are the same.
                    Chest oChest = (Chest) bXMin;
                    if (chest.getFacing() == oChest.getFacing()) {
                        //Must be North or South otherwise they can't connect.
                        if (chest.getFacing() == BlockFace.SOUTH) {
                            chest.setType(Chest.Type.LEFT);
                        } else if (chest.getFacing() == BlockFace.NORTH) {
                            chest.setType(Chest.Type.RIGHT);
                        }
                    }
                }

                block.setBlockData(chest, false);

                if(object.containsKey("properties")){
                    JSONObject props = (JSONObject) object.get("properties");
                    org.bukkit.block.Chest _chest = (org.bukkit.block.Chest) block.getState();

                    if(props.containsKey("loot_table")){
                        String _lootTable = (String) props.get("loot_table");
                        LootTable lootTable = LootTables.SIMPLE_DUNGEON.getLootTable();
                        if(_lootTable.startsWith("minecraft:")) {
                            _lootTable = _lootTable.substring(10);
                            _lootTable = _lootTable.substring(_lootTable.indexOf("/") + 1).toUpperCase();
                            try{
                                lootTable = LootTables.valueOf(_lootTable).getLootTable();
                            }catch (Exception ex){ }
                        }


                        Long loot_table_seed = Utils.readLong(props, "loot_table_seed");
                        if(loot_table_seed != null){
                            long _lootTableSeed = loot_table_seed;
                            _chest.setLootTable(lootTable, _lootTableSeed);
                        }else {
                            _chest.setLootTable(lootTable);
                        }
                    }

                    if(props.containsKey("items")){
                        Inventory chestInventory = _chest.getBlockInventory();
                        JSONArray itemsRaw = (JSONArray) props.get("items");
                        ItemsHelper.setItems(chestInventory, itemsRaw);
                    }
                }

            }

            case "minecraft:shulker_box", "minecraft:pink_shulker_box", "minecraft:red_shulker_box",
                 "minecraft:lime_shulker_box", "minecraft:gray_shulker_box","minecraft:cyan_shulker_box",
                 "minecraft:blue_shulker_box", "minecraft:white_shulker_box", "minecraft:brown_shulker_box",
                 "minecraft:green_shulker_box", "minecraft:black_shulker_box", "minecraft:orange_shulker_box",
                 "minecraft:yellow_shulker_box", "minecraft:purple_shulker_box", "minecraft:magenta_shulker_box",
                 "minecraft:light_blue_shulker_box", "minecraft:light_gray_shulker_box", "minecraft:dispenser", "minecraft:dropper", "minecraft:hopper" -> {


                if(object.containsKey("properties")){
                    JSONObject props = (JSONObject) object.get("properties");
                    LootTable lootTable = null;
                    Long _lootTableSeed = null;
                    JSONArray itemsRaw = null;

                    if(props.containsKey("loot_table")){
                        String _lootTable = (String) props.get("loot_table");
                        lootTable = LootTables.SIMPLE_DUNGEON.getLootTable();
                        if(_lootTable.startsWith("minecraft:")) {
                            _lootTable = _lootTable.substring(10);
                            _lootTable = _lootTable.substring(_lootTable.indexOf("/") + 1).toUpperCase();
                            try{
                                lootTable = LootTables.valueOf(_lootTable).getLootTable();
                            }catch (Exception ex){ }
                        }


                        Long loot_table_seed = Utils.readLong(props, "loot_table_seed");
                        if(loot_table_seed != null)
                            _lootTableSeed = loot_table_seed;
                    }

                    if(props.containsKey("items"))
                        itemsRaw = (JSONArray) props.get("items");

                    if(blockName.contains("shulker")) {
                        org.bukkit.block.ShulkerBox shulkerBox = (org.bukkit.block.ShulkerBox) block.getState();
                        if(lootTable != null) {
                            if(_lootTableSeed != null)
                                shulkerBox.setLootTable(lootTable, _lootTableSeed);
                            else
                                shulkerBox.setLootTable(lootTable);

                        }

                        if(itemsRaw != null){
                            Inventory chestInventory = shulkerBox.getInventory();
                            ItemsHelper.setItems(chestInventory, itemsRaw);
                        }
                    }else if(blockName.equals("minecraft:dispenser")) {
                        org.bukkit.block.Dispenser dispenser = (org.bukkit.block.Dispenser) block.getState();

                        if(lootTable != null) {
                            if(_lootTableSeed != null)
                                dispenser.setLootTable(lootTable, _lootTableSeed);
                            else
                                dispenser.setLootTable(lootTable);

                        }

                        if(itemsRaw != null){
                            Inventory inventory = dispenser.getInventory();
                            ItemsHelper.setItems(inventory, itemsRaw);
                        }
                    } else if(blockName.equals("minecraft:dropper")) {
                        org.bukkit.block.Dropper dropper = (org.bukkit.block.Dropper) block.getState();

                        if(lootTable != null) {
                            if(_lootTableSeed != null)
                                dropper.setLootTable(lootTable, _lootTableSeed);
                            else
                                dropper.setLootTable(lootTable);

                        }

                        if(itemsRaw != null){
                            Inventory inventory = dropper.getInventory();
                            ItemsHelper.setItems(inventory, itemsRaw);
                        }
                    } else if(blockName.equals("minecraft:hopper")) {
                        org.bukkit.block.Hopper hopper = (org.bukkit.block.Hopper) block.getState();

                        if(lootTable != null) {
                            if(_lootTableSeed != null)
                                hopper.setLootTable(lootTable, _lootTableSeed);
                            else
                                hopper.setLootTable(lootTable);

                        }

                        if(itemsRaw != null){
                            Inventory inventory = hopper.getInventory();
                            ItemsHelper.setItems(inventory, itemsRaw);
                        }
                    }
                }
            }

            case "minecraft:redstone_wire" -> {

                BlockData blockData = world.getBlockData(l);
                if (!(blockData instanceof RedstoneWire redstoneWire)) {
                    throw new BlockNotFoundException("Found " + blockData.getMaterial().name() + " expected " + object.get("block") + " at " + l.getX() + "," + l.getY() + "," + l.getZ());
                }

                //Check if adjacent blocks can connect to the redstone.
                //North (Negative Z)
                Location lZMin = new Location(world, l.getX(), l.getY(), l.getZ() - 1);
                if (redstoneConnects(world.getBlockAt(lZMin), BlockFace.NORTH)) {
                    redstoneWire.setFace(BlockFace.NORTH, RedstoneWire.Connection.SIDE);
                } else {
                    //Check if it can connect up.
                    if (redstoneConnectsUp(lZMin, BlockFace.NORTH)) {
                        redstoneWire.setFace(BlockFace.NORTH, RedstoneWire.Connection.UP);
                    }
                }

                //East (Positive X)
                Location lXMax = new Location(world, l.getX() + 1, l.getY(), l.getZ());
                if (redstoneConnects(world.getBlockAt(lXMax), BlockFace.EAST)) {
                    redstoneWire.setFace(BlockFace.EAST, RedstoneWire.Connection.SIDE);
                } else {
                    //Check if it can connect up.
                    if (redstoneConnectsUp(lXMax, BlockFace.EAST)) {
                        redstoneWire.setFace(BlockFace.EAST, RedstoneWire.Connection.UP);
                    }
                }

                //South (Positive Z)
                Location lZMax = new Location(world, l.getX(), l.getY(), l.getZ() + 1);
                if (redstoneConnects(world.getBlockAt(lZMax), BlockFace.SOUTH)) {
                    redstoneWire.setFace(BlockFace.SOUTH, RedstoneWire.Connection.SIDE);
                } else {
                    //Check if it can connect up.
                    if (redstoneConnectsUp(lZMax, BlockFace.SOUTH)) {
                        redstoneWire.setFace(BlockFace.SOUTH, RedstoneWire.Connection.UP);
                    }
                }

                //West (Negative X)
                Location lXMin = new Location(world, l.getX() - 1, l.getY(), l.getZ());
                if (redstoneConnects(world.getBlockAt(lXMin), BlockFace.WEST)) {
                    redstoneWire.setFace(BlockFace.WEST, RedstoneWire.Connection.SIDE);
                } else {
                    //Check if it can connect up.
                    if (redstoneConnectsUp(lXMin, BlockFace.WEST)) {
                        redstoneWire.setFace(BlockFace.WEST, RedstoneWire.Connection.UP);
                    }
                }

                //If only 1 face is set then the opposite side needs to be set to SIDE.
                int sum = 0;
                if (redstoneWire.getFace(BlockFace.NORTH) != RedstoneWire.Connection.NONE) {
                    sum++;
                }
                if (redstoneWire.getFace(BlockFace.EAST) != RedstoneWire.Connection.NONE) {
                    sum++;
                }
                if (redstoneWire.getFace(BlockFace.SOUTH) != RedstoneWire.Connection.NONE) {
                    sum++;
                }
                if (redstoneWire.getFace(BlockFace.WEST) != RedstoneWire.Connection.NONE) {
                    sum++;
                }
                if (sum == 1) {
                    if (redstoneWire.getFace(BlockFace.NORTH) != RedstoneWire.Connection.NONE) {
                        redstoneWire.setFace(BlockFace.SOUTH, RedstoneWire.Connection.SIDE);
                    }
                    if (redstoneWire.getFace(BlockFace.EAST) != RedstoneWire.Connection.NONE) {
                        redstoneWire.setFace(BlockFace.WEST, RedstoneWire.Connection.SIDE);
                    }
                    if (redstoneWire.getFace(BlockFace.SOUTH) != RedstoneWire.Connection.NONE) {
                        redstoneWire.setFace(BlockFace.NORTH, RedstoneWire.Connection.SIDE);
                    }
                    if (redstoneWire.getFace(BlockFace.WEST) != RedstoneWire.Connection.NONE) {
                        redstoneWire.setFace(BlockFace.EAST, RedstoneWire.Connection.SIDE);
                    }
                }

                block.setBlockData(redstoneWire, false);

            }

            case "minecraft:chorus_plant" -> {

                BlockData blockData = world.getBlockData(l);
                if (!(blockData instanceof MultipleFacing facing)) {
                    throw new BlockNotFoundException("Found " + blockData.getMaterial().name() + " expected " + object.get("block") + " at " + l.getX() + "," + l.getY() + "," + l.getZ());
                }

                //Check if all adjacent blocks are chorus plants.
                //If true they connect.
                //North (Negative Z)
                Location lZMin = new Location(world, l.getX(), l.getY(), l.getZ() - 1);
                if (world.getType(lZMin) == Material.CHORUS_PLANT || world.getType(lZMin) == Material.CHORUS_FLOWER) {
                    facing.setFace(BlockFace.NORTH, true);
                }

                //East (Positive X)
                Location lXMax = new Location(world, l.getX() + 1, l.getY(), l.getZ());
                if (world.getType(lXMax) == Material.CHORUS_PLANT || world.getType(lZMin) == Material.CHORUS_FLOWER) {
                    facing.setFace(BlockFace.EAST, true);
                }

                //South (Positive Z)
                Location lZMax = new Location(world, l.getX(), l.getY(), l.getZ() + 1);
                if (world.getType(lZMax) == Material.CHORUS_PLANT || world.getType(lZMin) == Material.CHORUS_FLOWER) {
                    facing.setFace(BlockFace.SOUTH, true);
                }

                //West (Negative X)
                Location lXMin = new Location(world, l.getX() - 1, l.getY(), l.getZ());
                if (world.getType(lXMin) == Material.CHORUS_PLANT || world.getType(lZMin) == Material.CHORUS_FLOWER) {
                    facing.setFace(BlockFace.WEST, true);
                }

                //Down
                Location lYMin = new Location(world, l.getX(), l.getY() - 1, l.getZ());
                if (world.getType(lYMin) == Material.CHORUS_PLANT || world.getType(lZMin) == Material.CHORUS_FLOWER || world.getType(lZMin) == Material.END_STONE) {
                    facing.setFace(BlockFace.DOWN, true);
                }

                //Up
                Location lYMax = new Location(world, l.getX(), l.getY() + 1, l.getZ());
                if (world.getType(lYMax) == Material.CHORUS_PLANT || world.getType(lZMin) == Material.CHORUS_FLOWER) {
                    facing.setFace(BlockFace.UP, true);
                }

                block.setBlockData(facing, false);
            }

            case "minecraft:red_bed" -> {

                //Get properties.
                JSONObject properties = (JSONObject) object.get("properties");

                //Set colour.
                switch ((String) properties.get("colour")) {
                    case "white" -> block.setType(Material.WHITE_BED, false);
                    case "orange" -> block.setType(Material.ORANGE_BED, false);
                    case "magenta" -> block.setType(Material.MAGENTA_BED, false);
                    case "light_blue" -> block.setType(Material.LIGHT_BLUE_BED, false);
                    case "yellow" -> block.setType(Material.YELLOW_BED, false);
                    case "lime" -> block.setType(Material.LIME_BED, false);
                    case "pink" -> block.setType(Material.PINK_BED, false);
                    case "gray" -> block.setType(Material.GRAY_BED, false);
                    case "light_gray" -> block.setType(Material.LIGHT_GRAY_BED, false);
                    case "cyan" -> block.setType(Material.CYAN_BED, false);
                    case "purple" -> block.setType(Material.PURPLE_BED, false);
                    case "blue" -> block.setType(Material.BLUE_BED, false);
                    case "brown" -> block.setType(Material.BROWN_BED, false);
                    case "green" -> block.setType(Material.GREEN_BED, false);
                    case "red" -> block.setType(Material.RED_BED, false);
                    case "black" -> block.setType(Material.BLACK_BED, false);
                }

                //Set part and facing.
                BlockData bd = block.getBlockData();

                if (!(bd instanceof Bed bed)) {
                    throw new BlockNotFoundException("Found " + bd.getMaterial().name() + " expected " + object.get("block") + " at " + l.getX() + "," + l.getY() + "," + l.getZ());
                }

                if (properties.get("part").equals("head")) {
                    bed.setPart(Bed.Part.HEAD);
                } else {
                    bed.setPart(Bed.Part.FOOT);
                }

                switch ((String) properties.get("facing")) {
                    case "north" -> bed.setFacing(BlockFace.NORTH);
                    case "east" -> bed.setFacing(BlockFace.EAST);
                    case "south" -> bed.setFacing(BlockFace.SOUTH);
                    case "west" -> bed.setFacing(BlockFace.WEST);
                }

                block.setBlockData(bed, false);

            }

            case "minecraft:white_banner" -> {

                //Get properties.
                JSONObject properties = (JSONObject) object.get("properties");

                //Set banner base colour.
                switch ((String) properties.get("colour")) {
                    case "white" -> block.setType(Material.WHITE_BANNER, false);
                    case "orange" -> block.setType(Material.ORANGE_BANNER, false);
                    case "magenta" -> block.setType(Material.MAGENTA_BANNER, false);
                    case "light_blue" -> block.setType(Material.LIGHT_BLUE_BANNER, false);
                    case "yellow" -> block.setType(Material.YELLOW_BANNER, false);
                    case "lime" -> block.setType(Material.LIME_BANNER, false);
                    case "pink" -> block.setType(Material.PINK_BANNER, false);
                    case "gray" -> block.setType(Material.GRAY_BANNER, false);
                    case "light_gray" -> block.setType(Material.LIGHT_GRAY_BANNER, false);
                    case "cyan" -> block.setType(Material.CYAN_BANNER, false);
                    case "purple" -> block.setType(Material.PURPLE_BANNER, false);
                    case "blue" -> block.setType(Material.BLUE_BANNER, false);
                    case "brown" -> block.setType(Material.BROWN_BANNER, false);
                    case "green" -> block.setType(Material.GREEN_BANNER, false);
                    case "red" -> block.setType(Material.RED_BANNER, false);
                    case "black" -> block.setType(Material.BLACK_BANNER, false);
                }

                //Set rotation.
                if (!(block.getBlockData() instanceof Rotatable rot)) {
                    throw new BlockNotFoundException("Found " + block.getType().name() + " expected " + object.get("block") + " at " + l.getX() + "," + l.getY() + "," + l.getZ());
                }

                setRotation(rot, Byte.parseByte((String) properties.get("rotation")));
                block.setBlockData(rot, false);

                //Set patterns
                setBannerPatterns(block, (JSONArray) properties.get("patterns"));

            }

            case "minecraft:white_wall_banner" -> {

                //Get properties.
                JSONObject properties = (JSONObject) object.get("properties");

                //Set banner base colour.
                if (block.getType() == Material.WHITE_WALL_BANNER) {

                    switch ((String) properties.get("colour")) {
                        case "white" -> block.setType(Material.WHITE_WALL_BANNER, false);
                        case "orange" -> block.setType(Material.ORANGE_WALL_BANNER, false);
                        case "magenta" -> block.setType(Material.MAGENTA_WALL_BANNER, false);
                        case "light_blue" -> block.setType(Material.LIGHT_BLUE_WALL_BANNER, false);
                        case "yellow" -> block.setType(Material.YELLOW_WALL_BANNER, false);
                        case "lime" -> block.setType(Material.LIME_WALL_BANNER, false);
                        case "pink" -> block.setType(Material.PINK_WALL_BANNER, false);
                        case "gray" -> block.setType(Material.GRAY_WALL_BANNER, false);
                        case "light_gray" -> block.setType(Material.LIGHT_GRAY_WALL_BANNER, false);
                        case "cyan" -> block.setType(Material.CYAN_WALL_BANNER, false);
                        case "purple" -> block.setType(Material.PURPLE_WALL_BANNER, false);
                        case "blue" -> block.setType(Material.BLUE_WALL_BANNER, false);
                        case "brown" -> block.setType(Material.BROWN_WALL_BANNER, false);
                        case "green" -> block.setType(Material.GREEN_WALL_BANNER, false);
                        case "red" -> block.setType(Material.RED_WALL_BANNER, false);
                        case "black" -> block.setType(Material.BLACK_WALL_BANNER, false);
                    }

                    //Set facing direction.
                    BlockData bd = block.getBlockData();

                    if (!(bd instanceof Directional direction)) {
                        throw new BlockNotFoundException("Found " + bd.getMaterial().name() + " expected " + object.get("block") + " at " + l.getX() + "," + l.getY() + "," + l.getZ());
                    }

                    switch ((String) properties.get("facing")) {
                        case "north" -> direction.setFacing(BlockFace.NORTH);
                        case "east" -> direction.setFacing(BlockFace.EAST);
                        case "south" -> direction.setFacing(BlockFace.SOUTH);
                        case "west" -> direction.setFacing(BlockFace.WEST);
                    }

                    block.setBlockData(direction, false);

                    //Set patterns
                    setBannerPatterns(block, (JSONArray) properties.get("patterns"));
                }
            }

            case "minecraft:flower_pot" -> {

                //Set flower pot type.
                JSONObject properties = (JSONObject) object.get("properties");
                switch ((String) properties.get("type")) {

                    case "potted_dandelion" -> block.setType(Material.POTTED_DANDELION, false);
                    case "potted_poppy" -> block.setType(Material.POTTED_POPPY, false);
                    case "potted_blue_orchid" -> block.setType(Material.POTTED_BLUE_ORCHID, false);
                    case "potted_allium" -> block.setType(Material.POTTED_ALLIUM, false);
                    case "potted_azure_bluet" -> block.setType(Material.POTTED_AZURE_BLUET, false);
                    case "potted_red_tulip" -> block.setType(Material.POTTED_RED_TULIP, false);
                    case "potted_orange_tulip" -> block.setType(Material.POTTED_ORANGE_TULIP, false);
                    case "potted_white_tulip" -> block.setType(Material.POTTED_WHITE_TULIP, false);
                    case "potted_pink_tulip" -> block.setType(Material.POTTED_PINK_TULIP, false);
                    case "potted_oxeye_daisy" -> block.setType(Material.POTTED_OXEYE_DAISY, false);
                    case "potted_oak_sapling" -> block.setType(Material.POTTED_OAK_SAPLING, false);
                    case "potted_spruce_sapling" -> block.setType(Material.POTTED_SPRUCE_SAPLING, false);
                    case "potted_birch_sapling" -> block.setType(Material.POTTED_BIRCH_SAPLING, false);
                    case "potted_jungle_sapling" -> block.setType(Material.POTTED_JUNGLE_SAPLING, false);
                    case "potted_acacia_sapling" -> block.setType(Material.POTTED_ACACIA_SAPLING, false);
                    case "potted_dark_oak_sapling" -> block.setType(Material.POTTED_DARK_OAK_SAPLING, false);
                    case "potted_red_mushroom" -> block.setType(Material.POTTED_RED_MUSHROOM, false);
                    case "potted_brown_mushroom" -> block.setType(Material.POTTED_BROWN_MUSHROOM, false);
                    case "potted_fern" -> block.setType(Material.POTTED_FERN, false);
                    case "potted_dead_bush" -> block.setType(Material.POTTED_DEAD_BUSH, false);
                    case "potted_cactus" -> block.setType(Material.POTTED_CACTUS, false);
                    default -> block.setType(Material.FLOWER_POT, false);

                }
            }

            case "minecraft:skeleton_skull" -> {

                JSONObject properties = (JSONObject) object.get("properties");

                //Set skull types and if playerhead set texture.
                String type = (String) properties.get("type");
                String facing = (String) properties.get("facing");

                if (facing.equals("floor")) {

                    //Skull
                    //Set material.
                    switch (type) {

                        case "skeleton_skull" -> block.setType(Material.SKELETON_SKULL, false);
                        case "wither_skeleton_skull" -> block.setType(Material.WITHER_SKELETON_SKULL, false);
                        case "zombie_head" -> block.setType(Material.ZOMBIE_HEAD, false);
                        case "player_head" -> block.setType(Material.PLAYER_HEAD, false);
                        case "creeper_head" -> block.setType(Material.CREEPER_HEAD, false);
                        case "dragon_head" -> block.setType(Material.DRAGON_HEAD, false);

                    }

                    if (!(block.getBlockData() instanceof Rotatable rot)) {
                        throw new BlockNotFoundException("Found " + block.getType().name() + " expected " + object.get("block") + " at " + l.getX() + "," + l.getY() + "," + l.getZ());
                    }


                    Byte rotation = Utils.readByte(properties, "rotation");
                    if(rotation != null)
                        setRotation(rot, rotation);

                    block.setBlockData(rot, false);

                } else if (facing.equals("north") || facing.equals("west") || facing.equals("south") || facing.equals("east")) {

                    //Wall Skull
                    //Set material.
                    switch (type) {

                        case "skeleton_skull" -> block.setType(Material.SKELETON_WALL_SKULL, false);
                        case "wither_skeleton_skull" -> block.setType(Material.WITHER_SKELETON_WALL_SKULL, false);
                        case "zombie_head" -> block.setType(Material.ZOMBIE_WALL_HEAD, false);
                        case "player_head" -> block.setType(Material.PLAYER_WALL_HEAD, false);
                        case "creeper_head" -> block.setType(Material.CREEPER_WALL_HEAD, false);
                        case "dragon_head" -> block.setType(Material.DRAGON_WALL_HEAD, false);

                    }

                    if (!(block.getBlockData() instanceof Directional dir)) {
                        throw new BlockNotFoundException("Found " + block.getType().name() + " expected " + object.get("block") + " at " + l.getX() + "," + l.getY() + "," + l.getZ());
                    }

                    switch ((String) properties.get("facing")) {

                        case "north" -> dir.setFacing(BlockFace.NORTH);
                        case "south" -> dir.setFacing(BlockFace.SOUTH);
                        case "west" -> dir.setFacing(BlockFace.WEST);
                        case "east" -> dir.setFacing(BlockFace.EAST);

                    }

                    block.setBlockData(dir, false);


                } else {
                    instance.getLogger().info("Not a skull at " + l.getX() + ", " + l.getY() + ", " + l.getZ());
                    return;
                }

                //If type is a player head, set the texture, ect.
                if (type.equals("player_head")) {

                    //If texture is set or profile name is set
                    if (properties.get("texture") != null || properties.containsKey("profileName")) {

                        if (!(block.getState() instanceof Skull skull)) {
                            throw new BlockNotFoundException("Found " + block.getType().name() + " expected " + object.get("block") + " at " + l.getX() + "," + l.getY() + "," + l.getZ());
                        }

                        skull.setType(block.getType());

                        if(properties.containsKey("texture")) {
                            String id = (String) properties.get("id");

                            UUID uuid;
                            if (id == null) {
                                uuid = UUID.randomUUID();
                            } else {
                                uuid = UUID.fromString(id);
                            }
                            try {
                                PlayerProfile profile = Bukkit.createProfile(uuid);
                                profile.getProperties().add(new ProfileProperty("textures", (String) properties.get("texture")));
                                skull.setPlayerProfile(profile);
                            }catch (Exception ex) {
                                instance.getLogger().warning("Error while setting Skull at " + l + ": " + ex.getMessage());
                            }
                        }else {
                            Player player = Bukkit.getOfflinePlayer((String) properties.get("profileName")).getPlayer();
                            if(player != null) {
                                PlayerProfile profile = player.getPlayerProfile();
                                skull.setPlayerProfile(profile);
                            }
                        }

                        skull.update(false, false); // so that the result can be seen

                    }
                }
            }

            case "minecraft:note_block" -> {

                JSONObject properties = (JSONObject) object.get("properties");

                BlockData blockData = world.getBlockData(l);

                if (!(blockData instanceof NoteBlock noteBlock)) {
                    throw new BlockNotFoundException("Found " + blockData.getMaterial().name() + " expected " + object.get("block") + " at " + l.getX() + "," + l.getY() + "," + l.getZ());
                }

                //Set the note from config.
                Integer note = Utils.readInteger(properties, "note");
                if(note != null)
                    noteBlock.setNote(new Note(note));

                //Set the instrument.
                //Get the block below and check which instrument it should be.
                Location lBelow = new Location(world, l.getX(), l.getY() - 1, l.getZ());
                noteBlock.setInstrument(getInstrument(world.getBlockAt(lBelow).getType()));

                block.setBlockData(noteBlock, false);

            }

            case "minecraft:tripwire" -> {

                BlockData blockData = world.getBlockData(l);

                if (!(blockData instanceof Tripwire tripwire)) {
                    throw new BlockNotFoundException("Found " + blockData.getMaterial().name() + " expected " + object.get("block") + " at " + l.getX() + "," + l.getY() + "," + l.getZ());
                }

                //Check if adjacent blocks are also tripwire, if true, connect them.
                //North (Negative Z)
                Location lZMin = new Location(world, l.getX(), l.getY(), l.getZ() - 1);
                if (world.getBlockData(lZMin) instanceof Tripwire) {
                    tripwire.setFace(BlockFace.NORTH, true);
                }

                //East (Positive X)
                Location lXMax = new Location(world, l.getX() + 1, l.getY(), l.getZ());
                if (world.getBlockData(lXMax) instanceof Tripwire) {
                    tripwire.setFace(BlockFace.EAST, true);
                }

                //South (Positive Z)
                Location lZMax = new Location(world, l.getX(), l.getY(), l.getZ() + 1);
                if (world.getBlockData(lZMax) instanceof Tripwire) {
                    tripwire.setFace(BlockFace.SOUTH, true);
                }

                //West (Negative X)
                Location lXMin = new Location(world, l.getX() - 1, l.getY(), l.getZ());
                if (world.getBlockData(lXMin) instanceof Tripwire) {
                    tripwire.setFace(BlockFace.WEST, true);
                }
            }

            case "minecraft:vine" -> {

                //Get block above.
                Location lUp = new Location(world, l.getX(), l.getY() + 1, l.getZ());
                if (canConnectAbove(world.getBlockData(lUp))) {

                    //Check if block above, if true, make it a vine.
                    BlockData blockData = world.getBlockData(l);

                    if (!(blockData instanceof MultipleFacing facing) || !(blockData.getMaterial() == Material.VINE)) {
                        //Don't throw the exception, just skip it.
                        throw new BlockNotFoundException("Found " + blockData.getMaterial().name() + " expected " + object.get("block") + " at " + l.getX() + "," + l.getY() + "," + l.getZ());
                    }

                    facing.setFace(BlockFace.UP, true);
                    block.setBlockData(facing, false);

                }
            }

            case "minecraft:iron_door", "minecraft:oak_door", "minecraft:spruce_door", "minecraft:birch_door", "minecraft:jungle_door", "minecraft:acacia_door", "minecraft:dark_oak_door" -> {

                //Get door instance.
                BlockData blockData = world.getBlockData(l);

                if (!(blockData instanceof Door door)) {
                    throw new BlockNotFoundException("Found " + blockData.getMaterial().name() + " expected " + object.get("block") + " at " + l.getX() + "," + l.getY() + "," + l.getZ());
                }

                //Get half.
                if (door.getHalf() == Bisected.Half.TOP) {

                    Location lBelow = new Location(world, l.getX(), l.getY() - 1, l.getZ());

                    //Check if block below is a door of the same material, if true get it's properties.
                    if (world.getBlockData(lBelow) instanceof Door bDoor && world.getType(lBelow) == door.getMaterial()) {

                        //Get facing direction from below door and open/closed status.
                        door.setFacing(bDoor.getFacing());
                        door.setOpen(bDoor.isOpen());

                        block.setBlockData(door, false);

                    }

                } else {

                    Location lAbove = new Location(world, l.getX(), l.getY() + 1, l.getZ());

                    //Check if block below is a door of the same material and different half, if true get it's properties.
                    if (world.getBlockData(lAbove) instanceof Door aDoor && world.getType(lAbove) == door.getMaterial()) {

                        if(door.getHalf() != aDoor.getHalf()) {

                            //Get hinge and powered status.
                            door.setHinge(aDoor.getHinge());
                            door.setPowered(aDoor.isPowered());

                            block.setBlockData(door, false);
                        }

                    }
                }
            }
        }

    }

    private void setTopFlower(Material mat, Block block) {
        BlockData newBD = mat.createBlockData();
        block.setBlockData(newBD, false);
        Bisected bisected = (Bisected) newBD;
        bisected.setHalf(Bisected.Half.TOP);
        block.setBlockData(bisected, false);
    }

    //Check if the material is of a fence.
    private boolean isFence(Material mat) {
        return (mat == Material.OAK_FENCE || mat == Material.SPRUCE_FENCE || mat == Material.BIRCH_FENCE || mat == Material.JUNGLE_FENCE || mat == Material.ACACIA_FENCE || mat == Material.DARK_OAK_FENCE || mat == Material.NETHER_BRICK_FENCE);
    }

    //Check if the material is of a fence or wall type.
    private boolean isFenceOrWall(BlockData bd) {
        return (bd instanceof Fence || bd instanceof GlassPane || bd instanceof Wall);
    }

    //Check if the material is of a fence gate.
    private boolean isFenceGate(Material mat) {
        return (mat == Material.OAK_FENCE_GATE || mat == Material.SPRUCE_FENCE_GATE || mat == Material.BIRCH_FENCE_GATE || mat == Material.JUNGLE_FENCE_GATE || mat == Material.ACACIA_FENCE_GATE || mat == Material.DARK_OAK_FENCE_GATE);
    }

    //Set the connections for walls with low or high.
    private void setConnections(Location l, BlockData block, Wall wall, Wall.Height height) {
        //North (Negative Z)
        Location lZMin = new Location(world, l.getX(), l.getY(), l.getZ() - 1);
        if (canConnect(block.getMaterial(), lZMin, BlockFace.NORTH)) {
            wall.setHeight(BlockFace.NORTH, height);
        }

        //East (Positive X)
        Location lXMax = new Location(world, l.getX() + 1, l.getY(), l.getZ());
        if (canConnect(block.getMaterial(), lXMax, BlockFace.NORTH)) {
            wall.setHeight(BlockFace.EAST, height);
        }

        //South (Positive Z)
        Location lZMax = new Location(world, l.getX(), l.getY(), l.getZ() + 1);
        if (canConnect(block.getMaterial(), lZMax, BlockFace.NORTH)) {
            wall.setHeight(BlockFace.SOUTH, height);
        }

        //West (Negative X)
        Location lXMin = new Location(world, l.getX() - 1, l.getY(), l.getZ());
        if (canConnect(block.getMaterial(), lXMin, BlockFace.NORTH)) {
            wall.setHeight(BlockFace.WEST, height);
        }

    }

    private boolean canConnect(Material mat, Location l, BlockFace face) {

        BlockData bd = world.getBlockData(l);

        //First check if the block is solid, return if true.
        switch (bd.getMaterial()) {
            case STONE, GRANITE, POLISHED_GRANITE, DIORITE, POLISHED_DIORITE, ANDESITE, POLISHED_ANDESITE,
                 GRASS_BLOCK, DIRT, COARSE_DIRT, PODZOL, COBBLESTONE,
                 OAK_PLANKS, SPRUCE_PLANKS, BIRCH_PLANKS, JUNGLE_PLANKS, ACACIA_PLANKS, DARK_OAK_PLANKS,
                 BEDROCK, SAND, RED_SAND, GRAVEL,
                 COAL_ORE, IRON_ORE, GOLD_ORE, REDSTONE_ORE, EMERALD_ORE, LAPIS_ORE, DIAMOND_ORE, NETHER_QUARTZ_ORE,
                 COAL_BLOCK, IRON_BLOCK, GOLD_BLOCK, DIAMOND_BLOCK,
                 OAK_LOG, SPRUCE_LOG, BIRCH_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG,
                 OAK_WOOD, SPRUCE_WOOD, BIRCH_WOOD, JUNGLE_WOOD, ACACIA_WOOD, DARK_OAK_WOOD,
                 SPONGE, WET_SPONGE, GLASS, LAPIS_BLOCK, SANDSTONE, CHISELED_SANDSTONE, CUT_SANDSTONE,
                 WHITE_WOOL, ORANGE_WOOL, MAGENTA_WOOL, LIGHT_BLUE_WOOL, YELLOW_WOOL, LIME_WOOL, PINK_WOOL, GRAY_WOOL,
                 LIGHT_GRAY_WOOL, CYAN_WOOL, PURPLE_WOOL, BLUE_WOOL, BROWN_WOOL, GREEN_WOOL, RED_WOOL, BLACK_WOOL,
                 SMOOTH_QUARTZ, SMOOTH_RED_SANDSTONE, SMOOTH_SANDSTONE, SMOOTH_STONE, BRICKS, BOOKSHELF,
                 MOSSY_COBBLESTONE, OBSIDIAN, PURPUR_BLOCK, PURPUR_PILLAR, CRAFTING_TABLE, FURNACE,
                 ICE, SNOW_BLOCK, CLAY, JUKEBOX, NETHERRACK, SOUL_SAND, GLOWSTONE,
                 INFESTED_STONE, INFESTED_COBBLESTONE, INFESTED_STONE_BRICKS, INFESTED_MOSSY_STONE_BRICKS, INFESTED_CRACKED_STONE_BRICKS, INFESTED_CHISELED_STONE_BRICKS,
                 STONE_BRICKS, MOSSY_STONE_BRICKS, CRACKED_STONE_BRICKS, CHISELED_STONE_BRICKS,
                 BROWN_MUSHROOM_BLOCK, RED_MUSHROOM_BLOCK, MUSHROOM_STEM, MYCELIUM, NETHER_BRICK, END_STONE, END_STONE_BRICKS,
                 EMERALD_BLOCK, BEACON, CHISELED_QUARTZ_BLOCK, QUARTZ_BLOCK, QUARTZ_PILLAR,
                 WHITE_TERRACOTTA, ORANGE_TERRACOTTA, MAGENTA_TERRACOTTA, LIGHT_BLUE_TERRACOTTA,
                 YELLOW_TERRACOTTA, LIME_TERRACOTTA, PINK_TERRACOTTA, GRAY_TERRACOTTA,
                 LIGHT_GRAY_TERRACOTTA, CYAN_TERRACOTTA, PURPLE_TERRACOTTA, BLUE_TERRACOTTA,
                 BROWN_TERRACOTTA, GREEN_TERRACOTTA, RED_TERRACOTTA, BLACK_TERRACOTTA,
                 HAY_BLOCK, TERRACOTTA, PACKED_ICE, PRISMARINE, PRISMARINE_BRICKS, DARK_PRISMARINE, SEA_LANTERN,
                 RED_SANDSTONE, CHISELED_RED_SANDSTONE, CUT_RED_SANDSTONE, MAGMA_BLOCK, RED_NETHER_BRICKS, BONE_BLOCK,
                 WHITE_GLAZED_TERRACOTTA, ORANGE_GLAZED_TERRACOTTA, MAGENTA_GLAZED_TERRACOTTA, LIGHT_BLUE_GLAZED_TERRACOTTA,
                 YELLOW_GLAZED_TERRACOTTA, LIME_GLAZED_TERRACOTTA, PINK_GLAZED_TERRACOTTA, GRAY_GLAZED_TERRACOTTA,
                 LIGHT_GRAY_GLAZED_TERRACOTTA, CYAN_GLAZED_TERRACOTTA, PURPLE_GLAZED_TERRACOTTA, BLUE_GLAZED_TERRACOTTA,
                 BROWN_GLAZED_TERRACOTTA, GREEN_GLAZED_TERRACOTTA, RED_GLAZED_TERRACOTTA, BLACK_GLAZED_TERRACOTTA,
                 WHITE_CONCRETE, ORANGE_CONCRETE, MAGENTA_CONCRETE, LIGHT_BLUE_CONCRETE, YELLOW_CONCRETE, LIME_CONCRETE, PINK_CONCRETE, GRAY_CONCRETE,
                 LIGHT_GRAY_CONCRETE, CYAN_CONCRETE, PURPLE_CONCRETE, BLUE_CONCRETE, BROWN_CONCRETE, GREEN_CONCRETE, RED_CONCRETE, BLACK_CONCRETE,
                 WHITE_CONCRETE_POWDER, ORANGE_CONCRETE_POWDER, MAGENTA_CONCRETE_POWDER, LIGHT_BLUE_CONCRETE_POWDER,
                 YELLOW_CONCRETE_POWDER, LIME_CONCRETE_POWDER, PINK_CONCRETE_POWDER, GRAY_CONCRETE_POWDER,
                 LIGHT_GRAY_CONCRETE_POWDER, CYAN_CONCRETE_POWDER, PURPLE_CONCRETE_POWDER, BLUE_CONCRETE_POWDER,
                 BROWN_CONCRETE_POWDER, GREEN_CONCRETE_POWDER, RED_CONCRETE_POWDER, BLACK_CONCRETE_POWDER,
                 REDSTONE_BLOCK, SLIME_BLOCK, PISTON, STICKY_PISTON, OBSERVER, DISPENSER, DROPPER,
                 TNT, REDSTONE_LAMP, NOTE_BLOCK,
                 WHITE_STAINED_GLASS, ORANGE_STAINED_GLASS, MAGENTA_STAINED_GLASS, LIGHT_BLUE_STAINED_GLASS, YELLOW_STAINED_GLASS, LIME_STAINED_GLASS, PINK_STAINED_GLASS,
                 GRAY_STAINED_GLASS, LIGHT_GRAY_STAINED_GLASS, CYAN_STAINED_GLASS, PURPLE_STAINED_GLASS, BLUE_STAINED_GLASS, BROWN_STAINED_GLASS, GREEN_STAINED_GLASS,
                 RED_STAINED_GLASS, BLACK_STAINED_GLASS -> {
                return true;
            }
        }

        //Stair case
        if (bd instanceof Stairs) {

            Stairs stair = getStair(l);

            switch (face) {
                case NORTH -> {
                    if (stair.getFacing() == BlockFace.SOUTH) {
                        return true;
                    } else if (stair.getFacing() == BlockFace.WEST && stair.getShape() == Stairs.Shape.INNER_LEFT) {
                        return true;
                    } else
                        return (stair.getFacing() == BlockFace.EAST && stair.getShape() == Stairs.Shape.INNER_RIGHT);
                }
                case WEST -> {
                    if (stair.getFacing() == BlockFace.EAST) {
                        return true;
                    } else if (stair.getFacing() == BlockFace.SOUTH && stair.getShape() == Stairs.Shape.INNER_LEFT) {
                        return true;
                    } else
                        return (stair.getFacing() == BlockFace.NORTH && stair.getShape() == Stairs.Shape.INNER_RIGHT);
                }
                case SOUTH -> {
                    if (stair.getFacing() == BlockFace.NORTH) {
                        return true;
                    } else if (stair.getFacing() == BlockFace.EAST && stair.getShape() == Stairs.Shape.INNER_LEFT) {
                        return true;
                    } else
                        return (stair.getFacing() == BlockFace.WEST && stair.getShape() == Stairs.Shape.INNER_RIGHT);
                }
                case EAST -> {
                    if (stair.getFacing() == BlockFace.WEST) {
                        return true;
                    } else if (stair.getFacing() == BlockFace.NORTH && stair.getShape() == Stairs.Shape.INNER_LEFT) {
                        return true;
                    } else
                        return (stair.getFacing() == BlockFace.SOUTH && stair.getShape() == Stairs.Shape.INNER_RIGHT);
                }
            }
        }

        //Slab Case
        if (bd instanceof Slab slab) {
            return (slab.getType() == Slab.Type.DOUBLE);
        }

        //Fences
        if (bd.getMaterial() == Material.NETHER_BRICK_FENCE) {
            return (bd.getMaterial() == mat);
        } else if (bd.getMaterial() == Material.OAK_FENCE || bd.getMaterial() == Material.SPRUCE_FENCE || bd.getMaterial() == Material.BIRCH_FENCE || bd.getMaterial() == Material.JUNGLE_FENCE || bd.getMaterial() == Material.ACACIA_FENCE || bd.getMaterial() == Material.DARK_OAK_FENCE) {
            return (mat == Material.OAK_FENCE || mat == Material.SPRUCE_FENCE || mat == Material.BIRCH_FENCE || mat == Material.JUNGLE_FENCE || mat == Material.ACACIA_FENCE || mat == Material.DARK_OAK_FENCE);
        } else if (bd.getMaterial() == Material.GLASS_PANE || bd.getMaterial() == Material.IRON_BARS || bd.getMaterial() == Material.COBBLESTONE_WALL || bd.getMaterial() == Material.MOSSY_COBBLESTONE_WALL || (bd instanceof GlassPane)) {
            return (mat == Material.GLASS_PANE || mat == Material.IRON_BARS || mat == Material.COBBLESTONE_WALL || mat == Material.MOSSY_COBBLESTONE_WALL ||
                    mat == Material.WHITE_STAINED_GLASS_PANE || mat == Material.ORANGE_STAINED_GLASS_PANE || mat == Material.MAGENTA_STAINED_GLASS_PANE ||
                    mat == Material.LIGHT_BLUE_STAINED_GLASS_PANE || mat == Material.YELLOW_STAINED_GLASS_PANE || mat == Material.LIME_STAINED_GLASS_PANE ||
                    mat == Material.PINK_STAINED_GLASS_PANE || mat == Material.GRAY_STAINED_GLASS_PANE || mat == Material.LIGHT_GRAY_STAINED_GLASS_PANE ||
                    mat == Material.CYAN_STAINED_GLASS_PANE || mat == Material.PURPLE_STAINED_GLASS_PANE || mat == Material.BLUE_STAINED_GLASS_PANE ||
                    mat == Material.BROWN_STAINED_GLASS_PANE || mat == Material.GREEN_STAINED_GLASS_PANE || mat == Material.RED_STAINED_GLASS_PANE || mat == Material.BLACK_STAINED_GLASS_PANE);
        }

        //Snow
        if (bd.getMaterial() == Material.SNOW) {
            Snow snow = (Snow) bd;
            return (snow.getLayers() == 8 && face != BlockFace.UP);
        }

        //Trapdoors
        if (bd instanceof TrapDoor trapDoor) {
            return (trapDoor.getFacing() == face && trapDoor.isOpen());
        }

        //Fence gates
        if (bd instanceof Gate gate) {
            if (isFence(mat)) {
                if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
                    return (gate.getFacing() == BlockFace.WEST || gate.getFacing() == BlockFace.EAST);
                } else if (face == BlockFace.EAST || face == BlockFace.WEST) {
                    return (gate.getFacing() == BlockFace.NORTH || gate.getFacing() == BlockFace.SOUTH);
                }
            }
            return false;
        }

        //Doors
        if (bd instanceof Door door) {
            if (door.getFacing() == face && !door.isOpen()) {
                return true;
            }

            if (door.isOpen()) {
                switch (face) {
                    case NORTH -> {
                        if (door.getHinge() == Door.Hinge.LEFT) {
                            return (door.getFacing() == BlockFace.WEST);
                        } else {
                            return (door.getFacing() == BlockFace.EAST);
                        }
                    }
                    case WEST -> {
                        if (door.getHinge() == Door.Hinge.LEFT) {
                            return (door.getFacing() == BlockFace.SOUTH);
                        } else {
                            return (door.getFacing() == BlockFace.NORTH);
                        }
                    }
                    case SOUTH -> {
                        if (door.getHinge() == Door.Hinge.LEFT) {
                            return (door.getFacing() == BlockFace.EAST);
                        } else {
                            return (door.getFacing() == BlockFace.WEST);
                        }
                    }
                    case EAST -> {
                        if (door.getHinge() == Door.Hinge.LEFT) {
                            return (door.getFacing() == BlockFace.NORTH);
                        } else {
                            return (door.getFacing() == BlockFace.SOUTH);
                        }
                    }
                }
            }
        }

        //All other cases.
        return false;
    }

    //Check if the wall can connect to this block above.
    private boolean canConnectAbove(BlockData bd) {

        //First check if the block is solid, return if true.
        switch (bd.getMaterial()) {
            case STONE, GRANITE, POLISHED_GRANITE, DIORITE, POLISHED_DIORITE, ANDESITE, POLISHED_ANDESITE,
                 GRASS_BLOCK, DIRT, COARSE_DIRT, PODZOL, COBBLESTONE,
                 OAK_PLANKS, SPRUCE_PLANKS, BIRCH_PLANKS, JUNGLE_PLANKS, ACACIA_PLANKS, DARK_OAK_PLANKS,
                 BEDROCK, SAND, RED_SAND, GRAVEL,
                 COAL_ORE, IRON_ORE, GOLD_ORE, REDSTONE_ORE, EMERALD_ORE, LAPIS_ORE, DIAMOND_ORE, NETHER_QUARTZ_ORE,
                 COAL_BLOCK, IRON_BLOCK, GOLD_BLOCK, DIAMOND_BLOCK,
                 OAK_LOG, SPRUCE_LOG, BIRCH_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG,
                 OAK_WOOD, SPRUCE_WOOD, BIRCH_WOOD, JUNGLE_WOOD, ACACIA_WOOD, DARK_OAK_WOOD,
                 SPONGE, WET_SPONGE, GLASS, LAPIS_BLOCK, SANDSTONE, CHISELED_SANDSTONE, CUT_SANDSTONE,
                 WHITE_WOOL, ORANGE_WOOL, MAGENTA_WOOL, LIGHT_BLUE_WOOL, YELLOW_WOOL, LIME_WOOL, PINK_WOOL, GRAY_WOOL,
                 LIGHT_GRAY_WOOL, CYAN_WOOL, PURPLE_WOOL, BLUE_WOOL, BROWN_WOOL, GREEN_WOOL, RED_WOOL, BLACK_WOOL,
                 SMOOTH_QUARTZ, SMOOTH_RED_SANDSTONE, SMOOTH_SANDSTONE, SMOOTH_STONE, BRICKS, BOOKSHELF,
                 MOSSY_COBBLESTONE, OBSIDIAN, PURPUR_BLOCK, PURPUR_PILLAR, CRAFTING_TABLE, FURNACE,
                 ICE, SNOW_BLOCK, CLAY, JUKEBOX, NETHERRACK, SOUL_SAND, GLOWSTONE,
                 INFESTED_STONE, INFESTED_COBBLESTONE, INFESTED_STONE_BRICKS, INFESTED_MOSSY_STONE_BRICKS, INFESTED_CRACKED_STONE_BRICKS, INFESTED_CHISELED_STONE_BRICKS,
                 STONE_BRICKS, MOSSY_STONE_BRICKS, CRACKED_STONE_BRICKS, CHISELED_STONE_BRICKS,
                 BROWN_MUSHROOM_BLOCK, RED_MUSHROOM_BLOCK, MUSHROOM_STEM, MYCELIUM, NETHER_BRICK, END_STONE, END_STONE_BRICKS,
                 EMERALD_BLOCK, BEACON, CHISELED_QUARTZ_BLOCK, QUARTZ_BLOCK, QUARTZ_PILLAR,
                 WHITE_TERRACOTTA, ORANGE_TERRACOTTA, MAGENTA_TERRACOTTA, LIGHT_BLUE_TERRACOTTA,
                 YELLOW_TERRACOTTA, LIME_TERRACOTTA, PINK_TERRACOTTA, GRAY_TERRACOTTA,
                 LIGHT_GRAY_TERRACOTTA, CYAN_TERRACOTTA, PURPLE_TERRACOTTA, BLUE_TERRACOTTA,
                 BROWN_TERRACOTTA, GREEN_TERRACOTTA, RED_TERRACOTTA, BLACK_TERRACOTTA,
                 HAY_BLOCK, TERRACOTTA, PACKED_ICE, PRISMARINE, PRISMARINE_BRICKS, DARK_PRISMARINE, SEA_LANTERN,
                 RED_SANDSTONE, CHISELED_RED_SANDSTONE, CUT_RED_SANDSTONE, MAGMA_BLOCK, RED_NETHER_BRICKS, BONE_BLOCK,
                 WHITE_GLAZED_TERRACOTTA, ORANGE_GLAZED_TERRACOTTA, MAGENTA_GLAZED_TERRACOTTA, LIGHT_BLUE_GLAZED_TERRACOTTA,
                 YELLOW_GLAZED_TERRACOTTA, LIME_GLAZED_TERRACOTTA, PINK_GLAZED_TERRACOTTA, GRAY_GLAZED_TERRACOTTA,
                 LIGHT_GRAY_GLAZED_TERRACOTTA, CYAN_GLAZED_TERRACOTTA, PURPLE_GLAZED_TERRACOTTA, BLUE_GLAZED_TERRACOTTA,
                 BROWN_GLAZED_TERRACOTTA, GREEN_GLAZED_TERRACOTTA, RED_GLAZED_TERRACOTTA, BLACK_GLAZED_TERRACOTTA,
                 WHITE_CONCRETE, ORANGE_CONCRETE, MAGENTA_CONCRETE, LIGHT_BLUE_CONCRETE, YELLOW_CONCRETE, LIME_CONCRETE, PINK_CONCRETE, GRAY_CONCRETE,
                 LIGHT_GRAY_CONCRETE, CYAN_CONCRETE, PURPLE_CONCRETE, BLUE_CONCRETE, BROWN_CONCRETE, GREEN_CONCRETE, RED_CONCRETE, BLACK_CONCRETE,
                 WHITE_CONCRETE_POWDER, ORANGE_CONCRETE_POWDER, MAGENTA_CONCRETE_POWDER, LIGHT_BLUE_CONCRETE_POWDER,
                 YELLOW_CONCRETE_POWDER, LIME_CONCRETE_POWDER, PINK_CONCRETE_POWDER, GRAY_CONCRETE_POWDER,
                 LIGHT_GRAY_CONCRETE_POWDER, CYAN_CONCRETE_POWDER, PURPLE_CONCRETE_POWDER, BLUE_CONCRETE_POWDER,
                 BROWN_CONCRETE_POWDER, GREEN_CONCRETE_POWDER, RED_CONCRETE_POWDER, BLACK_CONCRETE_POWDER,
                 REDSTONE_BLOCK, SLIME_BLOCK, PISTON, STICKY_PISTON, OBSERVER, DISPENSER, DROPPER,
                 TNT, REDSTONE_LAMP, NOTE_BLOCK,
                 WHITE_STAINED_GLASS, ORANGE_STAINED_GLASS, MAGENTA_STAINED_GLASS, LIGHT_BLUE_STAINED_GLASS, YELLOW_STAINED_GLASS, LIME_STAINED_GLASS, PINK_STAINED_GLASS,
                 GRAY_STAINED_GLASS, LIGHT_GRAY_STAINED_GLASS, CYAN_STAINED_GLASS, PURPLE_STAINED_GLASS, BLUE_STAINED_GLASS, BROWN_STAINED_GLASS, GREEN_STAINED_GLASS,
                 RED_STAINED_GLASS, BLACK_STAINED_GLASS,

                 OAK_LEAVES, SPRUCE_LEAVES, BIRCH_LEAVES, JUNGLE_LEAVES, ACACIA_LEAVES, DARK_OAK_LEAVES,
                 FARMLAND, DIRT_PATH, CARVED_PUMPKIN, JACK_O_LANTERN, MELON, ENCHANTING_TABLE, END_PORTAL_FRAME,
                 WHITE_CARPET, ORANGE_CARPET, MAGENTA_CARPET, LIGHT_BLUE_CARPET, YELLOW_CARPET, LIME_CARPET,
                 PINK_CARPET, GRAY_CARPET, LIGHT_GRAY_CARPET, CYAN_CARPET, PURPLE_CARPET, BLUE_CARPET,
                 BROWN_CARPET, GREEN_CARPET, RED_CARPET, BLACK_CARPET,
                 SHULKER_BOX, WHITE_SHULKER_BOX, ORANGE_SHULKER_BOX, MAGENTA_SHULKER_BOX, LIGHT_BLUE_SHULKER_BOX,
                 YELLOW_SHULKER_BOX, LIME_SHULKER_BOX, PINK_SHULKER_BOX, GRAY_SHULKER_BOX, LIGHT_GRAY_SHULKER_BOX,
                 CYAN_SHULKER_BOX, PURPLE_SHULKER_BOX, BLUE_SHULKER_BOX, BROWN_SHULKER_BOX, GREEN_SHULKER_BOX,
                 RED_SHULKER_BOX, BLACK_SHULKER_BOX, DAYLIGHT_DETECTOR -> {
                return true;
            }
        }

        //Stair case
        if (bd instanceof Stairs stair) {

            return (stair.getHalf() == Bisected.Half.BOTTOM);
        }

        //Slab Case
        if (bd instanceof Slab slab) {
            return (slab.getType() == Slab.Type.DOUBLE || slab.getType() == Slab.Type.BOTTOM);
        }

        //Trapdoors
        if (bd instanceof TrapDoor trapDoor) {
            return (trapDoor.getHalf() == Bisected.Half.BOTTOM && !trapDoor.isOpen());
        }

        //All other cases.
        return false;
    }

    private Stairs getStair(Location l) {
        /*

                Stair direction = direction 0
                Stair facing away on left = direction 1
                Stair facing away behind = direction 2
                Stair facing away on right = direction 3

                Order = Below-Left-Above-Right

                Formations:
                    Straight has priority
                    Stair always prioritises itself
                    Small corner over large corner

                Each of the 4 directions has:
                    Straight (Block is stair and attaches with a straight connection)
                    CornerI (Block is stair and attaches with an inner corner)
                    CornerO (Block is stair and attaches with an outer corner)
                    None (Block is stair without connections or a non-stair)

                Calculation:

                    1. If contains Straight
                        - Check for 2nd Straight ->
                            return Straight
                        - Check for CornerI ->
                            if CornerI[facing] != Straight[direction] ->
                                return CornerI in Straight[direction]
                        - Check for CornerO ->
                            if CornerO[facing] == Straight[direction] ->
                                return CornerO in !Straight[direction]
                        - Else return Straight

                    2. If contains CornerI return CornerI in !CornerI[direction]
                    3. If contains CornerO return CornerO in CornerO[direction]

                 */

        //Get main stair.
        BlockData bd = world.getBlockData(l);
        Stairs stair = (Stairs) bd;
        StairData[] stairs = new StairData[4];
        StairData mainStair = new StairData(stair, l);

        //Get 4 adjacent stairs, if they are stairs.
        Location lXMin = new Location(world, l.getX() - 1, l.getY(), l.getZ());
        if (world.getBlockData(lXMin) instanceof Stairs xMin) {
            if (mainStair.half == xMin.getHalf()) {
                //Add it to the array at index 0.
                stairs[0] = new StairData(xMin, lXMin, mainStair);
            }
        }
        Location lXMax = new Location(world, l.getX() + 1, l.getY(), l.getZ());
        if (world.getBlockData(lXMax) instanceof Stairs xMax) {
            if (mainStair.half == xMax.getHalf()) {
                //Add it to the array at index 1.
                stairs[1] = new StairData(xMax, lXMax, mainStair);
            }
        }
        Location lZMin = new Location(world, l.getX(), l.getY(), l.getZ() - 1);
        if (world.getBlockData(lZMin) instanceof Stairs zMin) {
            if (mainStair.half == zMin.getHalf()) {
                //Add it to the array at index 2.
                stairs[2] = new StairData(zMin, lZMin, mainStair);
            }
        }
        Location lZMax = new Location(world, l.getX(), l.getY(), l.getZ() + 1);
        if (world.getBlockData(lZMax) instanceof Stairs zMax) {
            if (mainStair.half == zMax.getHalf()) {
                //Add it to the array at index 3.
                stairs[3] = new StairData(zMax, lZMax, mainStair);
            }
        }

        //Set the stair shape.
        stair.setShape(mainStair.getShape(stairs));

        return stair;
    }

    private Wall getWall(Location l) {

        //Check if the fence can connect to adjacent blocks.
        BlockData block = world.getBlockData(l);
        Wall wall = (Wall) block;

        //First check which directions are connected for the wall.
        //Only then check which should be tall/low.
        setConnections(l, block, wall, Wall.Height.LOW);

        //Get the blocks above
        //If the block above is a fence or wall more checks are needed.
        Location lAbove = new Location(world, l.getX(), (l.getY() + 1), l.getZ());
        BlockData bAbove = world.getBlockData(lAbove);

        if (isFenceOrWall(bAbove)) {

            //Check in which directions it is connected.
            //North (Negative Z)
            Location lZMin = new Location(world, lAbove.getX(), lAbove.getY(), lAbove.getZ() - 1);
            if (canConnect(block.getMaterial(), lZMin, BlockFace.NORTH)) {
                if (wall.getHeight(BlockFace.NORTH) == Wall.Height.LOW) {
                    wall.setHeight(BlockFace.NORTH, Wall.Height.TALL);
                }
            }

            //East (Positive X)
            Location lXMax = new Location(world, lAbove.getX() + 1, lAbove.getY(), lAbove.getZ());
            if (canConnect(block.getMaterial(), lXMax, BlockFace.EAST)) {
                if (wall.getHeight(BlockFace.EAST) == Wall.Height.LOW) {
                    wall.setHeight(BlockFace.EAST, Wall.Height.TALL);
                }
            }

            //South (Positive Z)
            Location lZMax = new Location(world, lAbove.getX(), lAbove.getY(), lAbove.getZ() + 1);
            if (canConnect(block.getMaterial(), lZMax, BlockFace.SOUTH)) {
                if (wall.getHeight(BlockFace.SOUTH) == Wall.Height.LOW) {
                    wall.setHeight(BlockFace.SOUTH, Wall.Height.TALL);
                }
            }

            //West (Negative X)
            Location lXMin = new Location(world, lAbove.getX() - 1, lAbove.getY(), lAbove.getZ());
            if (canConnect(block.getMaterial(), lXMin, BlockFace.WEST)) {
                if (wall.getHeight(BlockFace.WEST) == Wall.Height.LOW) {
                    wall.setHeight(BlockFace.WEST, Wall.Height.TALL);
                }
            }
        } else if (isFenceGate(bAbove.getMaterial())) {

            //Check the direction of the fence gate.
            Gate gate = (Gate) bAbove;

            if (gate.getFacing() == BlockFace.NORTH || gate.getFacing() == BlockFace.SOUTH) {
                if (wall.getHeight(BlockFace.EAST) == Wall.Height.LOW) {
                    wall.setHeight(BlockFace.EAST, Wall.Height.TALL);
                }

                if (wall.getHeight(BlockFace.WEST) == Wall.Height.LOW) {
                    wall.setHeight(BlockFace.WEST, Wall.Height.TALL);
                }
            } else {
                if (wall.getHeight(BlockFace.NORTH) == Wall.Height.LOW) {
                    wall.setHeight(BlockFace.NORTH, Wall.Height.TALL);
                }

                if (wall.getHeight(BlockFace.SOUTH) == Wall.Height.LOW) {
                    wall.setHeight(BlockFace.SOUTH, Wall.Height.TALL);
                }
            }
        } else {
            //Check of the wall can connect to the block above.
            if (canConnectAbove(bAbove)) {


                //Set all heights to tall.
                setConnections(l, block, wall, Wall.Height.TALL);

            }
        }

        //Check if the walls up=true.
        //This is true if there are no connections, a single connection, a corner only connection.
        //Also a lot of other blocks will cause this
        //Set up to true by default.
        //Check for cases where this is not true.
        wall.setUp(true);

        //If wall has a straight or cross shape.
        if (
                ((wall.getHeight(BlockFace.NORTH) == wall.getHeight(BlockFace.SOUTH)) && wall.getHeight(BlockFace.NORTH) != Wall.Height.NONE &&
                        (wall.getHeight(BlockFace.WEST) == Wall.Height.NONE && wall.getHeight(BlockFace.EAST) == Wall.Height.NONE))
                        ||
                        (wall.getHeight(BlockFace.EAST) == wall.getHeight(BlockFace.WEST) && wall.getHeight(BlockFace.EAST) != Wall.Height.NONE &&
                                wall.getHeight(BlockFace.NORTH) == Wall.Height.NONE && wall.getHeight(BlockFace.SOUTH) == Wall.Height.NONE)
                        ||
                        ((wall.getHeight(BlockFace.NORTH) == wall.getHeight(BlockFace.SOUTH)) && (wall.getHeight(BlockFace.EAST) == wall.getHeight(BlockFace.WEST)) && wall.getHeight(BlockFace.NORTH) != Wall.Height.NONE)
        ) {
            wall.setUp(false);

            //Check for cases where this wouldn't be true.
            //Certain blocks will still set the wall to up.
            switch (bAbove.getMaterial()) {

                case TORCH, REDSTONE_TORCH, STONE_PRESSURE_PLATE, OAK_PRESSURE_PLATE,
                     LIGHT_WEIGHTED_PRESSURE_PLATE, HEAVY_WEIGHTED_PRESSURE_PLATE,
                     OAK_SIGN, BREWING_STAND, FLOWER_POT, POTTED_DANDELION, POTTED_POPPY,
                     POTTED_BLUE_ORCHID, POTTED_ALLIUM, POTTED_AZURE_BLUET, POTTED_RED_TULIP,
                     POTTED_ORANGE_TULIP, POTTED_WHITE_TULIP, POTTED_PINK_TULIP, POTTED_OXEYE_DAISY,
                     POTTED_OAK_SAPLING, POTTED_SPRUCE_SAPLING, POTTED_BIRCH_SAPLING, POTTED_JUNGLE_SAPLING,
                     POTTED_ACACIA_SAPLING, POTTED_DARK_OAK_SAPLING, POTTED_RED_MUSHROOM, POTTED_BROWN_MUSHROOM,
                     POTTED_FERN, POTTED_DEAD_BUSH, POTTED_CACTUS,
                     SKELETON_SKULL, WITHER_SKELETON_SKULL, PLAYER_HEAD, ZOMBIE_HEAD, CREEPER_HEAD, DRAGON_HEAD,
                     WHITE_BANNER, ORANGE_BANNER, MAGENTA_BANNER, LIGHT_BLUE_BANNER, YELLOW_BANNER, LIME_BANNER,
                     PINK_BANNER, GRAY_BANNER, LIGHT_GRAY_BANNER, CYAN_BANNER, PURPLE_BANNER, BLUE_BANNER,
                     BROWN_BANNER, GREEN_BANNER, RED_BANNER, BLACK_BANNER -> wall.setUp(true);

                case END_ROD, HOPPER -> {
                    Directional direction = (Directional) bAbove;
                    if (direction.getFacing() == BlockFace.UP || direction.getFacing() == BlockFace.DOWN) {
                        wall.setUp(true);
                    }
                }
            }

            if (isFenceGate(bAbove.getMaterial())) {
                if (wall.getHeight(BlockFace.NORTH) != Wall.Height.TALL || wall.getHeight(BlockFace.SOUTH) != Wall.Height.TALL
                        || wall.getHeight(BlockFace.EAST) != Wall.Height.TALL || wall.getHeight(BlockFace.WEST) == Wall.Height.TALL) {
                    wall.setUp(true);
                }
            } else if (bAbove instanceof Wall) {

                //Check if the wall above is up or not, if it's up then this wall will also be up.
                if (getWall(lAbove).isUp()) {
                    wall.setUp(true);
                }
            }
        }

        return wall;
    }

    private void setRotation(Rotatable rot, byte rotation) {
        switch (rotation) {

            case 0 -> rot.setRotation(BlockFace.SOUTH);
            case 1 -> rot.setRotation(BlockFace.SOUTH_SOUTH_WEST);
            case 2 -> rot.setRotation(BlockFace.SOUTH_WEST);
            case 3 -> rot.setRotation(BlockFace.WEST_SOUTH_WEST);
            case 4 -> rot.setRotation(BlockFace.WEST);
            case 5 -> rot.setRotation(BlockFace.WEST_NORTH_WEST);
            case 6 -> rot.setRotation(BlockFace.NORTH_WEST);
            case 7 -> rot.setRotation(BlockFace.NORTH_NORTH_WEST);
            case 8 -> rot.setRotation(BlockFace.NORTH);
            case 9 -> rot.setRotation(BlockFace.NORTH_NORTH_EAST);
            case 10 -> rot.setRotation(BlockFace.NORTH_EAST);
            case 11 -> rot.setRotation(BlockFace.EAST_NORTH_EAST);
            case 12 -> rot.setRotation(BlockFace.EAST);
            case 13 -> rot.setRotation(BlockFace.EAST_SOUTH_EAST);
            case 14 -> rot.setRotation(BlockFace.SOUTH_EAST);
            case 15 -> rot.setRotation(BlockFace.SOUTH_SOUTH_EAST);

        }

    }

    private void setBannerPatterns(Block block, JSONArray patterns) {
        if (patterns != null && !patterns.isEmpty()) {
            Banner banner = (Banner) block.getState();

            for (Object o : patterns) {
                JSONObject pattern = (JSONObject) o;

                banner.addPattern(new Pattern(Utils.getDyeColour((String) pattern.get("colour")),
                        Utils.getPatternType((String) pattern.get("pattern"))));
            }

            banner.update(false, false);
        }
    }

    private boolean redstoneConnects(Block block, BlockFace face) {

        switch (block.getType()) {

            case REDSTONE_WIRE, REDSTONE_BLOCK, REDSTONE_TORCH, LEVER, DAYLIGHT_DETECTOR, COMPARATOR,
                 STONE_BUTTON, OAK_BUTTON, TRAPPED_CHEST, STONE_PRESSURE_PLATE, OAK_PRESSURE_PLATE,
                 HEAVY_WEIGHTED_PRESSURE_PLATE, LIGHT_WEIGHTED_PRESSURE_PLATE, DETECTOR_RAIL -> {
                return true;
            }

            case REPEATER -> {
                Repeater repeater = (Repeater) block.getBlockData();
                if ((face == BlockFace.NORTH || face == BlockFace.SOUTH) &&
                        (repeater.getFacing() == BlockFace.NORTH || repeater.getFacing() == BlockFace.SOUTH)) {
                    return true;
                } else return ((face == BlockFace.EAST || face == BlockFace.WEST) &&
                        (repeater.getFacing() == BlockFace.EAST || repeater.getFacing() == BlockFace.WEST));
            }

            case OBSERVER -> {
                Observer observer = (Observer) block.getBlockData();
                return (observer.getFacing() == face);
            }

            default -> {
                return false;
            }
        }
    }

    private boolean redstoneConnectsUp(Location l, BlockFace face) {

        //Check if the block above is redstone, else return null.
        Location lUp = new Location(world, l.getX(), l.getY() + 1, l.getZ());
        if (world.getType(lUp) != Material.REDSTONE_WIRE) {
            return false;
        }

        //Check if the block is a slab or stair, these are special cases.
        if (world.getBlockData(l) instanceof Stairs stair) {
            return ((stair.getHalf() == Bisected.Half.TOP) && (((facingNumber(face) + 2 % 4)) == facingNumber(stair.getFacing())));
        } else if (world.getBlockData(l) instanceof Slab slab) {
            return (slab.getType() == Slab.Type.DOUBLE);
        } else return (world.getType(l).isOccluding());
    }

    //Set facing direction to a number for convenience.
    private int facingNumber(BlockFace f) {
        switch (f) {
            case NORTH -> {
                return 0;
            }
            case EAST -> {
                return 1;
            }
            case WEST -> {
                return 3;
            }
            case SOUTH -> {
                return 2;
            }
        }
        return 0;
    }

    //Get note block instrument from material of block below the note block.
    private Instrument getInstrument(Material mat) {

        switch (mat) {

            //Bass (String Bass)
            case OAK_PLANKS, SPRUCE_PLANKS, BIRCH_PLANKS, JUNGLE_PLANKS, ACACIA_PLANKS, DARK_OAK_PLANKS,
                 OAK_LOG, SPRUCE_LOG, BIRCH_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG,
                 OAK_WOOD, SPRUCE_WOOD, BIRCH_WOOD, JUNGLE_WOOD, ACACIA_WOOD, DARK_OAK_WOOD,
                 NOTE_BLOCK, BOOKSHELF, OAK_STAIRS, SPRUCE_STAIRS, BIRCH_STAIRS, JUNGLE_STAIRS, ACACIA_STAIRS, DARK_OAK_STAIRS,
                 CHEST, TRAPPED_CHEST, CRAFTING_TABLE, OAK_SIGN, SPRUCE_SIGN, BIRCH_SIGN, JUNGLE_SIGN, ACACIA_SIGN, DARK_OAK_SIGN,
                 OAK_DOOR, SPRUCE_DOOR, BIRCH_DOOR, JUNGLE_DOOR, ACACIA_DOOR, DARK_OAK_DOOR,
                 OAK_PRESSURE_PLATE, JUKEBOX, OAK_FENCE, SPRUCE_FENCE, BIRCH_FENCE, JUNGLE_FENCE, ACACIA_FENCE, DARK_OAK_FENCE,
                 OAK_TRAPDOOR, RED_MUSHROOM_BLOCK, BROWN_MUSHROOM_BLOCK, MUSHROOM_STEM,
                 OAK_FENCE_GATE, SPRUCE_FENCE_GATE, BIRCH_FENCE_GATE, JUNGLE_FENCE_GATE, ACACIA_FENCE_GATE, DARK_OAK_FENCE_GATE,
                 DAYLIGHT_DETECTOR, WHITE_BANNER, ORANGE_BANNER, MAGENTA_BANNER, LIGHT_BLUE_BANNER,
                 YELLOW_BANNER, LIME_BANNER, PINK_BANNER, GRAY_BANNER, LIGHT_GRAY_BANNER, CYAN_BANNER,
                 PURPLE_BANNER, BLUE_BANNER, BROWN_BANNER, GREEN_BANNER, RED_BANNER, BLACK_BANNER,
                 WHITE_WALL_BANNER, ORANGE_WALL_BANNER, MAGENTA_WALL_BANNER, LIGHT_BLUE_WALL_BANNER,
                 YELLOW_WALL_BANNER, LIME_WALL_BANNER, PINK_WALL_BANNER, GRAY_WALL_BANNER, LIGHT_GRAY_WALL_BANNER,
                 CYAN_WALL_BANNER, PURPLE_WALL_BANNER, BLUE_WALL_BANNER, BROWN_WALL_BANNER,
                 GREEN_WALL_BANNER, RED_WALL_BANNER, BLACK_WALL_BANNER,
                 OAK_SLAB, SPRUCE_SLAB, BIRCH_SLAB, JUNGLE_SLAB, ACACIA_SLAB, DARK_OAK_SLAB -> {
                return Instrument.BASS_GUITAR;
            }

            //Snare Drum
            case SAND, RED_SAND, GRAVEL, WHITE_CONCRETE_POWDER, ORANGE_CONCRETE_POWDER,
                 LIGHT_BLUE_CONCRETE_POWDER, YELLOW_CONCRETE_POWDER, LIME_CONCRETE_POWDER,
                 PINK_CONCRETE_POWDER, GRAY_CONCRETE_POWDER, LIGHT_GRAY_CONCRETE_POWDER, CYAN_CONCRETE_POWDER,
                 PURPLE_CONCRETE_POWDER, BLUE_CONCRETE_POWDER, BROWN_CONCRETE_POWDER, GREEN_CONCRETE_POWDER,
                 RED_CONCRETE_POWDER, BLACK_CONCRETE_POWDER -> {
                return Instrument.SNARE_DRUM;
            }

            //Clicks and Sticks (Hihat)
            case GLASS, WHITE_STAINED_GLASS, ORANGE_STAINED_GLASS, MAGENTA_STAINED_GLASS, LIGHT_BLUE_STAINED_GLASS,
                 YELLOW_STAINED_GLASS, LIME_STAINED_GLASS, PINK_STAINED_GLASS, GRAY_STAINED_GLASS,
                 LIGHT_GRAY_STAINED_GLASS, CYAN_STAINED_GLASS, PURPLE_STAINED_GLASS, BLUE_STAINED_GLASS,
                 BROWN_STAINED_GLASS, GREEN_STAINED_GLASS, RED_STAINED_GLASS, BLACK_STAINED_GLASS,
                 GLASS_PANE, WHITE_STAINED_GLASS_PANE, ORANGE_STAINED_GLASS_PANE, MAGENTA_STAINED_GLASS_PANE,
                 LIGHT_BLUE_STAINED_GLASS_PANE, YELLOW_STAINED_GLASS_PANE, LIME_STAINED_GLASS_PANE, PINK_STAINED_GLASS_PANE,
                 GRAY_STAINED_GLASS_PANE, LIGHT_GRAY_STAINED_GLASS_PANE, CYAN_STAINED_GLASS_PANE,
                 PURPLE_STAINED_GLASS_PANE, BLUE_STAINED_GLASS_PANE, BROWN_STAINED_GLASS_PANE,
                 GREEN_STAINED_GLASS_PANE, RED_STAINED_GLASS_PANE, BLACK_STAINED_GLASS_PANE,
                 BEACON, SEA_LANTERN -> {
                return Instrument.STICKS;
            }

            //Bass Drum (Kick)
            case STONE, DIORITE, GRANITE, ANDESITE, POLISHED_ANDESITE, POLISHED_DIORITE, POLISHED_GRANITE,
                 COBBLESTONE, MOSSY_COBBLESTONE, BEDROCK, COAL_ORE, IRON_ORE, GOLD_ORE, REDSTONE_ORE, LAPIS_ORE, DIAMOND_ORE, EMERALD_ORE,
                 DROPPER, DISPENSER, SANDSTONE, CUT_SANDSTONE, CHISELED_SANDSTONE, SMOOTH_SANDSTONE, RED_SANDSTONE, CUT_RED_SANDSTONE, CHISELED_RED_SANDSTONE, SMOOTH_RED_SANDSTONE,
                 SMOOTH_STONE, BRICKS, SPAWNER, FURNACE, COBBLESTONE_STAIRS, STONE_BRICK_STAIRS, SANDSTONE_STAIRS, RED_SANDSTONE_STAIRS, NETHER_BRICK_STAIRS, PURPUR_STAIRS, QUARTZ_STAIRS,
                 STONE_PRESSURE_PLATE, NETHERRACK, STONE_BRICKS, CHISELED_STONE_BRICKS, MOSSY_STONE_BRICKS, CRACKED_STONE_BRICKS, NETHER_BRICK,
                 NETHER_BRICK_FENCE, ENCHANTING_TABLE, END_PORTAL_FRAME, END_STONE, ENDER_CHEST, COBBLESTONE_WALL, MOSSY_COBBLESTONE_WALL, QUARTZ_BLOCK,
                 QUARTZ_PILLAR, CHISELED_QUARTZ_BLOCK, TERRACOTTA, WHITE_TERRACOTTA, ORANGE_TERRACOTTA, MAGENTA_TERRACOTTA, LIGHT_BLUE_TERRACOTTA,
                 YELLOW_TERRACOTTA, LIME_TERRACOTTA, PINK_TERRACOTTA, GRAY_TERRACOTTA, LIGHT_GRAY_TERRACOTTA, CYAN_TERRACOTTA, PURPLE_TERRACOTTA,
                 BLUE_TERRACOTTA, BROWN_TERRACOTTA, GREEN_TERRACOTTA, RED_TERRACOTTA, BLACK_TERRACOTTA, PRISMARINE, PRISMARINE_BRICKS, DARK_PRISMARINE,
                 COBBLESTONE_SLAB, STONE_BRICK_SLAB, SANDSTONE_SLAB, RED_SANDSTONE_SLAB, BRICK_SLAB, NETHER_BRICK_SLAB, QUARTZ_SLAB, PURPUR_SLAB,
                 OBSIDIAN, PURPUR_BLOCK, PURPUR_PILLAR, END_STONE_BRICKS, MAGMA_BLOCK, RED_NETHER_BRICKS, OBSERVER, WHITE_GLAZED_TERRACOTTA, ORANGE_GLAZED_TERRACOTTA,
                 MAGENTA_GLAZED_TERRACOTTA, LIGHT_BLUE_GLAZED_TERRACOTTA, YELLOW_GLAZED_TERRACOTTA, LIME_GLAZED_TERRACOTTA, PINK_GLAZED_TERRACOTTA,
                 GRAY_GLAZED_TERRACOTTA, LIGHT_GRAY_GLAZED_TERRACOTTA, CYAN_GLAZED_TERRACOTTA, PURPLE_GLAZED_TERRACOTTA, BLUE_GLAZED_TERRACOTTA,
                 BROWN_GLAZED_TERRACOTTA, GREEN_GLAZED_TERRACOTTA, RED_GLAZED_TERRACOTTA, BLACK_GLAZED_TERRACOTTA, WHITE_CONCRETE, ORANGE_CONCRETE,
                 MAGENTA_CONCRETE, LIGHT_BLUE_CONCRETE, YELLOW_CONCRETE, LIME_CONCRETE, PINK_CONCRETE, GRAY_CONCRETE, LIGHT_GRAY_CONCRETE, CYAN_CONCRETE,
                 PURPLE_CONCRETE, BLUE_CONCRETE, BROWN_CONCRETE, GREEN_CONCRETE, RED_CONCRETE, BLACK_CONCRETE -> {
                return Instrument.BASS_DRUM;
            }

            //Bells (Glockenspiel)
            case GOLD_BLOCK -> {
                return Instrument.BELL;
            }

            //Flute
            case CLAY, INFESTED_COBBLESTONE, INFESTED_STONE, INFESTED_CHISELED_STONE_BRICKS, INFESTED_CRACKED_STONE_BRICKS, INFESTED_MOSSY_STONE_BRICKS, INFESTED_STONE_BRICKS, SLIME_BLOCK -> {
                return Instrument.FLUTE;
            }

            //Chimes
            case PACKED_ICE -> {
                return Instrument.CHIME;
            }

            //Guitar
            case WHITE_WOOL, ORANGE_WOOL, MAGENTA_WOOL, LIGHT_BLUE_WOOL, YELLOW_WOOL, LIME_WOOL, PINK_WOOL, GRAY_WOOL,
                 LIGHT_GRAY_WOOL, CYAN_WOOL, PURPLE_WOOL, BLUE_WOOL, BROWN_WOOL, GREEN_WOOL, RED_WOOL, BLACK_WOOL -> {
                return Instrument.GUITAR;
            }

            //Xylophone
            case BONE_BLOCK -> {
                return Instrument.XYLOPHONE;
            }

            //Iron Xylophone
            case IRON_BLOCK -> {
                return Instrument.IRON_XYLOPHONE;
            }

            //Cow Bells
            case SOUL_SAND -> {
                return Instrument.COW_BELL;
            }

            //Bit (Square Wave)
            case EMERALD_BLOCK -> {
                return Instrument.BIT;
            }

            //Banjo
            case HAY_BLOCK -> {
                return Instrument.BANJO;
            }

            //Pling (Electric piano)
            case GLOWSTONE -> {
                return Instrument.PLING;
            }

            default -> {
                return Instrument.PIANO;
            }
        }
    }

    /**
     * Set an entity in the world
     * @param entityNamespace The namespace (name) of the entity
     * @param object JSON object containing the properties of the entity
     * @param location The location of the entity
     * @throws Exception
     */
    private void setEntity(String entityNamespace, JSONObject object, Location location) throws Exception {
        JSONObject objectProps = (JSONObject)object.get("properties");

        switch (entityNamespace) {
            case "minecraft:armor_stand" -> {
                ArmorStand armorStand = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
                ArmorStandHelper.propArmorStand(armorStand, objectProps);
            }
            case "minecraft:minecart" -> {
                RideableMinecart rideableMinecart = (RideableMinecart) world.spawnEntity(location, EntityType.MINECART);
                MinecartHelper.setCommonMinecartProps(rideableMinecart, objectProps);
            }
            case "minecraft:chest_minecart" ->{
                StorageMinecart storageMinecart = (StorageMinecart) world.spawnEntity(location, EntityType.MINECART_CHEST);
                MinecartHelper.setCommonMinecartProps(storageMinecart, objectProps);
                MinecartHelper.prepChestMinecart(storageMinecart, objectProps);
            }
            case "minecraft:hopper_minecart" -> {
                HopperMinecart hopperMinecart = (HopperMinecart) world.spawnEntity(location, EntityType.MINECART_HOPPER);
                MinecartHelper.setCommonMinecartProps(hopperMinecart, objectProps);
                MinecartHelper.prepHopperMinecart(hopperMinecart, objectProps);
            }
            case "minecraft:command_block_minecart" -> {
                CommandMinecart commandMinecart = (CommandMinecart) world.spawnEntity(location, EntityType.MINECART_COMMAND);
                MinecartHelper.setCommonMinecartProps(commandMinecart, objectProps);
                MinecartHelper.prepCommandMinecart(commandMinecart, objectProps);
            }
            case "minecraft:furnace_minecart" -> {
                PoweredMinecart furnaceMinecart = (PoweredMinecart) world.spawnEntity(location, EntityType.MINECART_FURNACE);
                MinecartHelper.setCommonMinecartProps(furnaceMinecart, objectProps);
                MinecartHelper.prepFurnaceMinecart(furnaceMinecart, objectProps);
            }
            case "minecraft:tnt_minecart" -> {
                ExplosiveMinecart explosiveMinecart = (ExplosiveMinecart) world.spawnEntity(location, EntityType.MINECART_TNT);
                MinecartHelper.setCommonMinecartProps(explosiveMinecart, objectProps);
                MinecartHelper.prepExplosiveMinecart(explosiveMinecart, objectProps);
            }
            case "minecraft:end_crystal" -> {
                EnderCrystal enderCrystal = (EnderCrystal) world.spawnEntity(location, EntityType.ENDER_CRYSTAL);
                Utils.prepEntity(enderCrystal, objectProps);
                if(objectProps.containsKey("beam_target")){
                    List<Integer> beamTarget = Utils.getIntegerListFromJson(objectProps, "beam_target");
                    enderCrystal.setBeamTarget(new Location(location.getWorld(), beamTarget.get(0), beamTarget.get(1), beamTarget.get(2)));
                }
                Integer show_button = Utils.readInteger(objectProps, "show_button");
                enderCrystal.setShowingBottom(show_button != null && show_button == 1);
            }
            case "minecraft:painting" -> {
                JSONArray tilePos = (JSONArray) objectProps.get("tile_pos");
                Number tileX = Utils.getNumericValue(tilePos.get(0));
                Number tileY = Utils.getNumericValue(tilePos.get(1));
                Number tileZ = Utils.getNumericValue(tilePos.get(2));

                if(tileX != null && tileY != null && tileZ != null) {
                    location = new Location(location.getWorld(), tileX.doubleValue(), tileY.doubleValue(), tileZ.doubleValue());

                    Painting painting = (Painting) world.spawnEntity(location, EntityType.PAINTING);
                    Utils.prepEntity(painting, objectProps);
                    Art motive = Art.getByName((String) objectProps.get("motive"));
                    painting.setArt(motive);
                    if (objectProps.containsKey("facing")) {
                        painting.setFacingDirection(BlockFace.valueOf((String) objectProps.get("facing")));
                    }
                }
            }
            case "minecraft:item_frame" -> {
                ItemFrame itemFrame = (ItemFrame) world.spawnEntity(location, EntityType.ITEM_FRAME);
                Utils.prepEntity(itemFrame, objectProps);
                Integer fixed = Utils.readInteger(objectProps, "fixed");
                Integer invisible = Utils.readInteger(objectProps, "invisible");
                if(fixed != null)
                    itemFrame.setFixed(fixed==1);
                if(invisible != null)
                    itemFrame.setVisible(invisible==0);
                if(objectProps.containsKey("item")){
                    JSONObject item = (JSONObject) objectProps.get("item");
                    JSONObject itemProps = (JSONObject) item.getOrDefault("Properties", new JSONObject());
                    String itemID = (String) item.get("id");
                    try {
                        ItemStack _item = ItemsHelper.getItem(itemID, itemProps);
                        if (_item != null)
                            itemFrame.setItem(_item);
                    }catch (Exception ex) {
                        instance.getLogger().warning(String.format("Exception while setting item frame for the item: %1$s | Error: %2$s", itemID, ex.getMessage()));
                    }

                }
                if(objectProps.containsKey("item_rotation")){
                    itemFrame.setRotation(Rotation.valueOf((String) objectProps.get("item_rotation")));
                }
                if(objectProps.containsKey("item_drop_chance")){
                    Double itemDropChance = Utils.readDouble(objectProps, "item_drop_chance");
                    if(itemDropChance != null)
                        itemFrame.setItemDropChance(itemDropChance.floatValue());
                }

            }

        }
    }

}
