package io.comrad.p2p.network;

import java.io.Serializable;

public class Node implements Serializable {
    private String mac;

    Node(String mac) {
        this.mac = mac;
    }

    public String getMac() {
        return this.mac;
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof Node) {
            return this.mac.equalsIgnoreCase(((Node) object).getMac());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.mac.hashCode();
    }

    @Override
    public String toString() {
        return this.mac;
    }
}
