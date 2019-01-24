package io.comrad.music;

import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import io.comrad.R;

import static android.content.ContentValues.TAG;


public class PlayMusic extends Fragment  {

//    private AudioTrack audioTrack;
    private ImageButton playButton;
    private MediaPlayer currentMediaPlayer = new MediaPlayer();
    private MediaPlayer nextMediaPlayer = new MediaPlayer();

    private void playCurrentSong() {
        if (currentMediaPlayer.isPlaying()) {
            currentMediaPlayer.pause();
            playButton.setImageResource(android.R.drawable.ic_media_play);

        } else {
            currentMediaPlayer.start();
            playButton.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View playmusic = inflater.inflate(R.layout.fragment_play_music, container, false);
        playButton = playmusic.findViewById(R.id.playButton);
        playButton.setImageResource(android.R.drawable.ic_media_play);
        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "HEY");
                playCurrentSong();
            }
        });
        return playmusic;
    }

    private void prepareNextMediaPlayer(byte[] mp3SoundByteArray, MediaPlayer mediaPlayer) {
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
            Log.d(TAG, "!~! currentMediaPlayer has been reset!");

            FileInputStream inputStream = new FileInputStream(tempMp3);

            mediaPlayer.setDataSource(inputStream.getFD(), 0, mp3SoundByteArray.length);

            inputStream.close();

            Log.d(TAG, "!~! currentMediaPlayer source has been set!");

            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer currentMediaPlayer) {
                    currentMediaPlayer.start();
                    playButton.setImageResource(android.R.drawable.ic_media_pause);
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void switchMediaPlayer() {
        MediaPlayer temp = currentMediaPlayer;
        currentMediaPlayer = nextMediaPlayer;
        nextMediaPlayer = currentMediaPlayer;
    }

    public void incrementProgress(int size, int diff) {
        ProgressBar progressbar = getActivity().findViewById(R.id.progressBar);
        double percentage =  (float)diff / (float)size * 100;
        progressbar.setProgress((int) Math.round(percentage));
    }
}
