package io.comrad.p2p.messages;

import android.bluetooth.BluetoothDevice;
import io.comrad.p2p.network.Graph;
import io.comrad.p2p.network.GraphUpdate;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class P2PMessage implements Serializable {
    private String destinationMAC;
    private MessageType type;
    private Serializable payload;

    public P2PMessage(String destinationMAC, MessageType type) {
        this(destinationMAC, type, null);
    }

    public P2PMessage(String destinationMAC, MessageType type, Serializable payload) {
        this.destinationMAC = destinationMAC;
        this.type = type;
        addPayload(payload);
    }

    public String getDestinationMAC() {
        return this.destinationMAC;
    }

    public MessageType getType() {
        return this.type;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byte[] result;
        try {
            ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
            objStream.writeObject(this);
            objStream.flush();
            result = byteStream.toByteArray();
        } finally {
            byteStream.close();
        }
        return result;
    }

    public void addPayload(Serializable payload) {
        try {
            switch (this.type) {
                case playlist:
                    break;
                case song:
                    String fileURI = (String) payload;
                    this.payload = readAudioFile(fileURI);
                    break;
                case update_network_structure:
                case handshake_network:
                case broadcast_message:
                    this.payload = payload;
                    break;
                default:
                    throw new IllegalStateException("Payload case was not handled");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void handle(P2PMessageHandler handler, BluetoothDevice sender) {
        System.out.println("Source Mac: " + sender.getAddress());
        System.out.println("Type: " + this.type);
        System.out.println("Message: " + this.payload);

        if (this.type == MessageType.handshake_network) {
            Graph graph = (Graph) this.payload;
            handler.network.createNode(sender.getAddress());
            handler.network.addEdge(handler.network.getSelfNode().getMac(), sender.getAddress());

            GraphUpdate update = graph.difference(handler.network);

            handler.network.apply(update);
            System.out.println(update);

            // Send update to all but source.
            P2PMessage message = new P2PMessage(handler.getBroadcastAddress(), MessageType.update_network_structure, update);
            handler.broadcastExcluding(message, sender.getAddress());
        } else if(this.type == MessageType.update_network_structure) {
            GraphUpdate update = (GraphUpdate) this.payload;
            handler.network.apply(update);
        }

        /* If starts with b:, it's a broadcast. */
        if(this.destinationMAC != null && this.destinationMAC.startsWith("b:")) {
            handler.sendToastToUI("We received a broadcast from " + sender.getAddress());

            System.out.println("Incoming broadcast: " + this.destinationMAC);
            String mac = this.destinationMAC.substring(2, 19);
            System.out.println(mac);

            int count = Integer.parseInt(this.destinationMAC.substring(20));
            System.out.println(count);

            synchronized (handler.counters) {
                Set<Integer> knownCounts = handler.counters.get(mac);
                if (knownCounts == null) {
                    knownCounts = new HashSet<>();
                    handler.counters.put(mac, knownCounts);
                }
                if (!knownCounts.contains(count)) {
                    knownCounts.add(count);
                    handler.broadcastExcluding(this, sender.getAddress());
                }
            }
        }
    }

    private static Serializable readAudioFile(String fileURI) throws IOException {
        File file = new File(fileURI);
        FileInputStream fin = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        int bytesRead = fin.read(data);

        if(bytesRead != -1) {
            throw new IllegalStateException("Could not convert entire audio file to byte stream.");
        }

        fin.close();
        return data;
    }

    private static Object readObject(byte[] payload) throws IOException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(payload);
        Object result = null;
        try {
            ObjectInputStream objStream = new ObjectInputStream(byteStream);
            result = objStream.readObject();
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            byteStream.close();
        }

        return result;
    }

    public static P2PMessage readMessage(ObjectInputStream byteStream) throws IOException {
        P2PMessage msg = null;

        try {
            msg = (P2PMessage) byteStream.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(msg == null) {
            throw new IllegalArgumentException("Byte stream could not be converted to a message, but instead was: null");
        }

        return msg;
    }
}
