package org.btuk.converter.cc;

public class ImmutablePair<K, V> {
    private final K key;
    private final V value;

    public ImmutablePair(K k, V v) {
        key = k;
        value = v;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public K getFirst() {
        return key;
    }

    public V getSecond() {
        return value;
    }
}
