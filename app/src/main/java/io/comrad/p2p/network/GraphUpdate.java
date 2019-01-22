package io.comrad.p2p.network;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class GraphUpdate implements Serializable {
    Set<Node> addedNodes;
    Set<Edge> addedEdges;
    Set<Edge> removedEdges;

    public GraphUpdate() {
        this(new HashSet<Node>(), new HashSet<Edge>(), new HashSet<Edge>());
    }

    public GraphUpdate(Set<Node> addedNodes, Set<Edge> addedEdges, Set<Edge> removedEdges) {
        this.addedNodes = addedNodes;
        this.addedEdges = addedEdges;
        this.removedEdges = removedEdges;
    }

    public void addEdge(String mac1, String mac2) {
        Node node1 = new Node(mac1);
        Node node2 = new Node(mac2);

        this.addedEdges.add(new Edge(node1, node2));
    }

    public void addNode(String mac1) {
        this.addedNodes.add(new Node(mac1));
    }

    public void removeEdge(String mac1, String mac2) {
        Node node1 = new Node(mac1);
        Node node2 = new Node(mac2);

        this.removedEdges.add(new Edge(node1, node2));
    }

    public Set<Edge> getAddedEdges() {
        return this.addedEdges;
    }

    public Set<Node> getAddedNodes() {
        return this.addedNodes;
    }

    public Set<Edge> getRemovedEdges() {
        return this.removedEdges;
    }

    @Override
    public String toString() {
        return "Nodes to be added: " + this.addedNodes + ", Edges to be added: " + this.addedEdges + ", Edges to be removed: " + this.removedEdges;
    }
}
