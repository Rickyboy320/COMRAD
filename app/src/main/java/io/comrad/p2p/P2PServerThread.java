package io.comrad;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

import io.comrad.p2p.P2PActivity;

import static android.content.ContentValues.TAG;

public class P2PServerThread extends Thread {
    private final BluetoothServerSocket mmServerSocket;

    public P2PServerThread(BluetoothAdapter adapter) {
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code.
            tmp = adapter.listenUsingRfcommWithServiceRecord(P2PActivity.SERVICE_NAME, P2PActivity.SERVICE_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's listen() method failed", e);
        }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                Log.e(TAG, "Socket's accept() method failed", e);
                break;
            }

            if (socket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
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