package io.comrad.p2p.messages;

public enum MessageType {
    handshake_network(MessagePriority.HIGH),
    update_network_structure(MessagePriority.MEDIUM),
    request_song(MessagePriority.LOW),
    send_song(MessagePriority.LOW),
    song_finished(MessagePriority.LOW);

    private MessagePriority priority;

    MessageType(MessagePriority priority) {
        this.priority = priority;
    }

    public MessagePriority getPriority() {
        return this.priority;
    }
}