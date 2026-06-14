package org.btuk.converter.cc;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Dimensions {

    private static final Set<Dimension> ALL_DIMENSIONS = new HashSet<>();

    static {
        addDimension(new Dimension("Overworld", ""));
        addDimension(new Dimension("The Nether", "DIM-1"));
        addDimension(new Dimension("The End", "DIM1"));
    }

    public static void addDimension(Dimension dim) {
        ALL_DIMENSIONS.add(dim);
    }

    public static Collection<Dimension> getDimensions() {
        return Collections.unmodifiableCollection(ALL_DIMENSIONS);
    }
}
