package io.comrad.p2p;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class P2PReceiver extends BroadcastReceiver
{
    private PeerAdapter peerAdapter;
    private List<BluetoothDevice> unknownDevices = new ArrayList<>();
    private List<BluetoothDevice> applicableDevices = new ArrayList<>();

    P2PReceiver(PeerAdapter peerAdapter)
    {
        this.peerAdapter = peerAdapter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            System.out.println("Device: " + device.getAddress() + " :: " + Arrays.toString(device.getUuids()));

            this.peerAdapter.getList().add(device.getAddress());
            this.peerAdapter.notifyDataSetChanged();

            //if(matchUUID(device)) { return; }

            this.unknownDevices.add(device);

        } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            System.out.println("Finished discovery!");
            this.hailMary();

            //fetchNextDevice();
        } /*else if(BluetoothDevice.ACTION_UUID.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Parcelable[] uuids = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);

            System.out.println("Device: " + device.getAddress());
            System.out.println("UUIDs: " + Arrays.toString(uuids));

            matchUUID(device);

            fetchNextDevice();
        }*/
    }

    private void fetchNextDevice()
    {
        if(this.unknownDevices.isEmpty()) {
            System.out.println("Applicable devices: " + this.applicableDevices);
            return;
        }

        BluetoothDevice device = this.unknownDevices.remove(0);
        device.fetchUuidsWithSdp();
    }

    private boolean matchUUID(BluetoothDevice device)
    {
        if(device.getUuids() == null) { return false; }

        for(ParcelUuid uuid : device.getUuids()) {
            if(uuid.getUuid().equals(P2PActivity.SERVICE_UUID)) {
                this.applicableDevices.add(device);
                return true;
            }
        }

        return false;
    }

    private void hailMary()
    {
        for(BluetoothDevice device : this.unknownDevices) {
            new P2PConnectThread(device).start();
        }
    }
}
