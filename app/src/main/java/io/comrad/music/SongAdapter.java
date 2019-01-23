package io.comrad.music;

import android.app.Activity;
import android.content.Context;
import android.icu.text.DecimalFormat;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.comrad.R;
import io.comrad.music.Song;
import io.comrad.p2p.messages.P2PMessage;

import static android.content.ContentValues.TAG;


public class SongAdapter extends ArrayAdapter<Song> {
    private Activity mContext;
    private List<Song> SongsList;
    private int layout;
    private LayoutInflater layoutInflater;

    public SongAdapter(Activity context, int layout,  ArrayList<Song> list) {
        super(context, 0, list);
        this.mContext = context;
//        this.SongsList = list;
//        this.layout = layout;
//        this.layoutInflater= (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    private String sizeToDisplayMB(int bytes) {
        double result = bytes / Math.pow(10, 6);
        result = Math.floor(result * 100) / 100;
        return String.valueOf(result) + " MB";
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        View listItem = view;
//        if (listItem == null) {
        LayoutInflater inflater = mContext.getLayoutInflater();
        if (listItem == null) {
            listItem = inflater.from(mContext).inflate(R.layout.song_item, parent, false);
        }
//        } else {
//        }
//        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
//        txtTitle.setText(web[position]);
//        imageView.setImageResource(imageId[position]);
//        return rowView;
//
//        if(listItem == null) {
//            listItem = LayoutInflater.from(mContext).inflate(R.layout.song_item, parent, false);
//        }

        Song currentSong = getItem(position);

        TextView name = listItem.findViewById(R.id.textView_name);
        TextView artist = listItem.findViewById(R.id.textView_artist);
        TextView size = listItem.findViewById(R.id.textView_size);
        name.setText(currentSong.getSongTitle());
        artist.setText(currentSong.getSongArtist());
        size.setText(sizeToDisplayMB(currentSong.getSongSize()));

        return listItem;
    }

//    @Override
//    public int getCount() {
//        return this.SongsList.size();
//    }
}
