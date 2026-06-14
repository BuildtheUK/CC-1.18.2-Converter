package org.btuk.converter.utils;

public class LegacyID {

    private byte id;
    private byte data;

    public LegacyID(byte id, byte data) {
        this.id = id;
        this.data = data;
    }

    public boolean equals(byte id, byte data) {
        return (this.id == id && this.data == data);
    }

    public byte getID() {
        return id;
    }

    public byte getData() {
        return data;
    }
}
