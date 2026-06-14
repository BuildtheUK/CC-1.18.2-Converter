package org.btuk.converter.cc;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

import cubicchunks.regionlib.api.region.IRegion;
import cubicchunks.regionlib.api.region.IRegionProvider;
import cubicchunks.regionlib.api.region.key.IKey;
import cubicchunks.regionlib.api.region.key.IKeyProvider;
import cubicchunks.regionlib.api.region.key.RegionKey;
import cubicchunks.regionlib.lib.header.IKeyIdToSectorMap;
import cubicchunks.regionlib.lib.header.IntPackedSectorMap;
import cubicchunks.regionlib.util.CheckedConsumer;
import cubicchunks.regionlib.util.CorruptedDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MemoryReadRegion<K extends IKey<K>> implements IRegion<K> {

    private static final Logger log = LoggerFactory.getLogger(MemoryReadRegion.class);

    private final IKeyIdToSectorMap<?, ?, K> sectorMap;
    private final int sectorSize;
    private SeekableByteChannel file;
    private final RegionKey regionKey;
    private final IKeyProvider<K> keyProvider;
    private final int keyCount;
    private ByteBuffer fileBuffer;

    private final Set<RegionKey> thrownIoExceptionRegionKeys = new HashSet<>();
    private final Set<RegionKey> thrownIllegalArgumentExceptionRegionKeys = new HashSet<>();

    private MemoryReadRegion(SeekableByteChannel file,
                             IntPackedSectorMap<K> sectorMap,
                             RegionKey regionKey,
                             IKeyProvider<K> keyProvider,
                             int sectorSize) {
        this.file = file;
        this.regionKey = regionKey;
        this.keyProvider = keyProvider;
        this.keyCount = keyProvider.getKeyCount(regionKey);
        this.sectorSize = sectorSize;
        this.sectorMap = sectorMap;
    }

    @Override public synchronized void writeValue(K key, ByteBuffer value) {
        throw new UnsupportedOperationException("Writing not supported in this implementation");
    }

    @Override
    public void writeValues(Map<K, ByteBuffer> entries) {
        throw new UnsupportedOperationException("Writing not supported in this implementation");
    }

    @Override public void writeSpecial(K key, Object marker) {
        throw new UnsupportedOperationException("Writing not supported in this implementation");
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException("Writing not supported in this implementation");
    }


    @Override public synchronized Optional<ByteBuffer> readValue(K key) throws IOException {
        if (fileBuffer == null) {
            this.fileBuffer = ByteBuffer.allocate((int) file.size());

            file.position(0);
            file.read(fileBuffer);
            file.close();
            file = null;
        }
        // a hack because Optional can't throw checked exceptions
        try {
            return sectorMap.trySpecialValue(key).map(reader -> reader.apply(key)).or(() -> doReadKey(key));
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    private Optional<ByteBuffer> doReadKey(K key) {
        return sectorMap.getEntryLocation(key).flatMap(loc -> {
            try {
                int sectorOffset = loc.getOffset();
                int sectorCount = loc.getSize();

                fileBuffer.limit(sectorOffset * sectorSize + Integer.BYTES);
                fileBuffer.position(sectorOffset * sectorSize);
                int dataLength = fileBuffer.getInt();
                if (dataLength > sectorCount * sectorSize) {
                    throw new CorruptedDataException(
                            "Expected data size max " + sectorCount * sectorSize + " but found " + dataLength + " at key: " + key.toString() + " (File: " +  key.getRegionKey().getName() + ") , skipping");
                }
                fileBuffer.position(sectorOffset * sectorSize + Integer.BYTES);
                fileBuffer.limit(sectorOffset * sectorSize + Integer.BYTES + dataLength);

                return Optional.of(ByteBuffer.allocate(dataLength).put(fileBuffer));
            } catch (IOException e) {
                //Only print the IOException message if the region key didn't already throw an error
                if(!thrownIoExceptionRegionKeys.contains(key.getRegionKey())) {
                    log.error("{}: {} on key {}", e.getClass().getName(), e.getMessage(), key);
                    thrownIoExceptionRegionKeys.add(key.getRegionKey());
                }
                return Optional.empty();
            } catch (IllegalArgumentException e) {
                //Only print the IllegalArgumentException message, if the region key didn't already throw a IllegalArgumentException
                if(!thrownIllegalArgumentExceptionRegionKeys.contains(key.getRegionKey())) {
                    log.error("Illegal argument while reading key {} at : {}. This may be caused by a corrupt section.", key, e.getMessage());
                    thrownIllegalArgumentExceptionRegionKeys.add(key.getRegionKey());
                }
                return Optional.empty();
            }
        });
    }

    /**
     * Returns true if something was stored there before within this region.
     */
    @Override public synchronized boolean hasValue(K key) {
        return sectorMap.getEntryLocation(key).isPresent();
    }

    @Override public void forEachKey(CheckedConsumer<? super K, IOException> cons) throws IOException {
        for (int id = 0; id < this.keyCount; id++) {
            int idFinal = id; // because java is stupid
            K key = sectorMap.getEntryLocation(id).map(loc -> keyProvider.fromRegionAndId(this.regionKey, idFinal)).orElse(null);
            if (key != null) {
                cons.accept(key);
            }
        }
    }

    @Override public void close() throws IOException {
        if (file != null) {
            file.close();
        }
    }

    /**
     * Internal Region builder. Using it is very unsafe, there are no safeguards against using it improperly. Should only be used by
     * {@link IRegionProvider} implementations.
     */
    // TODO: make a safer to use builder
    public static class Builder<K extends IKey<K>> {

        private Path directory;
        private int sectorSize = 512;
        private RegionKey regionKey;
        private IKeyProvider<K> keyProvider;
        private final List<IntPackedSectorMap.SpecialSectorMapEntry<K>> specialEntries = new ArrayList<>();

        public MemoryReadRegion.Builder<K> setDirectory(Path path) {
            this.directory = path;
            return this;
        }

        public MemoryReadRegion.Builder<K> setRegionKey(RegionKey key) {
            this.regionKey = key;
            return this;
        }

        public MemoryReadRegion.Builder<K> setKeyProvider(IKeyProvider<K> keyProvider) {
            this.keyProvider = keyProvider;
            return this;
        }

        public MemoryReadRegion.Builder<K> setSectorSize(int sectorSize) {
            this.sectorSize = sectorSize;
            return this;
        }

        public MemoryReadRegion<K> build() throws IOException {
            SeekableByteChannel file = Files.newByteChannel(directory.resolve(regionKey.getName()), CREATE, READ, WRITE);
            IntPackedSectorMap<K> sectorMap = IntPackedSectorMap.readOrCreate(file, keyProvider.getKeyCount(regionKey), specialEntries);
            return new MemoryReadRegion<>(file, sectorMap, this.regionKey, keyProvider, this.sectorSize);
        }
    }
}