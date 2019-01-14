package io.comrad.p2p;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import io.comrad.p2p.messages.P2PMessageHandler;

public class P2PConnectThread extends Thread {

    private static final Set<String> connecting = new HashSet<>();

    private final P2PMessageHandler handler;
    private final BluetoothDevice targetDevice;
    private final BluetoothSocket socket;

    public P2PConnectThread(BluetoothDevice targetDevice, P2PMessageHandler handler) {
        if(connecting.contains(targetDevice.getAddress()))
        {
            throw new IllegalStateException("Target Device is already connecting...");
        }

        connecting.add(targetDevice.getAddress());
        this.handler = handler;
        this.targetDevice = targetDevice;

        BluetoothSocket socket = null;
        try {
            socket = targetDevice.createRfcommSocketToServiceRecord(P2PActivity.SERVICE_UUID);
        } catch(IOException e) {
            handler.sendToastToUI("Could not create Rfcomm Socket.");
            e.printStackTrace();
        }

        this.socket = socket;
    }

    public void run() {
        try {
            this.socket.connect();
        } catch(IOException e) {
            try {
                this.socket.close();
            } catch(IOException e1) {
                e1.printStackTrace();
            }
            System.err.println("Failed to connect with: " + this.socket.getRemoteDevice().getAddress());
            e.printStackTrace();
            return;
        }

        handleConnection();
    }

    private void handleConnection() {
        if(this.handler.hasPeer(socket.getRemoteDevice().getAddress()))
        {
            this.close();
        }

        this.handler.sendToastToUI("Connected with: " + targetDevice.getAddress() + " : " + targetDevice.getName());

        P2PConnectedThread thread = new P2PConnectedThread(this.socket, this.handler);
        connecting.remove(socket.getRemoteDevice().getAddress());
        thread.start();

        this.handler.addPeer(socket.getRemoteDevice().getAddress(), thread);
    }

    public void close() {
        try {
            this.socket.close();
        } catch(IOException e) {
            this.handler.sendToastToUI("Failed to close client socket.");
            e.printStackTrace();
        }
    }

    public static boolean isConnecting(String mac) {
        return connecting.contains(mac);
    }
}
