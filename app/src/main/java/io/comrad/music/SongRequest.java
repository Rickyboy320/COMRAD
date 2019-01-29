package io.comrad.music;

import java.io.Serializable;

public class SongRequest implements Serializable {

    private int requestId;
    private Song song;

    public SongRequest(int requestId, Song song) {
        this.requestId = requestId;
        this.song = song;
    }

    public int getRequestId() { return this.requestId; }

    public Song getSong() { return this.song; }

    @Override
    public String toString() { return this.requestId + ": " + this.song.toString(); }
}
