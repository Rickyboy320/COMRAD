package io.comrad.music;

import java.io.Serializable;

public class SongPacket implements Serializable {
    public final transient static int SONG_PACKET_SIZE = 256000;
    private int id;
    private byte[] data;

    public SongPacket(int id, byte[] data) {
        this.id = id;
        this.data = data;
    }

    public int get_id() { return this.id; }

    public byte[] get_data() { return this.data; }
}
