package io.comrad.music;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Parcel;
import android.os.Parcelable;
import io.comrad.p2p.messages.P2PMessageHandler;

import java.io.*;

public class Song implements Parcelable, Serializable {
    private String songTitle;
    private String songArtist;
    private String songLocation;
    private int songSize;


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

    public SongMetaData getSongMetaData() {
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(this.getSongLocation());
            MediaFormat format = extractor.getTrackFormat(0);
            SongMetaData metaData = new SongMetaData(format.getInteger(MediaFormat.KEY_SAMPLE_RATE), format.getInteger(MediaFormat.KEY_CHANNEL_COUNT));

            return metaData;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
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

    @Override
    public boolean equals(Object object) {
        if (object instanceof Song) {
            Song song = (Song) object;
            return this.songTitle.equals(song.songTitle) && this.songArtist.equals(song.songArtist) && this.songLocation.equals(song.songLocation) && this.songSize == song.songSize;
        }

        return false;
    }

    public InputStream getStream(P2PMessageHandler handler) {
        File songFile = new File(this.getSongLocation());
        InputStream inputStream;

        try {
            inputStream = new FileInputStream(songFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            handler.sendToastToUI("Could not find file.");
            return null;
        }

        return inputStream;
    }

    public class SongMetaData implements Serializable {
        private int sampleRate;
        private int numChannels;

        public SongMetaData(int sampleRate, int numChannels) {
            this.sampleRate = sampleRate;
            this.numChannels = numChannels;
        }

        public int getSampleRate() {
            return sampleRate;
        }

        public int getNumChannels() {
            return numChannels;
        }
    }
}
