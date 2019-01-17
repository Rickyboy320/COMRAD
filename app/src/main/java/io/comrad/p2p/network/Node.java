package io.comrad.p2p.network;

import java.io.Serializable;
import java.util.ArrayList;

import io.comrad.music.Song;

public class Node implements Serializable {
    private String mac;
    private ArrayList<Song> playlist;

    Node(String mac) {
        this.mac = mac;
    }

    Node(String mac, ArrayList<Song> playlist) {
        this.playlist = playlist;
        this.mac = mac;
    }

    public String getMac() {
        return this.mac;
    }

    public ArrayList<Song> getPlaylist() {
        return playlist;
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof Node) {
            return this.mac.equalsIgnoreCase(((Node) object).getMac());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.mac.hashCode();
    }

    @Override
    public String toString() {
        return this.mac;
    }
}
