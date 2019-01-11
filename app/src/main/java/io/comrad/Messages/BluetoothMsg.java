package io.comrad.Messages;

import android.bluetooth.BluetoothDevice;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import io.comrad.R;

public class BluetoothMsg {
    BluetoothDevice destination;
    ArrayList<BluetoothDevice> route;
    MessageType type;
    byte[] payload;

    // Use super?
    BluetoothMsg(BluetoothDevice destination, ArrayList<BluetoothDevice> route,
                 MessageType type) {
        this.destination = destination;
        this.route = route;
        this.type = type;
    }

    BluetoothMsg(BluetoothDevice destination, ArrayList<BluetoothDevice> route,
                 MessageType type, byte[] payload) {
        this.destination = destination;
        this.route = route;
        this.type = type;
        this.payload = payload;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
        objStream.writeObject(this);

        return byteStream.toByteArray();
    }

    public void createByteArrayFromAudioFile(String fileURI) throws IOException {
        try {
            FileOutputStream fileoutputstream = new FileOutputStream(fileURI);
            fileoutputstream.write(payload);
            fileoutputstream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void add_payload(Object message) {
        switch(this.type){
            case routing :
                break;
            case playlist:
                break;
            case song:
                String fileURI = (String) message;
                try {
                    createByteArrayFromAudioFile(fileURI);
                } catch (IOException ex) {
                    //raise error in UI
                }
                break;
            case connection_msg:
                break;
            case routing_callback:
                break;
        }
    }
}
