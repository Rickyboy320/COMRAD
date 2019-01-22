package io.comrad.p2p.network;

import java.util.*;

public class Dijkstra {
    private Map<Node, Path> paths = new HashMap<>();

    public Dijkstra(Graph graph) {
        Map<Node, Integer> distances = new HashMap<>();

        Set<Node> settled = new HashSet<>();
        Set<Node> unsettled = new HashSet<>();

        unsettled.add(graph.getSelfNode());

        while(unsettled.size() != 0) {
            Node currentNode = getSmallestDistance(unsettled, distances);
            unsettled.remove(currentNode);

            for(Node node : graph.getPeers(currentNode)) {
                if(!settled.contains(node)) {
                    int sourceDistance = getOrDefault(distances, currentNode, 0);
                    if (sourceDistance + 1 < getOrDefault(distances, node, Integer.MAX_VALUE)) {
                        distances.put(node, sourceDistance + 1);

                        Path path = getOrDefault(paths, currentNode, new Path());
                        path = new Path(path);
                        path.addNode(currentNode);
                        paths.put(node, path);
                        unsettled.add(node);
                    }
                }
            }

            settled.add(currentNode);
        }

        for (Node node : paths.keySet()) {
            paths.get(node).addNode(node);
        }
    }

    public Map<Node, Path> getPaths() {
        return this.paths;
    }

    private static <T, U> U getOrDefault(Map<T, U> map, T key, U def) {
        return map.get(key) != null ? map.get(key) : def;
    }

    private static Node getSmallestDistance(Set<Node> nodes, Map<Node, Integer> distances) {
        int smallestDistance = Integer.MAX_VALUE;
        Node smallestNode = null;
        for(Node node : nodes) {
            int distance = getOrDefault(distances, node, 0);
            if(distance < smallestDistance) {
                smallestDistance = distance;
                smallestNode = node;
            }
        }

        return smallestNode;
    }

    public class Path {
        private LinkedList<Node> path = new LinkedList<>();

        public Path() {}

        public Path(Path path) {
            this.path = new LinkedList<>(path.path);
        }

        public void addNode(Node node) {
            this.path.add(node);
        }

        public Node getNextNode(Node node) {
            int index = path.indexOf(node);
            if(index + 1 >= path.size())
            {
                return null;
            }

            return path.get(index + 1);
        }

        public void invert() {
            LinkedList<Node> newPath = new LinkedList<>();
            for(Node node : this.path) {
                newPath.add(0, node);
            }

            this.path = newPath;
        }

        @Override
        public String toString() {
            return this.path.toString();
        }

        public int length() {
            return this.path.size();
        }
    }
}
