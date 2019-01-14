
package io.comrad.p2p.network;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Graph implements Serializable {
    private Node selfNode;
    private Set<Node> nodes;
    private Set<Edge> edges;

    public Graph(String selfMAC) {
        this(selfMAC, new HashSet<Node>(), new HashSet<Edge>());
    }

    public Graph(String selfMAC, Set<Node> nodes, Set<Edge> edges) {
        if(selfMAC == null) {
            throw new IllegalArgumentException("Mac was null");
        }

        this.nodes = nodes;
        this.edges = edges;

        this.selfNode = new Node(selfMAC);
        this.nodes.add(this.selfNode);
    }

    public boolean hasNode(String mac) {
        return this.nodes.contains(new Node(mac));
    }

    public Node getNode(String mac) {
        if (!hasNode(mac)) {
            throw new IllegalArgumentException("Node does not exist: " + mac);
        }

        return new Node(mac);
    }

    public void addEdge(String mac1, String mac2) {
        Node node1 = getNode(mac1);
        Node node2 = getNode(mac2);

        this.edges.add(new Edge(node1, node2));
    }

    public boolean hasEdge(String mac, String mac2) {
        Node node1 = getNode(mac);
        Node node2 = getNode(mac2);

        return edges.contains(new Edge(node1, node2));
    }

    public boolean removeEdge(String mac, String mac2) {
        Node node1 = getNode(mac);
        Node node2 = getNode(mac2);

        return edges.remove(new Edge(node1, node2));
    }

    public void createNode(String mac) {
        if(this.hasNode(mac)) {
            return;
        }

        Node node = new Node(mac);
        this.nodes.add(node);
    }

    public Node getSelfNode() {
        return this.selfNode;
    }

    public void apply(GraphUpdate update) {
        this.nodes.addAll(update.getAddedNodes());
        this.edges.addAll(update.getAddedEdges());
    }

   public GraphUpdate difference(Graph graph) {
        System.out.println("Calculating difference. Current: " + this.nodes + ", " + this.edges + ". Comparing: " + graph.nodes + ", " + graph.edges);

        Set<Node> nodes = new HashSet<>(this.nodes);
        Set<Edge> edges = new HashSet<>(this.edges);

        nodes.removeAll(graph.nodes);
        edges.removeAll(graph.edges);
        return new GraphUpdate(nodes, edges);
    }

    @Override
    public String toString() {
        return "Nodes: " + this.nodes + ", Edges: " + this.edges;
    }
}
