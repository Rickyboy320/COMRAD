package io.comrad.p2p;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

import io.comrad.p2p.messages.P2PMessageHandler;

public class P2PServerThread extends Thread {
    private final P2PMessageHandler handler;
    private final BluetoothServerSocket serverSocket;

    public P2PServerThread(BluetoothAdapter adapter, P2PMessageHandler handler) {
        this.handler = handler;

        BluetoothServerSocket tmp = null;
        try {
            tmp = adapter.listenUsingRfcommWithServiceRecord(P2PActivity.SERVICE_NAME, P2PActivity.SERVICE_UUID);
            this.handler.sendToastToUI("Started server... now listening for incoming connections.");
        } catch (IOException e) {
            this.handler.sendToastToUI("Failed to listen using a Rfcomm Socket.");
            e.printStackTrace();
        }
        serverSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket;
        while (true) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                this.handler.sendToastToUI("Failed to accept connection.");
                e.printStackTrace();
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
        if(this.handler.hasPeer(socket.getRemoteDevice().getAddress()))
        {
            this.close();
        }

        this.handler.sendToastToUI("Connected with: " + socket.getRemoteDevice().getAddress() + " : " + socket.getRemoteDevice().getName());

        P2PConnectedThread thread = new P2PConnectedThread(socket, this.handler);
        thread.start();
        this.handler.addPeer(socket.getRemoteDevice().getAddress(), thread);
    }

    // Closes the connect socket and causes the thread to finish.
    public void close() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            this.handler.sendToastToUI("Could not close the server socket.");
            e.printStackTrace();
        }
    }
}