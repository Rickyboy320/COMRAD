
package io.comrad.p2p.network;

import io.comrad.music.Song;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Graph implements Serializable {
    private Node selfNode;
    private Set<Node> nodes;
    private Set<Edge> edges;

    private transient Dijkstra dijkstra;

    public Graph(String selfMAC, List<Song> ownSongs) {
        this(selfMAC, new HashSet<Node>(), new HashSet<Edge>(), ownSongs);
    }

    public Graph(String selfMAC, Set<Node> nodes, Set<Edge> edges, List<Song> ownSongs) {
        if(selfMAC == null) {
            throw new IllegalArgumentException("Mac was null");
        }

        this.nodes = nodes;
        this.edges = edges;

        this.selfNode = new Node(selfMAC, ownSongs);
        this.nodes.add(this.selfNode);
    }

    public boolean hasNode(String mac) {
        return this.nodes.contains(new Node(mac));
    }

    public Node getNode(String mac) {
        for(Node node : this.nodes) {
            if(node.getMac().equalsIgnoreCase(mac))
            {
                return node;
            }
        }

        throw new IllegalArgumentException("Node does not exist: " + mac);
    }

    public Set<Node> getNodes() {
        return this.nodes;
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

    public Set<Node> getPeers(Node node) {
        Set<Node> result = new HashSet<>();
        for(Edge edge : this.edges)
        {
            if(edge.hasNode(node))
            {
                result.add(edge.getOther(node));
            }
        }

        return result;
    }

    public Node getSelfNode() {
        return this.selfNode;
    }

    public void setSelfNode(String destinationMAC) {
        this.nodes.remove(this.selfNode);
        this.selfNode = new Node(destinationMAC, this.selfNode.getPlaylist());
        this.nodes.add(this.selfNode);
    }

    public void replace(String replacent, String replacer) {
        if(!this.hasNode(replacent))
        {
            return;
        }

        Node node = this.getNode(replacent);
        this.nodes.remove(node);
        this.nodes.add(new Node(replacer, node.getPlaylist()));
    }

    public void apply(GraphUpdate update) {
        this.nodes.addAll(update.getAddedNodes());
        this.edges.addAll(update.getAddedEdges());
        this.edges.removeAll(update.getRemovedEdges());
        this.updateDijkstra();
    }

    public void updateDijkstra() {
        this.dijkstra = new Dijkstra(this);

        this.nodes.retainAll(dijkstra.getPaths().keySet());
        this.nodes.add(selfNode);

        Set<Edge> removeEdges = new HashSet<>();
        for(Edge edge : this.edges) {
            if(nodes.contains(edge.getNode1()) && nodes.contains(edge.getNode2())) {
                continue;
            }

            removeEdges.add(edge);
        }

        this.edges.removeAll(removeEdges);
    }

    private Dijkstra.Path getPath(Node target) {
        return this.dijkstra.getPaths().get(target);
    }

    public Node getNext(String macTarget) {
        Node target = getNode(macTarget);
        Dijkstra.Path path = getPath(target);
        if(path == null)
        {
            return null;
        }

        return path.getNextNode(this.selfNode);
    }

    public Node getNearestSong(Song song) {
        int smallestPath = Integer.MAX_VALUE;
        Node nearestNode = null;
        if(this.selfNode.getPlaylist().contains(song)) {
            return this.selfNode;
        }

        for(Node node : dijkstra.getPaths().keySet()) {
            int pathLength = dijkstra.getPaths().get(node).length();
            if(node.getPlaylist().contains(song) && pathLength < smallestPath) {
                smallestPath = pathLength;
                nearestNode = node;
            }
        }

        return nearestNode;
    }

    public GraphUpdate difference(Graph graph) {
        Set<Node> nodes = new HashSet<>(graph.nodes);
        Set<Edge> edges = new HashSet<>(graph.edges);

        nodes.removeAll(this.nodes);
        edges.removeAll(this.edges);
        return new GraphUpdate(nodes, edges, new HashSet<Edge>());
    }

    @Override
    public String toString() {
        return "Nodes: " + this.nodes + ", Edges: " + this.edges;
    }
}
