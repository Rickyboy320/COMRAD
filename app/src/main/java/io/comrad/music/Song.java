package io.comrad.music;

import android.bluetooth.BluetoothDevice;

public class Song {
    private String songTitle;
    private String songArtist;
    private String songLocation;
    private int songSize;
    private BluetoothDevice owner;

    public Song(String songTitle, String songArtist, String songLocation, int songSize,
                BluetoothDevice owner) {
        this.songTitle = songTitle;
        this.songArtist = songArtist;
        this.songLocation = songLocation;
        this.songSize = songSize;
        this.owner = owner;
    }

    @Override
    public String toString() {
        return this.getSongTitle() + "\n" + this.getSongArtist();
    }

    public String getSongTitle() {
        return this.songTitle;
    }

    public String getSongArtist() {
        return this.songArtist;
    }

    public String getSongLocation() {
        return this.songLocation;
    }

    public int getSongSize() {
        return this.songSize;
    }

    public BluetoothDevice getOwner() {
        return this.owner;
    }
}
