package io.comrad.music;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Song implements Parcelable, Serializable {
    private String songTitle;
    private String songArtist;
    private String songLocation;
    private int songSize;
    // MAC Address of the owner

    public Song(String songTitle, String songArtist, String songLocation, int songSize) {
        this.songTitle = songTitle;
        this.songArtist = songArtist;
        this.songLocation = songLocation;
        this.songSize = songSize;
    }

    private Song(Parcel in) {
        this.songTitle = in.readString();
        this.songArtist = in.readString();
        this.songLocation = in.readString();
        this.songSize = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.songTitle);
        out.writeString(this.songArtist);
        out.writeString(this.songLocation);
        out.writeInt(this.songSize);
    }

    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };


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
}
