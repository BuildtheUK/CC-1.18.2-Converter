package org.btuk.converter;

import cubicchunks.regionlib.impl.EntryLocation2D;
import net.querz.nbt.io.NBTInputStream;
import net.querz.nbt.io.NBTOutputStream;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * This class aims to manage the threads running the converter.
 * This will also store all the default information that needs to be retrieved by all the threads.
 * For example standard data for chunks.
 *
 */

public class ThreadManager {

    final BlockingQueue<String> queue;
    AtomicInteger activeThreads;

    WorldIterator itr;

    ArrayList<RegionConverter> threads = new ArrayList<>();

    EntryLocation2D.Provider provider2d;

    ListTag<CompoundTag> airPalette = new ListTag<>(CompoundTag.class);
    ListTag<StringTag> defaultBiome = new ListTag<>(StringTag.class);

    ListTag<ListTag<ShortTag>> post_processing = new ListTag<>(ListTag.class);
    CompoundTag structures = new CompoundTag();

    /**
     * The constructor of the thread manager class.
     * This class managed the threads of the converter.
     *
     * @param itr
     * the worlditerator instance, this class should always be instantiated from there
     *
     * @param threadCount
     * the number of threads to use
     */

    public ThreadManager(WorldIterator itr, int threadCount) {

        //Set max threadCount for the queue.
        queue = new LinkedBlockingQueue<>();
        activeThreads = new AtomicInteger(threadCount);

        this.itr = itr;

        //Create 2d and 3d region provider.
        provider2d = new EntryLocation2D.Provider();

        //Add air to airPalette for empty sections.
        CompoundTag block = new CompoundTag();
        //Add block name as String tag.
        block.putString("Name", "minecraft:air");
        airPalette.add(block);

        //Create the default biome palette;
        defaultBiome.addString(Main.DEFAULT_BIOME);

        //Create empty structures tag.
        structures.put("References", new CompoundTag());
        structures.put("starts", new CompoundTag());

        //Create empty post-processing compound list.
        for (int p = Main.MIN_Y_CUBE; p < Main.MAX_Y_CUBE; p++) {
            post_processing.add(new ListTag<>(ShortTag.class));
        }

        //Create the threads.
        for (int i = 0; i < threadCount; i++) {
            threads.add(new RegionConverter(UUID.randomUUID(), queue, this));
        }

    }

    //Start the threads.
    public void process() {

        for (RegionConverter thread : threads) {
            thread.start();
            //Add null values to the end of the queue so we know when to close the threads.
            try {
                queue.put("end");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public NamedTag readCompressedCC(InputStream is) throws IOException {
        try (NBTInputStream nbtInputStream = new NBTInputStream(new BufferedInputStream(new GZIPInputStream(is)))) {
            return nbtInputStream.readTag(Tag.DEFAULT_MAX_DEPTH);
        }
    }

    public int log2(int value) {
        return Integer.SIZE - Integer.numberOfLeadingZeros(value);
    }

    public ByteBuffer writeCompressed(CompoundTag tag, boolean prefixFormat) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        if (prefixFormat) {
            bytes.write(1); // mark as GZIP
        }
        try (NBTOutputStream nbtOut = new NBTOutputStream(new BufferedOutputStream(new GZIPOutputStream(bytes)))) {
            nbtOut.writeTag(tag, Tag.DEFAULT_MAX_DEPTH);
        }
        return ByteBuffer.wrap(bytes.toByteArray());
    }

    public CompoundTag createEmptySection(int y) {

        //Construct section.
        CompoundTag section = new CompoundTag();

        section.putByte("Y", (byte) y);

        //Create block_states compound tag.
        CompoundTag block_states = new CompoundTag();

        //Add palette with just air.
        block_states.put("palette", airPalette);

        section.put("block_states", block_states);

        //Add the biome using the default biome.
        CompoundTag biomes = new CompoundTag();
        biomes.put("palette", defaultBiome);

        section.put("biomes", biomes);

        //TODO Add BlockLight and SkyLight

        return section;
    }
}
