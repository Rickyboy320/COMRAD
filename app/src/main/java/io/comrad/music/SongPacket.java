package io.comrad.music;

import java.io.Serializable;

public class SongPacket implements Serializable {
    public final transient static int SONG_PACKET_SIZE = 256000;
    private int offset;
    private byte[] data;

    public SongPacket(int offset, byte[] data) {
        this.offset = offset;
        this.data = data;
    }

    public int getOffset() { return this.offset; }

    public byte[] getData() { return this.data; }
}
