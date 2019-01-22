package io.comrad.p2p.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.comrad.music.Song;
import io.comrad.p2p.P2PActivity;
import io.comrad.p2p.P2PConnectedThread;
import io.comrad.p2p.messages.MessageType;
import io.comrad.p2p.messages.P2PMessage;
import io.comrad.p2p.messages.P2PMessageHandler;

public class P2PNetworkHandler {
    private static int COUNTER = 0;

    private final P2PActivity activity;
    private final Graph network;

    private final Map<String, P2PConnectedThread> peerThreads = new ConcurrentHashMap<>();
    public final Map<String, Set<Integer>> counters = new ConcurrentHashMap<>();

    public P2PNetworkHandler(P2PActivity activity, List<Song> ownSongs, P2PMessageHandler handler) {
        this.activity = activity;
        this.network = new Graph(P2PActivity.getBluetoothMac(activity.getApplicationContext()), ownSongs, handler);
    }

    public void addPeer(String mac, P2PConnectedThread thread) {
        this.peerThreads.put(mac, thread);

        System.out.println("Sending network: " + this.network);
        synchronized (this.network) {
            P2PMessage p2pMessage = new P2PMessage(this.getSelfMac(), thread.getRemoteDevice().getAddress(), MessageType.handshake_network, this.network);
            thread.write(p2pMessage);
        }
    }

    public void removePeer(String mac) {
        this.peerThreads.remove(mac);

        GraphUpdate graphUpdate = new GraphUpdate();
        graphUpdate.removeEdge(this.getSelfMac(), mac);
        P2PMessage message = new P2PMessage(this.getSelfMac(), this.getBroadcastAddress(), MessageType.update_network_structure, graphUpdate);
        this.broadcast(message);

        synchronized (this.network) {
            this.network.apply(graphUpdate);
        }
    }

    public ArrayList<Song> getOwnPlayList() {
        return activity.getOwnPlayList();
    }

    public boolean hasPeer(String mac) {
        return this.peerThreads.containsKey(mac);
    }

    public void sendMessageToPeers(P2PMessage p2pMessage) {
        for(P2PConnectedThread thread : this.peerThreads.values()) {
            thread.write(p2pMessage);
        }
    }

    public void forwardMessage(P2PMessage p2pMessage) {
        synchronized (this.network) {
            Node closestMac = this.network.getNext(p2pMessage.getDestinationMAC());

            if (closestMac == null) {
                System.out.println("Next node was null");
            } else {
                this.peerThreads.get(closestMac.getMac()).write(p2pMessage);
            }
        }
    }

    public void closeAllConnections() {
        for(P2PConnectedThread thread : this.peerThreads.values()) {
            thread.close();
        }
    }

    public void broadcast(P2PMessage message) {
        for(P2PConnectedThread thread : this.peerThreads.values()) {
            thread.write(message);
        }
    }

    public byte[] getByteArrayFromSong(Song song) {
        return activity.getByteArrayFromSong(song);
    }

    public void broadcastExcluding(P2PMessage message, String address) {
        for(P2PConnectedThread thread : this.peerThreads.values()) {
            if(!thread.getRemoteDevice().getAddress().equalsIgnoreCase(address))
            {
                thread.write(message);
            }
        }
    }

    public String getSelfMac() {
        synchronized (network) {
            return this.network.getSelfNode().getMac();
        }
    }

    public Graph getGraph() {
        return this.network;
    }

    public synchronized String getBroadcastAddress() {
        COUNTER++;
        return "b:" + COUNTER;
    }
}
