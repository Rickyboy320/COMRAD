package io.comrad.p2p;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class P2PReceiver extends BroadcastReceiver
{
    private PeerAdapter peerAdapter;

    P2PReceiver(PeerAdapter peerAdapter)
    {
        this.peerAdapter = peerAdapter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            // Discovery has found a device. Get the BluetoothDevice
            // object and its info from the Intent.
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String deviceName = device.getName();
            this.peerAdapter.getList().add(device.getAddress());
            this.peerAdapter.notifyDataSetChanged();
        }
    }
}
