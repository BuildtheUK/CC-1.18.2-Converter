package org.btuk.converter.cc;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Loads chunk data from input format into an in-memory representation appropriate for a specific format converter.
 */
public interface ChunkDataReader<T> extends AutoCloseable {

    /**
     * Counts chunks in the input world. This is expected to run in the background.
     *
     * @param increment Runnable to run when a new chunk is detected,
     * to increment the internal counter and update progress report
     */
    void countInputChunks(Runnable increment) throws IOException, InterruptedException;

    /**
     * Loads chunks into memory, and gives them to the provided consumer.
     * The provided consumer will block if data is provided too fast.
     *
     * @param accept the data consumer
     * @param errorHandler the error handler. Returns true if reading should continue, false otherwise.
     */
    void loadChunks(Consumer<? super T> accept, Predicate<Throwable> errorHandler) throws IOException, InterruptedException;

    /**
     * Indicates that reading chunks should be stopped and
     * {@link #loadChunks(Consumer)} method should return.
     * Can be called from any thread.
     */
    void stop();
}
