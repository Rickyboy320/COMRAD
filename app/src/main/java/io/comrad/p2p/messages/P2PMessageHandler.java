package io.comrad.p2p.messages;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.provider.SelfDestructiveThread;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import io.comrad.music.Song;
import io.comrad.p2p.P2PActivity;
import io.comrad.p2p.network.Graph;
import io.comrad.p2p.network.Node;
import io.comrad.p2p.network.P2PNetworkHandler;

public class P2PMessageHandler extends Handler {

    public static final int MESSAGE_TOAST = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_SONG = 4;
    public static final int UPDATE_GRAPH = 5;

    public static final String TOAST = "Toast";
    public static final String SONG = "Song";
    public static final String GRAPH = "Graph";

    private final P2PActivity activity;
    private P2PNetworkHandler networkHandler;

    public P2PMessageHandler(P2PActivity activity) {
        this.activity = activity;
    }

    public void onBluetoothEnable(ArrayList<Song> ownSongs) {
        this.networkHandler = new P2PNetworkHandler(this.activity, ownSongs, this);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case P2PMessageHandler.MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                String writeMessage = new String(writeBuf);
                System.out.println("Writing: " + writeMessage);
                break;
            case P2PMessageHandler.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                String readMessage = new String(readBuf, 0, msg.arg1);
                System.out.println("Reading: " + readMessage);
                sendToastToUI("Incoming: " + readMessage);
                break;
            case P2PMessageHandler.MESSAGE_TOAST:
                Toast.makeText(activity.getApplicationContext(), msg.getData().getString(P2PMessageHandler.TOAST), Toast.LENGTH_SHORT).show();
                break;
            case P2PMessageHandler.MESSAGE_SONG:
                activity.sendByteArrayToPlayMusic(msg.getData().getByteArray(P2PMessageHandler.SONG));
                break;
                case P2PMessageHandler.UPDATE_GRAPH:
                activity.refreshPlaylist((Set<Node>) msg.getData().getSerializable(P2PMessageHandler.GRAPH));
                break;
        }
    }

    public void sendToastToUI(String message) {
        Message toast = this.obtainMessage(P2PMessageHandler.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(P2PMessageHandler.TOAST, message);
        toast.setData(bundle);
        this.sendMessage(toast);
    }

    public void sendPlayListToActivity(HashSet<Node> nodes) {
        Message graph = this.obtainMessage(P2PMessageHandler.UPDATE_GRAPH);
        Bundle bundle = new Bundle();
        bundle.putSerializable(P2PMessageHandler.GRAPH, nodes);
        graph.setData(bundle);
        this.sendMessage(graph);
    }

    public void sendSongToActivity(byte[] songBytes) {
        Message song = this.obtainMessage(P2PMessageHandler.MESSAGE_SONG);
        Bundle bundle = new Bundle();
        bundle.putByteArray("Song", songBytes);
        song.setData(bundle);
        this.sendMessage(song);
    }

    public  P2PNetworkHandler getNetwork() {
        return this.networkHandler;
    }
}
