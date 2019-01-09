package io.comrad.p2p;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

public class P2PConnectThread extends Thread {

    private final P2PMessageHandler handler;
    private final BluetoothDevice targetDevice;
    private final BluetoothSocket socket;

    public P2PConnectThread(BluetoothDevice targetDevice, P2PMessageHandler handler) {
        this.handler = handler;
        this.targetDevice = targetDevice;

        BluetoothSocket socket = null;
        try {
            socket = targetDevice.createRfcommSocketToServiceRecord(P2PActivity.SERVICE_UUID);
        } catch(IOException e) {
            handler.sendToast("Could not create Rfcomm Socket.");
            e.printStackTrace();
        }

        this.socket = socket;
    }

    public void run() {
        // TODO: Potentially disable discoverability;

        try {
            this.socket.connect();
        } catch(IOException e) {
            try {
                this.socket.close();
            } catch(IOException e1) {
                e1.printStackTrace();
            }
            return;
        }

        this.handler.sendToast("Connected with: " + targetDevice.getAddress() + " : " + targetDevice.getName());
        handleConnection();
    }

    private void handleConnection() {
        new P2PConnectedThread(this.socket, this.handler).start();
    }

    public void cancel() {
        try {
            this.socket.close();
        } catch(IOException e) {
            this.handler.sendToast("Failed to close client socket.");
            e.printStackTrace();
        }
    }
}
