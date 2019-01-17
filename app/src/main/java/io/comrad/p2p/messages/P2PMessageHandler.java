package io.comrad.p2p.messages;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.comrad.p2p.P2PActivity;
import io.comrad.p2p.P2PConnectedThread;
import io.comrad.p2p.network.Graph;
import io.comrad.p2p.network.GraphUpdate;
import io.comrad.p2p.network.Node;

public class P2PMessageHandler extends Handler {

    public static final int MESSAGE_TOAST = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;

    public static final String TOAST = "Toast";

    private static int COUNTER = 0;

    public Graph network;

    private final Map<String, P2PConnectedThread> peerThreads = new HashMap<>();
    public final Map<String, Set<Integer>> counters = new HashMap<>();

    private final P2PActivity activity;

    public P2PMessageHandler(P2PActivity activity) {
        this.activity = activity;
    }

    public void onBluetoothEnable() {
        System.out.println(P2PActivity.getBluetoothMac(this.activity.getApplicationContext()));
        this.network = new Graph(P2PActivity.getBluetoothMac(this.activity.getApplicationContext()));
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
        }
    }

    public void sendToastToUI(String message) {
        Message toast = this.obtainMessage(P2PMessageHandler.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(P2PMessageHandler.TOAST, message);
        toast.setData(bundle);
        this.sendMessage(toast);
    }

    public void addPeer(String mac, P2PConnectedThread thread) {
        this.peerThreads.put(mac, thread);

        System.out.println("Sending network: " + this.network);
        P2PMessage p2pMessage = new P2PMessage(null, null, MessageType.handshake_network, this.network);
        thread.write(p2pMessage);
    }

    public void removePeer(String mac) {
        this.peerThreads.remove(mac);

        GraphUpdate graphUpdate = new GraphUpdate();
        graphUpdate.removeEdge(this.network.getSelfNode().getMac(), mac);
        P2PMessage message = new P2PMessage(network.getSelfNode().getMac(), this.getBroadcastAddress(), MessageType.update_network_structure, graphUpdate);
        this.broadcast(message);

        this.network.apply(graphUpdate);
    }

    public boolean hasPeer(String mac) {
        return this.peerThreads.containsKey(mac);
    }

    public void sendMessageToPeers(P2PMessage p2pMessage) {
        for(P2PConnectedThread thread : this.peerThreads.values()) {
            thread.write(p2pMessage);
        }
    }

    public void forwardMessage(P2PMessage p2pMessage) {
        Node closestMac = this.network.getNext(p2pMessage.getDestinationMAC());

        if (closestMac == null) {
            System.out.println("Next node was null");
        } else {
            this.peerThreads.get(closestMac.getMac()).write(p2pMessage);
        }

    }

    public void closeAllConnections() {
        for(P2PConnectedThread thread : this.peerThreads.values()) {
            thread.close();
        }
    }

    public void broadcast(P2PMessage message) {
        for(P2PConnectedThread thread : this.peerThreads.values()) {
            thread.write(message);
        }
    }

    public void broadcastExcluding(P2PMessage message, String address) {
        for(P2PConnectedThread thread : this.peerThreads.values()) {
            if(!thread.getRemoteDevice().getAddress().equalsIgnoreCase(address))
            {
                thread.write(message);
            }
        }
    }

    public synchronized String getBroadcastAddress() {
        COUNTER++;
        return "b:" + COUNTER;
    }
}
