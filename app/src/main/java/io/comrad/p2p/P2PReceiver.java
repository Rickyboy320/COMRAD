package io.comrad.p2p;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class P2PReceiver extends BroadcastReceiver
{
    private PeerAdapter peerAdapter;
    private List<BluetoothDevice> devices = new ArrayList<>();

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
            this.peerAdapter.getList().add(device.getAddress());
            System.out.println("Device: " + device.getAddress() + " :: " + Arrays.toString(device.getUuids()));
            this.peerAdapter.notifyDataSetChanged();
            this.devices.add(device);
        } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            fetchNextDevice();
        } else if(BluetoothDevice.ACTION_UUID.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Parcelable[] uuids = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
            System.out.println("Device: " + device.getAddress());

            System.out.println("UUIDs: " + Arrays.toString(uuids));

            fetchNextDevice();
        }
    }

    private void fetchNextDevice()
    {
        if(this.devices.isEmpty()) {
            return;
        }

        BluetoothDevice device = this.devices.remove(0);
        device.fetchUuidsWithSdp();
    }
}
