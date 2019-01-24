package io.comrad.p2p;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import io.comrad.R;
import io.comrad.music.MusicListFragment;
import io.comrad.music.PlayMusic;
import io.comrad.music.Song;
import io.comrad.music.SongRequest;
import io.comrad.p2p.messages.MessageType;
import io.comrad.p2p.messages.P2PMessage;
import io.comrad.p2p.messages.P2PMessageHandler;
import io.comrad.p2p.network.Graph;
import io.comrad.p2p.network.Node;
import nl.erlkdev.adhocmonitor.AdhocMonitorBinder;
import nl.erlkdev.adhocmonitor.AdhocMonitorService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class P2PActivity extends FragmentActivity  {
    public final static String SERVICE_NAME = "COMRAD";
    public final static UUID SERVICE_UUID = UUID.fromString("7337958a-460f-4b0c-942e-5fa111fb2bee");

    private final static int REQUEST_ENABLE_BT = 1;
    private final static int REQUEST_DISCOVER = 2;
    private final static int LOCATION_PERMISSION = 3;
    private static final int REQUEST_MUSIC_FILE = 4;
    private static final int MUSIC_PERMISSION = 5;

    private ServiceConnection monitorService;
    private P2PServerThread serverThread;

    private P2PReceiver receiver;

    private ArrayList<Song> ownSongs = new ArrayList<>();

    private int currentId = 0;
    private Song currentSong;
    private AudioTrack audioTrack;

    private AdhocMonitorService monitor;

    private final P2PMessageHandler handler = new P2PMessageHandler(this);

    private long requestStart;

    private boolean isIdle = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p2p);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MUSIC_PERMISSION);
        } else {
            getMusic();
        }

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION);

        addComponents();
    }

    private void enableBluetoothServices() {
        this.handler.onBluetoothEnable(ownSongs);

        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        receiver = new P2PReceiver(handler);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_UUID);
        registerReceiver(receiver, filter);

        this.serverThread = new P2PServerThread(BluetoothAdapter.getDefaultAdapter(), this.handler);
        this.serverThread.start();

        startDiscovery(bluetoothAdapter);

        if(!this.handler.getNetwork().getSelfMac().equalsIgnoreCase("02:00:00:00:00:00")) {
            reattachMonitor();
        }
    }

    public void startDiscovery(final BluetoothAdapter adapter) {
        final Handler runner = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {

                System.out.println("Discovering if idle: " + isIdle);
                if(isIdle) {
                    connectToBondedDevices(adapter, handler);

                    if (adapter.isDiscovering()) {
                        adapter.cancelDiscovery();
                    }
                    adapter.startDiscovery();

                    runner.postDelayed(this, 60 * 1000);
                }
                else {
                    runner.postDelayed(this, 10 * 1000);
                }
            }
        };

        final Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
                startActivityForResult(discoverableIntent, REQUEST_DISCOVER);
                runner.postDelayed(this, 3600 * 1000);
            }
        };

        runner.post(runnable);
        runner.post(runnable2);
    }

    public void reattachMonitor() {
        Intent adhocIntent = new Intent(this, AdhocMonitorService.class);

        if(this.monitorService != null) {
            this.unbindService(this.monitorService);
        } else {
            startService(adhocIntent);
        }

        this.monitorService = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                Log.d("MonitorService", "Adhoc Monitor service is connected");
                AdhocMonitorBinder adhocMonitorBinder = (AdhocMonitorBinder) service;
                monitor = adhocMonitorBinder.getService();

                /* Starts the monitor. */
                monitor.startMonitor(handler.getNetwork().getSelfMac(), "145.109.45.90");

                handler.onMonitorEnable(monitor);
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                Log.d("MonitorService", "Adhoc Monitor service is disconnected");
            }
        };

        /* Bind monitor service. */
        bindService(adhocIntent, monitorService, BIND_AUTO_CREATE);
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

            songCursor.close();
        }
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

        Button showGraph = findViewById(R.id.showGraph);
        showGraph.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                synchronized (handler.getNetwork().getGraph()) {
                    System.out.println(handler.getNetwork().getGraph());
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
                if (this.handler.getNetwork().hasPeer(device.getAddress()) || P2PConnectThread.isConnecting(device.getAddress())) {
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

    public void refreshPlaylist(Set<Node> nodes) {
        ArrayList<Song> arrayList = new ArrayList<>();
        for (Node node : nodes) {
            if (node.getPlaylist() != null) {
                arrayList.addAll(node.getPlaylist());
            }
        }

        MusicListFragment fragment = (MusicListFragment) getSupportFragmentManager().findFragmentById(R.id.MusicActivity);

        if (fragment != null) {
            fragment.setPlayList(arrayList);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_DISCOVER) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "This device is not discoverable.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "This device is now discoverable for " + resultCode + " seconds.", Toast.LENGTH_SHORT).show();
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
    }

    public void requestSong(Song song) {
        Graph graph = this.handler.getNetwork().getGraph();
        synchronized (graph) {
            Node node = graph.getNearestSong(song);
            if(node == null) {
                throw new IllegalStateException("Song " + song + " was requested, but was not present in any of the nodes in the network.");
            }

            this.currentId++;
            this.currentSong = song;

            System.out.println(song.getSongSize());

            if(node.equals(this.handler.getNetwork().getGraph().getSelfNode())) {
                try {
                    Song.SongMetaData metaData = song.getSongMetaData();
                    this.prepareAudioTrack(metaData.getSampleRate(), metaData.getNumChannels());
                    this.saveMusicBytePacket(this.currentId, 0, convertStreamToByteArray(song.getStream(this.handler)));
                    this.sendByteArrayToPlayMusic(this.currentId);
                } catch(IOException e) {
                    e.printStackTrace();
                    this.handler.sendToastToUI("Could not play " + song.getSongTitle() + ".");
                    return;
                }
            } else {
                requestStart = System.currentTimeMillis();
                this.handler.getNetwork().forwardMessage(new P2PMessage(this.handler.getNetwork().getSelfMac(), node.getMac(), MessageType.request_song, new SongRequest(this.currentId, song)));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == MUSIC_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getMusic();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MUSIC_PERMISSION);
            }
        }
    }

    public static byte[] convertStreamToByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[10240];
        int i;
        while ((i = is.read(buff, 0, buff.length)) > 0) {
            baos.write(buff, 0, i);
        }

        return baos.toByteArray();

    }

    public void setIdle(boolean idle)
    {
        System.out.println("Setting idle state: " + idle);

        this.isIdle = idle;

        if(!idle) {
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            receiver.abort();
        }
    }

    public void prepareAudioTrack(int samplerate, int channels) {
        int channelType;
        switch(channels)
        {
            case 1:
                channelType = AudioFormat.CHANNEL_OUT_MONO;
                break;
            case 2:
                channelType = AudioFormat.CHANNEL_OUT_STEREO;
                break;
            case 4:
                channelType = AudioFormat.CHANNEL_OUT_QUAD;
                break;
            case 6:
                channelType = AudioFormat.CHANNEL_OUT_5POINT1;
                break;
            default:
                throw new IllegalArgumentException("Amount of channels is not supported: " + channels);
        }

        int size = AudioTrack.getMinBufferSize(samplerate, channelType, AudioFormat.ENCODING_PCM_8BIT);

        this.audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, samplerate,
                channelType, AudioFormat.ENCODING_PCM_8BIT,
                size, AudioTrack.MODE_STREAM);

        System.out.println("Samplerate: " + samplerate + ", channels: " + channels + ", size: " + size);
        System.out.println("Channel type stereo: " + (channelType == AudioFormat.CHANNEL_OUT_STEREO));
    }

    public void sendByteArrayToPlayMusic(int id) {
        if(id != currentId) {
            return;
        }

        if(requestStart != 0) {
            System.out.println("Time taken to receive song: " + (System.currentTimeMillis() - requestStart));
        }

        requestStart = 0;
        PlayMusic fragment = (PlayMusic) getSupportFragmentManager().findFragmentById(R.id.PlayMusic);
        fragment.setAudioTrack(this.audioTrack);
    }

    public void saveMusicBytePacket(int id, int offset, byte[] songBytes) {
        System.out.println("id: " + id);
        if (id != this.currentId) {
            return;
        }

        System.out.println("offset: " + offset);
        audioTrack.write(songBytes, offset, songBytes.length);
        setProgress(this.currentSong.getSongSize(), offset + songBytes.length);
    }

    public void setProgress(int size, int offset) {
        PlayMusic fragment = (PlayMusic) getSupportFragmentManager().findFragmentById(R.id.PlayMusic);
        fragment.incrementProgress(size, offset);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(this.monitorService);

        unregisterReceiver(receiver);
        this.handler.getNetwork().closeAllConnections();

        serverThread.close();
    }

    public static String getBluetoothMac(final Context context) {
        String result = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            result = android.provider.Settings.Secure.getString(context.getContentResolver(), "bluetooth_address");
        }

        if(result == null) {
            BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
            result = bta != null ? bta.getAddress() : "02:00:00:00:00:00";
        }

        return result;
    }
}
