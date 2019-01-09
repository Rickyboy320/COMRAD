package io.comrad.p2p;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

public class P2PConnectThread {

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
            this.socket.connect();
        } catch(IOException e) {
            try {
                this.socket.close();
            } catch(IOException e1) {
                e1.printStackTrace();
            }
        }

        handleConnection();
    }

    private void handleConnection() {
        System.out.println(socket);
    }

    public void cancel() {
        try {
            this.socket.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
