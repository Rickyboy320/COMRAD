package io.comrad.p2p.network;

import java.io.Serializable;

public class Edge implements Serializable {
    private Node node1;
    private Node node2;

    public Edge(Node node1, Node node2) {
        if(node1 == null || node2 == null) {
            throw new IllegalArgumentException("Either node was null: node1: " + node1 + ", node2: " + node2);
        } else if(node1.equals(node2)) {
            throw new IllegalArgumentException("Nodes are equal, mac: " + node1.getMac());
        }

        this.node1 = node1;
        this.node2 = node2;
    }

    public boolean hasNode(Node node) {
        return this.node1.equals(node) || this.node2.equals(node);
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof Edge) {
            Edge edge = (Edge) object;
            return (this.node1.equals(edge.node1) && this.node2.equals(edge.node2)) || (this.node1.equals(edge.node2) && this.node2.equals(edge.node1));
        }

        return false;
    }
}
