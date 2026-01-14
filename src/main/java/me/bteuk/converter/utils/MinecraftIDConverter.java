package org.btuk.converter.utils;

import org.btuk.converter.Main;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.tag.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class MinecraftIDConverter {
    private static final Logger log = LoggerFactory.getLogger(MinecraftIDConverter.class);
    public static MinecraftIDConverter instance = new MinecraftIDConverter();


        /*

    List of blocks for post-processing:

        175:11 (Top half of double_plant
        53, 67, 108, 109, 114, 128, 134, 135, 136, 156, 163, 156, 163, 164, 180, 203 (Stairs shape)
        85, 113, 188, 189, 190, 191, 192 (Fence connections)
        139, 139:1 (Wall connections)
        101 (Iron bar connections)
        102, 160:0 to 160:15 (Glass pane connections)
        54, 146 (Chest connections)
        55 (Redstone connections)
        199 (Chorus plant connections)
        26 (Bed colour)
        176, 177 (Banner colour and pattern)
        140 (Flower pot content)
        144 (Mob head types and player head texture)
        25 (Note block content)
        132 (Tripwire connections)
        106 (Vine on the top side)

     */

    //Convert a legacy Minecraft block id and get the 1.18.2 palette for the block.
    public static CompoundTag getBlock(LegacyID id) {

        //Create map for block.
        CompoundTag block = new CompoundTag();

        //Add block name as String tag.
        block.putString("Name", getNameSpace(id.getID(), id.getData()));

        //If block has properties, add them.
        if (hasBlockStates(id.getID(), id.getData())) {
            block.put("Properties", getBlockStates(id.getID(), id.getData()));
        }

        return block;
    }

    //Convert a legacy Minecraft biome id and get teh 1.18.2 namespace.
    public static String getBiome(byte id) {
        return "minecraft:" + getBiomeName(id);
    }

    public static String getBiomeName(byte id) {

        switch (id) {

            //Ocean
            case 0 -> {
                return "ocean";
            }

            //Plains
            case 1 -> {
                return "plains";
            }

            //Desert (Hills)
            case 2, 17, (byte) 130 -> {
                return "desert";
            }

            //Extreme Hills
            case 3, (byte) 131 -> {
                return "stony_peaks";
            }

            //Taiga (Hills)
            case 5, 19, (byte) 133 -> {
                return "taiga";
            }

            //Swampland
            case 6, (byte) 134 -> {
                return "swamp";
            }

            //River
            case 7 -> {
                return "river";
            }

            //Hell
            case 8 -> {
                return "nether_wastes";
            }

            //Sky
            case 9 -> {
                return "the_end";
            }

            //Frozen Ocean
            case 10 -> {
                return "frozen_ocean";
            }

            //Frozen River
            case 11 -> {
                return "frozen_river";
            }

            //Ice Flats
            case 12 -> {
                return "snowy_plains";
            }

            //Ice Mountains
            case 13 -> {
                return "frozen_peaks";
            }

            //Mushroom Island (Shore)
            case 14, 15 -> {
                return "mushroom_fields";
            }

            //Beach
            case 16 -> {
                return "beach";
            }

            //Extreme Hills Edge
            case 20 -> {
                return "windswept_hills";
            }

            //Jungle (Hills)
            case 21, 22, (byte) 149 -> {
                return "jungle";
            }

            //Jungle Edge
            case 23, (byte) 151 -> {
                return "sparse_jungle";
            }

            //Deep Ocean
            case 24 -> {
                return "deep_ocean";
            }

            //Stone Beach
            case 25 -> {
                return "stony_shore";
            }

            //Cold Beach
            case 26 -> {
                return "snowy_beach";
            }

            //Birch Forest (Hills)
            case 27, 28, (byte) 155, (byte) 156 -> {
                return "birch_forest";
            }

            //Roofed Forest
            case 29, (byte) 157 -> {
                return "dark_forest";
            }

            //Cold Taiga (Hills)
            case 30, 31, (byte) 158 -> {
                return "snowy_taiga";
            }

            //Mega Taiga (Hills)
            case 32, 33, (byte) 160, (byte) 161 -> {
                return "old_growth_spruce_taiga";
            }

            //Extreme Hills+
            case 34, (byte) 162 -> {
                return "windswept_forest";
            }

            //Savanna
            case 35, (byte) 163 -> {
                return "savanna";
            }

            //Savanna Plateau
            case 36, (byte) 164 -> {
                return "savanna_plateau";
            }

            //Mesa (Plateau)
            case 37, 39, (byte) 167 -> {
                return "badlands";
            }

            //Mesa Plateau F
            case 38, (byte) 166 -> {
                return "wooded_badlands";
            }

            //Void
            case 127 -> {
                return "the_void";
            }

            //Sunflower Plains
            case (byte) 129 -> {
                return "sunflower_plains";
            }

            //Flower Forest
            case (byte) 132 -> {
                return "flower_forest";
            }

            //Ice Plains Spikes
            case (byte) 140 -> {
                return "ice_spikes";
            }

            //Mesa Bryce
            case (byte) 165 -> {
                return "eroded_badlands";
            }

            default -> {
                return "forest";
            }
        }
    }


    //Convert a legacy Minecraft block id and get the 1.18.2 namespace version.
    public static String getNameSpace(byte id, byte data) {
        return ("minecraft:" + getBlockName(id, data));
    }

    //Check if the block has block states in 1.18.2, in this case it needs a properties tag in the palette.
    public static boolean hasBlockStates(byte id, byte data) {

        //Unique case for dirt.
        if (id == 3) {
            if (data == 2) {
                return true;
            } else {
                return false;
            }
        }

        //Unique case for double slabs (smooth stone and smooth sandstone)
        if (id == 43) {
            if (data == 8 || data == 9) {
                return false;
            } else {
                return true;
            }
        }

        switch (id) {

            case (byte) 139, 106, (byte) 131, (byte) 132, 96, (byte) 167, 50, 46, (byte) 255, 53, 67, 108, 109,
                    114, (byte) 128, (byte) 134, (byte) 135, (byte) 136, (byte) 156, (byte) 163, (byte) 164,
                    (byte) 180, (byte) 203, 78, 43, 125, (byte) 181, (byte) 204, 44, 126, (byte) 182, (byte) 205,
                    68, 63, (byte) 219, (byte) 220, (byte) 221, (byte) 222, (byte) 223, (byte) 224, (byte) 225,
                    (byte) 226, (byte) 227, (byte) 228, (byte) 229, (byte) 230, (byte) 231, (byte) 232,
                    (byte) 233, (byte) 234, 6, 76, 75, 93, 94, 123, 73, 124, 74, 55, (byte) 149, (byte) 150, 27,
                    28, (byte) 157, 66, (byte) 155, 70, 72, (byte) 147, (byte) 148, (byte) 142, (byte) 141, 59,
                    (byte) 207, 115, 104, 105, 83, 34, 29, 33, (byte) 218, 25, 90, 99, 100, 17, (byte) 162, 69,
                    18, (byte) 161, 10, 11, 65, 84, (byte) 154, (byte) 170, (byte) 202, 2, 110, 3, (byte) 235,
                    (byte) 236, (byte) 237, (byte) 238, (byte) 239, (byte) 240, (byte) 241, (byte) 242,
                    (byte) 243, (byte) 244, (byte) 245, (byte) 246, (byte) 247, (byte) 248, (byte) 249,
                    (byte) 250, 91, 86, 102, (byte) 160, 101, 61, 62, (byte) 212, (byte) 175, 51, 107, (byte) 183,
                    (byte) 184, (byte) 185, (byte) 186, (byte) 187, 85, 113, (byte) 188, (byte) 189, (byte) 190,
                    (byte) 191, (byte) 192, 60, (byte) 198, 120, 64, 71, (byte) 193, (byte) 194, (byte) 195,
                    (byte) 196, (byte) 197, 23, (byte) 158, (byte) 178, (byte) 151, (byte) 137, (byte) 210,
                    (byte) 211, 127, (byte) 199, (byte) 200, (byte) 130, 54, (byte) 146, 118, 92, 81, 77,
                    (byte) 143, 117, (byte) 216, 26, (byte) 177, (byte) 144, (byte) 176, (byte) 145, 8, 9 -> {
                return true;
            }

        }

        return false;

    }

    //Check if the block is a block entity (formally known as tile entity)
    public static boolean isBlockEntity(byte id) {

        switch (id) {

            case 23, 25, 26, 52, 54, 61, 62, 63, 68, 116, 117, 119, (byte) 130, (byte) 137, (byte) 138, (byte) 144,
                    (byte) 146, (byte) 149, (byte) 150, (byte) 151, (byte) 154, (byte) 158, (byte) 176,
                    (byte) 177, (byte) 178, (byte) 209, (byte) 210, (byte) 211, (byte) 219, (byte) 220, (byte) 221,
                    (byte) 222, (byte) 223, (byte) 224, (byte) 225, (byte) 226, (byte) 227, (byte) 228, (byte) 229,
                    (byte) 230, (byte) 231, (byte) 232, (byte) 233, (byte) 234, (byte) 255 -> {
                return true;
            }

        }

        return false;

    }

    //Check if the block entity should not be added to the block entity list.
    public static boolean blockEntityNotAdded(byte id) {

        switch (id) {

            //Note block (not a block entity in 1.18.2).
            //Skull, since that will be done in post-processing.
            case 25, (byte) 144 -> {
                return true;
            }

        }

        return false;

    }

    //Get the properties of the block for post-processing.
    public static boolean hasProperties(byte id) {
        
        /*

        List of blocks for post-processing with properties:

        53, 67, 108, 109, 114, 128, 134, 135, 136, 156, 163, 156, 163, 164, 180, 203 (Stairs shape)
        26 (Bed colour)
        176, 177 (Banner colour and pattern)
        140 (Flower pot content)
        144 (Mob head types and player head texture)
        23 Dispenser
        25 (Note block content)
        54 Chest
        146 Trapped Chest
        154 Hopper
        158 Dropper
        219-234 Skulker Box

        */

        switch (id) {

            case 23, 53, 67, 108, 109, 114, (byte) 128, (byte) 134, (byte) 135, (byte) 136, (byte) 154, (byte) 156, (byte) 158, (byte) 163,
                    (byte) 164, (byte) 180, (byte) 203, 26, (byte) 176, (byte) 177, (byte) 140, (byte) 144, 25,
                54, (byte) 146, (byte) 219, (byte) 220, (byte) 221, (byte) 222, (byte) 223, (byte) 224, (byte) 225,
                        (byte) 226, (byte) 227, (byte) 228, (byte) 229, (byte) 230, (byte) 231, (byte) 232, (byte) 233, (byte) 234 -> {
                return true;
            }

        }

        return false;

    }

    //Get properties for post-processing blocks and return a json object.
    public static JSONObject getProperties(byte id, byte data, CompoundTag block_entity) {
        
        /*

        List of blocks for post-processing with properties:

        53, 67, 108, 109, 114, 128, 134, 135, 136, 156, 163, 156, 163, 164, 180, 203 (Stairs shape)
        26 (Bed colour)
        176, 177 (Banner colour and pattern)
        140 (Flower pot content)
        144 (Mob head types and player head texture)
        25 (Note block content)
        23 Dispenser
        54 Chests
        146 Trapped Chest
        154 Hopper
        158 Dropper
        219-234 Skulker Box
        */

        JSONObject jo = new JSONObject();

        switch (id) {

            //Stairs
            case 53, 67, 108, 109, 114, (byte) 128, (byte) 134, (byte) 135, (byte) 136, (byte) 156, (byte) 163,
                    (byte) 164, (byte) 180, (byte) 203 -> {

                //facing
                switch (data) {

                    case 0, 4 -> jo.put("facing", "east");
                    case 1, 5 -> jo.put("facing", "west");
                    case 2, 6 -> jo.put("facing", "south");
                    case 3, 7 -> jo.put("facing", "north");

                }

                //half
                if (data >= 4) {
                    jo.put("half", "top");
                } else {
                    jo.put("half", "bottom");
                }
            }

            //Beds
            case 26 -> {

                //part
                switch (data) {

                    case 0, 1, 2, 3, 4, 5, 6, 7 -> jo.put("part", "foot");
                    case 8, 9, 10, 11, 12, 13, 14, 15 -> jo.put("part", "head");

                }

                //facing
                switch (data) {

                    case 0, 4, 8, 12 -> jo.put("facing", "south");
                    case 1, 5, 9, 13 -> jo.put("facing", "west");
                    case 2, 6, 10, 14 -> jo.put("facing", "north");
                    case 3, 7, 11, 15 -> jo.put("facing", "east");

                }

                //Get bed colour.
                jo.put("colour", colourNameSpace(block_entity.getInt("color")));
            }

            //Standing Banner
            case (byte) 176 -> {

                jo.put("rotation", String.valueOf(data));

                //Banner colour
                jo.put("colour", colourNameSpace(15 - block_entity.getInt("Base")));

                //Patterns
                getPatternTags(block_entity, jo);
            }

            //Wall Banner
            case (byte) 177 -> {

                switch (data) {

                    case 2 -> jo.put("facing", "north");
                    case 3 -> jo.put("facing", "south");
                    case 4 -> jo.put("facing", "west");
                    case 5 -> jo.put("facing", "east");

                }

                //Banner colour
                jo.put("colour", colourNameSpace(15 - block_entity.getInt("Base")));

                //Patterns
                getPatternTags(block_entity, jo);
            }

            //Flower Pot
            case (byte) 140 -> {

                //Null case
                if (block_entity == null) {
                    jo.put("type", "flower_pot");
                } else {

                    //Sort by item.
                    switch (block_entity.getString("Item")) {

                        case "minecraft:air" -> jo.put("type", "flower_pot");

                        case "minecraft:yellow_flower" -> jo.put("type", "potted_dandelion");

                        case "minecraft:red_flower" -> {

                            switch (block_entity.getByte("Data")) {

                                case 0 -> jo.put("type", "potted_poppy");
                                case 1 -> jo.put("type", "potted_blue_orchid");
                                case 2 -> jo.put("type", "potted_allium");
                                case 3 -> jo.put("type", "potted_azure_bluet");
                                case 4 -> jo.put("type", "potted_red_tulip");
                                case 5 -> jo.put("type", "potted_orange_tulip");
                                case 6 -> jo.put("type", "potted_white_tulip");
                                case 7 -> jo.put("type", "potted_pink_tulip");
                                case 8 -> jo.put("type", "potted_oxeye_daisy");

                            }
                        }

                        case "minecraft:sapling" -> {

                            switch (block_entity.getByte("Data")) {

                                case 0 -> jo.put("type", "potted_oak_sapling");
                                case 1 -> jo.put("type", "potted_spruce_sapling");
                                case 2 -> jo.put("type", "potted_birch_sapling");
                                case 3 -> jo.put("type", "potted_jungle_sapling");
                                case 4 -> jo.put("type", "potted_acacia_sapling");
                                case 5 -> jo.put("type", "potted_dark_oak_sapling");

                            }
                        }

                        case "minecraft:red_mushroom" -> jo.put("type", "potted_red_mushroom");
                        case "minecraft:brown_mushroom" -> jo.put("type", "potted_brown_mushroom");

                        case "minecraft:tallgrass" -> jo.put("type", "potted_fern");
                        case "minecraft:deadbush" -> jo.put("type", "potted_dead_bush");
                        case "minecraft:cactus" -> jo.put("type", "potted_cactus");

                    }
                }
            }

            //Mob and Player Heads
            case (byte) 144 -> {

                switch (data) {

                    case 2 -> jo.put("facing", "north");
                    case 3 -> jo.put("facing", "south");
                    case 4 -> jo.put("facing", "west");
                    case 5 -> jo.put("facing", "east");
                    default -> jo.put("facing", "floor");

                }
                //Skull type.
                switch (block_entity.getByte("SkullType")) {

                    case 0 -> jo.put("type", "skeleton_skull");
                    case 1 -> jo.put("type", "wither_skeleton_skull");
                    case 2 -> jo.put("type", "zombie_head");
                    case 3 -> {

                        jo.put("type", "player_head");

                        //Get owner data.
                        CompoundTag owner = block_entity.getCompoundTag("Owner");

                        if (owner != null) {

                            jo.put("id", owner.getString("Id"));

                            CompoundTag properties = owner.getCompoundTag("Properties");
                            ListTag<CompoundTag> textures = (ListTag<CompoundTag>) properties.getListTag("textures");

                            //Get first texture.
                            CompoundTag texture = textures.get(0);

                            jo.put("texture", texture.getString("Value"));

                        }

                    }
                    case 4 -> jo.put("type", "creeper_head");
                    case 5 -> jo.put("type", "dragon_head");

                }

                //Rotation
                jo.put("rotation", block_entity.getByte("Rot"));
            }

            //Note Block
            case 25 -> {
                jo.put("note", block_entity.getByte("note"));
            }

            //Dispenser, Dropper, Hopper, Shulker Boxes, Chests
            case 23, 54, (byte) 146, (byte) 154, (byte) 158, (byte) 219, (byte) 220, (byte) 221, (byte) 222, (byte) 223,
                (byte) 224, (byte) 225,
                (byte) 226, (byte) 227, (byte) 228, (byte) 229, (byte) 230, (byte) 231, (byte) 232, (byte) 233,
                (byte) 234 -> {
                getInventoryTags(block_entity, jo);
            }
        }

        return jo;

    }

    //Get the namespace for a 1.12.2 colour id.
    public static String colourNameSpace(int id) {

        switch (id) {

            case 0 -> {
                return "white";
            }

            case 1 -> {
                return "orange";
            }

            case 2 -> {
                return "magenta";
            }

            case 3 -> {
                return "light_blue";
            }

            case 4 -> {
                return "yellow";
            }

            case 5 -> {
                return "lime";
            }

            case 6 -> {
                return "pink";
            }

            case 7 -> {
                return "gray";
            }

            case 8 -> {
                return "light_gray";
            }

            case 9 -> {
                return "cyan";
            }

            case 10 -> {
                return "purple";
            }

            case 11 -> {
                return "blue";
            }

            case 12 -> {
                return "brown";
            }

            case 13 -> {
                return "green";
            }

            case 14 -> {
                return "red";
            }

            case 15 -> {
                return "black";
            }

            default -> {
                return null;
            }
        }
    }

    //Check if the block requires post-processing, in this case store the data in a txt file.
    public static boolean requiredPostProcessing(byte id, byte data) {

        switch (id) {

            case (byte) 175 -> {
                if (data == 11) {
                    return true;
                }
            }

            case 23, 53, 67, 108, 109, 114, (byte) 128, (byte) 134, (byte) 135, (byte) 136, (byte) 156, (byte) 163,
                (byte) 164, (byte) 180, (byte) 203, 85, 113, (byte) 188, (byte) 189, (byte) 190, (byte) 191,
                (byte) 192, (byte) 139, 101, 102, (byte) 160, 54, (byte) 146, 55, (byte) 199, 26,
                (byte) 176, (byte) 177, 104, 105, (byte) 140, (byte) 144, 25, (byte) 132, 106,
                64, 71, (byte) 154, (byte) 158, (byte) 193, (byte) 194, (byte) 195, (byte) 196, (byte) 197,
                (byte) 219, (byte) 220, (byte) 221, (byte) 222, (byte) 223, (byte) 224, (byte) 225,
                (byte) 226, (byte) 227, (byte) 228, (byte) 229, (byte) 230, (byte) 231, (byte) 232, (byte) 233,
                (byte) 234 -> {
                return true;
            }
        }

        return false;

    }

    //Get the block entity of a block
    public static CompoundTag getBlockEntity(byte id, byte data, CompoundTag tile_entity) {

        CompoundTag block_entity = new CompoundTag();

        //Add the global tags that are common to all block entities.
        //For unique case of spawner, beds, shulker boxes, signs and banners, the namespace of the block entity differs from the actual block namespace.
        if (id == 52) {
            block_entity.putString("id", "minecraft:mob_spawner");
        } else if (id == (byte) 176 || id == (byte) 177) {
            block_entity.putString("id", "minecraft:banner");
        } else if (id == 26) {
            block_entity.putString("id", "minecraft:bed");
        } else if (id == (byte) 219 || id == (byte) 220 || id == (byte) 221 || id == (byte) 222 ||
                id == (byte) 223 || id == (byte) 224 || id == (byte) 225 || id == (byte) 226 ||
                id == (byte) 227 || id == (byte) 228 || id == (byte) 229 || id == (byte) 230 ||
                id == (byte) 231 || id == (byte) 232 || id == (byte) 233 || id == (byte) 234) {
            block_entity.putString("id", "minecraft:shulker_box");
        } else if (id == 63 || id == 68) {
            block_entity.putString("id", "minecraft:sign");
        } else {
            block_entity.putString("id", getNameSpace(id, data));
        }
        block_entity.putBoolean("keepPacked", false);
        block_entity.putInt("x", tile_entity.getInt("x"));
        block_entity.putInt("y", tile_entity.getInt("y") + Main.OFFSET);
        block_entity.putInt("z", tile_entity.getInt("z"));

        //Now for the unique tags.
        switch (id) {

            //Dispenser, (Trapped) Chest, Shulker Boxes and Dropper (all default values since we don't care what's inside)
            case 23, 54, (byte) 146, (byte) 158, (byte) 219, (byte) 220, (byte) 221,
                (byte) 222, (byte) 223, (byte) 224, (byte) 225, (byte) 226, (byte) 227, (byte) 228, (byte) 229,
                (byte) 230, (byte) 231, (byte) 232, (byte) 233, (byte) 234 ->
                    block_entity.put("Items", new ListTag<>(CompoundTag.class));

            //Mob Spawner (all default values)
            case 52 -> {

                block_entity.putShort("Delay", (short) 0);
                block_entity.putShort("MaxNearbyEntities", (short) 6);
                block_entity.putShort("MaxSpawnDelay", (short) 800);
                block_entity.putShort("MinSpawnDelay", (short) 200);
                block_entity.putShort("RequiredPlayerRange", (short) 16);
                block_entity.putShort("SpawnCount", (short) 4);
                block_entity.putShort("SpawnRange", (short) 4);

                CompoundTag spawnData = new CompoundTag();
                CompoundTag entity = new CompoundTag();
                entity.putString("id", "minecraft:pig");
                spawnData.put("entity", entity);

                block_entity.put("SpawnData", spawnData);

            }

            //Furnace
            case 61, 62 -> {

                block_entity.put("Items", new ListTag<>(CompoundTag.class));
                block_entity.put("RecipesUsed", new ListTag<>(CompoundTag.class));
                block_entity.putShort("BurnTime", (short) 0);
                block_entity.putShort("CookTime", (short) 0);
                block_entity.putShort("CookTimeTotal", (short) 200);
                block_entity.putShort("BurnTime", (short) 0);

            }

            //Sign
            case 63, 68 -> {

                block_entity.putBoolean("GlowingText", false);
                block_entity.putString("Color", "black");
                block_entity.putString("Text1", tile_entity.getString("Text1"));
                block_entity.putString("Text2", tile_entity.getString("Text2"));
                block_entity.putString("Text3", tile_entity.getString("Text3"));
                block_entity.putString("Text4", tile_entity.getString("Text4"));

            }

            //Brewing Stand
            case 117 -> {

                block_entity.put("Items", new ListTag<>(CompoundTag.class));
                block_entity.putShort("BrewTime", (short) 0);
                block_entity.putByte("Fuel", (byte) 0);

            }

            //Command Block
            case (byte) 137, (byte) 210, (byte) 211 -> {

                block_entity.putBoolean("auto", tile_entity.containsKey("auto") ? tile_entity.getBoolean("auto") : false);
                block_entity.putString("Command", getCommand(tile_entity.getString("Command")));
                block_entity.putBoolean("conditionMet", tile_entity.containsKey("conditionMet") ? tile_entity.getBoolean("conditionMet") : false);
                block_entity.putLong("LastExecution", tile_entity.containsKey("LastExecution") ? tile_entity.getLong("LastExecution") : 0);
                block_entity.putString("LastOutput", "");
                block_entity.putBoolean("powered", tile_entity.containsKey("powered") ? tile_entity.getBoolean("powered") : false);
                block_entity.putInt("SuccessCount", tile_entity.containsKey("SuccessCount") ? tile_entity.getInt("SuccessCount") : 0);
                block_entity.putBoolean("TrackOutput", tile_entity.containsKey("TrackOutput") ? tile_entity.getBoolean("TrackOutput") : true);
                block_entity.putBoolean("UpdateLastExecution", tile_entity.containsKey("UpdateLastExecution") ? tile_entity.getBoolean("UpdateLastExecution") : true);

            }

            //Beacon
            case (byte) 138 -> {

                int level = 0;
                if(tile_entity.containsKey("Levels"))
                    level = tile_entity.getInt("Levels");
                int primary = -1;
                if(tile_entity.containsKey("Primary"))
                    primary = tile_entity.getInt("Primary");
                int secondary = -1;
                if(tile_entity.containsKey("Secondary"))
                    secondary = tile_entity.getInt("Secondary");

                block_entity.putInt("Levels", level);
                block_entity.putInt("Primary", primary);
                block_entity.putInt("Secondary", secondary);
            }

            //Redstone Comparator
            case (byte) 149, (byte) 150 -> block_entity.putInt("OutputSignal", 0);

            //Hopper
            case (byte) 154 -> {

                block_entity.put("Items", new ListTag<>(CompoundTag.class));
                block_entity.putInt("TransferCooldown", 0);

            }

            //Banners
            case (byte) 176, (byte) 177 -> block_entity.put("Patterns", new ListTag<>(CompoundTag.class));

            //End Gateway
            case (byte) 209 -> {

                block_entity.putLong("Age", 0);
                block_entity.putBoolean("ExactTeleport", true);

                CompoundTag exitPortal = new CompoundTag();
                exitPortal.putInt("X", 0);
                exitPortal.putInt("Y", 1);
                exitPortal.putInt("Z", 0);

                block_entity.put("ExitPortal", exitPortal);

            }

            //Structure Block
            case (byte) 255 -> {

                block_entity.putString("author", "?");
                block_entity.putBoolean("ignoreEntities", true);
                block_entity.putFloat("integrity", 1);
                block_entity.putString("metadata", "");
                block_entity.putString("mirror", "NONE");
                block_entity.putString("mode", "DATA");
                block_entity.putString("name", "");
                block_entity.putInt("posX", 0);
                block_entity.putInt("posY", 1);
                block_entity.putInt("posZ", 0);
                block_entity.putBoolean("powered", false);
                block_entity.putString("rotation", "NONE");
                block_entity.putLong("seed", 0);
                block_entity.putBoolean("showboundingbox", false);
                block_entity.putInt("sizeX", 1);
                block_entity.putInt("sizeY", 1);
                block_entity.putInt("sizeZ", 1);

            }

            //Other block entities which only have default values.
            //Includes: Bed, Enchantment Table, End Portal, Ender Chest, Daylight Detector

        }

        return block_entity;

    }

    //Get the block states of a block.
    public static CompoundTag getBlockStates(byte id, byte data) {

        CompoundTag block_states = new CompoundTag();

        switch (id) {

            //Anvil (all types)
            case (byte) 145 -> {

                switch (data) {
                    case 0, 4, 8 -> block_states.putString("facing", "south");
                    case 1, 5, 9 -> block_states.putString("facing", "west");
                    case 2, 6, 10 -> block_states.putString("facing", "north");
                    case 3, 7, 11 -> block_states.putString("facing", "east");
                }
            }

            //Standing Banner
            case (byte) 176 -> block_states.putString("rotation", String.valueOf(data));

            //Wall Banner and Mob Heads
            case (byte) 177, (byte) 144 -> {

                switch (data) {

                    case 2 -> block_states.putString("facing", "north");
                    case 3 -> block_states.putString("facing", "south");
                    case 4 -> block_states.putString("facing", "west");
                    case 5 -> block_states.putString("facing", "east");

                }
            }

            //Bed (all colours)
            case 26 -> {

                //part
                switch (data) {

                    case 0, 1, 2, 3, 4, 5, 6, 7 -> block_states.putString("part", "foot");
                    case 8, 9, 10, 11, 12, 13, 14, 15 -> block_states.putString("part", "head");

                }

                //facing
                switch (data) {

                    case 0, 4, 8, 12 -> block_states.putString("facing", "south");
                    case 1, 5, 9, 13 -> block_states.putString("facing", "west");
                    case 2, 6, 10, 14 -> block_states.putString("facing", "north");
                    case 3, 7, 11, 15 -> block_states.putString("facing", "east");

                }

                //occupied
                block_states.putString("occupied", "false");

            }

            //Bone Block
            case (byte) 216 -> {

                switch (data) {

                    case 0, 1, 2, 3, 12, 13, 14, 15 -> block_states.putString("axis", "y");
                    case 4, 5, 6, 7 -> block_states.putString("axis", "x");
                    case 8, 9, 10, 11 -> block_states.putString("axis", "z");

                }
            }

            //Brewing Stand
            case 117 -> {

                //Has Bottle 0
                switch (data) {

                    case 0, 2, 4, 6, 8, 10, 12, 14 -> block_states.putString("has_bottle_0", "false");
                    case 1, 3, 5, 7, 9, 11, 13, 15 -> block_states.putString("has_bottle_0", "true");

                }

                //Has Bottle 1
                switch (data) {

                    case 0, 1, 4, 5, 8, 9, 12, 13 -> block_states.putString("has_bottle_1", "false");
                    case 2, 3, 6, 7, 10, 11, 14, 15 -> block_states.putString("has_bottle_1", "true");

                }

                //Has Bottle 2
                switch (data) {

                    case 0, 1, 2, 3, 8, 9, 10, 11 -> block_states.putString("has_bottle_2", "false");
                    case 4, 5, 6, 7, 12, 13, 14, 15 -> block_states.putString("has_bottle_2", "true");

                }
            }

            //Stone/Wooden Button
            case 77, (byte) 143 -> {

                //face
                switch (data) {

                    case 0, 8 -> block_states.putString("face", "ceiling");
                    case 1, 2, 3, 4, 9, 10, 11, 12 -> block_states.putString("face", "wall");
                    case 5, 6, 7, 13, 14, 15 -> block_states.putString("face", "floor");

                }

                //facing
                switch (data) {

                    case 0, 4, 5, 6, 7, 8, 12, 13, 14, 15 -> block_states.putString("facing", "north");
                    case 1, 9 -> block_states.putString("facing", "east");
                    case 2, 10 -> block_states.putString("facing", "west");
                    case 3, 11 -> block_states.putString("facing", "south");

                }

                //powered
                block_states.putString("powered", "false");

            }

            //Cactus
            case 81 -> block_states.putString("age", "0");

            //Cake
            case 92 -> {
                block_states.putString("bites", String.valueOf(data));
                block_states.putString("lit", "false");
            }

            //Cauldron
            case 118 -> block_states.putString("level", String.valueOf(data));

            //(Trapped) Chest
            case 54, (byte) 146 -> {

                //facing
                switch (data) {

                    case 0, 1, 2, 6, 7, 8, 12, 13, 14 -> block_states.putString("facing", "north");
                    case 3, 9, 15 -> block_states.putString("facing", "south");
                    case 4, 10 -> block_states.putString("facing", "west");
                    case 5, 11 -> block_states.putString("facing", "east");

                }

                //type
                block_states.putString("type", "single");

                //waterlogged
                block_states.putString("waterlogged", "false");

            }

            //Ender Chest
            case (byte) 130 -> {

                //facing
                switch (data) {

                    case 0, 1, 2, 6, 7, 8, 12, 13, 14 -> block_states.putString("facing", "north");
                    case 3, 9, 15 -> block_states.putString("facing", "south");
                    case 4, 10 -> block_states.putString("facing", "west");
                    case 5, 11 -> block_states.putString("facing", "east");

                }

                //waterlogged
                block_states.putString("waterlogged", "false");

            }

            //Chorus Flower
            case (byte) 200 -> block_states.putString("age", String.valueOf(data));

            //Chorus Plant
            case (byte) 199 -> {

                //down
                block_states.putString("down", "false");

                //east
                block_states.putString("east", "false");

                //north
                block_states.putString("north", "false");

                //south
                block_states.putString("south", "false");

                //up
                block_states.putString("up", "false");

                //west
                block_states.putString("west", "false");

            }

            //Cocoa
            case 127 -> {

                //age
                switch (data) {

                    case 0, 1, 2, 3 -> block_states.putString("age", "0");
                    case 4, 5, 6, 7 -> block_states.putString("age", "1");
                    case 8, 9, 10, 11 -> block_states.putString("age", "2");

                }

                //facing
                switch (data) {

                    case 0, 4, 8 -> block_states.putString("facing", "south");
                    case 1, 5, 9 -> block_states.putString("facing", "west");
                    case 2, 6, 10 -> block_states.putString("facing", "north");
                    case 3, 7, 11 -> block_states.putString("facing", "east");

                }
            }

            //Command Block
            case (byte) 137, (byte) 210, (byte) 211 -> {

                //conditional
                switch (data) {

                    case 0, 1, 2, 3, 4, 5, 6, 7 -> block_states.putString("conditional", "false");
                    case 8, 9, 10, 11, 12, 13, 14, 15 -> block_states.putString("conditional", "true");

                }

                //facing
                switch (data) {

                    case 0, 6, 8, 14 -> block_states.putString("facing", "down");
                    case 1, 7, 9, 15 -> block_states.putString("facing", "up");
                    case 2, 10 -> block_states.putString("facing", "north");
                    case 3, 11 -> block_states.putString("facing", "south");
                    case 4, 12 -> block_states.putString("facing", "west");
                    case 5, 13 -> block_states.putString("facing", "earth");

                }
            }

            //Daylight Sensor
            case (byte) 151 -> {

                //inverted
                block_states.putString("inverted", "false");

                //power
                block_states.putString("power", "0");


            }

            //Inverted Daylight Sensor
            case (byte) 178 -> {

                //inverted
                block_states.putString("inverted", "true");

                //power
                block_states.putString("power", "0");

            }

            //Dispenser and Dropper
            case 23, (byte) 158 -> {

                //facing
                switch (data) {

                    case 0, 6, 8, 14 -> block_states.putString("facing", "down");
                    case 1, 7, 9, 15 -> block_states.putString("facing", "up");
                    case 2, 10 -> block_states.putString("facing", "north");
                    case 3, 11 -> block_states.putString("facing", "south");
                    case 4, 12 -> block_states.putString("facing", "west");
                    case 5, 13 -> block_states.putString("facing", "east");

                }

                //triggered
                block_states.putString("triggered", "false");

            }

            //Doors
            case 64, 71, (byte) 193, (byte) 194, (byte) 195, (byte) 196, (byte) 197 -> {

                //Bottom half
                if (data < 8) {

                    block_states.putString("half", "lower");

                } else {

                    //Top half
                    block_states.putString("half", "upper");

                }

                //facing
                switch (data) {

                    case 1, 5 -> block_states.putString("facing", "south");
                    case 2, 6 -> block_states.putString("facing", "west");
                    case 3, 7 -> block_states.putString("facing", "north");
                    default -> block_states.putString("facing", "east");

                }

                //hinge
                switch (data) {

                    case 8, 10, 12, 14, 4 -> block_states.putString("hinge", "left");
                    default -> block_states.putString("hinge", "right");

                }

                //open
                switch (data) {

                    case 4, 5, 6, 7, 12, 13, 14, 15 -> block_states.putString("open", "true");
                    default -> block_states.putString("open", "false");

                }

                //Powered
                switch (data) {
                    case 10, 11, 14, 15 -> block_states.putString("powered", "true");
                    default -> block_states.putString("powered", "false");
                }
            }

            //End Portal Frame
            case 120 -> {

                //eye
                switch (data) {

                    case 4, 5, 6, 7, 12, 13, 14, 15 -> block_states.putString("eye", "true");
                    default -> block_states.putString("eye", "false");

                }

                //facing
                switch (data) {

                    case 0, 4, 8, 12 -> block_states.putString("facing", "south");
                    case 1, 5, 9, 13 -> block_states.putString("facing", "west");
                    case 2, 6, 10, 14 -> block_states.putString("facing", "north");
                    case 3, 7, 11, 15 -> block_states.putString("facing", "east");

                }
            }

            //End Rod
            case (byte) 198 -> {

                //facing
                switch (data) {

                    case 0, 6, 12 -> block_states.putString("facing", "down");
                    case 1, 7, 13 -> block_states.putString("facing", "up");
                    case 2, 8, 14 -> block_states.putString("facing", "north");
                    case 3, 9, 15 -> block_states.putString("facing", "south");
                    case 4, 10 -> block_states.putString("facing", "west");
                    case 5, 11 -> block_states.putString("facing", "east");

                }
            }

            //Farmland
            case 60 -> {

                switch (data) {

                    //moisture
                    case 0, 8 -> block_states.putString("moisture", "0");
                    case 1, 9 -> block_states.putString("moisture", "1");
                    case 2, 10 -> block_states.putString("moisture", "2");
                    case 3, 11 -> block_states.putString("moisture", "3");
                    case 4, 12 -> block_states.putString("moisture", "4");
                    case 5, 13 -> block_states.putString("moisture", "5");
                    case 6, 14 -> block_states.putString("moisture", "6");
                    case 7, 15 -> block_states.putString("moisture", "7");

                }
            }

            //Fences
            case 85, 113, (byte) 188, (byte) 189, (byte) 190, (byte) 191, (byte) 192 -> {

                block_states.putString("east", "false");
                block_states.putString("north", "false");
                block_states.putString("south", "false");
                block_states.putString("waterlogged", "false");
                block_states.putString("west", "false");

            }

            //Fence Gates
            case 107, (byte) 183, (byte) 184, (byte) 185, (byte) 186, (byte) 187 -> {

                //facing
                switch (data) {

                    case 0, 4, 8, 12 -> block_states.putString("facing", "south");
                    case 1, 5, 9, 13 -> block_states.putString("facing", "west");
                    case 2, 6, 10, 14 -> block_states.putString("facing", "north");
                    case 3, 7, 11, 15 -> block_states.putString("facing", "east");

                }

                //in_wall
                block_states.putString("in_wall", "false");

                //open
                switch (data) {

                    case 4, 5, 6, 7, 12, 13, 14, 15 -> block_states.putString("open", "true");
                    default -> block_states.putString("open", "false");

                }

                //powered
                block_states.putString("powered", "false");

            }

            //Fire
            case 51 -> {

                block_states.putString("age", "0");
                block_states.putString("east", "false");
                block_states.putString("north", "false");
                block_states.putString("south", "false");
                block_states.putString("up", "false");
                block_states.putString("west", "false");

            }

            //Double Plant
            case (byte) 175 -> {

                if (data == 11) {
                    block_states.putString("half", "upper");
                } else {
                    block_states.putString("half", "lower");
                }

            }

            //Frosted Ice
            case (byte) 212 -> {

                if (data < 3) {
                    block_states.putString("age", String.valueOf(data));
                } else {
                    block_states.putString("age", "3");
                }

            }

            //Furnace
            case 61, 62 -> {

                //lit
                if (id == 62) {
                    block_states.putString("lit", "true");
                } else {
                    block_states.putString("lit", "false");
                }

                //facing
                switch (data) {

                    case 3, 9, 15 -> block_states.putString("facing", "south");
                    case 4, 10 -> block_states.putString("facing", "west");
                    case 5, 11 -> block_states.putString("facing", "east");
                    default -> block_states.putString("facing", "north");

                }
            }

            //Glass Panes and Iron Bars
            case 102, (byte) 160, 101 -> {

                block_states.putString("east", "false");
                block_states.putString("north", "false");
                block_states.putString("south", "false");
                block_states.putString("watterlogged", "false");
                block_states.putString("west", "false");

            }

            //Glazed Terracotta, Pumpkin Jack o'Lantern
            case (byte) 235, (byte) 236, (byte) 237, (byte) 238, (byte) 239, (byte) 240, (byte) 241, (byte) 242,
                    (byte) 243, (byte) 244, (byte) 245, (byte) 246, (byte) 247, (byte) 248, (byte) 249,
                    (byte) 250, 91, 86 -> {

                switch (data) {

                    case 0, 4, 8, 12 -> block_states.putString("facing", "south");
                    case 1, 5, 9, 13 -> block_states.putString("facing", "west");
                    case 2, 6, 10, 14 -> block_states.putString("facing", "north");
                    case 3, 7, 11, 15 -> block_states.putString("facing", "east");

                }
            }

            //Grass, Myceleum and Podzol
            case 2, 110, 3 -> {

                if (!(id == 3 && data != 2)) {

                    block_states.putString("snowy", "false");

                }
            }

            //Hay Bale, Purpur Pillar and Logs
            case (byte) 170, (byte) 202, 17, (byte) 162 -> {

                switch (data) {

                    case 4, 5, 6, 7 -> block_states.putString("axis", "x");
                    case 8, 9, 10, 11 -> block_states.putString("axis", "z");
                    default -> block_states.putString("axis", "y");

                }
            }

            //Hopper
            case (byte) 154 -> {

                //enabled
                block_states.putString("enabled", "false");

                //facing
                switch (data) {

                    case 0, 6, 8, 14, 15 -> block_states.putString("facing", "down");
                    case 2, 10 -> block_states.putString("facing", "north");
                    case 3, 11 -> block_states.putString("facing", "south");
                    case 4, 12 -> block_states.putString("facing", "west");
                    case 5, 13 -> block_states.putString("facing", "east");

                }
            }

            //Jukebox
            case 84 -> block_states.putString("has_record", "false");

            //Ladder
            case 65 -> {

                switch (data) {

                    case 3, 9, 15 -> block_states.putString("facing", "south");
                    case 4, 10 -> block_states.putString("facing", "west");
                    case 5, 11 -> block_states.putString("facing", "east");
                    default -> block_states.putString("facing", "north");

                }
            }

            //Lava
            case 10, 11 -> block_states.putString("level", String.valueOf(data));

            //Leaves
            case 18, (byte) 161 -> {

                block_states.putString("distance", "7");
                block_states.putString("persistent", "true");
                block_states.putString("waterlogged", "false");

            }

            //Lever
            case 69 -> {

                //face
                switch (data) {

                    case 0, 7, 8, 15 -> block_states.putString("face", "ceiling");
                    case 5, 6, 13, 14 -> block_states.putString("face", "floor");
                    case 1, 2, 3, 4, 9, 10, 11, 12 -> block_states.putString("face", "wall");

                }

                //facing
                switch (data) {

                    case 1, 9 -> block_states.putString("facing", "east");
                    case 2, 10, 0, 8, 6, 14 -> block_states.putString("facing", "west");
                    case 3, 11 -> block_states.putString("facing", "south");
                    case 4, 12, 7, 5, 13 -> block_states.putString("facing", "north");

                }

                //powered
                if (data >= 8) {
                    block_states.putString("powered", "true");
                } else {
                    block_states.putString("powered", "false");
                }

            }


            //Mushroom Blocks
            case 99, 100 -> {

                //east
                if (data == 3 || data == 6 || data == 9 || data == 10 || data == 14 || data == 15) {
                    block_states.putString("east", "true");
                } else {
                    block_states.putString("east", "false");
                }

                //down
                if (data == 14 || data == 15) {
                    block_states.putString("down", "true");
                } else {
                    block_states.putString("down", "false");
                }

                //north
                if (data == 1 || data == 2 || data == 3 || data == 10 || data == 14 || data == 15) {
                    block_states.putString("north", "true");
                } else {
                    block_states.putString("north", "false");
                }

                //south
                if (data == 7 || data == 8 || data == 9 || data == 10 || data == 14 || data == 15) {
                    block_states.putString("south", "true");
                } else {
                    block_states.putString("south", "false");
                }

                //up
                if (data == 0 || data == 10) {
                    block_states.putString("up", "false");
                } else {
                    block_states.putString("up", "true");
                }

                //west
                if (data == 1 || data == 4 || data == 7 || data == 10 || data == 14 || data == 15) {
                    block_states.putString("west", "true");
                } else {
                    block_states.putString("west", "false");
                }
            }

            //Nether Portal
            case 90 -> {
                if (data == 1) {
                    block_states.putString("axis", "z");
                } else {
                    block_states.putString("axis", "x");
                }
            }

            //Note Block
            case 25 -> {
                block_states.putString("instrument", "harp");
                block_states.putString("note", "0");
                block_states.putString("powered", "false");
            }

            //Observer
            case (byte) 218 -> {

                //facing
                switch (data) {

                    case 0, 8 -> block_states.putString("facing", "down");
                    case 1, 9 -> block_states.putString("facing", "up");
                    case 2, 10 -> block_states.putString("facing", "north");
                    case 3, 11 -> block_states.putString("facing", "south");
                    case 4, 12 -> block_states.putString("facing", "west");
                    case 5, 13 -> block_states.putString("facing", "east");

                }

                //powered
                block_states.putString("powered", "false");

            }

            //Pistons
            case 29, 33 -> {

                //extended
                if (data >= 8) {
                    block_states.putString("extended", "true");
                } else {
                    block_states.putString("extended", "false");
                }

                //facing
                switch (data) {

                    case 0, 8 -> block_states.putString("facing", "down");
                    case 1, 9 -> block_states.putString("facing", "up");
                    case 2, 10 -> block_states.putString("facing", "north");
                    case 3, 11 -> block_states.putString("facing", "south");
                    case 4, 12 -> block_states.putString("facing", "west");
                    case 5, 13 -> block_states.putString("facing", "east");

                }
            }

            //Piston Head
            case 34 -> {

                //facing
                switch (data) {

                    case 0, 8 -> block_states.putString("facing", "down");
                    case 1, 9 -> block_states.putString("facing", "up");
                    case 2, 10 -> block_states.putString("facing", "north");
                    case 3, 11 -> block_states.putString("facing", "south");
                    case 4, 12 -> block_states.putString("facing", "west");
                    case 5, 13 -> block_states.putString("facing", "east");

                }

                //short
                block_states.putString("short", "false");

                //type
                if (data >= 8) {
                    block_states.putString("type", "sticky");
                } else {
                    block_states.putString("type", "normal");
                }
            }

            //Potatoes, Carrots, Beetroots, Nether Wart, Melon/Pumpkin Stem, Sugar Canes and Wheat
            case (byte) 142, (byte) 141, 59, (byte) 207, 115, 104, 105, 83 -> block_states.putString("age", String.valueOf(data));

            //Pressure Plates
            case 70, 72, (byte) 147, (byte) 148 -> block_states.putString("powered", "false");

            //Quartz Pillar
            case (byte) 155 -> {
                switch (data) {

                    case 2 -> block_states.putString("axis", "y");
                    case 3 -> block_states.putString("axis", "x");
                    case 4 -> block_states.putString("axis", "z");

                }
            }

            //Rail
            case 66 -> {

                //shape
                switch (data) {

                    case 0 -> block_states.putString("shape", "north_south");
                    case 1 -> block_states.putString("shape", "east_west");
                    case 2 -> block_states.putString("shape", "ascending_east");
                    case 3 -> block_states.putString("shape", "ascending_west");
                    case 4 -> block_states.putString("shape", "ascending_north");
                    case 5 -> block_states.putString("shape", "ascending_south");
                    case 6 -> block_states.putString("shape", "south_east");
                    case 7 -> block_states.putString("shape", "south_west");
                    case 8 -> block_states.putString("shape", "north_west");
                    case 9 -> block_states.putString("shape", "north_east");

                }

                //waterlogged
                block_states.putString("waterlogged", "false");

            }

            //Activator Rail, Detector Rail and Powered Rail
            case 27, 28, (byte) 157 -> {

                //powered
                if (data >= 8) {
                    block_states.putString("powered", "true");
                } else {
                    block_states.putString("powered", "false");
                }

                //shape
                switch (data) {

                    case 0, 8 -> block_states.putString("shape", "north_south");
                    case 1, 9 -> block_states.putString("shape", "east_west");
                    case 2, 10 -> block_states.putString("shape", "ascending_east");
                    case 3, 11 -> block_states.putString("shape", "ascending_west");
                    case 4, 12 -> block_states.putString("shape", "ascending_north");
                    case 5, 13 -> block_states.putString("shape", "ascending_south");

                }

                //waterlogged
                block_states.putString("waterlogged", "false");

            }

            //Redstone Comparator
            case (byte) 149, (byte) 150 -> {

                //facing
                switch (data) {

                    case 0, 4, 8, 12 -> block_states.putString("facing", "north");
                    case 1, 5, 9, 13 -> block_states.putString("facing", "east");
                    case 2, 6, 10, 14 -> block_states.putString("facing", "south");
                    case 3, 7, 11, 15 -> block_states.putString("facing", "west");

                }

                //mode
                switch (data) {

                    case 4, 5, 6, 7, 12, 13, 14, 15 -> block_states.putString("mode", "subtract");
                    default -> block_states.putString("mode", "compare");

                }

                //powered
                if (data >= 8) {
                    block_states.putString("powered", "true");
                } else {
                    block_states.putString("powered", "false");
                }
            }

            //Redstone Dust
            case 55 -> {

                block_states.putString("east", "none");
                block_states.putString("north", "none");
                block_states.putString("power", "0");
                block_states.putString("south", "none");
                block_states.putString("west", "none");

            }

            //Redstone Lamp and Ore
            case 123, 73 -> block_states.putString("lit", "false");
            case 124, 74 -> block_states.putString("lit", "true");

            //Redstone Repeater
            case 93, 94 -> {

                //delay
                switch (data) {

                    case 0, 1, 2, 3 -> block_states.putString("delay", "1");
                    case 4, 5, 6, 7 -> block_states.putString("delay", "2");
                    case 8, 9, 10, 11 -> block_states.putString("delay", "3");
                    case 12, 13, 14, 15 -> block_states.putString("delay", "4");

                }

                //facing
                switch (data) {

                    case 0, 4, 8, 12 -> block_states.putString("facing", "south");
                    case 1, 5, 9, 13 -> block_states.putString("facing", "west");
                    case 2, 6, 10, 14 -> block_states.putString("facing", "north");
                    case 3, 7, 11, 15 -> block_states.putString("facing", "east");

                }
            }

            //Redstone Torch
            case 75 -> {

                //lit
                block_states.putString("lit", "false");

                //facing
                switch (data) {

                    case 1 -> block_states.putString("facing", "east");
                    case 2 -> block_states.putString("facing", "west");
                    case 3 -> block_states.putString("facing", "south");
                    case 4 -> block_states.putString("facing", "north");

                }
            }

            case 76 -> {

                //lit
                block_states.putString("lit", "true");

                //facing
                switch (data) {

                    case 1 -> block_states.putString("facing", "west");
                    case 2 -> block_states.putString("facing", "east");
                    case 3 -> block_states.putString("facing", "north");
                    case 4 -> block_states.putString("facing", "south");

                }
            }

            //Saplings
            case 6 -> block_states.putString("stage", "0");

            //Shulker Boxes
            case (byte) 219, (byte) 220, (byte) 221, (byte) 222, (byte) 223, (byte) 224, (byte) 225, (byte) 226,
                (byte) 227, (byte) 228, (byte) 229, (byte) 230, (byte) 231, (byte) 232, (byte) 233, (byte) 234 -> {
                switch (data) {

                    case 0 -> block_states.putString("facing", "down");
                    case 1 -> block_states.putString("facing", "up");
                    case 2 -> block_states.putString("facing", "north");
                    case 3 -> block_states.putString("facing", "south");
                    case 4 -> block_states.putString("facing", "west");
                    case 5 -> block_states.putString("facing", "east");

                }
            }

            //Sign
            case 63 -> {

                //rotation
                block_states.putString("rotation", String.valueOf(data));

                //waterlogged
                block_states.putString("waterlogged", "false");

            }

            case 68 -> {

                //facing
                switch (data) {

                    case 2 -> block_states.putString("facing", "north");
                    case 3 -> block_states.putString("facing", "south");
                    case 4 -> block_states.putString("facing", "west");
                    case 5 -> block_states.putString("facing", "east");

                }

                //waterlogged
                block_states.putString("waterlogged", "false");

            }

            //Slabs
            case 44, 126, (byte) 182, (byte) 205 -> {

                //type
                if (data < 8) {
                    block_states.putString("type", "bottom");
                } else {
                    block_states.putString("type", "top");
                }

                //waterlogged
                block_states.putString("waterlogged", "false");

            }

            case 43, 125, (byte) 181, (byte) 204 -> {

                //type
                if (data < 8) {
                    block_states.putString("type", "double");
                }

                //waterlogged
                block_states.putString("waterlogged", "false");

            }

            //Snow
            case 78 -> block_states.putString("layers", String.valueOf((data + 1)));

            //Stairs
            case 53, 67, 108, 109, 114, (byte) 128, (byte) 134, (byte) 135, (byte) 136,
                    (byte) 156, (byte) 163, (byte) 164, (byte) 180, (byte) 203 -> {

                //facing
                switch (data) {

                    case 0, 4 -> block_states.putString("facing", "east");
                    case 1, 5 -> block_states.putString("facing", "west");
                    case 2, 6 -> block_states.putString("facing", "south");
                    case 3, 7 -> block_states.putString("facing", "north");

                }

                //half
                if (data >= 4) {
                    block_states.putString("half", "top");
                } else {
                    block_states.putString("half", "bottom");
                }

                //shape
                block_states.putString("shape", "straight");

                //waterlogged
                block_states.putString("waterlogged", "false");

            }

            //Structure Block
            case (byte) 255 -> block_states.putString("mode", "data");

            //TNT
            case 46 -> block_states.putString("unstable", "false");

            //Torch
            case 50 -> {
                switch (data) {

                    case 1 -> block_states.putString("facing", "east");
                    case 2 -> block_states.putString("facing", "west");
                    case 3 -> block_states.putString("facing", "south");
                    case 4 -> block_states.putString("facing", "north");

                }
            }

            //Trapdoors
            case 96, (byte) 167 -> {

                //facing
                switch (data) {

                    case 0, 4, 8, 12 -> block_states.putString("facing", "north");
                    case 1, 5, 9, 13 -> block_states.putString("facing", "south");
                    case 2, 6, 10, 14 -> block_states.putString("facing", "west");
                    case 3, 7, 11, 15 -> block_states.putString("facing", "east");

                }

                //half
                if (data >= 8) {
                    block_states.putString("half", "top");
                } else {
                    block_states.putString("half", "bottom");
                }

                //open
                switch (data) {

                    case 4, 5, 6, 7, 12, 13, 14, 15 -> block_states.putString("open", "true");
                    default -> block_states.putString("open", "false");

                }

                //powered
                block_states.putString("powered", "false");

                //waterlogged
                block_states.putString("waterlogged", "false");

            }

            //Tripwire
            case (byte) 132 -> {

                //attached
                switch (data) {

                    case 4, 5, 6, 7, 12, 13, 14, 15 -> block_states.putString("attached", "true");
                    default -> block_states.putString("attached", "false");

                }

                //disarmed
                if (data >= 8) {
                    block_states.putString("disarmed", "true");
                } else {
                    block_states.putString("disarmed", "false");
                }

                //east
                block_states.putString("east", "false");

                //north
                block_states.putString("north", "false");

                //powered
                if (data % 2 == 1) {
                    block_states.putString("powered", "true");
                } else {
                    block_states.putString("powered", "false");
                }

                //south
                block_states.putString("south", "false");

                //west
                block_states.putString("west", "false");

            }

            //Tripwire Hook
            case (byte) 131 -> {

                //attached
                switch (data) {

                    case 4, 5, 6, 7, 12, 13, 14, 15 -> block_states.putString("attached", "true");
                    default -> block_states.putString("attached", "false");

                }

                //facing
                switch (data) {

                    case 0, 4, 8, 12 -> block_states.putString("facing", "south");
                    case 1, 5, 9, 13 -> block_states.putString("facing", "west");
                    case 2, 6, 10, 14 -> block_states.putString("facing", "north");
                    case 3, 7, 11, 15 -> block_states.putString("facing", "east");

                }

                //powered
                if (data >= 8) {
                    block_states.putString("powered", "true");
                } else {
                    block_states.putString("powered", "false");
                }

            }

            //Vines
            case 106 -> {

                //east
                if ((data & (byte) 8) == (byte) 8) {
                    block_states.putString("east", "true");
                } else {
                    block_states.putString("east", "false");
                }

                //north
                if ((data & (byte) 4) == (byte) 4) {
                    block_states.putString("north", "true");
                } else {
                    block_states.putString("north", "false");
                }

                //south
                if (data % 2 == 1) {
                    block_states.putString("south", "true");
                } else {
                    block_states.putString("false", "true");
                }

                //up
                block_states.putString("up", "false");

                //west
                if ((data & (byte) 2) == (byte) 2) {
                    block_states.putString("west", "true");
                } else {
                    block_states.putString("west", "false");
                }
            }

            //Walls
            case (byte) 139 -> {

                block_states.putString("east", "false");
                block_states.putString("north", "false");
                block_states.putString("south", "false");
                block_states.putString("up", "false");
                block_states.putString("waterlogged", "false");
                block_states.putString("west", "false");

            }

            //Water
            case 8, 9 -> block_states.putString("level", String.valueOf(data));

        }


        return block_states;
    }

    //Get the 1.18.2 block name.
    public static String getBlockName(byte id, byte data) {

        switch (id) {

            //Air
            case 0 -> {
                return "air";
            }

            //Stone
            case 1 -> {
                switch (data) {

                    case 0 -> {
                        return "stone";
                    }

                    case 1 -> {
                        return "granite";
                    }

                    case 2 -> {
                        return "polished_granite";
                    }

                    case 3 -> {
                        return "diorite";
                    }

                    case 4 -> {
                        return "polished_diorite";
                    }

                    case 5 -> {
                        return "andesite";
                    }

                    case 6 -> {
                        return "polished_andesite";
                    }
                }
            }

            //Grass
            case 2 -> {
                return "grass_block";
            }

            //Dirt
            case 3 -> {
                switch (data) {

                    case 0 -> {
                        return "dirt";
                    }

                    case 1 -> {
                        return "coarse_dirt";
                    }

                    case 2 -> {
                        return "podzol";
                    }
                }
            }

            //Cobblestone
            case 4 -> {
                return "cobblestone";
            }

            //Planks
            case 5 -> {
                switch (data) {

                    case 0 -> {
                        return "oak_planks";
                    }

                    case 1 -> {
                        return "spruce_planks";
                    }

                    case 2 -> {
                        return "birch_planks";
                    }

                    case 3 -> {
                        return "jungle_planks";
                    }

                    case 4 -> {
                        return "acacia_planks";
                    }

                    case 5 -> {
                        return "dark_oak_planks";
                    }
                }
            }

            //Sapling
            case 6 -> {
                switch (data) {

                    case 1 -> {
                        return "spruce_sapling";
                    }

                    case 2 -> {
                        return "birch_sapling";
                    }

                    case 3 -> {
                        return "jungle_sapling";
                    }

                    case 4 -> {
                        return "acacia_sapling";
                    }

                    case 5 -> {
                        return "dark_oak_sapling";
                    }

                    default -> {
                        return "oak_sapling";
                    }
                }
            }

            //Bedrock
            case 7 -> {
                return "bedrock";
            }

            //Water
            case 8, 9 -> {
                return "water";
            }

            //Lava
            case 10, 11 -> {
                return "lava";
            }

            //Sand
            case 12 -> {
                switch (data) {

                    case 0 -> {
                        return "sand";
                    }

                    case 1 -> {
                        return "red_sand";
                    }
                }
            }

            //Gravel
            case 13 -> {
                return "gravel";
            }

            //Gold Ore
            case 14 -> {
                return "gold_ore";
            }

            //Iron Ore
            case 15 -> {
                return "iron_ore";
            }

            //Coal Ore
            case 16 -> {
                return "coal_ore";
            }

            //Log and Wood
            case 17 -> {
                switch (data) {

                    case 0, 4, 8 -> {
                        return "oak_log";
                    }

                    case 1, 5, 9 -> {
                        return "spruce_log";
                    }

                    case 2, 6, 10 -> {
                        return "birch_log";
                    }

                    case 3, 7, 11 -> {
                        return "jungle_log";
                    }

                    //Wood 6 sided
                    case 12 -> {
                        return "oak_wood";
                    }

                    case 13 -> {
                        return "spruce_wood";

                    }

                    case 14 -> {
                        return "birch_wood";
                    }

                    case 15 -> {
                        return "jungle_wood";
                    }
                }
            }

            //Leaves
            case 18 -> {
                switch (data) {

                    case 0, 4, 8, 12 -> {
                        return "oak_leaves";
                    }

                    case 1, 5, 9, 13 -> {
                        return "spruce_leaves";
                    }

                    case 2, 6, 10, 14 -> {
                        return "birch_leaves";
                    }

                    case 3, 7, 11, 15 -> {
                        return "jungle_leaves";
                    }
                }
            }

            //Sponge
            case 19 -> {
                switch (data) {

                    case 0 -> {
                        return "sponge";
                    }

                    case 1 -> {
                        return "wet_sponge";
                    }
                }
            }

            //Glass
            case 20 -> {
                return "glass";
            }

            //Lapis Ore
            case 21 -> {
                return "lapis_ore";
            }

            //Lapis Block
            case 22 -> {
                return "lapis_block";
            }

            //Dispenser
            case 23 -> {
                return "dispenser";
            }

            //Sandstone
            case 24 -> {
                switch (data) {

                    case 0 -> {
                        return "sandstone";
                    }

                    case 1 -> {
                        return "chiseled_sandstone";
                    }

                    case 2 -> {
                        return "cut_sandstone";
                    }
                }
            }

            //Noteblock
            case 25 -> {
                return "note_block";
            }

            //Colour of the bed is stored as a block entity, this will be fixed in post-processing.
            //Bed
            case 26 -> {
                return "red_bed";
            }

            //Golden Rail
            case 27 -> {
                return "powered_rail";
            }

            //Detector Rail
            case 28 -> {
                return "detector_rail";
            }

            //Sticky Piston
            case 29 -> {
                return "sticky_piston";
            }

            //Cobweb
            case 30 -> {
                return "cobweb";
            }

            //Tallgrass
            case 31 -> {
                switch (data) {

                    case 0 -> {
                        return "dead_bush";
                    }

                    case 1 -> {
                        return "grass";
                    }

                    case 2 -> {
                        return "fern";
                    }
                }
            }

            //Dead Bush
            case 32 -> {
                return "dead_bush";
            }

            //Piston
            case 33 -> {
                return "piston";
            }

            //Piston Head
            case 34 -> {
                return "piston_head";
            }

            //Wool
            case 35 -> {
                switch (data) {

                    case 0 -> {
                        return "white_wool";
                    }

                    case 1 -> {
                        return "orange_wool";
                    }

                    case 2 -> {
                        return "magenta_wool";
                    }

                    case 3 -> {
                        return "light_blue_wool";
                    }

                    case 4 -> {
                        return "yellow_wool";
                    }

                    case 5 -> {
                        return "lime_wool";
                    }

                    case 6 -> {
                        return "pink_wool";
                    }

                    case 7 -> {
                        return "gray_wool";
                    }

                    case 8 -> {
                        return "light_gray_wool";
                    }

                    case 9 -> {
                        return "cyan_wool";
                    }

                    case 10 -> {
                        return "purple_wool";
                    }

                    case 11 -> {
                        return "blue_wool";
                    }

                    case 12 -> {
                        return "brown_wool";
                    }

                    case 13 -> {
                        return "green_wool";
                    }

                    case 14 -> {
                        return "red_wool";
                    }

                    case 15 -> {
                        return "black_wool";
                    }
                }
            }

            //Yellow Flower
            case 37 -> {
                return "dandelion";
            }

            //Red Flower
            case 38 -> {
                switch (data) {

                    case 0 -> {
                        return "poppy";
                    }

                    case 1 -> {
                        return "blue_orchid";
                    }

                    case 2 -> {
                        return "allium";
                    }

                    case 3 -> {
                        return "azure_bluet";
                    }

                    case 4 -> {
                        return "red_tulip";
                    }

                    case 5 -> {
                        return "orange_tulip";
                    }

                    case 6 -> {
                        return "white_tulip";
                    }

                    case 7 -> {
                        return "pink_tulip";
                    }

                    case 8 -> {
                        return "oxeye_daisy";
                    }
                }
            }

            //Brown Mushroom
            case 39 -> {
                return "brown_mushroom";
            }

            //Red Mushroom
            case 40 -> {
                return "red_mushroom";
            }

            //Gold Block
            case 41 -> {
                return "gold_block";
            }

            //Iron Block
            case 42 -> {
                return "iron_block";
            }

            //Double Stone Slab
            case 43 -> {
                switch (data) {

                    case 0 -> {
                        return "smooth_stone_slab";
                    }

                    case 1 -> {
                        return "sandstone_slab";
                    }

                    case 2 -> {
                        return "oak_slab";
                    }

                    case 3 -> {
                        return "cobblestone_slab";
                    }

                    case 4 -> {
                        return "brick_slab";
                    }

                    case 5 -> {
                        return "stone_brick_slab";
                    }

                    case 6 -> {
                        return "nether_brick_slab";
                    }

                    case 7 -> {
                        return "quartz_slab";
                    }

                    case 8 -> {
                        return "smooth_stone";
                    }

                    case 9 -> {
                        return "smooth_sandstone";
                    }
                }
            }

            //Stone Slab
            case 44 -> {
                switch (data) {

                    case 0, 8 -> {
                        return "smooth_stone_slab";
                    }

                    case 1, 9 -> {
                        return "sandstone_slab";
                    }

                    case 2, 10 -> {
                        return "oak_slab";
                    }

                    case 3, 11 -> {
                        return "cobblestone_slab";
                    }

                    case 4, 12 -> {
                        return "brick_slab";
                    }

                    case 5, 13 -> {
                        return "stone_brick_slab";
                    }

                    case 6, 14 -> {
                        return "nether_brick_slab";
                    }

                    case 7, 15 -> {
                        return "quartz_slab";
                    }
                }
            }

            //Brick Block
            case 45 -> {
                return "bricks";
            }

            //Tnt
            case 46 -> {
                return "tnt";
            }

            //Bookshelf
            case 47 -> {
                return "bookshelf";
            }

            //Moss Stone
            case 48 -> {
                return "mossy_cobblestone";
            }

            //Obsidian
            case 49 -> {
                return "obsidian";
            }

            //Torch
            case 50 -> {
                switch (data) {

                    case 1, 2, 3, 4 -> {
                        return "wall_torch";
                    }

                    default -> {
                        return "torch";
                    }
                }
            }

            //Fire
            case 51 -> {
                return "fire";
            }

            //Mob Spawner
            case 52 -> {
                return "spawner";
            }

            //Oak Stairs
            case 53 -> {
                return "oak_stairs";
            }

            //Chest
            case 54 -> {
                return "chest";
            }

            //Redstone Wire
            case 55 -> {
                return "redstone_wire";
            }

            //Diamond Ore
            case 56 -> {
                return "diamond_ore";
            }

            //Diamond Block
            case 57 -> {
                return "diamond_block";
            }

            //Crafting Table
            case 58 -> {
                return "crafting_table";
            }

            //Wheat Crops
            case 59 -> {
                return "wheat";
            }

            //Farmland
            case 60 -> {
                return "farmland";
            }

            //(Burning) Furnace
            case 61, 62 -> {
                return "furnace";
            }

            //Standing Sign Block
            case 63 -> {
                return "oak_sign";
            }

            //Oak Door Block
            case 64 -> {
                return "oak_door";
            }

            //Ladder
            case 65 -> {
                return "ladder";
            }

            //Rail
            case 66 -> {
                return "rail";
            }

            //Cobblestone Stairs
            case 67 -> {
                return "cobblestone_stairs";
            }

            //Wall Mounted Sign Block
            case 68 -> {
                return "oak_wall_sign";
            }

            //Lever
            case 69 -> {
                return "lever";
            }

            //Stone Pressure Plate
            case 70 -> {
                return "stone_pressure_plate";
            }

            //Iron Door Block
            case 71 -> {
                return "iron_door";
            }

            //Wooden Pressure Plate
            case 72 -> {
                return "oak_pressure_plate";
            }

            //Redstone Ore
            case 73, 74 -> {
                return "redstone_ore";
            }

            //Redstone Torch
            case 75, 76 -> {
                switch (data) {

                    case 1, 2, 3, 4 -> {
                        return "redstone_wall_torch";
                    }

                    default -> {
                        return "redstone_torch";
                    }
                }
            }

            //Stone Button
            case 77 -> {
                return "stone_button";
            }

            //Snow
            case 78 -> {
                return "snow";
            }

            //Ice
            case 79 -> {
                return "ice";
            }

            //Snow Block
            case 80 -> {
                return "snow_block";
            }

            //Cactus
            case 81 -> {
                return "cactus";
            }

            //Clay
            case 82 -> {
                return "clay";
            }

            //Sugar Canes
            case 83 -> {
                return "sugar_cane";
            }

            //Jukebox
            case 84 -> {
                return "jukebox";
            }

            //Oak Fence
            case 85 -> {
                return "oak_fence";
            }

            //Pumpkin
            case 86 -> {
                return "carved_pumpkin";
            }

            //Netherrack
            case 87 -> {
                return "netherrack";
            }

            //Soul Sand
            case 88 -> {
                return "soul_sand";
            }

            //Glowstone
            case 89 -> {
                return "glowstone";
            }

            //Nether Portal
            case 90 -> {
                return "nether_portal";
            }

            //Jack o'Lantern
            case 91 -> {
                return "jack_o_lantern";
            }

            //Cake Block
            case 92 -> {
                return "cake";
            }

            //Redstone Repeater
            case 93, 94 -> {
                return "repeater";
            }

            //Stained Glass
            case 95 -> {

                switch (data) {

                    case 0 -> {
                        return "white_stained_glass";
                    }

                    case 1 -> {
                        return "orange_stained_glass";
                    }

                    case 2 -> {
                        return "magenta_stained_glass";
                    }

                    case 3 -> {
                        return "light_blue_stained_glass";
                    }

                    case 4 -> {
                        return "yellow_stained_glass";
                    }

                    case 5 -> {
                        return "lime_stained_glass";
                    }

                    case 6 -> {
                        return "pink_stained_glass";
                    }

                    case 7 -> {
                        return "gray_stained_glass";
                    }

                    case 8 -> {
                        return "light_gray_stained_glass";
                    }

                    case 9 -> {
                        return "cyan_stained_glass";
                    }

                    case 10 -> {
                        return "purple_stained_glass";
                    }

                    case 11 -> {
                        return "blue_stained_glass";
                    }

                    case 12 -> {
                        return "brown_stained_glass";
                    }

                    case 13 -> {
                        return "green_stain_glass";
                    }

                    case 14 -> {
                        return "red_stained_glass";
                    }

                    case 15 -> {
                        return "black_stained_glass";
                    }
                }
            }

            //Wooden Trapdoor
            case 96 -> {
                return "oak_trapdoor";
            }

            //Monster Egg
            case 97 -> {

                switch (data) {

                    case 0 -> {
                        return "infested_stone";
                    }

                    case 1 -> {
                        return "infested_cobblestone";
                    }

                    case 2 -> {
                        return "infested_stone_bricks";
                    }

                    case 3 -> {
                        return "infested_mossy_stone_bricks";
                    }

                    case 4 -> {
                        return "infested_cracked_stone_bricks";
                    }

                    case 5 -> {
                        return "infested_chiseled_stone_bricks";
                    }
                }
            }

            //Stone Brick
            case 98 -> {
                switch (data) {

                    case 0 -> {
                        return "stone_bricks";
                    }

                    case 1 -> {
                        return "mossy_stone_bricks";
                    }

                    case 2 -> {
                        return "cracked_stone_bricks";
                    }

                    case 3 -> {
                        return "chiseled_stone_bricks";
                    }
                }
            }

            //Brown Mushroom Block
            case 99 -> {
                switch (data) {

                    case 10, 15 -> {
                        return "mushroom_stem";
                    }

                    default -> {
                        return "brown_mushroom_block";
                    }
                }
            }

            //Red Mushroom Block
            case 100 -> {
                switch (data) {

                    case 10, 15 -> {
                        return "mushroom_stem";
                    }

                    default -> {
                        return "red_mushroom_block";
                    }
                }
            }

            //Iron Bars
            case 101 -> {
                return "iron_bars";
            }

            //Glass Pane
            case 102 -> {
                return "glass_pane";
            }

            //Melon Block
            case 103 -> {
                return "melon";
            }

            //Pumpkin Stem
            case 104 -> {
                return "pumpkin_stem";
            }

            //Melon Stem
            case 105 -> {
                return "melon_stem";
            }

            //Vines
            case 106 -> {
                return "vine";
            }

            //Oak Fence Gate
            case 107 -> {
                return "oak_fence_gate";
            }

            //Brick Stairs
            case 108 -> {
                return "brick_stairs";
            }

            //Stone Brick Stairs
            case 109 -> {
                return "stone_brick_stairs";
            }

            //Mycelium
            case 110 -> {
                return "mycelium";
            }

            //Lily Pad
            case 111 -> {
                return "lily_pad";
            }

            //Nether Brick
            case 112 -> {
                return "nether_bricks";
            }

            //Nether Brick Fence
            case 113 -> {
                return "nether_brick_fence";
            }

            //Nether Brick Stairs
            case 114 -> {
                return "nether_brick_stairs";
            }

            //Nether Wart
            case 115 -> {
                return "nether_wart";
            }

            //Enchantment Table
            case 116 -> {
                return "enchanting_table";
            }

            //Brewing Stand
            case 117 -> {
                return "brewing_stand";
            }

            //Cauldron
            case 118 -> {
                return "cauldron";
            }

            //End Portal
            case 119 -> {
                return "end_portal";
            }

            //End Portal Frame
            case 120 -> {
                return "end_portal_frame";
            }

            //End Stone
            case 121 -> {
                return "end_stone";
            }

            //Dragon Egg
            case 122 -> {
                return "dragon_egg";
            }

            //Redstone Lamp
            case 123, 124 -> {
                return "redstone_lamp";
            }

            //Double Wood Slabs
            case 125 -> {
                switch (data) {

                    case 0 -> {
                        return "oak_slab";
                    }

                    case 1 -> {
                        return "spruce_slab";
                    }

                    case 2 -> {
                        return "birch_slab";
                    }

                    case 3 -> {
                        return "jungle_slab";
                    }

                    case 4 -> {
                        return "acacia_slab";
                    }

                    case 5 -> {
                        return "dark_oak_slab";
                    }
                }
            }

            //Wooden Slab
            case 126 -> {
                switch (data) {

                    case 0, 8 -> {
                        return "oak_slab";
                    }

                    case 1, 9 -> {
                        return "spruce_slab";
                    }

                    case 2, 10 -> {
                        return "birch_slab";
                    }

                    case 3, 11 -> {
                        return "jungle_slab";
                    }

                    case 4, 12 -> {
                        return "acacia_slab";
                    }

                    case 5, 13 -> {
                        return "dark_oak_slab";
                    }
                }
            }

            //Cocoa
            case 127 -> {
                return "cocoa";
            }

            //Sandstone Stairs
            case (byte) 128 -> {
                return "sandstone_stairs";
            }

            //Emerald Ore
            case (byte) 129 -> {
                return "emerald_ore";
            }

            //Ender Chest
            case (byte) 130 -> {
                return "ender_chest";
            }

            //Tripwire Hook
            case (byte) 131 -> {
                return "tripwire_hook";
            }

            //Tripwire
            case (byte) 132 -> {
                return "tripwire";
            }

            //Emerald Block
            case (byte) 133 -> {
                return "emerald_block";
            }

            //Spruce Wood Stairs
            case (byte) 134 -> {
                return "spruce_stairs";
            }

            //Birch Wood Stairs
            case (byte) 135 -> {
                return "birch_stairs";
            }

            //Jungle Wood Stairs
            case (byte) 136 -> {
                return "jungle_stairs";
            }

            //Command Block
            case (byte) 137 -> {
                return "command_block";
            }

            //Beacon
            case (byte) 138 -> {
                return "beacon";
            }

            //Cobblestone Wall
            case (byte) 139 -> {
                switch (data) {

                    case 0 -> {
                        return "cobblestone_wall";
                    }

                    case 1 -> {
                        return "mossy_cobblestone_wall";
                    }
                }
            }

            //Flower Pot
            case (byte) 140 -> {
                return "flower_pot";
            }

            //Carrots
            case (byte) 141 -> {
                return "carrots";
            }

            //Potatoes
            case (byte) 142 -> {
                return "potatoes";
            }

            //Wooden Button
            case (byte) 143 -> {
                return "oak_button";
            }

            //Mob Head
            case (byte) 144 -> {
                return "skeleton_skull";
            }

            //Anvil
            case (byte) 145 -> {
                switch (data) {

                    case 0, 1, 2, 3 -> {
                        return "anvil";
                    }

                    case 4, 5, 6, 7 -> {
                        return "chipped_anvil";
                    }

                    case 8, 9, 10, 11 -> {
                        return "damaged_anvil";
                    }
                }
            }

            //Trapped Chest
            case (byte) 146 -> {
                return "trapped_chest";
            }

            //Weighted Pressure Plate (light)
            case (byte) 147 -> {
                return "light_weighted_pressure_plate";
            }

            //Weighted Pressure Plate (heavy)
            case (byte) 148 -> {
                return "heavy_weighted_pressure_plate";
            }

            //Redstone Comporator
            case (byte) 149, (byte) 150 -> {
                return "comparator";
            }

            //Daylight Sensor
            case (byte) 151, (byte) 178 -> {
                return "daylight_detector";
            }

            //Redstone Block
            case (byte) 152 -> {
                return "redstone_block";
            }

            //Nether Quartz Ore
            case (byte) 153 -> {
                return "nether_quartz_ore";
            }

            //Hopper
            case (byte) 154 -> {
                return "hopper";
            }

            //Quartz Block
            case (byte) 155 -> {
                switch (data) {

                    case 0 -> {
                        return "quartz_block";
                    }

                    case 1 -> {
                        return "chiseled_quartz_block";
                    }

                    case 2,3,4 -> {
                        return "quartz_pillar";
                    }
                }
            }

            //Quartz Stairs
            case (byte) 156 -> {
                return "quartz_stairs";
            }

            //Activitor Rail
            case (byte) 157 -> {
                return "activator_rail";
            }

            //Dropper
            case (byte) 158 -> {
                return "dropper";
            }

            //Hardened Clay
            case (byte) 159 -> {
                switch (data) {

                    case 0 -> {
                        return "white_terracotta";
                    }

                    case 1 -> {
                        return "orange_terracotta";
                    }

                    case 2 -> {
                        return "magenta_terracotta";
                    }

                    case 3 -> {
                        return "light_blue_terracotta";
                    }

                    case 4 -> {
                        return "yellow_terracotta";
                    }

                    case 5 -> {
                        return "lime_terracotta";
                    }

                    case 6 -> {
                        return "pink_terracotta";
                    }

                    case 7 -> {
                        return "gray_terracotta";
                    }

                    case 8 -> {
                        return "light_gray_terracotta";
                    }

                    case 9 -> {
                        return "cyan_terracotta";
                    }

                    case 10 -> {
                        return "purple_terracotta";
                    }

                    case 11 -> {
                        return "blue_terracotta";
                    }

                    case 12 -> {
                        return "brown_terracotta";
                    }

                    case 13 -> {
                        return "green_terracotta";
                    }

                    case 14 -> {
                        return "red_terracotta";
                    }

                    case 15 -> {
                        return "black_terracotta";
                    }
                }
            }

            //Stained Glass Panes
            case (byte) 160 -> {
                switch (data) {

                    case 0 -> {
                        return "white_stained_glass_pane";
                    }

                    case 1 -> {
                        return "orange_stained_glass_pane";
                    }

                    case 2 -> {
                        return "magenta_stained_glass_pane";
                    }

                    case 3 -> {
                        return "light_blue_stained_glass_pane";
                    }

                    case 4 -> {
                        return "yellow_stained_glass_pane";
                    }

                    case 5 -> {
                        return "lime_stained_glass_pane";
                    }

                    case 6 -> {
                        return "pink_stained_glass_pane";
                    }

                    case 7 -> {
                        return "gray_stained_glass_pane";
                    }

                    case 8 -> {
                        return "light_gray_stained_glass_pane";
                    }

                    case 9 -> {
                        return "cyan_stained_glass_pane";
                    }

                    case 10 -> {
                        return "purple_stained_glass_pane";
                    }

                    case 11 -> {
                        return "blue_stained_glass_pane";
                    }

                    case 12 -> {
                        return "brown_stained_glass_pane";
                    }

                    case 13 -> {
                        return "green_stained_glass_pane";
                    }

                    case 14 -> {
                        return "red_stained_glass_pane";
                    }

                    case 15 -> {
                        return "black_stained_glass_pane";
                    }
                }
            }

            //Leaves 2
            case (byte) 161 -> {
                switch (data) {

                    case 0, 4, 8, 12 -> {
                        return "acacia_leaves";
                    }

                    case 1, 5, 9, 13 -> {
                        return "dark_oak_leaves";
                    }
                }
            }

            //Log 2
            case (byte) 162 -> {
                switch (data) {

                    case 0, 4, 8 -> {
                        return "acacia_log";
                    }

                    case 1, 5, 9 -> {
                        return "dark_oak_log";
                    }

                    case 12 -> {
                        return "acacia_wood";
                    }

                    case 13 -> {
                        return "dark_oak_wood";
                    }
                }
            }

            //Acacia Wood Stairs
            case (byte) 163 -> {
                return "acacia_stairs";
            }

            //Dark Oak Wood Stairs
            case (byte) 164 -> {
                return "dark_oak_stairs";
            }

            //Slime Block
            case (byte) 165 -> {
                return "slime_block";
            }

            //Barrier
            case (byte) 166 -> {
                return "barrier";
            }

            //Iron Trapdoor
            case (byte) 167 -> {
                return "iron_trapdoor";
            }

            //Prismarine
            case (byte) 168 -> {
                switch (data) {

                    case 0 -> {
                        return "prismarine";
                    }

                    case 1 -> {
                        return "prismarine_bricks";
                    }

                    case 2 -> {
                        return "dark_prismarine";
                    }
                }
            }

            //Sea Lantern
            case (byte) 169 -> {
                return "sea_lantern";
            }

            //Hay Bale
            case (byte) 170 -> {
                return "hay_block";
            }

            //Carpet
            case (byte) 171 -> {
                switch (data) {

                    case 0 -> {
                        return "white_carpet";
                    }

                    case 1 -> {
                        return "orange_carpet";
                    }

                    case 2 -> {
                        return "magenta_carpet";
                    }

                    case 3 -> {
                        return "light_blue_carpet";
                    }

                    case 4 -> {
                        return "yellow_carpet";
                    }

                    case 5 -> {
                        return "lime_carpet";
                    }

                    case 6 -> {
                        return "pink_carpet";
                    }

                    case 7 -> {
                        return "gray_carpet";
                    }

                    case 8 -> {
                        return "light_gray_carpet";
                    }

                    case 9 -> {
                        return "cyan_carpet";
                    }

                    case 10 -> {
                        return "purple_carpet";
                    }

                    case 11 -> {
                        return "blue_carpet";
                    }

                    case 12 -> {
                        return "brown_carpet";
                    }

                    case 13 -> {
                        return "green_carpet";
                    }

                    case 14 -> {
                        return "red_carpet";
                    }

                    case 15 -> {
                        return "black_carpet";
                    }
                }
            }

            //Hardened Clay
            case (byte) 172 -> {
                return "terracotta";
            }

            //Block of Coal
            case (byte) 173 -> {
                return "coal_block";
            }

            //Packed Ice
            case (byte) 174 -> {
                return "packed_ice";
            }

            //Double Plant
            //Top half is converted to the default (sunflower top), this will  be edited in post-processing
            case (byte) 175 -> {
                switch (data) {

                    case 1 -> {
                        return "lilac";
                    }

                    case 2 -> {
                        return "tall_grass";
                    }

                    case 3 -> {
                        return "large_fern";
                    }

                    case 4 -> {
                        return "rose_bush";
                    }

                    case 5 -> {
                        return "peony";
                    }

                    default -> {
                        return "sunflower";
                    }
                }
            }

            //Banner colour is stored in the block entity data, this will be applied post-processing.
            //Free-standing Banner
            case (byte) 176 -> {
                return "white_banner";
            }

            //Banner colour is stored in the block entity data, this will be applied post-processing.
            //Wall-mounted Banner
            case (byte) 177 -> {
                return "white_wall_banner";
            }

            //Red Sandstone
            case (byte) 179 -> {
                switch (data) {

                    case 0 -> {
                        return "red_sandstone";
                    }

                    case 1 -> {
                        return "chiseled_red_sandstone";
                    }

                    case 2 -> {
                        return "cut_red_sandstone";
                    }
                }
            }

            //Red Sandstone Stais
            case (byte) 180 -> {
                return "red_sandstone_stairs";
            }

            //Red Sandstone Slab
            case (byte) 181, (byte) 182 -> {

                if (id == (byte) 181 && data >= 8) {
                    return "smooth_red_sandstone";
                } else {
                    return "red_sandstone_slab";
                }
            }

            //Spruce Fence Gate
            case (byte) 183 -> {
                return "spruce_fence_gate";
            }

            //Birch Fence Gate
            case (byte) 184 -> {
                return "birch_fence_gate";
            }

            //Jungle Fence Gate
            case (byte) 185 -> {
                return "jungle_fence_gate";
            }

            //Dark Oak Fence Gate
            case (byte) 186 -> {
                return "dark_oak_fence_gate";
            }

            //Acacia Fence Gate
            case (byte) 187 -> {
                return "acacia_fence_gate";
            }

            //Spruce Fence
            case (byte) 188 -> {
                return "spruce_fence";
            }

            //Birch Fence
            case (byte) 189 -> {
                return "birch_fence";
            }

            //Jungle Fence
            case (byte) 190 -> {
                return "jungle_fence";
            }

            //Dark Oak Fence
            case (byte) 191 -> {
                return "dark_oak_fence";
            }

            //Acacia Fence
            case (byte) 192 -> {
                return "acacia_fence";
            }

            //Spruce Door
            case (byte) 193 -> {
                return "spruce_door";
            }

            //Birch Door
            case (byte) 194 -> {
                return "birch_door";
            }

            //Jungle Door
            case (byte) 195 -> {
                return "jungle_door";
            }

            //Acacia Door
            case (byte) 196 -> {
                return "acacia_door";
            }

            //Dark Oak Door
            case (byte) 197 -> {
                return "dark_oak_door";
            }

            //End Rod
            case (byte) 198 -> {
                return "end_rod";
            }

            //Chorus Plant
            case (byte) 199 -> {
                return "chorus_plant";
            }

            //Chorus Flower
            case (byte) 200 -> {
                return "chorus_flower";
            }

            //Purpur Block
            case (byte) 201 -> {
                return "purpur_block";
            }

            //Purpur Pillar
            case (byte) 202 -> {
                return "purpur_pillar";
            }

            //Purpur Stairs
            case (byte) 203 -> {
                return "purpur_stairs";
            }

            //Purpur Slab
            case (byte) 204, (byte) 205 -> {
                return "purpur_slab";
            }

            //End Stone Bricks
            case (byte) 206 -> {
                return "end_stone_bricks";
            }

            //Beetroot Block
            case (byte) 207 -> {
                return "beetroots";
            }

            //Grass Path
            case (byte) 208 -> {
                return "dirt_path";
            }

            //End Gateway
            case (byte) 209 -> {
                return "end_gateway";
            }

            //Repeating Command Block
            case (byte) 210 -> {
                return "repeating_command_block";
            }

            //Chain Command Block
            case (byte) 211 -> {
                return "chain_command_block";
            }

            //Frosted Ice
            case (byte) 212 -> {
                return "frosted_ice";
            }

            //Magma Block
            case (byte) 213 -> {
                return "magma_block";
            }

            //Nether Wart Block
            case (byte) 214 -> {
                return "nether_wart_block";
            }

            //Red Nether Brick
            case (byte) 215 -> {
                return "red_nether_bricks";
            }

            //Bone Block
            case (byte) 216 -> {
                return "bone_block";
            }

            //Structure Void
            case (byte) 217 -> {
                return "structure_void";
            }

            //Observer
            case (byte) 218 -> {
                return "observer";
            }

            //White Shulker Box
            case (byte) 219 -> {
                return "white_shulker_box";
            }

            //Orange Shulker Box
            case (byte) 220 -> {
                return "orange_shulker_box";
            }

            //Magenta Shulker Box
            case (byte) 221 -> {
                return "magenta_shulker_box";
            }

            //Light Blue Shulker Box
            case (byte) 222 -> {
                return "light_blue_shulker_box";
            }

            //Yellow Shulker Box
            case (byte) 223 -> {
                return "yellow_shulker_box";
            }

            //Lime Shulker Box
            case (byte) 224 -> {
                return "lime_shulker_box";
            }

            //Pink Shulker Box
            case (byte) 225 -> {
                return "pink_shulker_box";
            }

            //Gray Shulker Box
            case (byte) 226 -> {
                return "gray_shulker_box";
            }

            //Light Gray Shulker Box
            case (byte) 227 -> {
                return "light_gray_shulker_box";
            }

            //Cyan Shulker Box
            case (byte) 228 -> {
                return "cyan_shulker_box";
            }

            //Default Shulker Box
            case (byte) 229 -> {
                return "shulker_box";
            }

            //Blue Shulker Box
            case (byte) 230 -> {
                return "blue_shulker_box";
            }

            //Brown Shulker Box
            case (byte) 231 -> {
                return "brown_shulker_box";
            }

            //Green Shulker Box
            case (byte) 232 -> {
                return "green_shulker_box";
            }

            //Red Shulker Box
            case (byte) 233 -> {
                return "red_shulker_box";
            }

            //Black Shulker Box
            case (byte) 234 -> {
                return "black_shulker_box";
            }

            //White Glazed Terracotta
            case (byte) 235 -> {
                return "white_glazed_terracotta";
            }

            //Orange Glazed Terracotta
            case (byte) 236 -> {
                return "orange_glazed_terracotta";
            }

            //Magenta Glazed Terracotta
            case (byte) 237 -> {
                return "magenta_glazed_terracotta";
            }

            //Light Blue Glazed Terracotta
            case (byte) 238 -> {
                return "light_blue_glazed_terracotta";
            }

            //Yellow Glazed Terracotta
            case (byte) 239 -> {
                return "yellow_glazed_terracotta";
            }

            //Lime Glazed Terracotta
            case (byte) 240 -> {
                return "lime_glazed_terracotta";
            }

            //Pink Glazed Terracotta
            case (byte) 241 -> {
                return "pink_glazed_terracotta";
            }

            //Gray Glazed Terracotta
            case (byte) 242 -> {
                return "gray_glazed_terracotta";
            }

            //Light Gray Glazed Terracotta
            case (byte) 243 -> {
                return "light_gray_glazed_terracotta";
            }

            //Cyan Glazed Terracotta
            case (byte) 244 -> {
                return "cyan_glazed_terracotta";
            }

            //Purple Glazed Terracotta
            case (byte) 245 -> {
                return "purple_glazed_terracotta";
            }

            //Blue Glazed Terracotta
            case (byte) 246 -> {
                return "blue_glazed_terracotta";
            }

            //Brown Glazed Terracotta
            case (byte) 247 -> {
                return "brown_glazed_terracotta";
            }

            //Green Glazed Terracotta
            case (byte) 248 -> {
                return "green_glazed_terracotta";
            }

            //Red Glazed Terracotta
            case (byte) 249 -> {
                return "red_glazed_terracotta";
            }

            //Black Glazed Terracotta
            case (byte) 250 -> {
                return "black_glazed_terracotta";
            }

            //Concrete
            case (byte) 251 -> {
                switch (data) {

                    case 0 -> {
                        return "white_concrete";
                    }

                    case 1 -> {
                        return "orange_concrete";
                    }

                    case 2 -> {
                        return "magenta_concrete";
                    }

                    case 3 -> {
                        return "light_blue_concrete";
                    }

                    case 4 -> {
                        return "yellow_concrete";
                    }

                    case 5 -> {
                        return "lime_concrete";
                    }

                    case 6 -> {
                        return "pink_concrete";
                    }

                    case 7 -> {
                        return "gray_concrete";
                    }

                    case 8 -> {
                        return "light_gray_concrete";
                    }

                    case 9 -> {
                        return "cyan_concrete";
                    }

                    case 10 -> {
                        return "purple_concrete";
                    }

                    case 11 -> {
                        return "blue_concrete";
                    }

                    case 12 -> {
                        return "brown_concrete";
                    }

                    case 13 -> {
                        return "green_concrete";
                    }

                    case 14 -> {
                        return "red_concrete";
                    }

                    case 15 -> {
                        return "black_concrete";
                    }
                }
            }

            //Concrete Powder
            case (byte) 252 -> {
                switch (data) {

                    case 0 -> {
                        return "white_concrete_powder";
                    }

                    case 1 -> {
                        return "orange_concrete_powder";
                    }

                    case 2 -> {
                        return "magenta_concrete_powder";
                    }

                    case 3 -> {
                        return "light_blue_concrete_powder";
                    }

                    case 4 -> {
                        return "yellow_concrete_powder";
                    }

                    case 5 -> {
                        return "lime_concrete_powder";
                    }

                    case 6 -> {
                        return "pink_concrete_powder";
                    }

                    case 7 -> {
                        return "gray_concrete_powder";
                    }

                    case 8 -> {
                        return "light_gray_concrete_powder";
                    }

                    case 9 -> {
                        return "cyan_concrete_powder";
                    }

                    case 10 -> {
                        return "purple_concrete_powder";
                    }

                    case 11 -> {
                        return "blue_concrete_powder";
                    }

                    case 12 -> {
                        return "brown_concrete_powder";
                    }

                    case 13 -> {
                        return "green_concrete_powder";
                    }

                    case 14 -> {
                        return "red_concrete_powder";
                    }

                    case 15 -> {
                        return "black_concrete_powder";
                    }
                }
            }

            case (byte) 255 -> {
                return "structure_block";
            }
        }

        return "air";

    }

    public static String getBlockName(String legacyNamespaceID, CompoundTag blockStates, JSONObject newBlockStates){
        String legacyID = legacyNamespaceID.substring(10);

        Set<String> depreciatedBlockStates = new HashSet<>();
        String namespaceID = legacyNamespaceID;

        switch (legacyID){
            case "grass":
                namespaceID = "minecraft:grass_block";
                break;
            case "monster_egg":
                depreciatedBlockStates.add("variant");
                namespaceID = "minecraft:" + blockStates.getString("variant");
                break;
            case "stone_slab":
                depreciatedBlockStates.add("variant");
                String variant = blockStates.getString("variant");
                namespaceID = "minecraft:" +(variant.equals("stone") ? "smooth_stone" : variant ) + "_slab";
                break;
            case "double_stone_slab":
                depreciatedBlockStates.add("variant");
                String variant2 = blockStates.getString("variant");
                namespaceID = "minecraft:" + (variant2.equals("stone") ? "smooth_stone" : variant2);
                break;
            case "wooden_slab":
                depreciatedBlockStates.add("variant");
                namespaceID = "minecraft:" + blockStates.getString("variant") + "_slab";
                break;
            case "double_wooden_slab":
                depreciatedBlockStates.add("variant");
                namespaceID = "minecraft:" + blockStates.getString("variant") + "double_slab";
                break;
            case "stonebrick":
                namespaceID = "minecraft:stone_bricks";
                break;
            case "log":
            case "log2":
                depreciatedBlockStates.add("wood_type");
                namespaceID = "minecraft:" + blockStates.getString("wood_type") + "_log";
                break;
            case "planks":
                depreciatedBlockStates.add("wood_type");
                namespaceID = "minecraft:" + blockStates.getString("wood_type") + "_planks";
                break;
            case "red_nether_brick":
                if(blockStates.containsKey("type"))
                    depreciatedBlockStates.add("type");
                namespaceID = legacyNamespaceID + "s";
                break;
            case "mossy_cobblestone":
                namespaceID = "minecraft:mossy_stone_bricks";
                break;
            case "concrete":
                depreciatedBlockStates.add("color");
                namespaceID = "minecraft:" + blockStates.getString("color") + "_concrete";
                break;
            case "concrete_powder":
                depreciatedBlockStates.add("color");
                namespaceID = "minecraft:" + blockStates.getString("color") + "_concrete_powder";
                break;
            case "hardened_clay":
                namespaceID = "minecraft:terracotta";
                break;
            case "stained_hardened_clay":
                depreciatedBlockStates.add("color");
                namespaceID = "minecraft:" + blockStates.getString("color") + "_terracotta";
                break;
            case "leaves":
            case "leaves2":
                if(blockStates.containsKey("decayable")) {
                    depreciatedBlockStates.add("decayable");
                    newBlockStates.put("persistent", blockStates.getBoolean("decayable"));
                }
                if(blockStates.containsKey("check_decay")){
                    depreciatedBlockStates.add("check_decay");
                    newBlockStates.put("persistent", !blockStates.getBoolean("check_decay"));
                    newBlockStates.put("distance", 7);
                }

                depreciatedBlockStates.add("variant");
                namespaceID = "minecraft:" + blockStates.getString("variant") + "_leaves";
                break;
        }

        for(String blockState : depreciatedBlockStates)
            blockStates.remove(blockState);

        TagConv.getCompoundTagProperties(blockStates, newBlockStates);

        return namespaceID;
    }

    public static byte getLegacyBlockID(String legacyID) {
        if(legacyID.contains("bed"))
            return 26;

        if(legacyID.contains("wool"))
            return 35;

        switch (legacyID) {

            //Air
            case "air" -> {
                return 0;
            }

            //Stone
            case "stone" -> {
                return 1;
            }

            //Grass
            case "grass" -> {
                return 2;
            }

            //Dirt
            case "dirt" -> {
                return 3;
            }

            //Cobblestone
            case "cobblestone" -> {
                return 4;
            }

            //Planks
            case "planks" -> {
                return 5;
            }

            //Sapling
            case "sapling" -> {
                return 6;
            }

            //Bedrock
            case "bedrock" -> {
                return 7;
            }

            //Water
            case "flowing_water" -> {
                return 8;
            }

            case "water" -> {
                return 9;
            }

            //Lava
            case "flowing_lava" -> {
                return 10;
            }

            case "lava" -> {
                return 11;
            }

            //Sand
            case "sand" -> {
                return 12;
            }

            //Gravel
            case "gravel" -> {
                return 13;
            }

            //Gold Ore
            case "gold_ore" -> {
                return 14;
            }

            //Iron Ore
            case "iron_ore" -> {
                return 15;
            }

            //Coal Ore
            case "coal_ore" -> {
                return 16;
            }

            //Log and Wood
            case "log" -> {
                return 17;
            }

            //Leaves
            case "leaves" -> {
                return 18;
            }

            //Sponge
            case "sponge" -> {
                return 19;
            }

            //Glass
            case "glass" -> {
                return 20;
            }

            //Lapis Ore
            case "lapis_ore" -> {
                return 21;
            }

            //Lapis Block
            case "lapis_block" -> {
                return 22;
            }

            //Dispenser
            case "dispenser" -> {
                return 23;
            }

            //Sandstone
            case "sandstone" -> {
                return 24;
            }

            //Noteblock
            case "noteblock" -> {
                return 25;
            }

            //Golden Rail
            case "golden_rail" -> {
                return 27;
            }

            //Detector Rail
            case "detector_rail" -> {
                return 28;
            }

            //Sticky Piston
            case "sticky_piston" -> {
                return 29;
            }

            //Cobweb
            case "web" -> {
                return 30;
            }

            //Tallgrass
            case "tallgrass" -> {
                return 31;
            }

            //Dead Bush
            case "deadbush" -> {
                return 32;
            }

            //Piston
            case "piston" -> {
                return 33;
            }

            //Piston Head
            case "piston_head" -> {
                return 34;
            }

            //Yellow Flower
            case "yellow_flower" -> {
                return 37;
            }

            //Red Flower
            case "red_flower" -> {
                return 38;
            }

            //Brown Mushroom
            case "brown_mushroom" -> {
                return 39;
            }

            //Red Mushroom
            case "red_mushroom" -> {
                return 40;
            }

            //Gold Block
            case "gold_block" -> {
                return 41;
            }

            //Iron Block
            case "iron_block" -> {
                return 42;
            }

            //Double Stone Slab
            case "double_stone_slab" -> {
                return 43;
            }

            //Stone Slab
            case "stone_slab" -> {
                return 44;
            }

            //Brick Block
            case "brick_block" -> {
                return 45;
            }

            //Tnt
            case "tnt" -> {
                return 46;
            }

            //Bookshelf
            case "bookshelf" -> {
                return 47;
            }

            //Moss Stone
            case "mossy_cobblestone" -> {
                return 48;
            }

            //Obsidian
            case "obsidian" -> {
                return 49;
            }

            //Torch
            case "torch" -> {
                return 50;
            }

            //Fire
            case "fire" -> {
                return 51;
            }

            //Mob Spawner
            case "mob_spawner" -> {
                return 52;
            }

            //Oak Stairs
            case "oak_stairs" -> {
                return 53;
            }

            //Chest
            case "chest" -> {
                return 54;
            }

            //Redstone Wire
            case "redstone_wire" -> {
                return 55;
            }

            //Diamond Ore
            case "diamond_ore" -> {
                return 56;
            }

            //Diamond Block
            case "diamond_block" -> {
                return 57;
            }

            //Crafting Table
            case "crafting_table" -> {
                return 58;
            }

            //Wheat Crops
            case "wheat" -> {
                return 59;
            }

            //Farmland
            case "farmland" -> {
                return 60;
            }

            //(Burning) Furnace
            case "furnace" -> {
                return 61;
            }

            case "lit_furnace" -> {
                return 62;
            }

            //Standing Sign Block
            case "standing_sign" -> {
                return 63;
            }

            //Oak Door Block
            case "wooden_door" -> {
                return 64;
            }

            //Ladder
            case "ladder" -> {
                return 65;
            }

            //Rail
            case "rail" -> {
                return 66;
            }

            //Cobblestone Stairs
            case "cobblestone_stairs" -> {
                return 67;
            }

            //Wall Mounted Sign Block
            case "wall_sign" -> {
                return 68;
            }

            //Lever
            case "lever" -> {
                return 69;
            }

            //Stone Pressure Plate
            case "stone_pressure_plate" -> {
                return 70;
            }

            //Iron Door Block
            case "iron_door" -> {
                return 71;
            }

            //Wooden Pressure Plate
            case "wooden_pressure_plate" -> {
                return 72;
            }

            //Redstone Ore
            case "redstone_ore" -> {
                return 73;
            }

            case "lit_redstone_ore" -> {
                return 74;
            }

            //Redstone Torch
            case "unlit_redstone_torch" -> {
                return 75;
            }

            case "redstone_torch" -> {
                return 76;
            }

            //Stone Button
            case "stone_button" -> {
                return 77;
            }

            //Snow
            case "snow_layer" -> {
                return 78;
            }

            //Ice
            case "ice" -> {
                return 79;
            }

            //Snow Block
            case "snow" -> {
                return 80;
            }

            //Cactus
            case "cactus" -> {
                return 81;
            }

            //Clay
            case "clay" -> {
                return 82;
            }

            //Sugar Canes
            case "reeds" -> {
                return 83;
            }

            //Jukebox
            case "jukebox" -> {
                return 84;
            }

            //Oak Fence
            case "fence" -> {
                return 85;
            }

            //Pumpkin
            case "pumpkin" -> {
                return 86;
            }

            //Netherrack
            case "netherrack" -> {
                return 87;
            }

            //Soul Sand
            case "soul_sand" -> {
                return 88;
            }

            //Glowstone
            case "glowstone" -> {
                return 89;
            }

            //Nether Portal
            case "portal" -> {
                return 90;
            }

            //Jack o'Lantern
            case "lit_pumpkin" -> {
                return 91;
            }

            //Cake Block
            case "cake" -> {
                return 92;
            }

            //Redstone Repeater
            case "unpowered_repeater" -> {
                return 93;
            }
            case "powered_repeater" -> {
                return 94;
            }

            //Stained Glass
            case "stained_glass" -> {
                return 95;
            }

            //Wooden Trapdoor
            case "trapdoor" -> {
                return 96;
            }

            //Monster Egg
            case "monster_egg" -> {
                return 97;
            }

            //Stone Brick
            case "stonebrick" -> {
                return 98;
            }

            //Brown Mushroom Block
            case "brown_mushroom_block" -> {
                return 99;
            }

            //Red Mushroom Block
            case "red_mushroom_block" -> {
                return 100;
            }

            //Iron Bars
            case "iron_bars" -> {
                return 101;
            }

            //Glass Pane
            case "glass_pane" -> {
                return 102;
            }

            //Melon Block
            case "melon_block" -> {
                return 103;
            }

            //Pumpkin Stem
            case "pumpkin_stem" -> {
                return 104;
            }

            //Melon Stem
            case "melon_stem" -> {
                return 105;
            }

            //Vines
            case "vine" -> {
                return 106;
            }

            //Oak Fence Gate
            case "fence_gate" -> {
                return 107;
            }

            //Brick Stairs
            case "brick_stairs" -> {
                return 108;
            }

            //Stone Brick Stairs
            case "stone_brick_stairs" -> {
                return 109;
            }

            //Mycelium
            case "mycelium" -> {
                return 110;
            }

            //Lily Pad
            case "waterlily" -> {
                return 111;
            }

            //Nether Brick
            case "nether_brick" -> {
                return 112;
            }

            //Nether Brick Fence
            case "nether_brick_fence" -> {
                return 113;
            }

            //Nether Brick Stairs
            case "nether_brick_stairs" -> {
                return 114;
            }

            //Nether Wart
            case "nether_wart" -> {
                return 115;
            }

            //Enchantment Table
            case "enchanting_table" -> {
                return 116;
            }

            //Brewing Stand
            case "brewing_stand" -> {
                return 117;
            }

            //Cauldron
            case "cauldron" -> {
                return 118;
            }

            //End Portal
            case "end_portal" -> {
                return 119;
            }

            //End Portal Frame
            case "end_portal_frame" -> {
                return 120;
            }

            //End Stone
            case "end_stone" -> {
                return 121;
            }

            //Dragon Egg
            case "dragon_egg" -> {
                return 122;
            }

            //Redstone Lamp
            case "redstone_lamp" -> {
                return 123;
            }
            case "lit_redstone_lamp" -> {
                return 124;
            }

            //Double Wood Slabs
            case "double_wooden_slab" -> {
                return 125;
            }

            //Wooden Slab
            case "wooden_slab" -> {
                return 126;
            }

            //Cocoa
            case "cocoa" -> {
                return 127;
            }

            //Sandstone Stairs
            case "sandstone_stairs" -> {
                return (byte) 128;
            }

            //Emerald Ore
            case "emerald_ore" -> {
                return (byte) 129;
            }

            //Ender Chest
            case "ender_chest" -> {
                return (byte) 130;
            }

            //Tripwire Hook
            case "tripwire_hook" -> {
                return (byte) 131;
            }

            //Tripwire
            case "tripwire" -> {
                return (byte) 132;
            }

            //Emerald Block
            case "emerald_block" -> {
                return (byte) 133;
            }

            //Spruce Wood Stairs
            case "spruce_stairs" -> {
                return (byte) 134;
            }

            //Birch Wood Stairs
            case "birch_stairs" -> {
                return (byte) 135;
            }

            //Jungle Wood Stairs
            case "jungle_stairs" -> {
                return (byte) 136;
            }

            //Command Block
            case "command_block" -> {
                return (byte) 137;
            }

            //Beacon
            case "beacon" -> {
                return (byte) 138;
            }

            //Cobblestone Wall
            case "cobblestone_wall" -> {
                return (byte) 139;
            }

            //Flower Pot
            case "flower_pot" -> {
                return (byte) 140;
            }

            //Carrots
            case "carrots" -> {
                return (byte) 141;
            }

            //Potatoes
            case "potatoes" -> {
                return (byte) 142;
            }

            //Wooden Button
            case "wooden_button" -> {
                return (byte) 143;
            }

            //Mob Head
            case "skull" -> {
                return (byte) 144;
            }

            //Anvil
            case "anvil" -> {
                return (byte) 145;
            }

            //Trapped Chest
            case "trapped_chest" -> {
                return (byte) 146;
            }

            //Weighted Pressure Plate (light)
            case "light_weighted_pressure_plate" -> {
                return (byte) 147;
            }

            //Weighted Pressure Plate (heavy)
            case "heavy_weighted_pressure_plate" -> {
                return (byte) 148;
            }

            //Redstone Comporator
            case "unpowered_comparator" -> {
                return (byte) 149;
            }
            case "powered_comparator" -> {
                return (byte) 150;
            }

            //Daylight Sensor
            case "daylight_detector" -> {
                return (byte) 151;
            }
            case "daylight_detector_inverted" -> {
                return (byte) 178;
            }

            //Redstone Block
            case "redstone_block" -> {
                return (byte) 152;
            }

            //Nether Quartz Ore
            case "quartz_ore" -> {
                return (byte) 153;
            }

            //Hopper
            case "hopper" -> {
                return (byte) 154;
            }

            //Quartz Block
            case "quartz_block" -> {
                return (byte) 155;
            }

            //Quartz Stairs
            case "quartz_stairs" -> {
                return (byte) 156;
            }

            //Activitor Rail
            case "activator_rail" -> {
                return (byte) 157;
            }

            //Dropper
            case "dropper" -> {
                return (byte) 158;
            }

            //Hardened Clay
            case "stained_hardened_clay" -> {
                return (byte) 159;
            }

            //Stained Glass Panes
            case "stained_glass_pane" -> {
                return (byte) 160;
            }

            //Leaves 2
            case "leaves2" -> {
                return (byte) 161;
            }

            //Log 2
            case "log2" -> {
                return (byte) 162;
            }

            //Acacia Wood Stairs
            case "acacia_stairs" -> {
                return (byte) 163;
            }

            //Dark Oak Wood Stairs
            case "dark_oak_stairs" -> {
                return (byte) 164;
            }

            //Slime Block
            case "slime" -> {
                return (byte) 165;
            }

            //Barrier
            case "barrier" -> {
                return (byte) 166;
            }

            //Iron Trapdoor
            case "iron_trapdoor" -> {
                return (byte) 167;
            }

            //Prismarine
            case "prismarine" -> {
                return (byte) 168;
            }

            //Sea Lantern
            case "sea_lantern" -> {
                return (byte) 169;
            }

            //Hay Bale
            case "hay_block" -> {
                return (byte) 170;
            }

            //Carpet
            case "carpet" -> {
                return (byte) 171;
            }

            //Hardened Clay
            case "hardened_clay" -> {
                return (byte) 172;
            }

            //Block of Coal
            case "coal_block" -> {
                return (byte) 173;
            }

            //Packed Ice
            case "packed_ice" -> {
                return (byte) 174;
            }

            //Double Plant
            //Top half is converted to the default (sunflower top), this will  be edited in post-processing
            case "double_plant" -> {
                return (byte) 175;
            }

            //Banner colour is stored in the block entity data, this will be applied post-processing.
            //Free-standing Banner
            case "standing_banner" -> {
                return (byte) 176;
            }

            //Banner colour is stored in the block entity data, this will be applied post-processing.
            //Wall-mounted Banner
            case "wall_banner" -> {
                return (byte) 177;
            }

            //Red Sandstone
            case "red_sandstone" -> {
                return (byte) 179;
            }

            //Red Sandstone Stais
            case "red_sandstone_stairs" -> {
                return (byte) 180;
            }

            //Red Sandstone Slab
            case "double_stone_slab2" -> {
                return (byte) 181;
            }
            case "stone_slab2" -> {
                return (byte) 182;
            }

            //Spruce Fence Gate
            case "spruce_fence_gate" -> {
                return (byte) 183;
            }

            //Birch Fence Gate
            case "birch_fence_gate" -> {
                return (byte) 184;
            }

            //Jungle Fence Gate
            case "jungle_fence_gate" -> {
                return (byte) 185;
            }

            //Dark Oak Fence Gate
            case "dark_oak_fence_gate" -> {
                return (byte) 186;
            }

            //Acacia Fence Gate
            case "acacia_fence_gate" -> {
                return (byte) 187;
            }

            //Spruce Fence
            case "spruce_fence" -> {
                return (byte) 188;
            }

            //Birch Fence
            case "birch_fence" -> {
                return (byte) 189;
            }

            //Jungle Fence
            case "jungle_fence" -> {
                return (byte) 190;
            }

            //Dark Oak Fence
            case "dark_oak_fence" -> {
                return (byte) 191;
            }

            //Acacia Fence
            case "acacia_fence" -> {
                return (byte) 192;
            }

            //Spruce Door
            case "spruce_door" -> {
                return (byte) 193;
            }

            //Birch Door
            case "birch_door" -> {
                return (byte) 194;
            }

            //Jungle Door
            case "jungle_door" -> {
                return (byte) 195;
            }

            //Acacia Door
            case "acacia_door" -> {
                return (byte) 196;
            }

            //Dark Oak Door
            case "dark_oak_door" -> {
                return (byte) 197;
            }

            //End Rod
            case "end_rod" -> {
                return (byte) 198;
            }

            //Chorus Plant
            case "chorus_plant" -> {
                return (byte) 199;
            }

            //Chorus Flower
            case "chorus_flower" -> {
                return (byte) 200;
            }

            //Purpur Block
            case "purpur_block" -> {
                return (byte) 201;
            }

            //Purpur Pillar
            case "purpur_pillar" -> {
                return (byte) 202;
            }

            //Purpur Stairs
            case "purpur_stairs" -> {
                return (byte) 203;
            }

            //Purpur Slab
            case "purpur_double_slab" -> {
                return (byte) 204;
            }
            case "purpur_slab" -> {
                return (byte) 205;
            }

            //End Stone Bricks
            case "end_bricks" -> {
                return (byte) 206;
            }

            //Beetroot Block
            case "beetroots" -> {
                return (byte) 207;
            }

            //Grass Path
            case "grass_path" -> {
                return (byte) 208;
            }

            //End Gateway
            case "end_gateway" -> {
                return (byte) 209;
            }

            //Repeating Command Block
            case "repeating_command_block" -> {
                return (byte) 210;
            }

            //Chain Command Block
            case "chain_command_block" -> {
                return (byte) 211;
            }

            //Frosted Ice
            case "frosted_ice" -> {
                return (byte) 212;
            }

            //Magma Block
            case "magma" -> {
                return (byte) 213;
            }

            //Nether Wart Block
            case "nether_wart_block" -> {
                return (byte) 214;
            }

            //Red Nether Brick
            case "red_nether_bricks" -> {
                return (byte) 215;
            }

            //Bone Block
            case "bone_block" -> {
                return (byte) 216;
            }

            //Structure Void
            case "structure_void" -> {
                return (byte) 217;
            }

            //Observer
            case "observer" -> {
                return (byte) 218;
            }

            //White Shulker Box
            case "white_shulker_box" -> {
                return (byte) 219;
            }

            //Orange Shulker Box
            case "orange_shulker_box" -> {
                return (byte) 220;
            }

            //Magenta Shulker Box
            case "magenta_shulker_box" -> {
                return (byte) 221;
            }

            //Light Blue Shulker Box
            case "light_blue_shulker_box" -> {
                return (byte) 222;
            }

            //Yellow Shulker Box
            case "yellow_shulker_box" -> {
                return (byte) 223;
            }

            //Lime Shulker Box
            case "lime_shulker_box" -> {
                return (byte) 224;
            }

            //Pink Shulker Box
            case "pink_shulker_box" -> {
                return (byte) 225;
            }

            //Gray Shulker Box
            case "gray_shulker_box" -> {
                return (byte) 226;
            }

            //Light Gray Shulker Box
            case "silver_shulker_box" -> {
                return (byte) 227;
            }

            //Cyan Shulker Box
            case "cyan_shulker_box" -> {
                return (byte) 228;
            }

            //Default Shulker Box
            case "purple_shulker_box" -> {
                return (byte) 229;
            }

            //Blue Shulker Box
            case "blue_shulker_box" -> {
                return (byte) 230;
            }

            //Brown Shulker Box
            case "brown_shulker_box" -> {
                return (byte) 231;
            }

            //Green Shulker Box
            case "green_shulker_box" -> {
                return (byte) 232;
            }

            //Red Shulker Box
            case "red_shulker_box" -> {
                return (byte) 233;
            }

            //Black Shulker Box
            case "black_shulker_box" -> {
                return (byte) 234;
            }

            //White Glazed Terracotta
            case "white_glazed_terracotta" -> {
                return (byte) 235;
            }

            //Orange Glazed Terracotta
            case "orange_glazed_terracotta" -> {
                return (byte) 236;
            }

            //Magenta Glazed Terracotta
            case "magenta_glazed_terracotta" -> {
                return (byte) 237;
            }

            //Light Blue Glazed Terracotta
            case "light_blue_glazed_terracotta" -> {
                return (byte) 238;
            }

            //Yellow Glazed Terracotta
            case "yellow_glazed_terracotta" -> {
                return (byte) 239;
            }

            //Lime Glazed Terracotta
            case "lime_glazed_terracotta" -> {
                return (byte) 240;
            }

            //Pink Glazed Terracotta
            case "pink_glazed_terracotta" -> {
                return (byte) 241;
            }

            //Gray Glazed Terracotta
            case "gray_glazed_terracotta" -> {
                return (byte) 242;
            }

            //Light Gray Glazed Terracotta
            case "light_gray_glazed_terracotta" -> {
                return (byte) 243;
            }

            //Cyan Glazed Terracotta
            case "cyan_glazed_terracotta" -> {
                return (byte) 244;
            }

            //Purple Glazed Terracotta
            case "purple_glazed_terracotta" -> {
                return (byte) 245;
            }

            //Blue Glazed Terracotta
            case "blue_glazed_terracotta" -> {
                return (byte) 246;
            }

            //Brown Glazed Terracotta
            case "brown_glazed_terracotta" -> {
                return (byte) 247;
            }

            //Green Glazed Terracotta
            case "green_glazed_terracotta" -> {
                return (byte) 248;
            }

            //Red Glazed Terracotta
            case "red_glazed_terracotta" -> {
                return (byte) 249;
            }

            //Black Glazed Terracotta
            case "black_glazed_terracotta" -> {
                return (byte) 250;
            }

            //Concrete
            case "concrete" -> {
                return (byte) 251;
            }

            //Concrete Powder
            case "concrete_powder" -> {
                return (byte) 252;
            }

            case "structure_block" -> {
                return (byte) 255;
            }
        }

        return 0;

    }

    public static String getBlockSelector(String legacyID, byte data, HashMap<String, String> states) {


        //A data of -1 indicates any data value/state of the block
        if(data == -1){
            switch (legacyID){
                case "dirt" -> {
                    return "#dirt";
                }
                case "planks", "double_wooden_slab" -> {
                    return "#planks";
                }
                case "sapling" -> {
                    return "oak_sapling";
                }
                case "sand" -> {
                    return "#sand";
                }
                case "log", "log2" -> {
                    return "#logs";
                }
                case "bed" -> {
                    return "#beds";
                }
                case "leaves" -> {
                    return "#leaves";
                }
                case "wool" -> {
                    return  "#wool";
                }
                case "red_flower" -> {
                    return "#flowers";
                }
                case "stone_slab" -> {
                    return "#slabs";
                }
                case "standing_sign" -> {
                    return "#signs";
                }
                case "wooden_door" -> {
                    return "oak_door";
                }
                case "stone_stairs" -> {
                    return "cobblestone_stairs";
                }
                case "wall_sign" -> {
                    return "#wall_signs";
                }
                case "wooden_pressure_plate" -> {
                    return "#wooden_pressure_plates";
                }
                case "redstone_ore", "lit_redstone_ore" -> {
                    return "#redstone_ores";
                }
                case "stone_button" -> {
                    return "#stone_buttons";
                }
                case "snow_layer","snow" -> {
                    return "#snow";
                }
                case "ice" -> {
                    return "#ice";
                }
                case "fence" -> {
                    return "#wooden_fences";
                }
                case "portal" -> {
                    return "#portals";
                }
                case "trapdoor" -> {
                    return "#wooden_trapdoors";
                }
                case "stonebrick" -> {
                    return "#stone_bricks";
                }
                case "fence_gate" -> {
                    return "#fence_gates";
                }
                case "cauldron" -> {
                    return "#cauldrons";
                }
                case "wooden_slab" -> {
                    return "#wooden_slabs";
                }
                case "emerald_ore" -> {
                    return "#emerald_ores";
                }
                case "cobblestone_wall" -> {
                    return "#walls";
                }
                case "flower_pot" -> {
                    return "#flower_pots";
                }
                case "wooden_button" -> {
                    return "#wooden_buttons";
                }
                case "anvil" -> {
                    return "#anvil";
                }
                case "stained_hardened_clay" -> {
                    return "terracotta";
                }
                case "leaves2" -> {
                    return "#leaves";
                }
                case "carpet" -> {
                    return "wool_carpets";
                }
                case "hardened_clay" -> {
                    return "#terracotta";
                }
                case "standing_banner", "wall_banner" -> {
                    return "banners";
                }
                case "concrete_powder" -> {
                    return "#concrete_powder";
                }
            }

            if(legacyID.contains("bed"))
                return legacyID;

            byte legacyBlockID = getLegacyBlockID(legacyID);
            return getBlockName(legacyBlockID, (byte) 0);
        }

        CompoundTag blockStates = new CompoundTag();
        String blockName = "";

        if(legacyID.contains("bed")){

            if(legacyID.equals("bed"))
                blockName = "#beds";
            else
                blockName = legacyID;

            if (hasBlockStates((byte) 26, data))
                blockStates = getBlockStates((byte) 26, data);
        }else {
            byte legacyBlockID = getLegacyBlockID(legacyID);
            blockName = getBlockName(legacyBlockID, data);
            if(hasBlockStates(legacyBlockID, data))
                blockStates = getBlockStates(legacyBlockID, data);
        }

        if(!states.isEmpty()){
            for(String key : states.keySet()){
                blockStates.putString(key, states.get(key));
            }
        }

        if(blockStates.size() != 0){
            String flattenedBlockStates = TagConv.flattenStringsCompoundTag(blockStates);
            return String.format("%1$s[%2$s]", blockName, flattenedBlockStates);
        }

        return blockName;
    }

    /**
     * Checks if an entiy is supported for conversion
     * @param legacyNamespaceID The namespace ID of the entity, ex: minecraft:armor_stand
     * @return True if it's supported, else false
     */
    public static boolean isEntitySupported(String legacyNamespaceID){
        if(!legacyNamespaceID.startsWith("minecraft"))
            return false;

        String legacyID = legacyNamespaceID.substring(10);

        switch (legacyID){
            case "armor_stand":
            case "boat":
            case "chest_minecart":
            case "commandblock_minecart":
            case "ender_crystal":
            case "furnace_minecart":
            case "hopper_minecart":
            case "item":
            case "item_frame":
            case "minecart":
            case "painting":
            case "shulker":
            case "tnt":
            case "tnt_minecart":
            case "wither_skull":
                return true;
        }

        return false;
    }

    /**
     * Get the new namespace id from the legacy (1.12.2) namespace ID
     * @param legacyNamespaceID Legacy 1.12.2 ID of the entity
     * @return The new 1.18.2+ entity namespace id
     */
    public static String getEntityID(String legacyNamespaceID){

        switch (legacyNamespaceID){
            case "minecraft:commandblock_minecart":
                return "minecraft:command_block_minecart";
            case "minecraft:ender_crystal":
                return "minecraft:end_crystal";
            case "minecraft:eye_of_ender_signal":
                return "minecraft:ender_pearl";
            default:
                return legacyNamespaceID;
        }
    }

    /**
     * Get the new mob namespace ID from legacy (1.12.2) mob namespace id
     * @param legacyMobID The 1.12.2 namespace ID of the mob
     * @return The new 1.18.2+ mob namespace ID of the mob
     */
    public static String getMobID(String legacyMobID){
        if(!legacyMobID.startsWith("minecraft:"))
            return legacyMobID;

        String legacyID = legacyMobID.substring(10);
        switch (legacyID) {
            case "evocation_illager": return "minecraft:evoker";
            case "vindication_illager": return "minecraft:vindicator";
            case "illusion_illager": return "minecraft:illusioner";
            case "zombie_pigman": return "minecraft:zombified_piglin";
            case "snowman": return "minecraft:snow_golem";
            case "villager_golem": return "iron_golem";
        }
        return legacyMobID;

    }

    /**
     * Get the new string Enchantments ID from the legacy integer ID
     * @param legacyID The 1.12.2 ID of the Enchantment
     * @return The 1.18.2+ string Enchantments ID
     */
    public static String getEnchantmentsID(short legacyID){
        switch (legacyID){
            case 6: return "aqua_affinity";
            case 18: return "bane_of_arthropods";
            case 3: return "blast_protection";
            case 10: return "binding_curse";
            case 71: return "vanishing_curse";
            case 8: return "depth_strider";
            case 32: return "efficiency";
            case 2: return "feather_falling";
            case 20: return "fire_aspect";
            case 1: return "fire_protection";
            case 50: return "flame";
            case 35: return "fortune";
            case 9: return "frost_walker";
            case 51: return "infinity";
            case 19: return "knockback";
            case 21: return "looting";
            case 61: return "luck_of_the_sea";
            case 62: return "lure";
            case 70: return "mending";
            case 48: return "power";
            case 4: return "projectile_protection";
            case 49: return "punch";
            case 5: return "respiration";
            case 16: return "sharpness";
            case 33: return "silk_touch";
            case 17: return "smite";
            case 22: return "sweeping";
            case 7: return "thorns";
            case 34: return "unbreaking";
            default: return "protection";
        }
    }

    /**
     * Get the new string effect ID from the legacy integer ID
     * @param legacyID The 1.12.2 ID of the effect
     * @return The new 1.18.2+ string ID of the effect
     */
    public static String getEffectID(int legacyID){

        switch (legacyID) {
            case 1:
                return "speed";
            case 2:
                return "slowness";
            case 3:
                return "haste";
            case 4:
                return "mining_fatigue";
            case 5:
                return "strength";
            case 6:
                return "instant_health";
            case 7:
                return "instant_damage";
            case 8:
                return "jump_boost";
            case 9:
                return "nausea";
            case 10:
                return "regeneration";
            case 11:
                return "resistance";
            case 12:
                return "fire_resistance";
            case 13:
                return "water_breathing";
            case 14:
                return "invisibility";
            case 15:
                return "blindness";
            case 16:
                return "night_vision";
            case 17:
                return "hunger";
            case 18:
                return "weakness";
            case 19:
                return "poison";
            case 20:
                return "wither";
            case 21:
                return "health_boost";
            case 22:
                return "absorption";
            case 23:
                return "saturation";
            case 24:
                return "glowing";
            case 25:
                return "levitation";
            case 26:
                return "luck";
            case 27:
                return "unluck";
            case 28:
                return "slow_falling";
            case 29:
                return "conduit_power";
            case 30:
                return "dolphins_grace";
            case 31:
                return "bad_omen";
            case 32:
                return "hero_of_the_village";
            case 33:
                return "darkness";
            default:
                return "fatal_poison";
        }
    }

    /**
     * Get the legacy 1.12.2 string Item ID based on the legacy numerical item ID
     *
     * @param legacyID The legacy 1.12.2 numerical item ID
     * @return
     */
    public static String getItemNamespaceID(int legacyID) {
        return switch (legacyID) {
            case 256 -> "iron_shovel";
            case 257 -> "iron_pickaxe";
            case 258 -> "iron_axe";
            case 259 -> "flint_and_steel";
            case 260 -> "apple";
            case 261 -> "bow";
            case 262 -> "arrow";
            case 263 -> "coal";
            case 264 -> "diamond";
            case 265 -> "iron_ingot";
            case 266 -> "gold_ingot";
            case 267 -> "iron_sword";
            case 268 -> "wooden_sword";
            case 269 -> "wooden_shovel";
            case 270 -> "wooden_pickaxe";
            case 271 -> "wooden_pickaxe";
            case 272 -> "stone_sword";
            case 273 -> "stone_shovel";
            case 274 -> "stone_pickaxe";
            case 275 -> "stone_axe";
            case 276 -> "diamond_sword";
            case 277 -> "diamond_shovel";
            case 278 -> "diamond_pickaxe";
            case 279 -> "diamond_axe";
            case 280 -> "stick";
            case 281 -> "bowl";
            case 282 -> "mushroom_stew";
            case 283 -> "golden_sword";
            case 284 -> "golden_shovel";
            case 285 -> "golden_pickaxe";
            case 286 -> "golden_axe";
            case 287 -> "string";
            case 288 -> "feather";
            case 289 -> "gunpowder";
            case 290 -> "wooden_hoe";
            case 291 -> "stone_hoe";
            case 292 -> "iron_hoe";
            case 293 -> "diamond_hoe";
            case 294 -> "golden_hoe";
            case 295 -> "wheat_seeds";
            case 296 -> "wheat";
            case 297 -> "bread";
            case 298 -> "leather_helmet";
            case 299 -> "leather_chestplate";
            case 300 -> "leather_leggings";
            case 301 -> "leather_boots";
            case 302 -> "chainmail_helmet";
            case 303 -> "chainmail_chestplate";
            case 304 -> "chainmail_leggings";
            case 305 -> "chainmail_boots";
            case 306 -> "iron_helmet";
            case 307 -> "iron_chestplate";
            case 308 -> "iron_leggings";
            case 309 -> "iron_boots";
            case 310 -> "diamond_helmet";
            case 311 -> "diamond_chestplate";
            case 312 -> "diamond_leggings";
            case 313 -> "diamond_boots";
            case 314 -> "golden_helmet";
            case 315 -> "golden_chestplate";
            case 316 -> "golden_leggings";
            case 317 -> "golden_boots";
            case 318 -> "flint";
            case 319 -> "porkchop";
            case 320 -> "cooked_porkchop";
            case 321 -> "painting";
            case 322 -> "golden_apple";
            case 323 -> "sign";
            case 324 -> "wooden_door";
            case 325 -> "bucket";
            case 326 -> "water_bucket";
            case 327 -> "lava_bucket";
            case 328 -> "minecart";
            case 329 -> "saddle";
            case 330 -> "iron_door";
            case 331 -> "redstone";
            case 332 -> "snowball";
            case 333 -> "boat";
            case 334 -> "leather";
            case 335 -> "milk_bucket";
            case 336 -> "brick";
            case 337 -> "clay_ball";
            case 338 -> "reeds";
            case 339 -> "paper";
            case 340 -> "book";
            case 341 -> "slime_ball";
            case 342 -> "chest_minecart";
            case 343 -> "furnace_minecart";
            case 344 -> "egg";
            case 345 -> "compass";
            case 346 -> "fishing_rod";
            case 347 -> "clock";
            case 348 -> "glowstone_dust";
            case 349 -> "fish";
            case 350 -> "cooked_fish";
            case 351 -> "dye";
            case 352 -> "bone";
            case 353 -> "sugar";
            case 354 -> "cake";
            case 355 -> "bed";
            case 356 -> "repeater";
            case 357 -> "cookie";
            case 358 -> "filled_map";
            case 359 -> "shears";
            case 360 -> "melon";
            case 361 -> "pumpkin_seeds";
            case 362 -> "melon_seeds";
            case 363 -> "beef";
            case 364 -> "cooked_beef";
            case 365 -> "chicken";
            case 366 -> "cooked_chicken";
            case 367 -> "rotten_flesh";
            case 368 -> "ender_pearl";
            case 369 -> "blaze_rod";
            case 370 -> "ghast_tear";
            case 371 -> "gold_nugget";
            case 372 -> "nether_wart";
            case 373 -> "potion";
            case 374 -> "glass_bottle";
            case 375 -> "spider_eye";
            case 376 -> "fermented_spider_eye";
            case 377 -> "blaze_powder";
            case 378 -> "magma_cream";
            case 379 -> "brewing_stand";
            case 380 -> "cauldron";
            case 381 -> "ender_eye";
            case 382 -> "speckled_melon";
            case 383 -> "spawn_egg";
            case 384 -> "experience_bottle";
            case 385 -> "fire_charge";
            case 386 -> "writable_book";
            case 387 -> "written_book";
            case 388 -> "emerald";
            case 389 -> "item_frame";
            case 390 -> "flower_pot";
            case 391 -> "carrot";
            case 392 -> "potato";
            case 393 -> "baked_potato";
            case 394 -> "poisonous_potato";
            case 395 -> "map";
            case 396 -> "golden_carrot";
            case 397 -> "skull";
            case 398 -> "carrot_on_a_stick";
            case 399 -> "nether_star";
            case 400 -> "pumpkin_pie";
            case 401 -> "fireworks";
            case 402 -> "firework_charge";
            case 403 -> "enchanted_book";
            case 404 -> "comparator";
            case 405 -> "netherbrick";
            case 406 -> "quartz";
            case 407 -> "tnt_minecart";
            case 408 -> "hopper_minecart";
            case 409 -> "prismarine_shard";
            case 410 -> "prismarine_crystals";
            case 411 -> "rabbit";
            case 412 -> "cooked_rabbit";
            case 413 -> "rabbit_stew";
            case 414 -> "rabbit_foot";
            case 415 -> "rabbit_hide";
            case 416 -> "armor_stand";
            case 417 -> "iron_horse_armor";
            case 418 -> "golden_horse_armor";
            case 419 -> "diamond_horse_armor";
            case 420 -> "lead";
            case 421 -> "name_tag";
            case 422 -> "command_block_minecart";
            case 423 -> "mutton";
            case 424 -> "cooked_mutton";
            case 425 -> "banner";
            case 426 -> "end_crystal";
            case 427 -> "spruce_door";
            case 428 -> "birch_door";
            case 429 -> "jungle_door";
            case 430 -> "acacia_door";
            case 431 -> "dark_oak_door";
            case 432 -> "chorus_fruit";
            case 433 -> "chorus_fruit_popped";
            case 434 -> "beetroot";
            case 435 -> "beetroot_seeds";
            case 436 -> "beetroot_soup";
            case 437 -> "dragon_breath";
            case 438 -> "splash_potion";
            case 439 -> "spectral_arrow";
            case 440 -> "tipped_arrow";
            case 441 -> "lingering_potion";
            case 442 -> "shield";
            case 443 -> "shield";
            case 444 -> "spruce_boat";
            case 445 -> "birch_boat";
            case 446 -> "jungle_boat";
            case 447 -> "acacia_boat";
            case 448 -> "dark_oak_boat";
            case 449 -> "totem_of_undying";
            case 450 -> "shulker_shell";
            case 452 -> "iron_nugget";
            case 2256 -> "record_13";
            case 2257 -> "record_cat";
            case 2258 -> "record_blocks";
            case 2259 -> "record_chirp";
            case 2260 -> "record_far";
            case 2261 -> "record_mall";
            case 2262 -> "record_mellohi";
            case 2263 -> "record_stal";
            case 2264 -> "record_strad";
            case 2265 -> "record_ward";
            case 2266 -> "record_11";
            case 2267 -> "record_wait";
            default -> "item";
        };
    }

    /**
     * Get the new 1.18.2+ string Item ID based on the legacy namespace ID of the item and the "Damage" NBT tag from the Item compound tag.
     * Convert and transfer the NBT tags from the Item compound tag to the JSON object properties
     *
     * @param legacyNamespaceID The string legacy namespace ID of the Item
     * @param item              CompoundTag of the Item
     * @param props             JSON object to write the converted properties from the Item compound tag to
     * @return The new, flattened 1.18.2+ string ID of the 1.12.2 Item
     */
    public static String getItemID(String legacyNamespaceID,  CompoundTag item, JSONObject props) {
        return getItemID(legacyNamespaceID, null, item, props);
    }

    /**
     * Get the new 1.18.2+ string Item ID based on the numerical legacy namespace ID of the item and the "Damage" NBT tag from the Item compound tag.
     * Convert and transfer the NBT tags from the Item compound tag to the JSON object properties
     *
     * @param legacyNamespaceID The numerical legacy namespace ID of the Item
     * @param item              CompoundTag of the Item
     * @param props             JSON object to write the converted properties from the Item compound tag to
     * @return The new, flattened 1.18.2+ string ID of the 1.12.2 Item
     */
    public static String getItemID(Integer legacyNamespaceID,  CompoundTag item, JSONObject props) {
        return getItemID(null, legacyNamespaceID, item, props);
    }

    private static String getItemID(String legacyNamespaceID, Integer legacyNumericalID,  CompoundTag item, JSONObject props) {
        String namespaceID = legacyNamespaceID;
        String newID = "";

        if(legacyNamespaceID == null)
            legacyNamespaceID = "minecraft:item";

        if (legacyNumericalID != null) {
            //Check if item is a block
            if (legacyNumericalID < 256) {
                short meta = 0;
                if (item.containsKey("Damage"))
                    meta = item.getShort("Damage");
                newID = getBlockName(legacyNumericalID.byteValue(), (byte) meta);
                legacyNamespaceID = "minecraft:" + newID;
            } else { //Else get the string legacy or new item id
                newID = getItemNamespaceID(legacyNumericalID);
                legacyNamespaceID = "minecraft:" + newID;
            }

            namespaceID = legacyNamespaceID;
        }

        if (legacyNamespaceID.startsWith("minecraft:")) {
            String legacyID = legacyNamespaceID.substring(10);
            short damage = 0;
            if (item.containsKey("Damage"))
                damage = item.getShort("Damage");
            CompoundTag tagItem = new CompoundTag();
            if (item.containsKey("tag"))
                tagItem = item.getCompoundTag("tag");

            TagConv.getByteTagProperty(item, "Count", "count", props);
            TagConv.getByteTagProperty(item, "Slot", "slot", props);
            //Create json object to store potion effect of the potion items
            JSONObject potionEffects = new JSONObject();

            boolean processTag = true;

            switch (legacyID) {
                case "golden_apple" -> {
                    if (damage == 1)
                        newID = "enchanted_golden_apple";
                }
                case "boat" -> {
                    newID = "oak_boat";
                }
                case "reeds" -> {
                    newID = "sugar_cane";
                }
                case "fish" -> {
                    newID = switch (damage) {
                        case 1 -> "salmon";
                        case 2 -> "tropical_fish";
                        case 3 -> "pufferfish";
                        default -> "cod";
                    };
                }
                case "cooked_fish" -> {
                    newID = (damage == 1) ? "cooked_salmon" : "cooked_cod";
                }
                case "dye" -> {
                    newID = switch (damage){
                        case 1 ->  "red_dye";
                        case 2 ->  "green_dye";
                        case 3 ->  "brown_dye";
                        case 4 ->  "blu_dye";
                        case 5 ->  "purple_dye";
                        case 6 ->  "cyan_dye";
                        case 7 ->  "light_gray_dye";
                        case 8 ->  "gray_dye";
                        case 9 ->  "pink_dye";
                        case 10 ->  "lime_dye";
                        case 11 ->  "yellow_dye";
                        case 12 ->  "light_blue_dye";
                        case 13 ->  "magenta_dye";
                        case 14 ->  "orange_dye";
                        case 15 ->  "white_dye";
                        default ->  "black_dye";
                    };
                }
                case "melon" -> newID = "melon_slice";
                case "speckled_melon" -> newID = "glistering_melon_slice";
                case "spawn_egg" -> {
                    processTag = false;
                    if(tagItem.containsKey("EntityTag")){
                        CompoundTag entityTag = tagItem.getCompoundTag("EntityTag");
                        String mobType = getMobID(entityTag.getString("id"));
                        if(mobType.startsWith("minecraft:"))
                            newID = String.format("%s_spawn_egg", mobType.substring(10));
                    }else {
                        //Else Get the mob type from the damage/meta
                        String mobType = switch (damage) {
                            case 4 -> "elder_guardian";
                            case 5 -> "wither_skeleton";
                            case 6 -> "stray";
                            case 23 -> "husk";
                            case 27 -> "zombie_villager";
                            case 28 -> "skeleton_horse";
                            case 29 -> "zombie_horse";
                            case 31 -> "donkey";
                            case 32 -> "mule";
                            case 34 -> "evoker";
                            case 35 -> "vex";
                            case 36 -> "vindicator";
                            case 50 -> "creeper";
                            case 51 -> "skeleton";
                            case 52 -> "spider";
                            case 54 -> "zombie";
                            case 55 -> "slime";
                            case 56 -> "ghast";
                            case 57 -> "zombie_pigman";
                            case 58 -> "enderman";
                            case 59 -> "cave";
                            case 61 -> "blaze";
                            case 62 -> "magma_cube";
                            case 65 -> "bat";
                            case 66 -> "witch";
                            case 67 -> "endermite";
                            case 68 -> "guardian";
                            case 69 -> "shulker";
                            case 90 -> "pig";
                            case 91 -> "sheep";
                            case 92 -> "cow";
                            case 94 -> "squid";
                            case 95 -> "wolf";
                            case 96 -> "mooshroom";
                            case 98 -> "ocelot";
                            case 100 -> "horse";
                            case 101 -> "rabbit";
                            case 102 -> "polar_bear";
                            case 103 -> "llama";
                            case 120 -> "villager";
                            default -> "chicken";
                        };
                        newID = String.format("%s_spawn_egg", mobType);
                    }
                }
                case "firework_charge" -> newID = "firework_star";
                case "fireworks" -> newID = "firework_rocket";
                case "netherbrick" -> newID = "nether_brick";
                case "banner" -> {
                    newID = switch (damage) {
                        case 15 ->  "white_banner";
                        case 14 ->  "orange_banner";
                        case 13 ->  "magenta_banner";
                        case 12 ->  "light_blue_banner";
                        case 11 ->  "yellow_banner";
                        case 10 ->  "lime_banner";
                        case 9 ->  "pink_banner";
                        case 8 ->  "gray_banner";
                        case 7 ->  "light_gray_banner";
                        case 6 ->  "cyan_banner";
                        case 5 ->  "purple_banner";
                        case 4 ->  "blue_banner";
                        case 3 ->  "brown_banner";
                        case 2 ->  "green_banner";
                        case 1 ->  "red_banner";
                        default -> "black_banner";
                    };
                }
                case "mob_spawner" -> newID = "spawner";
                case "writable_book", "written_book" -> {
                    if (legacyID.equals("written_book")) {
                        if (tagItem.containsKey("generation")) {
                            Tag<?> _generationTag = tagItem.get("generation");
                            byte _generation =  (_generationTag instanceof ByteTag) ? ((ByteTag)_generationTag).asByte() : ((IntTag)_generationTag).asByte();
                            String generation = switch (_generation) {
                                case 1 -> "COPY_OF_ORIGINAL";
                                case 2 -> "COPY_OF_COPY";
                                case 3 -> "TATTERED";
                                default -> "ORIGINAL";
                            };
                            props.put("book_generation", generation);
                        }
                        TagConv.getStringTagProperty(tagItem, "author", "book_author", props);
                        TagConv.getStringTagProperty(tagItem, "title", "book_title", props);
                    }
                    TagConv.getStringTagListProperty(tagItem, "pages", "book_pages", props);
                }
                case "knowledge_book" -> {
                    if(tagItem.containsKey("Tags")){
                        List<String> _recipes = new ArrayList<>();
                        Tag<?> tags = tagItem.get("Tags");
                        if(tags instanceof CompoundTag){
                            CompoundTag _tag = (CompoundTag) tags;
                            getRecipes(_tag, _recipes);
                        }else if(tags instanceof ListTag<?>){
                            ListTag<CompoundTag> _tags = ((ListTag<?>) tags).asCompoundTagList();
                            for(CompoundTag _tag : _tags)
                                getRecipes(_tag, _recipes);
                        }

                        if(!_recipes.isEmpty())
                            props.put("book_recipes", _recipes);
                    }
                }
                case "filled_map" -> {
                    try {
                        short org_id = instance.convertMapItem(damage);
                        if(org_id != -1) {
                            props.put("org_id", org_id);
                            props.put("map_session", MinecraftIDConverter.instance.mapSession);
                        }
                    }catch (Exception ex){
                        log.error(ex.toString());
                    }
                }
                case "skull" -> {
                    newID = switch (damage){
                        case 1 -> "wither_skeleton_skull";
                        case 2 -> "zombie_head";
                        case 3 -> "player_head";
                        case 4 -> "creeper_head";
                        case 5 -> "dragon_head";
                        default -> "skeleton_skull";
                    };
                }
                case "chorus_fruit_popped" -> newID = "popped_chorus_fruit";
                case "wooden_door" -> newID = "oak_door";
                case "potion" -> {
                    //Get the effect of the potion from the damage and store it under the potion effects
                    String effect = switch (damage) {
                        case 32 -> "thick";
                        case 64 -> "mundane";
                        case 8193, 16385 -> "regeneration";
                        case 8194, 16386 -> "swiftness";
                        case 8195, 16387 -> "fire_resistance";
                        case 8196, 16388 -> "poison";
                        case 8197, 16389 -> "healing";
                        case 8198, 16390 -> "night_vision";
                        case 8200, 16392 -> "weakness";
                        case 8201, 16393 -> "strength";
                        case 8202, 16394 -> "slowness";
                        case 8204, 16396 -> "harming";
                        case 8205, 16397 -> "water_breathing";
                        case 8206, 16398 -> "invisibility";
                        case 8225, 8289, 16417, 16481 -> "strong_regeneration";
                        case 8226, 8290, 16418, 16482 -> "strong_swiftness";
                        case 8228, 8292, 16420, 16425, 16484 -> "strong_poison";
                        case 8229, 16421 -> "strong_healing";
                        case 8233, 8297, 16489 -> "strong_strength";
                        case 8235, 16427 -> "strong_leaping";
                        case 8236, 16428 -> "strong_harming";
                        case 8257, 16449 -> "long_regeneration";
                        case 8258, 16450 -> "long_swiftness";
                        case 8259, 16451 -> "long_fire_resistance";
                        case 8260, 16452 -> "long_poison";
                        case 8262, 16454 -> "long_night_vision";
                        case 8264, 16456 -> "long_weakness";
                        case 8265, 16457 -> "long_strength";
                        case 8266, 16458 -> "long_slowness";
                        case 8267, 16459 -> "leaping";
                        case 8269, 16461 -> "long_water_breathing";
                        case 8270, 16462 -> "long_invisibility";
                        default -> "akward";
                    };

                    if(damage >= 16385) {
                        newID = "splash_potion";
                    }

                    potionEffects.put("Potion", effect);
                }
            }

            //Music Discs
            if(legacyID.startsWith("record_"))
                newID = legacyID.replace("record_", "music_disc_");

            if(!newID.isEmpty())
                namespaceID = "minecraft:" + newID;

            if(newID.isEmpty()) {
                byte id = getLegacyBlockID(legacyID);

                //item is not a block item
                if (!legacyID.equals("air") && id == 0) {
                    //Get the 1.18+ entity namespace id
                    namespaceID = getEntityID(namespaceID);
                } else {
                    //Item is a block
                    if (legacyID.equals("bed")) {
                        String bedVersion = switch (damage) {
                            case 1 -> "orange_bed";
                            case 2 -> "magenta_bed";
                            case 3 -> "light_blue_bed";
                            case 4 -> "yellow_bed";
                            case 5 -> "lime_bed";
                            case 6 -> "pink_bed";
                            case 7 -> "gray_bed";
                            case 8 -> "light_gray_bed";
                            case 9 -> "cyan_bed";
                            case 10 -> "purple_bed";
                            case 11 -> "blue_bed";
                            case 12 -> "brown_bed";
                            case 13 -> "green_bed";
                            case 14 -> "red_bed";
                            case 15 -> "black_bed";
                            default -> "white_bed";
                        };
                        namespaceID = "minecraft:" + bedVersion;
                    } else if (legacyID.equals("skull")) {
                        String skullType = switch (damage) {
                            case 1 -> "wither_skeleton_skull";
                            case 2 -> "player_head";
                            case 3 -> "zombie_head";
                            case 4 ->  "creeper_head";
                            case 5 ->  "dragon_head";
                            default -> "skeleton_skull";
                        };
                        namespaceID = "minecraft:" + skullType;
                    } else {
                        //Block items use the "Damage" tag as the "block data" tag, that determines the variant of the block
                        namespaceID = "minecraft:" + getBlockName(id, (byte) damage);
                    }
                }
            }

            //Parse the items structure tags, and It's "tag" NBT tag (https://minecraft.fandom.com/wiki/Player.dat_format?oldid=1161216)
            if(processTag && (item.containsKey("tag") && item.getCompoundTag("tag").size() > 0)) {
                JSONObject generalTags = new JSONObject();
                JSONObject blockTags = new JSONObject();
                JSONObject enchantmentsTags = new JSONObject();
                JSONArray attributeModifiers = new JSONArray();


                //General Tags
                TagConv.getByteTagProperty(tagItem, "Unbreakable", "unbreakable", generalTags);
                if (tagItem.containsKey("CanDestroy")) {
                    ListTag<StringTag> canDestroyBlocks = tagItem.getListTag("CanDestroy").asStringTagList();
                    List<String> canDestroy = new ArrayList<>();
                    for(StringTag blockID : canDestroyBlocks){
                        byte _id = getLegacyBlockID(blockID.getValue().substring(10));
                        canDestroy.add(getNameSpace(_id, (byte) 0));
                    }
                    if(!canDestroy.isEmpty())
                        generalTags.put("CanDestroy", canDestroy);
                }


                //Block Tags
                if(tagItem.containsKey("CanPlaceOn")){
                    ListTag<StringTag> canPlaceOnBlocks = tagItem.getListTag("CanPlaceOn").asStringTagList();
                    List<String> canPlaceOn = new ArrayList<>();
                    for(StringTag blockID : canPlaceOnBlocks){
                        byte _id = getLegacyBlockID(blockID.getValue().substring(10));
                        canPlaceOn.add(getBlockName(_id, (byte) 0));
                    }
                    if(!canPlaceOn.isEmpty())
                        blockTags.put("CanPlaceOn", canPlaceOn);
                }
                if(tagItem.containsKey("BlockEntityTag")) {
                    CompoundTag blockEntityTag = tagItem.getCompoundTag("BlockEntityTag");
                    JSONObject entityTag = new JSONObject();
                    if(legacyID.equals("banner"))
                        getPatternTags(blockEntityTag, entityTag);
                    else if(legacyID.contains("shulker_box"))
                        getInventoryTags(blockEntityTag, entityTag);


                    //TagConv.getCompoundTagProperties(blockEntityTag, entityTag);

                    if(!entityTag.isEmpty())
                        blockTags.put("BlockEntityTag", entityTag);
                }


                //Enchantments
                if(tagItem.containsKey("ench")){
                    ListTag<CompoundTag> enchantmentsTag = tagItem.getListTag("ench").asCompoundTagList();
                    JSONArray enchantments = getEnchantments(enchantmentsTag);
                    if(!enchantments.isEmpty())
                        enchantmentsTags.put("enchantments", enchantments);
                }
                if(tagItem.containsKey("StoredEnchantments")){
                    ListTag<CompoundTag> storedEnchantmentsTag = tagItem.getListTag("StoredEnchantments").asCompoundTagList();
                    JSONArray enchantments = getEnchantments(storedEnchantmentsTag);
                    if(!enchantments.isEmpty())
                        enchantmentsTags.put("stored_enchantments", enchantments);
                }
                TagConv.getIntTagProperty(tagItem, "RepairCost", "repair_cost", enchantmentsTags);


                //Attribute Modifiers
                if(tagItem.containsKey("AttributeModifiers")){
                    ListTag<CompoundTag> attributeModifiersTags = tagItem.getListTag("AttributeModifiers").asCompoundTagList();
                    for(CompoundTag attributeModifiersTag : attributeModifiersTags){
                        JSONObject attributeModifierItem = new JSONObject();
                        TagConv.getStringTagProperty(attributeModifiersTag, "AttributeName", "attribute_name", attributeModifierItem);
                        TagConv.getStringTagProperty(attributeModifiersTag, "Name", "name", attributeModifierItem);
                        TagConv.getStringTagProperty(attributeModifiersTag, "Slot", "slot", attributeModifierItem);
                        TagConv.getLongTagProperty(attributeModifiersTag, "UUIDMost", "uuid_most", attributeModifierItem);
                        TagConv.getLongTagProperty(attributeModifiersTag, "UUIDLeast", "uuid_least", attributeModifierItem);
                        int operationID = attributeModifiersTag.getInt("Operation");
                        String operation = "ADD_NUMBER";
                        if(operationID == 1)
                            operation = "ADD_SCALAR";
                        else if(operationID == 2)
                            operation = "MULTIPLY_SCALAR_1";
                        attributeModifierItem.put("operation", operation);
                        TagConv.getDoubleTagProperty(attributeModifiersTag, "Amount", "amount", attributeModifierItem);

                        attributeModifiers.add(attributeModifierItem);
                    }
                }


                //Potions
                TagConv.getStringTagProperty(tagItem,"Potion", "Potion", potionEffects);
                TagConv.getIntTagProperty(tagItem, "CustomPotionColor", "custom_potion_color", potionEffects);
                //CustomPotionEffects
                if(tagItem.containsKey("CustomPotionEffects")){
                    ListTag<CompoundTag> customPotionEffects = tagItem.getListTag("CustomPotionEffects").asCompoundTagList();
                    JSONArray customPotionEffectsArray = new JSONArray();
                    for(CompoundTag customPotionEffect : customPotionEffects){
                        JSONObject customPotionEffectItem = new JSONObject();
                        customPotionEffectItem.put("id", getEffectID(customPotionEffect.getInt("Id")));
                        TagConv.getByteTagProperty(customPotionEffect, "Amplifier", "amplifier", customPotionEffectItem);
                        TagConv.getIntTagProperty(customPotionEffect, "Duration", "duration", customPotionEffectItem);
                        TagConv.getByteTagProperty(customPotionEffect, "Ambient", "ambient", customPotionEffectItem);
                        TagConv.getByteTagProperty(customPotionEffect, "ShowParticles", "show_particles", customPotionEffectItem);
                        customPotionEffectsArray.add(customPotionEffectItem);
                    }

                    if(!customPotionEffectsArray.isEmpty())
                        potionEffects.put("custom_potion_effects", customPotionEffectsArray);
                }


                //Display Properties
                getDisplayProps(tagItem, props);

                //Player heads
                getSkullOwner(tagItem, props);

                //Fireworks
                getFireworkTags(tagItem, props);

                if(!generalTags.isEmpty())
                    props.put("GeneralTags", generalTags);
                if(!blockTags.isEmpty())
                    props.put("BlockTags", blockTags);
                if(!enchantmentsTags.isEmpty())
                    props.put("EnchantmentsTags", enchantmentsTags);
                if(!attributeModifiers.isEmpty())
                    props.put("AttributeModifiers", attributeModifiers);

            }

            if (!potionEffects.isEmpty())
                props.put("PotionEffects", potionEffects);

        } else {
            JSONObject customItem = new JSONObject();
            TagConv.getCompoundTagProperties(item, customItem);
            if(!customItem.isEmpty())
                props.put("CustomItemProps", customItem);
        }


        return namespaceID;
    }

    /**
     * Parse the item "tag" display properties tags
     * @param tag The "tag" tag of the item
     * @param props If "tag" contains display properties, write them under "DisplayProps" in the props
     */
    public static void getDisplayProps(CompoundTag tag, JSONObject props){
        if (tag.containsKey("display")) {
            JSONObject displayProps = new JSONObject();
            CompoundTag displayTag = tag.getCompoundTag("display");
            TagConv.getStringTagProperty(displayTag, "Name", "display_name", displayProps);
            TagConv.getIntTagProperty(displayTag, "color", "display_color", displayProps);

            if(!displayProps.isEmpty())
                props.put("DisplayProps", displayProps);
        }
    }

    /**
     * Parse the item "tag" skull owner tag
     * @param tag The "tag" tag of the item
     * @param props If the "tag" contains the skull owner tag, write it under "SkullOwner" in the props
     */
    public static void getSkullOwner(CompoundTag tag, JSONObject props){
        if(tag.containsKey("SkullOwner")) {
            CompoundTag skullOwnerTag = tag.getCompoundTag("SkullOwner");
            JSONObject skullOwnerItem = new JSONObject();
            skullOwnerItem.put("id", skullOwnerTag.getString("Id"));

            if (skullOwnerTag.containsKey("Properties")) {
                CompoundTag skullOwnerProperties = skullOwnerTag.getCompoundTag("Properties");
                if (skullOwnerProperties.containsKey("textures")) {
                    ListTag skullOwnerTextures = skullOwnerProperties.getListTag("textures");
                    if (skullOwnerTextures.size() > 0) {
                        Tag skullOwnerTextureTag = skullOwnerTextures.get(0);
                        TagConv.getStringTagProperty((CompoundTag) skullOwnerTextureTag, "Value", "texture", skullOwnerItem);
                    }
                }
            }

            props.put("SkullOwner", skullOwnerItem);
        }
    }

    /**
     * Parse the item "tag" fireworks tags
     * @param tag The "tag" tag of the item
     * @param props If the "tag" contains fireworks tag, write it under "Fireworks" in the props
     */
    public static void getFireworkTags(CompoundTag tag, JSONObject props){
        JSONObject fireworksItems = new JSONObject();
        if(tag.containsKey("Explosion")){
            CompoundTag explosionTag = tag.getCompoundTag("Explosion");
            JSONObject explosionItem = new JSONObject();
            getExplosionTags(explosionTag, explosionItem);

            if(!explosionItem.isEmpty())
                fireworksItems.put("Explosion", explosionItem);
        }

        if(tag.containsKey("Fireworks")){
            CompoundTag fireworksTag = tag.getCompoundTag("Fireworks");
            JSONObject fireworksItem = new JSONObject();
            TagConv.getByteTagProperty(fireworksTag, "Flight", "flight", fireworksItem);
            if(fireworksTag.containsKey("Explosions")){
                ListTag<CompoundTag> explosions = fireworksTag.getListTag("Explosions").asCompoundTagList();
                List<JSONObject> explosionsItems = new ArrayList<>();
                for(CompoundTag explosion : explosions){
                    JSONObject explosionItem = new JSONObject();
                    getExplosionTags(explosion, explosionItem);
                    explosionsItems.add(explosionItem);
                }

                fireworksItem.put("explosions", explosionsItems);
            }

            if(!fireworksItem.isEmpty())
                fireworksItems.put("Fireworks", fireworksItem);
        }

        if(!fireworksItems.isEmpty())
            props.put("Fireworks", fireworksItems);
    }

    /**
     * Parse the Explosion tags to the JSON object
     * @param explosion CompoundTag containing info about the explosion
     * @param explosionItem JSON object to write the parsed Explosion tags to
     */
    private static void getExplosionTags(CompoundTag explosion, JSONObject explosionItem){
        TagConv.getByteTagProperty(explosion, "Flicker", "flicker", explosionItem);
        TagConv.getByteTagProperty(explosion, "Trail", "trail", explosionItem);
        TagConv.getByteTagProperty(explosion, "Type", "type", explosionItem);
        TagConv.getIntArrayTagProperty(explosion, "Colors", "colors", explosionItem);
        TagConv.getIntArrayTagProperty(explosion, "FadeColors", "fade_colors", explosionItem);
    }

    /**
     * Parse the Inventory tags "Items" and "LootTable" to the JSON object
     * @param inventory CompoundTag containing the Inventory tags
     * @param properties JSON object to write the parsed tags to
     */
    public static void getInventoryTags(CompoundTag inventory, JSONObject properties){
        if(inventory.containsKey("Items")){
            ListTag<CompoundTag> itemsList = inventory.getListTag("Items").asCompoundTagList();
            JSONArray itemsArray = MinecraftIDConverter.getItems(itemsList);
            if(!itemsArray.isEmpty())
                properties.put("items", itemsArray);
        }
        if (inventory.containsKey("LootTable"))
            properties.put("loot_table", MinecraftIDConverter.getLootTable(inventory.getString("LootTable")));
        TagConv.getLongTagProperty(inventory, "LootTableSeed", "loot_table_seed", properties);
    }

    /**
     * Parse and convert the "Patterns" tag of the banner to the JSON object properties
     * @param banner The banner CompoundTag
     * @param properties The JSON object to write the parsed tags ("patterns") to
     */
    public static void getPatternTags(CompoundTag banner, JSONObject properties){
        JSONArray _patterns = new JSONArray();
        ListTag<CompoundTag> patterns = (ListTag<CompoundTag>) banner.getListTag("Patterns");
        if (patterns != null) {
            for (CompoundTag pattern : patterns) {
                JSONObject _pattern = new JSONObject();
                _pattern.put("colour", colourNameSpace(15 - pattern.getInt("Color")));
                _pattern.put("pattern", pattern.getString("Pattern"));
                _patterns.add(_pattern);
            }

            properties.put("patterns", _patterns);
        }
    }

    /**
     * Parse the "Recipes" NBT tag in the knowledge book to a list of strings
     * @param recipeTag The CompoundTag containing the "Recipes" NBT tag
     * @param recipes List of string to write the recipes to
     */
    public static void getRecipes(CompoundTag recipeTag, List<String> recipes){
        if(recipeTag.containsKey("Recipes")){
            Tag<?> recipesTag = recipeTag.get("Recipes");
            if(recipesTag instanceof StringTag)
                recipes.add(((StringTag)recipesTag).getValue());
            else if(recipesTag instanceof ListTag<?>){
                ListTag<StringTag> recipesListTag = ((ListTag<?>) recipesTag).asStringTagList();
                for(StringTag recipeStringTag : recipesListTag)
                    recipes.add(recipeStringTag.getValue());
            }
        }
    }

    public Path dataPath;
    public Path mapsPath;
    public String mapSession = UUID.randomUUID().toString();
    public ConcurrentHashMap<Short, CompletableFuture<Short>> convertedMapItems = new ConcurrentHashMap<>();

    /**
     * Return a CompletableFuture to convert a Filled Map Item based on the ID of the Item
     * @param id The short ID of the Filled Map
     * @return A CompletableFuture to process the map item
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public short convertMapItem(short id) throws ExecutionException, InterruptedException {
        CompletableFuture<Short> future = convertedMapItems.computeIfAbsent(id,this::processMapItem);
        return future.get();
    }

    /**
     * Process the filled map by directly reading the .dat file of it inside the data folder.
     * and writing the tag values from the CompoundTag to the JSON map file in the output/maps/maps_[sessionID] folder
     * Each time the converter is run, a random UUID sessionID is generated, as to avoid duplicate map ID's in the converted world
     * @param id The short ID of the map Item
     * @return The short ID of the map Item
     */
    private CompletableFuture<Short> processMapItem(short id){
        return CompletableFuture.supplyAsync(() -> {
            Path mapDatPath = dataPath.resolve("map_" + id + ".dat");
            if(Files.exists(mapDatPath)){
                try {
                    CompoundTag mapTag = (CompoundTag) NBTUtil.read(mapDatPath.toFile()).getTag();
                    CompoundTag mapDataTag = mapTag.getCompoundTag("data");

                    if(mapDataTag.getInt("dimension") != 0)
                        return (short)-1;

                    JSONObject mapItem = new JSONObject();
                    TagConv.getByteTagProperty(mapDataTag, "scale", "scale", mapItem);
                    //TagConv.getShortTagProperty(mapDataTag, "height", "height", mapItem);
                    //TagConv.getShortTagProperty(mapDataTag, "width", "width", mapItem);
                    TagConv.getByteTagProperty(mapDataTag, "unlimitedTracking", "unlimited_tracking", mapItem);
                    TagConv.getIntTagProperty(mapDataTag, "xCenter", "x_center", mapItem);
                    TagConv.getIntTagProperty(mapDataTag, "zCenter", "z_center", mapItem);
                    TagConv.getByteArrayTagProperty(mapDataTag, "colors", "colors", mapItem);

                    /*if(mapDataTag.containsKey("colors")){
                        byte[] _colors = mapDataTag.getByteArrayTag("colors").getValue();
                        List<Integer> colors = new ArrayList<>();
                        for(int c = 0; c < _colors.length; c++)
                            colors.add((Byte.toUnsignedInt(_colors[c]) + 1) / 4);
                        mapItem.put("colors", colors);
                    }*/

                    TagConv.getCompoundTagProperties(mapDataTag, mapItem);

                    Path mapSessionFolder = mapsPath.resolve("maps_" + mapSession);
                    Files.createDirectories(mapSessionFolder);

                    FileWriter mapFile = new FileWriter(mapSessionFolder.resolve("map_" + id + ".json" ).toFile());
                    mapFile.write(mapItem.toJSONString());
                    mapFile.flush();
                    mapFile.close();
                } catch (Exception ex) {
                    log.error(String.format("Error while processing map_%1$d.dat | Error: %2$s", id, ex.getMessage()));
                }
            }
            return id;
        });
    }

    /**
     * Write the "maps" json file in the maps_[sessionID], which stores the ID's of the converted map items, and the
     * ID of the session when the converted was run. This approach enables, for example if you want to convert multiple 1.12.2 worlds
     * to a single converted world, as these multiple 1.12.2 worlds may use the same map item ID's, so the newer converted world,
     * would overwrite the old map item JSON files that use the same ID.
     */
    public void writeMapsSessionConfig(){
        try {
            JSONObject sessionItem = new JSONObject();
            JSONArray mapsItem = new JSONArray();
            for(Short mapId : convertedMapItems.keySet()){
                mapsItem.add("map_" + mapId);
            }

            if(!mapsItem.isEmpty()) {
                sessionItem.put("maps", mapsItem);
                sessionItem.put("maps_session", mapSession);

                Path mapSessionFolder = mapsPath.resolve("maps_" + mapSession);
                FileWriter mapFile = new FileWriter(mapSessionFolder.resolve("maps.json").toFile());
                mapFile.write(sessionItem.toJSONString());
                mapFile.flush();
                mapFile.close();
            }


        }catch (Exception ex){
            log.error(String.format("Error while writing maps session config"));
        }
    }

    /**
     * Get the Enchantments from a ListTag of CompoundTags to a JSON array
     * @param enchantments ListTag of Enchantments CompoundTags
     * @return JSON array containing properties from the Enchantments CompoundTags
     */
    private static JSONArray getEnchantments(ListTag<CompoundTag> enchantments){
        JSONArray enchantmentItems = new JSONArray();
        for(CompoundTag enchantmentTag : enchantments){
            JSONObject enchantment = new JSONObject();
            enchantment.put("id", getEnchantmentsID(enchantmentTag.getShort("id")));
            enchantment.put("lvl", enchantmentTag.getShort("lvl"));
            enchantmentItems.add(enchantment);
        }
        return enchantmentItems;
    }

    /**
     * Get the new 1.18.2+ loot table name from the legacy 1.12.2 loot table name, else return it as it is
     * @param legacyLootTable The 1.12.2 name of the legacy table
     * @return The new 1.18.2+ loot table name
     */
    public static String getLootTable(String legacyLootTable){
        switch (legacyLootTable){
            case "minecraft:chests/village_blacksmith":
                return "minecraft:chests/village/village_weaponsmith";
            case "minecraft:entities/zombie_pigman":
                return "minecraft:entities/zombified_piglin";
            case "minecraft:chests/end_chest":
                return "minecraft:chest/player";
        }

        return legacyLootTable;
    }

    /**
     * Convert the syntax of the 1.12.2 command to the syntax of commands in 1.18 and later
     * @param legacyCommand The 1.12.2 command
     * @return Converted to 1.18.2 command
     */
    public static String getCommand(String legacyCommand){
        boolean addSlash = false;
        if(legacyCommand.startsWith("/")) {
            addSlash = true;
            legacyCommand = legacyCommand.substring(1);
        }

        if(legacyCommand.startsWith("blockdata"))
            legacyCommand = legacyCommand.replace("blockdata", "data merge block");
        else if(legacyCommand.startsWith("entitydata"))
            legacyCommand = legacyCommand.replace("entitydata", "data merge entity");
        else if(legacyCommand.startsWith("execute")){
            //   execute <entity> <x> <y> <z> <command …>
            legacyCommand = legacyCommand.substring(8);
            //   <entity> <x> <y> <z> <command …>
            int index = legacyCommand.indexOf(" ");
            String entitySelector = legacyCommand.substring(0, index);
            legacyCommand = legacyCommand.substring(index + 1);

            //Find third index of white space
            index = legacyCommand.indexOf(" ");
            for(int i = 0; i < 2; i++)
                index = legacyCommand.indexOf(" ", index + 1);
            String position = getPositionSelector(legacyCommand.substring(0, index));


            legacyCommand = legacyCommand.substring(index + 1);

            //If the command syntax is the alternative syntax
            if(legacyCommand.startsWith("detect")){
                //    detect <x2> <y2> <z2> <block> <dataValue|state> <command …>
                legacyCommand = legacyCommand.substring(7);

                //    <x2> <y2> <z2> <block> <dataValue|state> <command …>
                //Find third index of white space
                index = legacyCommand.indexOf(" ");
                for(int i = 0; i < 2; i++)
                    index = legacyCommand.indexOf(" ", index + 1);
                String blockPos = getPositionSelector(legacyCommand.substring(0, index));
                legacyCommand = legacyCommand.substring(index + 1);

                //    <block> <dataValue|state> <command …>
                index = legacyCommand.indexOf(" ");
                String blockSelector = legacyCommand.substring(0, index);
                legacyCommand = legacyCommand.substring(index + 1);

                //    <dataValue|state> <command …>
                index = legacyCommand.indexOf(" ");
                String dataOrState = legacyCommand.substring(0, index);
                String command = legacyCommand.substring(index + 1);
                command = getCommand(command);

                if(blockSelector.startsWith("minecraft:"))
                    blockSelector = blockSelector.substring(10);

                byte data = -1;
                HashMap<String, String> states = new HashMap<>();

                try {
                    int parsedState = Integer.parseInt(dataOrState);
                    data = (byte) parsedState;
                }catch (Exception ex){
                    //Threat the <dataValue|state> parameter as a state
                    TagConv.parseFlattenedTags(dataOrState, states);
                    data = 0;
                }
                blockSelector = getBlockSelector(blockSelector, data, states);

                legacyCommand = String.format("execute as %1$s positioned %2$s if block %3$s %4$s run %5$s", entitySelector, position, blockPos, blockSelector, command);

            }else {
                String command = getCommand(legacyCommand);
                legacyCommand = String.format("execute as %1$s positioned %2$s run %3$s", entitySelector, position, command);
            }
        }else if(legacyCommand.startsWith("setblock")){
            legacyCommand = legacyCommand.substring(9);
            //      3945016 538 -4147002 birch_stairs facing=east
            //      3945016 538 -4147001 concrete 3 destroy
            //      3945016 538 -4147002 concrete
            String[] arguments = legacyCommand.split(" ");
            List<String> newArguments = new ArrayList<>();

            if(arguments.length >= 4){
                newArguments.add(arguments[0]);
                if(!arguments[1].startsWith("~")){
                    arguments[1] = offsetPositionSelector(arguments[1]);
                }
                newArguments.add(arguments[1]);
                newArguments.add(arguments[2]);

                String blockSelector = "";
                HashMap<String, String> states = new HashMap<>();
                byte data = 0;
                String oldBlockHandling = "";

                if(arguments.length >= 5){
                    //Check if the optional parameter [dataValue|state] is a data value or a state

                    try {
                        int parsedData = Integer.parseInt(arguments[4]);
                        //optional parameter is a data value
                        data = (byte) parsedData;

                    }catch (Exception ex){
                        //optional parameter is a state
                        TagConv.parseFlattenedTags(arguments[4], states);
                    }

                    //Check if the arguments include the optional oldBlockHandling argument
                    if(arguments.length >= 6){
                        oldBlockHandling = arguments[5];
                    }
                }

                blockSelector = getBlockSelector(arguments[3], data, states);
                newArguments.add(blockSelector);
                if(!oldBlockHandling.isEmpty())
                    newArguments.add(oldBlockHandling);
            }

            legacyCommand = "setblock";
            for(String arg : newArguments)
                legacyCommand = String.format("%1$s %2$s", legacyCommand, arg);
        }

        if(addSlash)
            legacyCommand = String.format("/%s", legacyCommand);


        return  legacyCommand;
    }

    /**
     * Get the new position selector as the result of the world shifting on the Y axis
     * @param position The 1.12.2 position selector, ex. 10 20 5
     * @return The offset position selector, ex. 10 (20 + Main.OFFSET) 5 -> 10 5 5
     */
    public static String getPositionSelector(String position){
        String[] positions = position.split(" ");
        if(!positions[1].startsWith("~")){
            positions[1] = offsetPositionSelector(positions[1]);
        }
        return String.format("%1$s %2$s %3$s", positions[0], positions[1], positions[2]);
    }

    /**
     * Offset the Y position by Main.OFFSET
     * @param yPos The Y position to offset
     * @return The offset Y position
     */
    private static String offsetPositionSelector(String yPos){
        if(yPos.contains(".")){
            Double parsedPosition = Double.parseDouble(yPos);
            parsedPosition -= Main.OFFSET;
            return parsedPosition.toString();
        }else {
            Integer parsedPosition = Integer.parseInt(yPos);
            parsedPosition += Main.OFFSET;
            return parsedPosition.toString();
        }
    }

    /**
     * Parse through, convert and return a JSON array of Items from a ListTag of CompoundTags
     * @param itemsList ListTag of CompoundTags containing the Items of an entity, ex chest
     * @return And JSON array containing the converted item ID's and parsed item properties
     */
    public static JSONArray getItems(ListTag<CompoundTag> itemsList){
        JSONArray itemsArray = new JSONArray();
        for (CompoundTag compoundTag : itemsList) {
            JSONObject jsonItem = new JSONObject();
            if (compoundTag.containsKey("id")) {
                JSONObject itemProps = new JSONObject();
                Tag<?> idTag = compoundTag.get("id");
                jsonItem.put("id", (idTag instanceof StringTag) ? getItemID(((StringTag) idTag).getValue(), compoundTag, itemProps) : getItemID(((ShortTag) idTag).asInt(), compoundTag, itemProps));
                if (!itemProps.isEmpty()) {
                    jsonItem.put("Properties", itemProps);
                }
            }

            itemsArray.add(jsonItem);
        }

        return itemsArray;
    }

    /**
     * Parse through the NBT tags of the entity, convert them if needed, and write them to the JSON object properties
     * @param legacyID The legacy 1.12.2 namespace ID of the entity
     * @param entity The CompoundTag of the entity
     * @param properties The JSON object to write the parsed NBT tags to
     */
    public static void getEntitiesTags(String legacyID ,CompoundTag entity, JSONObject properties) {
        TagConv.getByteTagProperty(entity,"NoGravity","NoGravity",properties);
        TagConv.floatTagListToJson("Rotation", entity, properties);

        if (legacyID.contains("minecart")) {
            TagConv.getByteTagProperty(entity, "CustomDisplayTile", "display_tile", properties);
            TagConv.getIntTagProperty(entity, "DisplayOffset", "display_tile_offset", properties);

            if(entity.containsKey("DisplayState")){
                CompoundTag displayStateTag = entity.getCompoundTag("DisplayState");

                TagConv.getStringTagProperty(displayStateTag, "Name", "", properties);

                String legacyNamespaceID = displayStateTag.getString("Name");
                if(displayStateTag.containsKey("Properties")){
                    CompoundTag displayStateProp = displayStateTag.getCompoundTag("Properties");
                    JSONObject displayTileBlockStates = new JSONObject();
                    properties.put("display_tile_block", getBlockName(legacyNamespaceID, displayStateProp, displayTileBlockStates));
                    if(!displayTileBlockStates.isEmpty())
                        properties.put("display_tile_block_states", displayTileBlockStates);
                }else
                    properties.put("display_tile_block", getBlockName(legacyNamespaceID, new CompoundTag(), new JSONObject()));
            }
        }

        switch (legacyID) {
            case "minecraft:armor_stand" -> {
                properties.put("ShowArms", entity.getByte("ShowArms"));
                properties.put("Invisible", entity.getByte("Invisible"));
                properties.put("Small", entity.getByte("Small"));
                properties.put("NoBasePlate", entity.getByte("NoBasePlate"));

                JSONObject poseTagItem = new JSONObject();
                CompoundTag armorPose = entity.getCompoundTag("Pose");

                TagConv.floatTagListToJson("Body", armorPose, poseTagItem);
                TagConv.floatTagListToJson("Head", armorPose, poseTagItem);
                TagConv.floatTagListToJson("LeftArm", armorPose, poseTagItem);
                TagConv.floatTagListToJson("RightArm", armorPose, poseTagItem);
                TagConv.floatTagListToJson("LeftLeg", armorPose, poseTagItem);
                TagConv.floatTagListToJson("RightLeg", armorPose, poseTagItem);

                if (!poseTagItem.isEmpty())
                    properties.put("Pose", poseTagItem);


                ListTag<CompoundTag> armorItems = entity.getListTag("ArmorItems").asCompoundTagList();
                List<JSONObject> jsonArmorItems = new ArrayList<>();
                for (CompoundTag armor : armorItems) {

                    if (armor.entrySet().isEmpty()) {
                        jsonArmorItems.add(new JSONObject());
                        continue;
                    }

                    JSONObject jsonArmorItem = new JSONObject();
                    JSONObject jsonArmorItemProps = new JSONObject();
                    String itemID = getItemID(armor.getString("id"), armor, jsonArmorItemProps);
                    jsonArmorItem.put("id", itemID);
                    if(!jsonArmorItemProps.isEmpty())
                        jsonArmorItem.put("Properties", jsonArmorItemProps);

                    jsonArmorItems.add(jsonArmorItem);
                }

                properties.put("ArmorItems", jsonArmorItems);

                ListTag<CompoundTag> handItems = entity.getListTag("HandItems").asCompoundTagList();
                List<String> handItemsList = new ArrayList<>();
                for (CompoundTag handItem : handItems) {
                    if (handItem.entrySet().isEmpty()) {
                        handItemsList.add("");
                        continue;
                    }

                    handItemsList.add(getItemID(handItem.getString("id"), new CompoundTag(), new JSONObject()));
                }

                properties.put("HandItems", handItemsList);


            }
            case "minecraft:chest_minecart", "minecraft:hopper_minecart" -> {
                getInventoryTags(entity, properties);

                if (legacyID.contains("hopper")) {
                    TagConv.getByteTagProperty(entity, "Enabled", "enabled", properties);
                }
            }
            case "minecraft:furnace_minecart" -> {
                TagConv.getShortTagProperty(entity, "Fuel", "fuel", properties);
                TagConv.getDoubleTagProperty(entity, "PushX", "push_x", properties);
                TagConv.getDoubleTagProperty(entity, "PushZ", "push_z", properties);
            }
            case "minecraft:tnt_minecart" -> {
                TagConv.getIntTagProperty(entity, "TNTFuse", "tnt_fuse", properties);
            }
            case "minecraft:commandblock_minecart" -> {
                if (entity.containsKey("Command"))
                    properties.put("command", getCommand(entity.getString("Command")));
            }
            case "minecraft:ender_crystal" -> {
                if(entity.containsKey("BeamTarget")){
                    CompoundTag beamTarget = entity.getCompoundTag("BeamTarget");
                    properties.put("beam_target", Arrays.asList(new Integer[] {beamTarget.getInt("X"), beamTarget.getInt("Y") + Main.OFFSET, beamTarget.getInt("Z")}));
                }
                TagConv.getByteTagProperty(entity, "ShowBottom", "show_button", properties);
            }
            case "minecraft:painting" -> {
                byte facing = entity.getByte("Facing");
                String enumFacing = "SOUTH";
                switch (facing) {
                    case 3 -> { enumFacing = "EAST"; }
                    case 2 -> { enumFacing = "NORTH"; }
                    case 1 -> { enumFacing = "WEST"; }
                    case 0 -> { enumFacing = "SOUTH"; }
                }
                properties.put("facing", enumFacing);
                properties.put("tile_pos", Arrays.asList(new Integer[]{entity.getInt("TileX"), entity.getInt("TileY") + Main.OFFSET, entity.getInt("TileZ")}));

                String motive = entity.getString("Motive");
                switch (motive) {
                    case "SkullAndRoses" ->   motive = "SKULL_AND_ROSES";
                    case "DonkeyKong" -> motive = "DONKEY_KONG";
                    case "BurningSkull" -> motive = "BURNING_SKULL";
                    default ->  motive = motive.toUpperCase();
                }
                properties.put("motive", motive);
            }
            case "minecraft:chest" -> {
                getInventoryTags(entity, properties);
            }
            case "minecraft:item_frame" -> {
                TagConv.getByteTagProperty(entity, "Fixed", "fixed", properties);
                TagConv.getByteTagProperty(entity, "Invisible","invisible", properties);
                if(entity.containsKey("Item")){
                    CompoundTag item = entity.getCompoundTag("Item");
                    String itemID = item.getString("id");
                    JSONObject itemProps = new JSONObject();
                    itemID = getItemID(itemID, item, itemProps);
                    JSONObject _itemObj = new JSONObject();
                    _itemObj.put("id", itemID);
                    if(!itemProps.isEmpty())
                        _itemObj.put("Properties", itemProps);
                    properties.put("item", _itemObj);
                }
                TagConv.getFloatTagProperty(entity,"ItemDropChance","item_drop_chance", properties);
                if(entity.containsKey("ItemRotation")){
                    byte itemRotation = entity.getByte("ItemRotation");
                    String rotation = "NONE";
                    switch (itemRotation){
                        case 1 -> rotation = "CLOCKWISE_45";
                        case 2 -> rotation = "CLOCKWISE";
                        case 3 -> rotation = "CLOCKWISE_135";
                        case 4 -> rotation = "FLIPPED";
                        case 5 -> rotation = "FLIPPED_45";
                        case 6 -> rotation = "COUNTER_CLOCKWISE";
                        case 7 -> rotation = "COUNTER_CLOCKWISE_45";
                    }
                    properties.put("item_rotation", rotation);
                }
            }
        }
    }
}
