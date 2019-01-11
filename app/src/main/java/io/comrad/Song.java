package io.comrad;

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
        return this.get_songTitle() + "\n" + this.get_songArtist();
    }

    public String get_songTitle() {
        return this.songTitle;
    }

    public String get_songArtist() {
        return this.songArtist;
    }

    public String get_songLocation() {
        return this.songLocation;
    }

    public int get_songSize() {
        return this.songSize;
    }

    public BluetoothDevice get_owner() {
        return this.owner;
    }
}
