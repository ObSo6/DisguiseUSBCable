package com.obso6.disguiseapp.config;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.obso6.disguiseapp.view.HidActivity;
import com.obso6.disguiseapp.view.KeyActivity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SppBle {
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    private static Handler mHandler = null;
    //UAT服务的UUID和相关特征
    public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    //通知所需的BTLE客户端特征的UUID
    public static final UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    //BTLE状态
    private BluetoothAdapter adapter;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic tx;
    private BluetoothGattCharacteristic rx;
    private static Context mContext;
    private static int mConnectionState = STATE_DISCONNECTED;
    public static BluetoothAdapter mBluetoothAdapter;
    private static String mBluetoothDeviceAddress;
    private static String mBluetoothDeviceName;

    public SppBle(Context c, Handler h){
        mContext = c;
        mHandler = h;
        mBluetoothAdapter =BluetoothAdapter.getDefaultAdapter();
    }

    public void setUUID(UUID uart,UUID txd,UUID rxd){
        UART_UUID = uart;
        TX_UUID = txd;
        RX_UUID = rxd;
    }


    public void ConnectDevice(BluetoothDevice bluetoothDevice){
        gatt = bluetoothDevice.connectGatt(mContext, false, callback);
    }

    public void connect(final String address, final String devicename, final Context context) {
        mContext = context;
        if (mBluetoothAdapter == null || address == null) {
            return;
        }
        final BluetoothDevice device = mBluetoothAdapter
                .getRemoteDevice(address);
        if (device == null) {
            return;
        }
        mBluetoothDeviceAddress = address;
        mBluetoothDeviceName = devicename;
        gatt = device.connectGatt(mContext, false, callback);
    }
    public void connect(final BluetoothDevice device, final Context context) {
        mContext = context;
        if (device == null) {
            return;
        }
        mConnectionState = STATE_CONNECTING;
        mHandler.obtainMessage(HidActivity.MESSAGE_STATE_CHANGE, mConnectionState, -1).sendToTarget();
        mHandler.obtainMessage(KeyActivity.MESSAGE_STATE_CHANGE, mConnectionState, -1).sendToTarget();
        mBluetoothDeviceAddress = device.getAddress();
        mBluetoothDeviceName = device.getName();
        retryCount = 0;
        gatt = device.connectGatt(mContext, false, callback);

    }

    public static String getmBluetoothDeviceAddress() {
        return mBluetoothDeviceAddress;
    }

    public static String getmBluetoothDeviceName() {
        return mBluetoothDeviceName;
    }

    public int getConnectionState() {
        return mConnectionState;
    }
    public int getRSSI(){
        if(gatt == null)return 0;
        gatt.readRemoteRssi();
        return 0;
    }

    int retryCount = 0;
    // Main BTLE device callback where much of the logic occurs.
    private final BluetoothGattCallback callback = new BluetoothGattCallback() {
        // Called whenever the device connection state changes, i.e. from disconnected to connected.
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                writeLine("Connected!");
                mConnectionState = STATE_CONNECTED;
                mHandler.obtainMessage(HidActivity.MESSAGE_STATE_CHANGE, mConnectionState, -1).sendToTarget();
                mHandler.obtainMessage(KeyActivity.MESSAGE_STATE_CHANGE, mConnectionState, -1).sendToTarget();
                // Discover services.
                if (!gatt.discoverServices()) {
                    writeLine("Failed to start discovering services!");
                }
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                writeLine("Disconnected!");
                mConnectionState = STATE_DISCONNECTED;
                mHandler.obtainMessage(HidActivity.MESSAGE_STATE_CHANGE, mConnectionState, -1).sendToTarget();
                mHandler.obtainMessage(KeyActivity.MESSAGE_STATE_CHANGE, mConnectionState, -1).sendToTarget();
            } else {
                writeLine("Connection state changed.  New state: " + newState);
            }
        }

        // Called when services have been discovered on the remote device.
        // It seems to be necessary to wait for this discovery to occur before
        // manipulating any services or characteristics.
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                writeLine("Service discovery completed!");
            }
            else {
                writeLine("Service discovery failed with status: " + status);
            }

            if(UART_UUID == null){
                writeLine("uart UUID error");
                return;
            }
            writeLine(UART_UUID.toString());

            if(gatt.getService(UART_UUID) == null){
                writeLine("service error");
                // Discover services.

                mHandler.obtainMessage(101, 0, -1).sendToTarget();
                if(retryCount ++< 10)
                    if (!gatt.discoverServices()) {
                        writeLine("Failed to start discovering services!");
                    }
                return;
            }
            // Save reference to each characteristic.
            tx = gatt.getService(UART_UUID).getCharacteristic(TX_UUID);
            rx = gatt.getService(UART_UUID).getCharacteristic(RX_UUID);
            if(tx != null){
                int charaProp = tx.getProperties();
                // 可读
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {

                }
                // 可写，注：要 & 其可写的两个属性
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0
                        || (charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {

                }
                // 可通知，可指示
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0
                        || (charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {

                }
            }
            // Setup notifications on RX characteristic changes (i.e. data received).
            // First call setCharacteristicNotification to enable notification.
            if (!gatt.setCharacteristicNotification(rx, true)) {
                writeLine("Couldn't set notifications for RX characteristic!");

            }
            // Next update the RX characteristic's client descriptor to enable notifications.
            if (rx.getDescriptor(CLIENT_UUID) != null) {
                BluetoothGattDescriptor desc = rx.getDescriptor(CLIENT_UUID);
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                if (!gatt.writeDescriptor(desc)) {
                    writeLine("Couldn't write RX client descriptor value!");
                    mHandler.obtainMessage(101, 1, -1).sendToTarget();
                }
            }
            else {
                writeLine("Couldn't get RX client descriptor!");
                mHandler.obtainMessage(101, 1, -1).sendToTarget();
            }
        }

        // Called when a remote characteristic changes (like the RX characteristic).
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            byte[] data = characteristic.getValue();
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //broadcastUpdate(status, rssi);
                mHandler.obtainMessage(100, rssi, -1).sendToTarget();
            }
        }
    };


    private void writeLine(String str){
        //Log.d("BLE",str);
    }

    public void Write(byte[] data){
        // Update TX characteristic value.  Note the setValue overload that takes a byte array must be used.
        if(tx == null)return;
        tx.setValue(data);
        if (gatt.writeCharacteristic(tx)) {
            mHandler.obtainMessage(KeyActivity.MESSAGE_WRITE, data.length, -1,data)
                    .sendToTarget();
            mHandler.obtainMessage(HidActivity.MESSAGE_WRITE, data.length, -1,data)
                    .sendToTarget();
            writeLine("Sent: " );
        }
        else {
            writeLine("Couldn't write TX characteristic!");
        }
    }

    public void disconnect(){
        if (gatt != null) {
            // For better reliability be careful to disconnect and close the connection.
            gatt.disconnect();
            gatt.close();
            gatt = null;
            tx = null;
            rx = null;
            mConnectionState = STATE_DISCONNECTED;
            mHandler.obtainMessage(HidActivity.MESSAGE_STATE_CHANGE, mConnectionState, -1).sendToTarget();
            mHandler.obtainMessage(KeyActivity.MESSAGE_STATE_CHANGE, mConnectionState, -1).sendToTarget();
        }
    }

    private List<UUID> parseUUIDs(final byte[] advertisedData) {
        List<UUID> uuids = new ArrayList<>();

        int offset = 0;
        while (offset < (advertisedData.length - 2)) {
            int len = advertisedData[offset++];
            if (len == 0)
                break;

            int type = advertisedData[offset++];
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (len > 1) {
                        int uuid16 = advertisedData[offset++];
                        uuid16 += (advertisedData[offset++] << 8);
                        len -= 2;
                        uuids.add(UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", uuid16)));
                    }
                    break;
                case 0x06:// Partial list of 128-bit UUIDs
                case 0x07:// Complete list of 128-bit UUIDs
                    // Loop through the advertised 128-bit UUID's.
                    while (len >= 16) {
                        try {
                            // Wrap the advertised bits and order them.
                            ByteBuffer buffer = ByteBuffer.wrap(advertisedData, offset++, 16).order(ByteOrder.LITTLE_ENDIAN);
                            long mostSignificantBit = buffer.getLong();
                            long leastSignificantBit = buffer.getLong();
                            uuids.add(new UUID(leastSignificantBit,
                                    mostSignificantBit));
                        } catch (IndexOutOfBoundsException e) {
                            // Defensive programming.
                            //getRSSI.e(LOG_TAG, e.toString());
                            continue;
                        } finally {
                            // Move the offset to read the next uuid.
                            offset += 15;
                            len -= 16;
                        }
                    }
                    break;
                default:
                    offset += (len - 1);
                    break;
            }
        }
        return uuids;
    }
}
