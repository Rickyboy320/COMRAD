package io.comrad.music;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import io.comrad.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.ContentValues.TAG;


public class PlayMusic extends Fragment  {

    Song current;
    private byte[] currentBytes;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    public PlayMusic() {
        this.current = new Song("No Song playing", "", "", 0);
    }

    private void PlayCurrentSong() {
        if (currentBytes != null) {
            playMp3Bytes(currentBytes);
        } else {
            // No song chosen
        }
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlayMusic.
     */
    // TODO: Rename and change types and number of parameters
    public static PlayMusic newInstance(String param1, String param2) {
        PlayMusic fragment = new PlayMusic();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View playmusic = inflater.inflate(R.layout.fragment_play_music, container, false);
        final Button button = playmusic.findViewById(R.id.play);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PlayCurrentSong();
            }
        });
        return playmusic;
    }

    /*
     * Plays a mp3 received from the given byte stream.
     */
    private void playMp3Bytes(byte[] mp3SoundByteArray) {
        try {
            /* create temp file that will hold byte array */
            File tempMp3 = File.createTempFile("tmpSong", "mp3", getActivity().getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.flush();
            fos.close();

            // resetting mediaplayer instance to evade problems
            mediaPlayer.reset();
            Log.d(TAG, "!~! mediaPlayer has been reset!");

            FileInputStream inputStream = new FileInputStream(tempMp3);

            mediaPlayer.setDataSource(inputStream.getFD(), 0, mp3SoundByteArray.length);

            inputStream.close();

            Log.d(TAG, "!~! mediaPlayer source has been set!");

            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void addSongBytes(byte[] songBytes) {
        currentBytes = songBytes;
        PlayCurrentSong();
    }
}
