package io.comrad.music;

import java.io.Serializable;

public class SongPacket implements Serializable {
    public final transient static int SONG_PACKET_SIZE = 256000;
    private int requestId;
    private int offset;
    private byte[] data;

    public SongPacket(int requestId, int offset, byte[] data) {
        this.requestId = requestId;
        this.offset = offset;
        this.data = data;
    }

    public int getRequestId() { return this.requestId; }

    public int getOffset() { return this.offset; }

    public byte[] getData() { return this.data; }
}
