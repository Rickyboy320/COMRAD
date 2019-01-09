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
        System.out.println("Starting to run server");
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                socket = mmServerSocket.accept();
                System.out.println("Accepted socket: " + socket);
            } catch (IOException e) {
                Log.e(TAG, "Socket's accept() method failed", e);
                break;
            }

            if (socket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
                System.out.println("Added socket: " + socket);
                addSocket(socket);
                socket = null;
            }
        }
    }

    public void addSocket(BluetoothSocket socket)
    {
        System.out.println("HI");
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