package io.comrad.music;

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
import io.comrad.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;


public class PlayMusic extends Fragment  {
    private ImageButton playButton;
    private ArrayList<MediaPlayer> mediaPlayers = new ArrayList<>();

    private void playCurrentSong() {
        if (mediaPlayers.size() >= 1) {
            if (mediaPlayers.get(0).isPlaying()) {
                mediaPlayers.get(0).pause();
                playButton.setImageResource(android.R.drawable.ic_media_play);

            } else {
                mediaPlayers.get(0).start();
                playButton.setImageResource(android.R.drawable.ic_media_pause);
            }
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

    public void newBufferMessage(byte[] message) {
        MediaPlayer media = new MediaPlayer();
        prepareNextMediaPlayer(message, media);
    }

    private boolean prepareMediaPlayer(byte[] soundArray, MediaPlayer mediaPlayer) {
        try {
            /* create temp file that will hold byte array */
            File tempFile = File.createTempFile("tmpSong", null, getActivity().getCacheDir());
            System.out.println("Creating file: " + tempFile.getAbsolutePath());
            tempFile.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(soundArray);
            fos.flush();
            fos.close();

            // resetting mediaplayer instance to evade problems
            mediaPlayer.reset();
            Log.d(TAG, "!~! mediaPlayers.get(mediaPlayerIndex) has been reset!");

            FileInputStream inputStream = new FileInputStream(tempFile);

            mediaPlayer.setDataSource(inputStream.getFD());

            inputStream.close();

            Log.d(TAG, "!~! mediaPlayers.get(mediaPlayerIndex) source has been set!");

            mediaPlayer.prepareAsync();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mediaPlayers.size() > 0) {
                        mediaPlayers.remove(0);
                    }

                    mp.stop();
                    mp.release();
                }
            });
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void prepareNextMediaPlayer(byte[] soundArray, MediaPlayer mediaPlayer) {
        boolean success = prepareMediaPlayer(soundArray, mediaPlayer);
        if (success) {
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayers.add(mediaPlayer);

                    if(mediaPlayers.size() == 1) {
                        mediaPlayer.start();
                        playButton.setImageResource(android.R.drawable.ic_media_pause);
                    } else if(mediaPlayers.size() > 1) {
                        mediaPlayers.get(mediaPlayers.size() - 2).setNextMediaPlayer(mediaPlayer);
                    }
                }
            });
        } else {
            System.out.println("Failed preparing...");
        }

    }

    public void incrementProgress(int size, int diff) {
        System.out.println("Progress")
        ProgressBar progressbar = getActivity().findViewById(R.id.progressBar);
        double percentage =  (float)diff / (float)size * 100;
        progressbar.setProgress((int) Math.round(percentage));
    }

    public void clearBuffers() {
        System.out.println("Resetting buffers");
        for(MediaPlayer mp : this.mediaPlayers) {
            mp.stop();
            mp.release();
        }

        this.mediaPlayers.clear();
    }
}
