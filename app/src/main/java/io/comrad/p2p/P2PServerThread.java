package io.comrad.p2p;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class P2PServerThread extends Thread {
    private final P2PMessageHandler handler;
    private final BluetoothServerSocket serverSocket;

    public P2PServerThread(BluetoothAdapter adapter, P2PMessageHandler handler) {
        this.handler = handler;

        BluetoothServerSocket tmp = null;
        try {
            tmp = adapter.listenUsingRfcommWithServiceRecord(P2PActivity.SERVICE_NAME, P2PActivity.SERVICE_UUID);
            this.handler.sendToast("Started server... now listening for incoming connections.");
        } catch (IOException e) {
            this.handler.sendToast("Failed to listen using a Rfcomm Socket.");
            e.printStackTrace();
        }
        serverSocket = tmp;


        try {
            Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
            ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(adapter, null);

            if(uuids != null) {
                System.out.println("===================== Listening to UUIDs: " + Arrays.toString(uuids));
            } else{
                System.out.println("Uuids not found, be sure to enable Bluetooth!");
            }

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        BluetoothSocket socket;
        while (true) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                this.handler.sendToast("Failed to accept connection.");
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
        this.handler.sendToast("Connected with: " + socket.getRemoteDevice().getAddress() + " : " + socket.getRemoteDevice().getName());

        P2PConnectedThread thread = new P2PConnectedThread(socket, this.handler);
        this.handler.addPeer(socket.getRemoteDevice().getAddress(), thread);
        thread.start();
    }

    // Closes the connect socket and causes the thread to finish.
    public void close() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            this.handler.sendToast("Could not close the server socket.");
            e.printStackTrace();
        }
    }
}