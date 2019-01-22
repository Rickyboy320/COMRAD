package io.comrad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import io.comrad.music.MusicListFragment;
import io.comrad.p2p.P2PActivity;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button musicButton = findViewById(R.id.music);
        musicButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MusicListFragment.class);
                startActivity(intent);
            }
        });

        Button p2pButton = findViewById(R.id.p2p);
        p2pButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, P2PActivity.class);
                startActivity(intent);
            }
        });
    }
}
