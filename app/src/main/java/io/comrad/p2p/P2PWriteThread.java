package io.comrad.p2p;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import io.comrad.p2p.messages.P2PMessage;
import io.comrad.p2p.messages.P2PMessageHandler;
import nl.erlkdev.adhocmonitor.AdhocMonitorService;

public class P2PWriteThread extends Thread {

    private final P2PMessageHandler handler;

    private final BluetoothSocket socket;
    private ObjectOutputStream output;
    private List<P2PMessage> messages = new ArrayList<>();

    public P2PWriteThread(BluetoothSocket socket, P2PMessageHandler handler) {
        this.handler = handler;
        this.socket = socket;

        OutputStream tmpOut = null;

        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            handler.sendToastToUI("Error occurred when creating output stream.");
            e.printStackTrace();
        }

        try {
            output = new ObjectOutputStream(tmpOut);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new P2PReadThread().start();
    }

    public void run() {
        if (output == null) {
            handler.sendToastToUI("Failed to setup proper connection.");
            this.close();
            return;
        }

        while (true) {
            try {
                if(messages.size() == 0) { continue; }

                P2PMessage message;
                synchronized (messages) {
                    message = messages.remove(0);
                }

                byte[] stream = message.toByteArray();
                System.out.println("Size of to send:"  + stream.length);
                output.writeObject(message);

                AdhocMonitorService monitor = handler.getNetwork().getMonitor();
                if(monitor != null) { monitor.getMonitorNode().addSendIO(this.socket.getRemoteDevice().getAddress(), stream.length); }

            } catch (IOException e) {
                handler.sendToastToUI("Error occurred when sending data.");
                e.printStackTrace();
                this.close();
            }
        }
    }

    // Call this from the activity_p2p activity to send data to the remote device.
    public void write(P2PMessage message) {
        synchronized (messages) {
            messages.add(message);
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

        handler.getNetwork().removePeer(this.socket.getRemoteDevice().getAddress());
    }

    public BluetoothDevice getRemoteDevice() {
        return this.socket.getRemoteDevice();
    }

    private class P2PReadThread extends Thread {
        private ObjectInputStream input;

        public P2PReadThread() {
            InputStream tmpIn = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                handler.sendToastToUI( "Error occurred when creating input stream.");
                e.printStackTrace();
            }

            try {
                input = new ObjectInputStream(tmpIn);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            if(output == null) {
                handler.sendToastToUI("Failed to setup proper connection.");
                close();
                return;
            }

            while (true) {
                try {
                    P2PMessage message = P2PMessage.readMessage(input);
                    message.handle(handler, socket.getRemoteDevice());
                } catch(IOException e) {
                    handler.sendToastToUI("Input stream was disconnected.");
                    e.printStackTrace();
                    close();
                    break;
                }
            }
        }
    }
}
