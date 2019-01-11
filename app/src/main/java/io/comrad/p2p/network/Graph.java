package io.comrad.p2p.network;

import java.util.HashSet;
import java.util.Set;

public class Graph {
    private Node selfNode;
    private Set<Node> nodes;

    public Graph(String selfMAC) {
        this(selfMAC, new HashSet<Node>());
    }

    public Graph(String selfMAC, Set<Node> nodes) {
        this.nodes = nodes;
        this.selfNode = new Node(selfMAC);
        this.nodes.add(this.selfNode);
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

    public void addEdge(String mac1, String mac2) {
        Node node1 = getNode(mac1);
        Node node2 = getNode(mac2);

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

    private void addNode(Node node) {
        this.nodes.add(node);
        this.nodes.addAll(node.getPeers());
    }

    public void createNode(String mac) {
        Node node = new Node(mac);
        this.nodes.add(node);
    }

    public void removeNode(String mac) {
        Node node = this.getNode(mac);
        this.nodes.remove(node);
        node.removeAllPeers();
    }

    public Node getSelfNode() {
        return this.selfNode;
    }
}
