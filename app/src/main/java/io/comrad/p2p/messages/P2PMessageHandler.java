package io.comrad.p2p.messages;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import io.comrad.p2p.P2PActivity;
import io.comrad.p2p.P2PConnectedThread;
import io.comrad.p2p.network.Graph;

public class P2PMessageHandler extends Handler {

    public static final int MESSAGE_TOAST = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;

    public static final String TOAST = "Toast";

    public Graph network;
    private final Map<String, P2PConnectedThread> peerThreads = new HashMap<>();

    private final P2PActivity activity;

    public P2PMessageHandler(P2PActivity activity) {
        this.activity = activity;
    }

    public void onBluetoothEnable() {
        this.network = new Graph(Settings.Secure.getString(this.activity.getApplicationContext().getContentResolver(), "bluetooth_address"));
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

    public void sendBufferToUI(byte[] buffer, int bufferSize) {
        Message readMsg = this.obtainMessage(P2PMessageHandler.MESSAGE_READ, bufferSize, -1, buffer);
        readMsg.sendToTarget();
    }

    public void addPeer(String mac, P2PConnectedThread thread) {
        this.peerThreads.put(mac, thread);

        System.out.println("Sending network: " + this.network);
        P2PMessage p2pMessage = new P2PMessage("..", MessageType.handshake_network, this.network);
        thread.write(p2pMessage);

        this.network.addEdge(this.network.getSelfNode().getMac(), mac);
    }

    public void removePeer(String mac) {
        this.peerThreads.remove(mac);
    }

    public boolean hasPeer(String mac) {
        return this.peerThreads.containsKey(mac);
    }

    public void sendMessageToPeers(P2PMessage p2pMessage) {
        for(P2PConnectedThread thread : this.peerThreads.values()) {
            thread.write(p2pMessage);
        }
    }

    public void sendGraphToPeers() {
        P2PMessage p2pGraph = new P2PMessage("..", MessageType.update_network_structure, this.network);
        for(P2PConnectedThread thread : this.peerThreads.values()) {
            thread.write(p2pGraph);
        }
    }

    public void closeAllConnections() {
        for(P2PConnectedThread thread : this.peerThreads.values()) {
            thread.close();
        }
    }

}