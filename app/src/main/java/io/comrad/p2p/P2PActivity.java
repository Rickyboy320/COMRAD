package io.comrad.p2p;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.comrad.R;

import static android.bluetooth.BluetoothAdapter.SCAN_MODE_CONNECTABLE;
import static android.bluetooth.BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE;
import static android.bluetooth.BluetoothAdapter.SCAN_MODE_NONE;

public class P2PActivity extends Activity {

    public final static String SERVICE_NAME = "COMRAD";
    public final static UUID SERVICE_UUID = UUID.fromString("7337958a-460f-4b0c-942e-5fa111fb2bee");

    private final static int REQUEST_ENABLE_BT = 1;
    private final static int PERMISSION_REQUEST = 2;
    private P2PReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST);

        enableBluetooth();
        registerComponents();
    }

    private void enableBluetooth()
    {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "This device does not support bluetooth.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        final List<String> peerList = new ArrayList<>();
        RecyclerView peerView = findViewById(R.id.peersList);

        LinearLayoutManager mng = new LinearLayoutManager(this);
        peerView.setLayoutManager(mng);

        final PeerAdapter peerAdapter = new PeerAdapter(peerList);
        peerView.setAdapter(peerAdapter);

        receiver = new P2PReceiver(peerAdapter);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        Button discoverButton = findViewById(R.id.discover);
        discoverButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast toast = Toast.makeText(getApplicationContext(), "Discovering devices...", Toast.LENGTH_LONG);
                toast.show();

                if (bluetoothAdapter.isDiscovering()) {
                    // Bluetooth is already in modo discovery mode, we cancel to restart it again
                    bluetoothAdapter.cancelDiscovery();
                }
                bluetoothAdapter.startDiscovery();
                peerList.clear();
                peerAdapter.notifyDataSetChanged();

                Intent discoverableIntent =
                        new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);
            }
        });

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                System.out.println(device.getBluetoothClass().toString());
            }
        }
    }


    private void registerComponents()
    {

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BT) {
            if(resultCode == SCAN_MODE_NONE) {
                Toast toast = Toast.makeText(getApplicationContext(), "This device is not connectable.", Toast.LENGTH_LONG);
                toast.show();
            } else if(resultCode == SCAN_MODE_CONNECTABLE) {
                Toast toast = Toast.makeText(getApplicationContext(), "This device is not discoverable, but can be connected.", Toast.LENGTH_LONG);
                toast.show();
            } else if(resultCode == SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                Toast toast = Toast.makeText(getApplicationContext(), "This device is now discoverable!", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST) {
            //TODO
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }
}
