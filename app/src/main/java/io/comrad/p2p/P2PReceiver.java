package io.comrad.p2p;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

public class P2PReceiver extends BroadcastReceiver
{
    private P2PMessageHandler handler;
    private PeerAdapter peerAdapter;
    private List<BluetoothDevice> unknownDevices = new ArrayList<>();

    P2PReceiver(PeerAdapter peerAdapter, P2PMessageHandler handler)
    {
        this.handler = handler;
        this.peerAdapter = peerAdapter;
        this.handler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            this.peerAdapter.getList().add(device.getAddress());
            this.peerAdapter.notifyDataSetChanged();

            this.unknownDevices.add(device);

        } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            this.hailMary();
        }
    }

    private void hailMary()
    {
        for(BluetoothDevice device : this.unknownDevices) {
            new P2PConnectThread(device, handler).start();
        }
    }
}
