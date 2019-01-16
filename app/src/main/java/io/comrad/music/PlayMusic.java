package io.comrad.music;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import static android.content.ContentValues.TAG;

import io.comrad.R;
import io.comrad.p2p.P2PActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlayMusic.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlayMusic#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayMusic extends Fragment implements View.OnClickListener {

//    private static final String TAG = null;
    private Song current;

    private OnFragmentInteractionListener mListener;

    public PlayMusic() {
        this.current = new Song("No Song playing", "", "", 0, "");
        // Required empty public constructor
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
    public void onClick(View v) {
        Log.d(TAG,"on CLick");
        switch (v.getId()) {
            case R.id.play:
                Log.d(TAG, "Play on click");
//                Toast.makeText(getContext(), "Button One", Toast.LENGTH_SHORT).show();
                ((P2PActivity)getActivity()).play();
                break;
//            case R.id.button_2:
//                Toast.makeText(getContext(), "Button Two", Toast.LENGTH_SHORT).show();
//                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View playmusic = inflater.inflate(R.layout.fragment_play_music, container, false);
//        Button buttonOne = playmusic.findViewById(R.id.play);
        final Button button = playmusic.findViewById(R.id.play);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "HEY");
                // Code here executes on main thread after user presses button
            }
        });
        // Inflate the layout for this fragment
        return playmusic;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.sendSongToFragment(false, current);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void sendSongToFragment(boolean play, Song song);
    }
}
