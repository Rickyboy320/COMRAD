package io.comrad.p2p;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

import static android.content.ContentValues.TAG;

public class P2PServerThread extends Thread {
    private final BluetoothServerSocket mmServerSocket;

    public P2PServerThread(BluetoothAdapter adapter) {
        BluetoothServerSocket tmp = null;
        try {
            tmp = adapter.listenUsingRfcommWithServiceRecord(P2PActivity.SERVICE_NAME, P2PActivity.SERVICE_UUID);
            System.out.println("Setup server: " + tmp);
        } catch (IOException e) {
            System.out.println("Failed: " + e);
            Log.e(TAG, "Socket's listen() method failed", e);
        }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket;
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                Log.e(TAG, "Socket's accept() method failed", e);
                break;
            }

            if (socket != null) {
                handleConnection(socket);
                socket = null;
            }
        }
    }

    public void handleConnection(BluetoothSocket socket)
    {
        new P2PConnectedThread(socket).start();
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}