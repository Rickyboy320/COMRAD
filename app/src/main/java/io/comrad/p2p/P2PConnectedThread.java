package io.comrad.p2p;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import io.comrad.p2p.messages.P2PMessage;
import io.comrad.p2p.messages.P2PMessageHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import static android.content.ContentValues.TAG;

public class P2PConnectedThread extends Thread {

    private final P2PMessageHandler handler;

    private final BluetoothSocket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    public P2PConnectedThread(BluetoothSocket socket, P2PMessageHandler handler) {
        this.handler = handler;
        this.socket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        input = null;
        output = null;

        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            handler.sendToastToUI( "Error occurred when creating input stream.");
            e.printStackTrace();
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            handler.sendToastToUI("Error occurred when creating output stream.");
            e.printStackTrace();
        }

        try {
            output = new ObjectOutputStream(tmpOut);
            output.flush();
            input = new ObjectInputStream(tmpIn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
                P2PMessage message = P2PMessage.readMessage(input);
                message.handle(this.handler);
            } catch(IOException e) {
                handler.sendToastToUI("Input stream was disconnected.");
                e.printStackTrace();
                break;
            }
        }
    }

    // Call this from the activity_p2p activity to send data to the remote device.
    public void write(P2PMessage message) {
        try {
            byte[] stream = message.toByteArray();
            System.out.println("Size of to send message: " + stream.length);
            output.writeObject(message);
        } catch (IOException e) {
            handler.sendToastToUI("Error occurred when sending data.");
            e.printStackTrace();
        }
    }

    // Call this method from the activity_p2p activity to shut down the connection.
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            handler.sendToastToUI("Could not close the connect socket.");
            e.printStackTrace();
        }
    }

    public BluetoothDevice getRemoteDevice() {
        return this.socket.getRemoteDevice();
    }
}