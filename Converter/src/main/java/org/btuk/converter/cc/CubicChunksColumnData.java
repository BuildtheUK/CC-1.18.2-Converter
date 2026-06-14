package org.btuk.converter.cc;

import cubicchunks.regionlib.impl.EntryLocation2D;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;

public class CubicChunksColumnData {

    private final Dimension dimension;
    private final EntryLocation2D position;
    private final ByteBuffer columnData;
    private final Map<Integer, ByteBuffer> cubeData;

    public CubicChunksColumnData(Dimension dimension, EntryLocation2D position, ByteBuffer columnData,
                                 Map<Integer, ByteBuffer> cubeData) {
        this.dimension = dimension;
        this.position = position;
        this.columnData = columnData;
        this.cubeData = cubeData;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public EntryLocation2D getPosition() {
        return position;
    }

    public ByteBuffer getColumnData() {
        return columnData;
    }

    public Map<Integer, ByteBuffer> getCubeData() {
        return cubeData;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CubicChunksColumnData that = (CubicChunksColumnData) o;
        return dimension.equals(that.dimension) &&
                position.equals(that.position) &&
                Objects.equals(columnData, that.columnData) &&
                cubeData.equals(that.cubeData);
    }

    @Override public int hashCode() {
        return Objects.hash(dimension, position, columnData, cubeData);
    }

    @Override public String toString() {
        return "CubicChunksColumnData{" +
                "dimension='" + dimension + '\'' +
                ", position=" + position +
                ", columnData=" + columnData +
                ", cubeData=" + cubeData +
                '}';
    }
}
