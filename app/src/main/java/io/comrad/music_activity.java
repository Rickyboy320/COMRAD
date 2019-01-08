package io.comrad;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class music_activity extends AppCompatActivity {
    private static final int MY_PERMISSION_REQUEST = 1;

    ArrayList<String> arrayList;

    ListView listView;

    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_activity);

        if(ContextCompat.checkSelfPermission(music_activity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(music_activity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(music_activity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            } else {
                ActivityCompat.requestPermissions(music_activity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }
        } else {
            showMusic();
        }
    }

    public void showMusic() {
        listView = (ListView) findViewById(R.id.musicView);
        arrayList = new ArrayList<>();
        getMusic();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter((adapter));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // open music player to play desired song.
            }
        });

    }

    public void getMusic() {
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null, null, null, null);

        if (songCursor != null && songCursor.moveToFirst()) {
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

            do {
                String currentTitle = songCursor.getString(songTitle);
                String currentArtist = songCursor.getString(songArtist);
                arrayList.add(currentTitle + "\n" + currentArtist);
            } while (songCursor.moveToNext());
        }
    }

    // @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(ContextCompat.checkSelfPermission(music_activity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();

                        showMusic();
                    }
                } else {
                    Toast.makeText(this, "No permission granted!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }
}
