package io.comrad.p2p;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.comrad.R;
import io.comrad.music.MusicActivity;
import io.comrad.music.PlayMusic;
import io.comrad.music.Song;
import io.comrad.p2p.messages.MessageType;
import io.comrad.p2p.messages.P2PMessage;
import io.comrad.p2p.messages.P2PMessageHandler;
import io.comrad.p2p.network.Node;

import static android.bluetooth.BluetoothAdapter.SCAN_MODE_CONNECTABLE;
import static android.bluetooth.BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE;
import static android.bluetooth.BluetoothAdapter.SCAN_MODE_NONE;

public class P2PActivity extends FragmentActivity  {
    public final static String SERVICE_NAME = "COMRAD";
    public final static UUID SERVICE_UUID = UUID.fromString("7337958a-460f-4b0c-942e-5fa111fb2bee");

    private final static int REQUEST_ENABLE_BT = 1;
    private final static int REQUEST_DISCOVER = 2;
    private final static int PERMISSION_REQUEST = 3;
    static final int REQUEST_MUSIC_FILE = 4;
    private static final int MY_PERMISSION_REQUEST = 5;

    private P2PServerThread serverThread;

    private P2PReceiver receiver;

    private ArrayList<Song> ownSongs = new ArrayList<>();


    private final P2PMessageHandler handler = new P2PMessageHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p2p);

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }
        } else {
            getMusic();
        }

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST);


        addComponents();
    }

    public void sendByteArrayToPlayMusic(byte[] songBytes) {
        PlayMusic fragment = (PlayMusic) getSupportFragmentManager().findFragmentById(R.id.PlayMusic);
        fragment.addSongBytes(songBytes);
    }

    private void enableBluetoothServices() {
        this.handler.onBluetoothEnable(ownSongs);

        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final List<String> peerList = new ArrayList<>();
        RecyclerView peerView = findViewById(R.id.peersList);

        LinearLayoutManager mng = new LinearLayoutManager(this);
        peerView.setLayoutManager(mng);

        final PeerAdapter peerAdapter = new PeerAdapter(peerList);
        peerView.setAdapter(peerAdapter);

        Button discoverButton = findViewById(R.id.discover);
        discoverButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Discovering devices...", Toast.LENGTH_LONG).show();

                connectToBondedDevices(bluetoothAdapter, handler);

                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }
                peerList.clear();
                peerAdapter.notifyDataSetChanged();
                bluetoothAdapter.startDiscovery();
            }
        });




        receiver = new P2PReceiver(peerAdapter, handler);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_UUID);
        registerReceiver(receiver, filter);

        this.serverThread = new P2PServerThread(BluetoothAdapter.getDefaultAdapter(), this.handler);
        this.serverThread.start();

        connectToBondedDevices(bluetoothAdapter, handler);
    }

    /*
     * Retrieves all music on the device and adds them to the class variable arrayList.
     */
    public void getMusic() {
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null, null, null, null);

        if (songCursor != null && songCursor.moveToFirst()) {
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songLocation = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int songSize = songCursor.getColumnIndex(MediaStore.Audio.Media.SIZE);
            String currentTitle;
            String currentArtist;
            String currentLocation;
            int currentSize;

            // Loop over the found songs and add them to the arraylist
            do {
                currentTitle = songCursor.getString(songTitle);
                currentArtist = songCursor.getString(songArtist);
                currentLocation = songCursor.getString(songLocation);
                currentSize = songCursor.getInt(songSize);
                ownSongs.add(new Song(currentTitle, currentArtist, currentLocation, currentSize));
            } while (songCursor.moveToNext());
        }
    }

    public ArrayList<Song> getOwnPlayList() {
        return ownSongs;
    }

    private void addComponents() {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "This device does not support bluetooth.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Button serverButton = findViewById(R.id.server);
        serverButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
                startActivityForResult(discoverableIntent, REQUEST_DISCOVER);
            }
        });

        Button stopDiscovery = findViewById(R.id.stopDiscovery);
        stopDiscovery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Stopped discovering.", Toast.LENGTH_LONG).show();
                bluetoothAdapter.cancelDiscovery();
            }
        });

        Button sendMessage = findViewById(R.id.sendMessage);
        sendMessage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                handler.sendMessageToPeers(new P2PMessage(handler.network.getSelfNode().getMac(), handler.getBroadcastAddress(), MessageType.broadcast_message, "Hello world!"));
            }
        });

        Button chooseMusic = findViewById(R.id.chooseMusic);
        chooseMusic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MusicActivity.class);
                ArrayList<Song> arrayList = new ArrayList<>();
                for (Node node : handler.network.getNodes()) {
                    if (node.getPlaylist() != null) {
                        arrayList.addAll(node.getPlaylist());
                    }
                }
                System.out.println("<<<<" + arrayList.toString());
                intent.putExtra("Nodes", (Serializable) arrayList);
                startActivityForResult(intent, REQUEST_MUSIC_FILE);
            }
        });

        Button showGraph = findViewById(R.id.showGraph);
        showGraph.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println(handler.network);
            }
        });

        Button send5mini = findViewById(R.id.send5smini);
        send5mini.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!(handler.network.getSelfNode().getMac() == "6C:2F:2C:82:67:11")) {
                    P2PMessage message = new P2PMessage(handler.network.getSelfNode().getMac(), "6C:2F:2C:82:67:11", MessageType.send_message, "I am a song :D");
                    handler.forwardMessage(message);
                }
            }
        });

        if (bluetoothAdapter.isEnabled()) {
            enableBluetoothServices();
        }
    }


    private void connectToBondedDevices(BluetoothAdapter bluetoothAdapter, P2PMessageHandler handler) {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        System.out.println("Paired devices:");
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                System.out.println(device.getAddress() + " : " + Arrays.toString(device.getUuids()));
                if (this.handler.hasPeer(device.getAddress()) || P2PConnectThread.isConnecting(device.getAddress())) {
                    continue;
                }

                if (device.getUuids() != null) {
                    for (ParcelUuid uuid : device.getUuids()) {
                        if (uuid.getUuid().equals(P2PActivity.SERVICE_UUID)) {
                            new P2PConnectThread(device, handler).start();
                        }
                    }
                }
            }
        }
    }


    public static byte[] convertStreamToByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[10240];
        int i = Integer.MAX_VALUE;
        while ((i = is.read(buff, 0, buff.length)) > 0) {
            baos.write(buff, 0, i);
        }

        return baos.toByteArray(); // be sure to close InputStream in calling function
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_DISCOVER) {
            if (resultCode == SCAN_MODE_NONE) {
                Toast.makeText(getApplicationContext(), "This device is not connectable.", Toast.LENGTH_LONG).show();
            } else if (resultCode == SCAN_MODE_CONNECTABLE) {
                Toast.makeText(getApplicationContext(), "This device is not discoverable, but can be connected.", Toast.LENGTH_LONG).show();
            } else if (resultCode == SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                Toast.makeText(getApplicationContext(), "This device is now discoverable!", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                enableBluetoothServices();
            } else if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        if (requestCode == REQUEST_MUSIC_FILE) {
            if (resultCode == RESULT_OK) {
                Song songResult = (Song) data.getParcelableExtra("song");
                byte[] byteStream = getByteArrayFromSong(songResult);

                if (!(handler.network.getSelfNode().getMac() == "6C:2F:2C:82:67:11")) {

                    P2PMessage message = new P2PMessage(handler.network.getSelfNode().getMac(), "6C:2F:2C:82:67:11", MessageType.send_message, byteStream);
                    handler.forwardMessage(message);
                }

                //P2PMessage p2pMessage = new P2PMessage(handler.network.getSelfNode().getMac(), target, MessageType.song, byteStream);
                //handler.sendMessageToPeers(p2pMessage);
            } else {
                // ERROR?
            }
        }
    }

    public byte[] getByteArrayFromSong(Song song) {
        File songFile = new File(song.getSongLocation());
        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream(songFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            this.handler.sendToastToUI("Could find file.");
            return null;
        }

        byte[] byteStream = null;

        try {
            return convertStreamToByteArray(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            //TODO
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
        this.handler.closeAllConnections();

        serverThread.close();
    }

    public static String getBluetoothMac(final Context context) {
        String result = null;
        if (context.checkCallingOrSelfPermission(Manifest.permission.BLUETOOTH)
                == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                result = android.provider.Settings.Secure.getString(context.getContentResolver(), "bluetooth_address");
            } else {
                BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
                result = bta != null ? bta.getAddress() : "";
            }
        }
        return result;
    }
}
