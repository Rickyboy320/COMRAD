package io.comrad.p2p;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

public class P2PConnectThread extends Thread {

    private final BluetoothDevice targetDevice;
    private final BluetoothSocket socket;

    public P2PConnectThread(BluetoothDevice targetDevice) {
        BluetoothSocket socket = null;
        this.targetDevice = targetDevice;
        try {
            socket = targetDevice.createRfcommSocketToServiceRecord(P2PActivity.SERVICE_UUID);
        } catch(IOException e) {
            e.printStackTrace();
            //TODO: Add toast
        }

        this.socket = socket;
    }

    public void run() {
        // TODO: Potentially disable discoverability;

        try {
            System.out.println("Attempting connection socket: " + socket);
            this.socket.connect();
            System.out.println("Successfully connected with: " + targetDevice.getAddress() + " : " + targetDevice.getName());
        } catch(IOException e) {
            System.out.println("Failed conneciton: " + e);
            try {
                this.socket.close();
            } catch(IOException e1) {
                e1.printStackTrace();
            }
            return;
        }

        System.out.println("Complete!");
        handleConnection();
    }

    private void handleConnection() {
        new P2PConnectedThread(this.socket).start();
    }

    public void cancel() {
        try {
            this.socket.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
