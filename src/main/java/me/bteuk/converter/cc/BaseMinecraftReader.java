package org.btuk.converter.cc;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public abstract class BaseMinecraftReader<DATA, SAVE extends Closeable> implements ChunkDataReader<DATA> {
    protected final Path srcDir;
    protected final Map<Dimension, SAVE> saves;

    public BaseMinecraftReader(Path srcDir, BiFunction<Dimension, Path, SAVE> pathToSave) {
        this.srcDir = srcDir;
        this.saves = new ConcurrentHashMap<>();
        for (Dimension d : Dimensions.getDimensions()) {
            SAVE save = pathToSave.apply(d, srcDir);
            if (save != null) {
                saves.put(d, save);
            }
        }
    }

    @Override public void close() throws Exception {
        boolean exception = false;
        for (SAVE save : saves.values()) {
            try {
                save.close();
            } catch (IOException e) {
                e.printStackTrace();
                exception = true;
            }
        }
        if (exception) {
            throw new IOException();
        }
    }
}
