package io.comrad.p2p.messages;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import io.comrad.music.Song;
import io.comrad.music.SongPacket;
import io.comrad.p2p.P2PActivity;
import io.comrad.p2p.network.Node;
import io.comrad.p2p.network.P2PNetworkHandler;
import nl.erlkdev.adhocmonitor.AdhocMonitorService;

public class P2PMessageHandler extends Handler {

    public static final int MESSAGE_TOAST = 1;
    public static final int MESSAGE_SONG = 2;
    public static final int UPDATE_GRAPH = 3;
    public static final int MESSAGE_SONG_FINISHED = 4;

    public static final String TOAST = "Toast";
    public static final String OFFSET = "Song Offset";
    public static final String SONG = "Song";
    public static final String REQUEST_ID = "Song Request Id";
    public static final String NODES = "Nodes";

    private final P2PActivity activity;
    private P2PNetworkHandler networkHandler;

    public P2PMessageHandler(P2PActivity activity) {
        this.activity = activity;
    }

    public void onBluetoothEnable(ArrayList<Song> ownSongs) {
        this.networkHandler = new P2PNetworkHandler(this.activity, ownSongs, this);
    }

    public void onMonitorEnable(AdhocMonitorService monitor) {
        this.networkHandler.setMonitor(monitor);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case P2PMessageHandler.MESSAGE_TOAST:
                System.out.println("Toast");
                Toast.makeText(activity.getApplicationContext(), msg.getData().getString(P2PMessageHandler.TOAST), Toast.LENGTH_SHORT).show();
                break;
            case P2PMessageHandler.MESSAGE_SONG:
                System.out.println("Message song");
                Bundle data = msg.getData();
                int id = data.getInt(P2PMessageHandler.REQUEST_ID);
                int offset = data.getInt(P2PMessageHandler.OFFSET);
                byte[] song = data.getByteArray(P2PMessageHandler.SONG);
                activity.saveMusicBytePacket(id, offset, song);
                break;
            case P2PMessageHandler.MESSAGE_SONG_FINISHED:
                System.out.println("Finishing song");
                id = msg.getData().getInt(P2PMessageHandler.REQUEST_ID);
                activity.finishSong(id);
                break;
            case P2PMessageHandler.UPDATE_GRAPH:
                System.out.println("Updating graph");
                activity.refreshPlaylist((Set<Node>) msg.getData().getSerializable(P2PMessageHandler.NODES));
                break;
        }
    }

    public void sendToastToUI(String message) {
        System.out.println("Prepping toast: " + message);
        Message toast = this.obtainMessage(P2PMessageHandler.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(P2PMessageHandler.TOAST, message);
        toast.setData(bundle);
        this.sendMessage(toast);
    }

    public void sendPlayListToActivity(HashSet<Node> nodes) {
        Message graph = this.obtainMessage(P2PMessageHandler.UPDATE_GRAPH);
        Bundle bundle = new Bundle();
        bundle.putSerializable(P2PMessageHandler.NODES, nodes);
        graph.setData(bundle);
        this.sendMessage(graph);
    }

    public void sendSongToActivity(SongPacket songPacket) {
        Message song = this.obtainMessage(P2PMessageHandler.MESSAGE_SONG);
        Bundle bundle = new Bundle();
        bundle.putInt(P2PMessageHandler.REQUEST_ID, songPacket.getRequestId());
        bundle.putInt(P2PMessageHandler.OFFSET, songPacket.getOffset());
        bundle.putByteArray(P2PMessageHandler.SONG, songPacket.getData());
        song.setData(bundle);
        this.sendMessage(song);
    }

    public void sendSongFinshed(int id) {
        Message msg = this.obtainMessage(P2PMessageHandler.MESSAGE_SONG_FINISHED);
        Bundle bundle = new Bundle();
        bundle.putInt(P2PMessageHandler.REQUEST_ID, id);
        msg.setData(bundle);
        this.sendMessage(msg);
    }

    public  P2PNetworkHandler getNetwork() {
        return this.networkHandler;
    }

    public void reattachMonitor() {
        activity.reattachMonitor();
    }

    public void setIdle(boolean idle) {
        this.activity.setIdle(idle);
    }
}
