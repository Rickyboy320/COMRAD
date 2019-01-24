package io.comrad.music;

import android.media.AudioTrack;
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

import static android.content.ContentValues.TAG;


public class PlayMusic extends Fragment  {

    private AudioTrack audioTrack;
    private ImageButton playButton;

    private void playCurrentSong() {
        if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.pause();
            playButton.setImageResource(android.R.drawable.ic_media_play);

        } else {
            audioTrack.play();
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
//        ImageView img = (ImageView)playmusic.findViewById(R.id.playIcon);
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

    public void setAudioTrack(AudioTrack audioTrack) {
        this.audioTrack = audioTrack;
        this.audioTrack.play();
    }

    public void incrementProgress(int size, int diff) {
        ProgressBar progressbar = getActivity().findViewById(R.id.progressBar);
        double percentage =  (float)diff / (float)size * 100;
        System.out.println("LALALALAL" + diff + " " + size);
        progressbar.setProgress((int) Math.round(percentage));
    }
}
