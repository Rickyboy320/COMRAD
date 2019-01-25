package io.comrad.p2p.network;

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
import nl.erlkdev.adhocmonitor.AdhocMonitorService;
import nl.erlkdev.adhocmonitor.NodeStatus;

public class P2PNetworkHandler {
    private static int COUNTER = 0;

    private final P2PActivity activity;
    private final Graph graph;

    private final Map<String, P2PConnectedThread> peerThreads = new ConcurrentHashMap<>();
    public final Map<String, Set<Integer>> counters = new ConcurrentHashMap<>();

    private AdhocMonitorService monitor;

    public P2PNetworkHandler(P2PActivity activity, List<Song> ownSongs, P2PMessageHandler handler) {
        this.activity = activity;
        this.graph = new Graph(P2PActivity.getBluetoothMac(activity.getApplicationContext()), ownSongs, handler);
    }

    public void addPeer(String mac, P2PConnectedThread thread) {
        this.peerThreads.put(mac, thread);

        if (this.monitor != null) {
            this.monitor.getMonitorNode().setCurrentNeighbours(this.peerThreads.keySet().toArray(new String[0]));
        }

        synchronized (this.graph) {
            P2PMessage p2pMessage = new P2PMessage(this.getSelfMac(), thread.getRemoteDevice().getAddress(), MessageType.handshake_network, this.graph);
            thread.write(p2pMessage);
        }
    }

    public void removePeer(String mac) {
        this.peerThreads.remove(mac);

        if (this.monitor != null) {
            this.monitor.getMonitorNode().setCurrentNeighbours(this.peerThreads.keySet().toArray(new String[0]));
        }

        GraphUpdate graphUpdate = new GraphUpdate();
        graphUpdate.removeEdge(this.getSelfMac(), mac);
        P2PMessage message = new P2PMessage(this.getSelfMac(), this.getBroadcastAddress(), MessageType.update_network_structure, graphUpdate);
        this.broadcast(message);

        synchronized (this.graph) {
            this.graph.apply(graphUpdate);
        }
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
        synchronized (this.graph) {
            Node closestNode = this.graph.getNext(p2pMessage.getDestinationMAC());

            if (closestNode == null || !this.peerThreads.containsKey(closestNode.getMac())) {
                //TODO: Handle special case where there's no more path.
                System.out.println("Next node was null, could not continue sending.");
            } else {
                this.peerThreads.get(closestNode.getMac()).write(p2pMessage);
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

    public void broadcastExcluding(P2PMessage message, String address) {
        for(P2PConnectedThread thread : this.peerThreads.values()) {
            if(!thread.getRemoteDevice().getAddress().equalsIgnoreCase(address))
            {
                thread.write(message);
            }
        }
    }

    public String getSelfMac() {
        synchronized (graph) {
            return this.graph.getSelfNode().getMac();
        }
    }

    public Graph getGraph() {
        return this.graph;
    }

    public AdhocMonitorService getMonitor()
    {
        return this.monitor;
    }

    public synchronized String getBroadcastAddress() {
        COUNTER++;
        return "b:" + COUNTER;
    }

    public void setMonitor(AdhocMonitorService monitor) {
        monitor.getMonitorNode().setCurrentNeighbours(this.peerThreads.keySet().toArray(new String[0]));
        monitor.getMonitorNode().setNodeStatus(NodeStatus.IDLE);
        this.monitor = monitor;
    }
}
