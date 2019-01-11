package io.comrad.p2p.messages;

import java.io.*;

public class P2PMessage {
    private String destinationMAC;
    private MessageType type;
    private Object payload;

    P2PMessage(String destinationMAC, MessageType type) {
        this(destinationMAC, type, null);
    }

    P2PMessage(String destinationMAC, MessageType type, Object payload) {
        this.destinationMAC = destinationMAC;
        this.type = type;
        addPayload(payload);
    }

    public String getDestinationMAC() {
        return this.destinationMAC;
    }

    public MessageType getType() {
        return this.type;
    }

    public byte[] toByteStream() throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byte[] result;
        try {
            ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
            objStream.writeObject(this);
            objStream.flush();
            result = byteStream.toByteArray();
        } finally {
            byteStream.close();
        }
        return result;
    }

    public void addPayload(Object payload) {
        try {
            switch (this.type) {
                case playlist:
                    break;
                case song:
                    String fileURI = (String) payload;
                    this.payload = readAudioFile(fileURI);
                    break;
                case update_network_structure:
                    this.payload = payload;
                    break;
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void handle(P2PMessageHandler handler) {
        System.out.println("Type: " + this.type);
        System.out.println("Message: " + this.payload);
    }

    private static Object readAudioFile(String fileURI) throws IOException {
        File file = new File(fileURI);
        FileInputStream fin = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        int bytesRead = fin.read(data);

        if(bytesRead != -1) {
            throw new IllegalStateException("Could not convert entire audio file to byte stream.");
        }

        fin.close();
        return data;
    }

    private static Object readObject(byte[] payload) throws IOException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(payload);
        Object result = null;
        try {
            ObjectInputStream objStream = new ObjectInputStream(byteStream);
            result = objStream.readObject();
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            byteStream.close();
        }

        return result;
    }

    public static P2PMessage readMessage(InputStream byteStream) throws IOException {
        byte[] buffer = new byte[4096];

        int readSize = byteStream.read(buffer);
        if(readSize != -1) {
            throw new IllegalStateException("Message was too large to read...");
        }

        Object object = readObject(buffer);
        if(!(object instanceof P2PMessage))
        {
            throw new IllegalArgumentException("Byte stream could not be converted to a message, but instead was: " + object);
        }

        return (P2PMessage) object;
    }
}
