package io.comrad.p2p.network;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class Node {
    private Collection<Node> peers;
    private String mac;

    Node(String mac) {
        this.peers = new HashSet<>();
        this.mac = mac;
    }

    public String getMac() {
        return this.mac;
    }

    public Collection<Node> getPeers() {
        return Collections.unmodifiableCollection(this.peers);
    }

    void removePeer(Node peer) {
        this.peers.remove(peer);
        peer.peers.remove(this);
    }

    void addPeer(Node peer) {
        this.peers.add(peer);
        peer.peers.add(this);
    }

    void removeAllPeers() {
        for(Node peer : this.getPeers()) {
            this.removePeer(peer);
        }
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof Node) {
            return this.mac.equalsIgnoreCase(((Node) object).getMac());
        }

        return false;
    }
}
