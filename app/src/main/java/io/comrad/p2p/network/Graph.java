package io.comrad.p2p.network;

import java.util.HashSet;
import java.util.Set;

public class Graph {
    private Set<Node> nodes;

    public Graph() {
        this(new HashSet<Node>());
    }

    public Graph(Set<Node> nodes) {
        this.nodes = nodes;
    }

    public void merge(Graph graph) {
        for(Node node : graph.nodes) {
            this.addNode(node);
        }
    }

    public boolean hasNode(String mac) {
        return getNode(mac) != null;
    }

    public Node getNode(String mac) {
        for(Node node : nodes) {
            if(node.getMac().equalsIgnoreCase(mac)) {
                return node;
            }
        }
        return null;
    }

    public void addEdge(Node node1, Node node2) {
        this.nodes.add(node1);
        this.nodes.add(node2);

        node1.addPeer(node2);
    }

    public boolean hasEdge(String mac, String mac2) {
        Node node = getNode(mac);
        Node node1 = getNode(mac2);

        if(node == null || node1 == null) {
            return false;
        }

        return node.getPeers().contains(node1);
    }

    public void addNode(Node node) {
        this.nodes.add(node);
        this.nodes.addAll(node.getPeers());
    }

    public void removeNode(Node node) {
        this.nodes.remove(node);
        node.removeAllPeers();
    }
}
