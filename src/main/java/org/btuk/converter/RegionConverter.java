package org.btuk.converter;

/*

This class will be run to convert a single region file.
This will allow the converter to run multiple threads so regions can be converter simultaneously.

 */

import cubicchunks.regionlib.api.region.key.RegionKey;
import cubicchunks.regionlib.impl.EntryLocation2D;
import cubicchunks.regionlib.impl.EntryLocation3D;
import cubicchunks.regionlib.impl.MinecraftChunkLocation;
import cubicchunks.regionlib.impl.header.TimestampHeaderEntryProvider;
import cubicchunks.regionlib.impl.save.MinecraftSaveSection;
import cubicchunks.regionlib.lib.provider.SimpleRegionProvider;
import org.btuk.converter.cc.MemoryWriteRegion;
import org.btuk.converter.cc.RWLockingCachedRegionProvider;
import org.btuk.converter.cc.Utils;
import org.btuk.converter.utils.LegacyID;
import org.btuk.converter.utils.MinecraftIDConverter;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.DoubleTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.StringTag;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static cubicchunks.regionlib.impl.save.MinecraftSaveSection.MinecraftRegionType.MCA;

public class RegionConverter extends Thread {

    ThreadManager mng;

    private Thread t;

    boolean unique;

    final BlockingQueue<String> queue;

    //Biome related stuff.
    ListTag<StringTag> biomePalette = new ListTag<>(StringTag.class);
    HashMap<Byte, Integer> biomePaletteID = new HashMap<>();
    String biomeName;

    ArrayList<LegacyID> uniqueBlocks = new ArrayList<>();
    ListTag<CompoundTag> tile_entities = new ListTag<>(CompoundTag.class);
    CompoundTag tEntity;

    RegionKey regionKey;
    ListTag<CompoundTag> entities;

    HashMap<LegacyID, Integer> paletteID = new HashMap<>();
    ListTag<CompoundTag> palette = new ListTag<>(CompoundTag.class);

    int entryX;
    int entryZ;

    UUID uuid;

    JSONArray jaEntities;

    byte meta;
    byte[] data;

    byte[] blocks;

    int cX;
    int cY;
    int cZ;

    EntryLocation3D e3d;
    CompoundTag block_entity;
    CompoundTag biomes;

    JSONArray ja;

    String file;

    EntryLocation2D e2d;

    ListTag<CompoundTag> block_entities;
    ListTag<CompoundTag> sections;

    boolean isEmpty;
    long[] newData = null;
    long[] biomeData;

    int counter;

    public RegionConverter(UUID uuid, BlockingQueue<String> queue, ThreadManager mng) {

        this.uuid = uuid;
        this.queue = queue;
        this.mng = mng;

    }

    /**
     * Start the thread and run until there are no items left in the queue.
     */
    public void run() {

        //Iterate until the queue is empty.
        try {
            while (true) {
                file = queue.take();

                if (file.equals("end")) {
                    mng.activeThreads.decrementAndGet();

                    //Write entities file.
                    //FileWriter ppFile = new FileWriter(mng.itr.output.resolve("entities") + "/" + uuid + ".json");
                    //ppFile.write(jaEntities.toJSONString());
                    //ppFile.flush();
                    //ppFile.close();
                    break;
                }



                try {
                    convert();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.out.println("An IOException occurred converting the file: " + file);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("An Exception occurred converting the file: " + file);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }/* catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    /**
     * Converts a single region (512x512) to 1.18.2.
     * The file is set globally before calling this method.
     *
     * @throws IOException
     */
    private void convert() throws IOException {

        //Create new json array to store blocks for post-processing in.
        ja = new JSONArray();
        //Create json array to store entities.
        jaEntities = new JSONArray();

        //Iterate through all possible chunk columns in the file and continue with any that contain data.
        //A region2d is 512x512 which is 32*32 in terms of chunks, so 1024 individual chunks to iterate.
        for (int i = 0; i < 1024; i++) {

            convertColumn(i);

        }

        try {

            //Write the json array to a file.
            FileWriter ppFile = new FileWriter(mng.itr.output.resolve("post-processing") + "/" + file.replace(".2dr", ".json"));
            JSONObject postProcObj = new JSONObject();
            postProcObj.put("block", ja);
            if(!jaEntities.isEmpty())
                postProcObj.put("entity", jaEntities);
            ppFile.write(postProcObj.toJSONString());
            ppFile.flush();
            ppFile.close();

        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts a single 16x16 column of the world and saves the new chunk to the region.
     *
     * @param columnID
     * the id of the column to be converted, id is from 0 to 1023
     *
     * @throws IOException
     */
    private void convertColumn(int columnID) throws IOException {
        CompoundTag chunk = new CompoundTag();

        RegionKey key = new RegionKey(file);
        e2d = mng.provider2d.fromRegionAndId(key, columnID);
        entryX = e2d.getEntryX();
        entryZ = e2d.getEntryZ();

        //Create list of sections to store the new sections in.
        sections = new ListTag<>(CompoundTag.class);

        //Gets the data from the 2dr file.
        Optional<ByteBuffer> column = mng.itr.saveCubeColumns.load(e2d, true);

        //Create a list of Block Entities for this chunk.
        block_entities = new ListTag<>(CompoundTag.class);

        //If the columndata is not empty
        if (column.isPresent()) {

            //Create a check for if a cube is empty, if the whole column is empty it can be skipped.
            isEmpty = true;

            //Clear the biome palette.
            biomePalette.clear();
            biomePaletteID.clear();

            CompoundTag columnTag = (CompoundTag) mng.readCompressedCC(new ByteArrayInputStream(column.get().array())).getTag();
            CompoundTag columnLevel = columnTag.getCompoundTag("Level");

            biomes = new CompoundTag();

            //Get the biome data and convert it.
            byte[] oldBiomes = columnLevel.getByteArray("Biomes");
            getBiomes(oldBiomes);

            //Get all cubes that could be in the range of heights.
            for (int y = Main.MIN_Y_CUBE; y < Main.MAX_Y_CUBE; y++) {

                convertCube(y);

            }

            //If the whole chunk is empty, don't save it.
            if (!isEmpty) {

                //Add block entities to the chunk.
                chunk.put("block_entities", block_entities);

                //Add sections to chunk.
                chunk.put("sections", sections);

                //Set chunk status.
                chunk.putString("Status", "heightmaps");

                //Set data version
                chunk.putInt("DataVersion", 2975);

                //Set position of chunk.
                chunk.putInt("xPos", columnLevel.getInt("x"));
                chunk.putInt("zPos", columnLevel.getInt("z"));
                chunk.putInt("yPos", (Main.MIN_Y_CUBE + (Main.OFFSET / 16)));

                //Set last update.
                chunk.putLong("LastUpdate", 0);

                //Add block and fluid ticks.
                chunk.put("fluid_ticks", new ListTag<>(CompoundTag.class));
                chunk.put("block_ticks", new ListTag<>(CompoundTag.class));

                //Inhabited Time
                chunk.putLong("InhabitedTime", 0);

                //Add Post Processing
                chunk.put("PostProcessing", mng.post_processing);

                //Add Structures
                chunk.put("structures", mng.structures);

                //Is Light On
                chunk.putBoolean("isLightOn", true);

                //Save the chunk in the region file.
                save(mng.itr.output.resolve("region"), mng.writeCompressed(chunk, true));
            }
        }  //Else Skip this column.
    }

    /**
     * Converts the biomes from the old column to the new chunk.
     * Since in 1.18.2 biomes are stored completely differently to 1.12.2 we convert the old data to the new format manually.
     * The new biome format is stored in 4x4x4 areas, while the old format was 16x16 with no veritcal support.
     * Therefore to we convert every 4 blocks and duplicate it horizontally for all 4x4x4 areas in that vertical column.
     *
     * @param oldBiomes
     * the old biomes which are stored in a 16x16 grid, vertical biomes do not yet exist in this version
     */
    private void getBiomes(byte[] oldBiomes) {
        if (oldBiomes != null) {

            //Get the biomes, since Minecraft 1.18.2 stored biomes in 4x4x4 cubes
            // we must create a 4x4x4 array of cubes to fill a section.
            //However we it is stored using a palette with a reference to the palette,
            // so we start by constructing the palette using the number of unique biomes.
            for (int ind = 0; ind < oldBiomes.length; ind++) {

                //We can skip 3 out of 4 indexes since we only need a 4x4 layer rather than 16x16.
                //We'll skip all indixes divisible by 2 and 3, this will leave the second index out of 4 (0,1,2,3).
                if (ind % 4 == 1 && ind / 16 % 4 == 1) {

                    biomeName = MinecraftIDConverter.getBiome(oldBiomes[ind]);
                    if (!containsBiome(biomeName)) {
                        //Index equals biome size before adding the new biome.
                        biomePaletteID.put(oldBiomes[ind], biomePalette.size());
                        biomePalette.add(new StringTag(biomeName));
                    } else if (!biomePaletteID.containsKey(oldBiomes[ind])) {
                        int index = 0;
                        for (StringTag biome : biomePalette) {
                            if (biome.getValue().equals(biomeName)) {
                                //Index equals biome size before adding the new biome.
                                biomePaletteID.put(oldBiomes[ind], index);
                            }
                            index++;
                        }
                    }
                }
            }

            //Add the biome palette
            biomes.put("palette", biomePalette);

            //If palette has more than 1 biome we need to create a data array.
            if (biomePalette.size() > 1) {

                //Find the number of blocks that will be stored in each long array. If the count is 16 or less then each block will use 4 bits.
                //A long allows for 64 bits in total.
                int blockSize = mng.log2(biomePalette.size() - 1);
                int blocksPerLong = 64 / blockSize;
                biomeData = new long[(64 + blocksPerLong - 1) / blocksPerLong];

                int counter = 0;
                //To remember the index of the long.
                int index = 0;
                long blockIndex;

                //To get the index from the old biome array.
                int oldIndex = 0;

                //Long value
                long blockStorage = 0;

                //Convert the data to the new section structure of Minecraft 1.18.2
                for (int j = 0; j < 64; j++) {

                    //If counter is equal or greater than the blocksPerLong we need to create a new long and reset the counter.
                    if (counter >= blocksPerLong) {
                        counter = 0;
                        blockStorage = 0;
                        index++;
                    }

                    //Find the next correct index for the old biome array.
                    while (oldIndex % 4 != 1 || oldIndex / 16 % 4 != 1) {
                        //If we've reached the end of the array indexes return to 1, which is the first valid index.
                        if (oldIndex >= 255) {
                            oldIndex = 1;
                        }
                        oldIndex++;
                    }

                    //Get the id for the block.
                    blockIndex = biomePaletteID.get(oldBiomes[oldIndex]);
                    oldIndex++;

                    //Shift the blockIndex by blockSize * counter so the value gets put in the right place in the long.
                    //Using the bitwise or function we can set the values in the long.
                    blockIndex = blockIndex << (counter * blockSize);
                    blockStorage = blockStorage | blockIndex;

                    //Update the counter.
                    counter++;

                    //Set the long in the array when the counter is at the blocksPerLong value.
                    if (counter == blocksPerLong || j == 63) {
                        biomeData[index] = blockStorage;
                    }
                }

                //Add the biome data to the biomes tag.
                biomes.putLongArray("data", biomeData);
            }
        }
    }

    /**
     * Convert a cube (16x16x16) to 1.18.2
     *
     * @param y
     * y level of the cube, a new y level for every 16 blocks
     *
     * @throws IOException
     */
    private void convertCube(int y) throws IOException {

        //Get the cube of data.
        e3d = new EntryLocation3D(e2d.getEntryX(), y, e2d.getEntryZ());
        Optional<ByteBuffer> cube = mng.itr.saveCubeColumns.load(e3d, true);

        //Check if it exists.
        if (cube.isPresent()) {

            //Retrieve the data from the cube.
            CompoundTag cubeTag = (CompoundTag) mng.readCompressedCC(new ByteArrayInputStream(cube.get().array())).getTag();
            CompoundTag cubeLevel = cubeTag.getCompoundTag("Level");

            if (cubeLevel == null) {
                sections.add(mng.createEmptySection(y));
                return;
            }

            if (cubeLevel.get("Sections") == null) {
                sections.add(mng.createEmptySection(y));
                return;
            }

            //Get the sections from the cube.
            ListTag<CompoundTag> oldSections = (ListTag<CompoundTag>) cubeLevel.getListTag("Sections");
            CompoundTag oldSection = oldSections.get(0);

            //Get unique blocks in section using Blocks and Data.
            blocks = oldSection.getByteArray("Blocks");
            data = oldSection.getByteArray("Data");

            //Clear uniqueBlocks.
            uniqueBlocks.clear();

            //Load entities.
            entities = (ListTag<CompoundTag>) cubeLevel.getListTag("Entities");
            regionKey = e3d.getRegionKey();

            //Add them to the json file.
            for (CompoundTag entity : entities) {
                convertEntity(entity);
            }

            //Load tile entities.
            tile_entities = (ListTag<CompoundTag>) cubeLevel.getListTag("TileEntities");

            //Convert each block in the chunk individually.
            for (int j = 0; j < 4096; j++) {
                convertBlock(y, j);
            }

            //Convert the list of unique blocks to a 1.18.2 block palette.
            palette = new ListTag<>(CompoundTag.class);
            paletteID = new HashMap<>();

            counter = 0;
            for (LegacyID id : uniqueBlocks) {
                //Store the index of this block, so we can easily reference it
                // from the palette without having the convert it again.
                if(id.equals((byte) -60, (byte) 4)){
                    String w = "2";
                }
                paletteID.put(id, counter);
                counter++;
                palette.add(MinecraftIDConverter.getBlock(id));
            }

            //If the palette only has one block skip the next part entirely.
            if (palette.size() != 1) {

                createBlockPalette();

            } else {

                //If the single block is not air, set the chunk to not empty.
                if (uniqueBlocks.get(0).getID() != 0) {
                    isEmpty = false;
                } else {
                    sections.add(mng.createEmptySection(y));
                    return;
                }
            }

            //Construct section.
            CompoundTag section = new CompoundTag();

            section.putByte("Y", (byte) (y + (Main.OFFSET / 16)));
            //Create block_states compound tag.
            CompoundTag block_states = new CompoundTag();
            block_states.put("palette", palette);
            if (palette.size() > 1) {
                block_states.putLongArray("data", newData);
            }

            section.put("block_states", block_states);

            section.put("biomes", biomes);

            section.putByteArray("BlockLight", oldSection.getByteArray("BlockLight"));
            section.putByteArray("SkyLight", oldSection.getByteArray("SkyLight"));

            //Add section to sections.
            sections.add(section);

        } else {

            //Add empty section
            sections.add(mng.createEmptySection(y));

        }
    }

    /**
     * Convert a block to 1.18.2 format, also check for block entity and store in post-processing if necessary.
     *
     * @param y
     * y level of the cube, not the y level of the block
     *
     * @param j
     * index of the block within the cube
     */
    private void convertBlock(int y, int j) {

        //Get data for block.
        if (j % 2 == 0) {
            meta = (byte) (data[j >> 1] & 0x0f);
        } else {
            meta = (byte) ((data[j >> 1] >> 4) & 0x0f);
        }

        //Add block if unique.
        addUnique(blocks[j], meta);

        //Convert block entities
        //If the block is a block entity, load it.
        if (MinecraftIDConverter.isBlockEntity(blocks[j]) || MinecraftIDConverter.requiredPostProcessing(blocks[j], meta)) {

            //Convert current block to x,y,z coordinate.
            //blockPos = y*256 + z*16 + x = i
            cY = j / 256;
            cZ = (j - (cY * 256)) / 16;
            cX = j - (cY * 256) - (cZ * 16);


            //Find the tile entity from the list.
            if (MinecraftIDConverter.isBlockEntity(blocks[j])) {

                for (CompoundTag tile_entity : tile_entities) {

                    //If the coordinates are equal.
                    if ((tile_entity.getInt("x") == (entryX * 16) + cX) &&
                            (tile_entity.getInt("z") == (entryZ * 16) + cZ) &&
                            (tile_entity.getInt("y") == (y * 16 + cY))) {

                        //Get the block entity
                        block_entity = MinecraftIDConverter.getBlockEntity(blocks[j], meta, tile_entity);
                        tEntity = tile_entity.clone();

                        //Add the block entity to the list.
                        //Only if the block entity needs to be added.
                        if (!MinecraftIDConverter.blockEntityNotAdded(blocks[j])) {
                            block_entities.add(block_entity);
                        }
                        break;

                    }
                }
            }

            //If block requires post-processing add it to the txt file.
            if (MinecraftIDConverter.requiredPostProcessing(blocks[j], meta)) {

                //Create new json object for this block and add it to the array.
                JSONObject obj = new JSONObject();
                obj.put("block", MinecraftIDConverter.getNameSpace(blocks[j], meta));

                //Add the coordinates of the block to the object.
                obj.put("x", (entryX * 16) + cX);
                obj.put("y", ((y * 16) + Main.OFFSET + cY));
                obj.put("z", (entryZ * 16) + cZ);

                //Add properties for certain blocks.
                if (MinecraftIDConverter.hasProperties(blocks[j])) {
                    //If it's a block entity.
                    if (MinecraftIDConverter.isBlockEntity(blocks[j])) {
                        //Add the properties.
                        obj.put("properties", MinecraftIDConverter.getProperties(blocks[j], meta, tEntity));

                    } else {
                        //Add the properties
                        obj.put("properties", MinecraftIDConverter.getProperties(blocks[j], meta, null));

                    }
                }

                //Add object to array.
                ja.add(obj);
            }
        }
    }

    /**
     * Create the block palette for the chunk, this stores the blocks and their block data.
     * The indexes of the palette are referenced by the chunk to see which block is where.
     * If a chunk only contains a single type of block then the palette is not used.
     */
    private void createBlockPalette() {

        //Set column to not empty.
        isEmpty = false;

        //Find the number of blocks that will be stored in each long array. If the count is 16 or less then each block will use 4 bits.
        //A long allows for 64 bits in total.
        int blockSize = mng.log2(uniqueBlocks.size() - 1);
        if (blockSize < 4) {
            blockSize = 4;
        }
        int blocksPerLong = 64 / blockSize;
        newData = new long[(4096 + blocksPerLong - 1) / blocksPerLong];

        counter = 0;
        //To remember the index of the long.
        int index = 0;
        long blockIndex;

        //Long value
        long blockStorage = 0;

        //Convert the data to the new section structure of Minecraft 1.18.2
        for (int j = 0; j < 4096; j++) {

            //If counter is equal or greater than the blocksPerLong we need to create a new long and reset the counter.
            if (counter >= blocksPerLong) {
                counter = 0;
                blockStorage = 0;
                index++;
            }

            //Get data for block.
            if (j % 2 == 0) {
                meta = (byte) (data[j >> 1] & 0x0f);
            } else {
                meta = (byte) ((data[j >> 1] >> 4) & 0x0f);
            }

            //Get the id for the block.
            blockIndex = getPaletteID(blocks[j], meta);

            //Shift the blockIndex by blockSize * counter so the value gets put in the right place in the long.
            //Using the bitwise or function we can set the values in the long.
            blockIndex = blockIndex << (counter * blockSize);
            blockStorage = blockStorage | blockIndex;

            //Update the counter.
            counter++;

            //Set the long in the array when the counter is at the blocksPerLong value.
            if (counter == blocksPerLong || j == 4095) {
                newData[index] = blockStorage;
            }
        }
    }

    public void start() {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }

    private boolean containsBiome(String biome) {
        for (StringTag biomeNamespace : biomePalette) {
            if (biomeNamespace.getValue().equals(biome)) {
                return true;
            }
        }
        return false;
    }

    private void addUnique(byte block, byte data) {
        unique = true;
        for (LegacyID id : uniqueBlocks) {
            if (id.equals(block, data)) {
                unique = false;
            }
        }
        if (unique) {
            uniqueBlocks.add(new LegacyID(block, data));
        }
    }

    private int getPaletteID(byte block, byte data) {
        for (LegacyID id : uniqueBlocks) {
            if (id.equals(block, data)) {
                return paletteID.get(id);
            }
        }
        return 0;
    }

    private void save(Path regionDir, ByteBuffer data) throws IOException {
        Utils.createDirectories(regionDir);
        MinecraftSaveSection save = new MinecraftSaveSection(new RWLockingCachedRegionProvider<>(
                new SimpleRegionProvider<>(new MinecraftChunkLocation.Provider(MCA.name().toLowerCase()), regionDir, (keyProvider, regionKey) ->
                        MemoryWriteRegion.<MinecraftChunkLocation>builder()
                                .setDirectory(regionDir)
                                .setSectorSize(4096)
                                .setKeyProvider(keyProvider)
                                .setRegionKey(regionKey)
                                .addHeaderEntry(new TimestampHeaderEntryProvider<>(TimeUnit.SECONDS))
                                .build(),
                        (file, key) -> Files.exists(file)
                )
        ));
        save.save(new MinecraftChunkLocation(entryX, entryZ, "mca"), data);
    }

    /**
     * Convert the CompoundTag of entity to a 1.18.2+ suitable format, and add it to the entities JSON array
     * @param entity The compound tag describing the entity
     */
    private void convertEntity(CompoundTag entity){
        JSONObject object = new JSONObject();

        String id = entity.getString("id");

        if(MinecraftIDConverter.isEntitySupported(id)){
            object.put("entity", MinecraftIDConverter.getEntityID(id));

            ListTag<DoubleTag> pos = entity.getListTag("Pos").asDoubleTagList();

            object.put("x", pos.get(0).asDouble());
            object.put("y", pos.get(1).asDouble() + (double) Main.OFFSET);
            object.put("z", pos.get(2).asDouble());

            JSONObject properties = new JSONObject();
            MinecraftIDConverter.getEntitiesTags(id, entity, properties);
            if(!properties.isEmpty())
                object.put("properties", properties);

            jaEntities.add(object);
        }


    }
}
