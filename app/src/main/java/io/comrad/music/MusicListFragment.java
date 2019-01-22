package io.comrad.music;

import android.Manifest;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import io.comrad.R;

import static android.content.ContentValues.TAG;


public class MusicListFragment extends Fragment {

    static final int REQUEST_MUSIC_FILE = 4;

    private static final int MY_PERMISSION_REQUEST = 1;
    ArrayList<Song> playList;
    ListView listView;
    ArrayAdapter<Song> adapter;

    public MusicListFragment() {

    }

    public void setPlayList(ArrayList<Song> playList) {
        this.playList = playList;
        showMusic();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.fragment_music);
//        Intent intent = getIntent();
//        playList = (ArrayList<Song>) intent.getSerializableExtra("Nodes");

//        System.out.println("<<<<<" + playList);
        if (playList != null) {
            showMusic();
        }
//        if(ContextCompat.checkSelfPermission(MusicListFragment.this,
//                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            if(ActivityCompat.shouldShowRequestPermissionRationale(MusicListFragment.this,
//                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                ActivityCompat.requestPermissions(MusicListFragment.this,
//                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
//            } else {
//                ActivityCompat.requestPermissions(MusicListFragment.this,
//                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
//            }
//        } else {
//            showMusic();
//        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View musiclist = inflater.inflate(R.layout.fragment_music, container, false);
//        final Button button = playmusic.findViewById(R.id.play);
//        button.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                Log.d(TAG, "HEY");
//                PlayCurrentSong();
//            }
//        });
        return musiclist;
    }


    /*
     * Calls functions to retrieve all music on the device and shows them in a listview.
     */
    public void showMusic() {
        listView = getActivity().findViewById(R.id.musicView);
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, playList);
        listView.setAdapter((adapter));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Intent result = new Intent();
                Log.d("songClick", playList.get(i).toString());

//                result.putExtra("song", (Parcelable)playList.get(i));
//                setResult(Activity.RESULT_OK, result);
//                finish();

                // TODO send back to activity
                // playMp3Bytes(BYTE STREAM HERE);
            }
        });
    }


    /*
     * Adds a song to the adapter for viewing in the listview.s
     */
    public void addSong(Song song) {
        adapter.add(song);
    }

    /*
     * Adds multiple songs to the adapter for viewing in the lisview.
     */
    public void addSongs(ArrayList<Song> songs) {
        adapter.addAll(songs);
    }

    /*
     * Retrieves all music on the device and adds them to the class variable arrayList.
     */
    public void getMusic() {
        ContentResolver contentResolver = getActivity().getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null, null, null, null);

        if (songCursor != null && songCursor.moveToFirst()) {
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songLocation = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int songSize = songCursor.getColumnIndex(MediaStore.Audio.Media.SIZE);
            String currentTitle;
            String currentArtist;
            String currentLocation;
            int currentSize;

            // Loop over the found songs and add them to the arraylist
            do {
                currentTitle = songCursor.getString(songTitle);
                currentArtist = songCursor.getString(songArtist);
                currentLocation = songCursor.getString(songLocation);
                currentSize = songCursor.getInt(songSize);
                playList.add(new Song(currentTitle, currentArtist, currentLocation, currentSize));
            } while (songCursor.moveToNext());
        }
    }

    /*
     * Asks for a permission and shows the result in a toast.
     */
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getActivity(), "Permission granted!", Toast.LENGTH_SHORT).show();

                        showMusic();
                    }
                } else {
                    Toast.makeText(getActivity(), "No permission granted!", Toast.LENGTH_SHORT).show();
//                    finish();
                }
                return;
            }
        }
    }
}
