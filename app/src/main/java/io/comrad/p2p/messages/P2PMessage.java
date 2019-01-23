package io.comrad.p2p.messages;

import io.comrad.music.SongPacket;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.comrad.music.Song;
import io.comrad.p2p.network.Graph;
import io.comrad.p2p.network.GraphUpdate;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

import static android.content.ContentValues.TAG;
import static io.comrad.music.SongPacket.SONG_PACKET_SIZE;

public class P2PMessage implements Serializable {
    private String sourceMac;
    private String destinationMAC;
    private MessageType type;
    private Serializable payload;

    public P2PMessage(String sourceMac, String destinationMAC, MessageType type, Serializable payload) {
        this.sourceMac = sourceMac;
        this.destinationMAC = destinationMAC;
        this.type = type;
        this.payload = payload;
    }

    public String getSourceMac() {
        return this.sourceMac;
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

    public void handle(P2PMessageHandler handler, BluetoothDevice sender) {
        /* If starts with b:, it's a broadcast. */
        if(this.destinationMAC != null && this.destinationMAC.startsWith("b:")) {
            String mac = this.sourceMac;
            System.out.println(mac);

            if(mac.equalsIgnoreCase(handler.getNetwork().getSelfMac())) {
                return;
            }

            int count = Integer.parseInt(this.destinationMAC.substring(2));
            System.out.println(count);

            Set<Integer> knownCounts = handler.getNetwork().counters.get(mac);
            if (knownCounts == null) {
                knownCounts = new HashSet<>();
                handler.getNetwork().counters.put(mac, knownCounts);
            }
            if (knownCounts.contains(count)) {
                System.out.println(count);
                return;
            }
            knownCounts.add(count);
            handler.getNetwork().broadcastExcluding(this, sender.getAddress());
        }

        System.out.println("Source Mac: " + this.sourceMac);
        System.out.println("Gotten from-Mac: " + sender.getAddress());
        System.out.println("Desitnation mac: " + this.getDestinationMAC());
        System.out.println("Type: " + this.type);
        System.out.println("Message: " + this.payload);

        if (this.type == MessageType.handshake_network) {
            Graph graph = (Graph) this.payload;
            graph.replace("02:00:00:00:00:00", this.sourceMac);

            synchronized (handler.getNetwork().getGraph()) {
                if(handler.getNetwork().getSelfMac().equalsIgnoreCase("02:00:00:00:00:00"))
                {
                    handler.getNetwork().getGraph().setSelfNode(this.getDestinationMAC());
                    handler.reattachMonitor();
                }

                GraphUpdate update = handler.getNetwork().getGraph().difference(graph);
                update.addEdge(handler.getNetwork().getSelfMac(), sender.getAddress());

                handler.getNetwork().getGraph().apply(update);
                P2PMessage message = new P2PMessage(handler.getNetwork().getSelfMac(), handler.getNetwork().getBroadcastAddress(), MessageType.update_network_structure, update);
                handler.getNetwork().broadcastExcluding(message, sender.getAddress());
            }
        } else if(this.type == MessageType.update_network_structure) {
            GraphUpdate update = (GraphUpdate) this.payload;
            synchronized (handler.getNetwork().getGraph()) {
                handler.getNetwork().getGraph().apply(update);
            }
        } else if (this.type == MessageType.song_size) {
            handler.sendSongSizeToActivity((int) this.payload);
        } else if (this.type == MessageType.request_song) {
            if (this.getDestinationMAC().equalsIgnoreCase(handler.getNetwork().getSelfMac())) {
                handler.sendToastToUI("We received a request from " + this.sourceMac);

                /* Send buffer size to receiving node. */
                P2PMessage msg = new P2PMessage(handler.getNetwork().getSelfMac(), this.sourceMac,
                        MessageType.song_size, ((Song) this.payload).getSongSize());
                handler.getNetwork().forwardMessage(msg);

                /* Setup payload buffers for sending the song. */
                byte[] payload = handler.getNetwork().getByteArrayFromSong((Song) this.payload);
                byte[] tmpPayload;
                SongPacket songPacket;

                /* Send songs in bursts to the receiver. */
                for (int i = 0; i < payload.length; i += SONG_PACKET_SIZE) {
                    if (i == payload.length / SONG_PACKET_SIZE) {
                        tmpPayload = Arrays.copyOfRange(payload, i, payload.length);
                    } else {
                        tmpPayload = Arrays.copyOfRange(payload, i, i + SONG_PACKET_SIZE);
                    }

                    songPacket = new SongPacket(i, tmpPayload);
                    msg = new P2PMessage(handler.getNetwork().getSelfMac(), this.sourceMac,
                                                        MessageType.send_song, songPacket);
                    handler.getNetwork().forwardMessage(msg);
                }

                msg = new P2PMessage(handler.getNetwork().getSelfMac(), this.sourceMac,
                        MessageType.song_finished, null);
                handler.getNetwork().forwardMessage(msg);
            } else {
                handler.getNetwork().forwardMessage(this);
            }
        } else if (this.type == MessageType.send_song) {
            if (this.getDestinationMAC().equalsIgnoreCase(handler.getNetwork().getSelfMac())) {
                handler.sendToastToUI("We received a song from " + this.sourceMac);
                SongPacket songPacket = (SongPacket) this.payload;
                handler.sendSongToActivity(songPacket.get_id(), songPacket.get_data());
            } else {
                handler.getNetwork().forwardMessage(this);
            }
        } else if (this.type == MessageType.song_finished) {
            handler.sendSongFinshed();
        }
    }

    public static P2PMessage readMessage(ObjectInputStream byteStream) throws IOException {
        P2PMessage msg = null;

        try {
            msg = (P2PMessage) byteStream.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //TODO: IOException on closing of stream during reading.

        if(msg == null) {
            throw new IllegalArgumentException("Byte stream could not be converted to a message, but instead was: null");
        }

        return msg;
    }
}
