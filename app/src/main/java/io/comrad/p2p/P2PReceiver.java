package io.comrad.p2p;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.os.Parcelable;
import io.comrad.p2p.messages.P2PMessageHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class P2PReceiver extends BroadcastReceiver
{
    private P2PMessageHandler handler;
    private PeerAdapter peerAdapter;

    private List<BluetoothDevice> unknownDevices = new ArrayList<>();

    private BluetoothDevice currentlyFetching = null;

    P2PReceiver(PeerAdapter peerAdapter, P2PMessageHandler handler)
    {
        this.handler = handler;
        this.peerAdapter = peerAdapter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if(handler.getNetwork().hasPeer(device.getAddress()) || P2PConnectThread.isConnecting(device.getAddress()))
            {
                return;
            }

            if(device.getUuids() != null) {
                for (ParcelUuid uuid : device.getUuids()) {
                    if(uuid.getUuid().equals(P2PActivity.SERVICE_UUID)) {
                        new P2PConnectThread(device, handler).start();
                        this.peerAdapter.getList().add("**" + device.getAddress());
                        return;
                    }
                }
            }

            this.peerAdapter.getList().add(device.getAddress());
            this.unknownDevices.add(device);
            this.peerAdapter.notifyDataSetChanged();
        } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            fetchNextDevice(null);
        } else if(BluetoothDevice.ACTION_UUID.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Parcelable[] uuids = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);

            fetchNextDevice(device);

            if(handler.getNetwork().hasPeer(device.getAddress()) || P2PConnectThread.isConnecting(device.getAddress())) {
                return;
            }

            if (uuids == null) {
                return;
            }

            for (Parcelable parcelable : uuids) {
                if (UUID.fromString(parcelable.toString()).equals(P2PActivity.SERVICE_UUID)) {
                    new P2PConnectThread(device, handler).start();
                    return;
                }
            }
        }
    }

    private void fetchNextDevice(BluetoothDevice previousDevice)
    {
        if((currentlyFetching == null && previousDevice != null) || (previousDevice == null && currentlyFetching != null)) {
            return;
        }

        if(currentlyFetching != previousDevice && !(currentlyFetching.getAddress().equals(previousDevice.getAddress()))) {
            return;
        }

        if(this.unknownDevices.isEmpty()) {
            previousDevice = null;
            return;
        }

        BluetoothDevice device = this.unknownDevices.remove(0);
        currentlyFetching = device;
        device.fetchUuidsWithSdp();
    }
}
