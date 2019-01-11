package io.comrad.p2p;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class P2PMessageHandler extends Handler {

    public static final int MESSAGE_TOAST = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;

    public static final String TOAST = "Toast";

    private final P2PActivity activity;

    public P2PMessageHandler(P2PActivity activity) {
        this.activity = activity;
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
                sendToast("Incoming: " + readMessage);

                break;
            case P2PMessageHandler.MESSAGE_TOAST:
                Toast.makeText(activity.getApplicationContext(), msg.getData().getString(P2PMessageHandler.TOAST), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void sendToast(String message) {
        Message toast = this.obtainMessage(P2PMessageHandler.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(P2PMessageHandler.TOAST, message);
        toast.setData(bundle);
        this.sendMessage(toast);
    }

    public void sendBuffer(byte[] buffer, int bufferSize) {
        Message readMsg = this.obtainMessage(P2PMessageHandler.MESSAGE_READ, bufferSize, -1, buffer);
        readMsg.sendToTarget();
    }

    public void addPeer(String mac, P2PConnectedThread thread) {
        this.activity.peers.put(mac, thread);
    }

    public void removePeer(String mac) {
        this.activity.peers.remove(mac);
    }

    public boolean hasPeer(String mac) {
        return this.activity.peers.containsKey(mac);
    }

    public void sendMessageToPeers(String message) {
        for(P2PConnectedThread thread : this.activity.peers.values()) {
            thread.write(message.getBytes());
        }
    }

}
