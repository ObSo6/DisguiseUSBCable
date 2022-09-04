package com.obso6.disguiseapp.config;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.obso6.disguiseapp.view.HidActivity;
import com.obso6.disguiseapp.view.KeyActivity;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

//蓝牙连接设备和通信
public class BluetoothChatService {
    //创建服务器套接字时SDP记录的名称
    private static final String NAME = "BluetoothCom";
    //唯一的UUID
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //蓝牙串行板的UUID 00001101-0000-1000-8000-00805F9B34FB
    //private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    //成员字段
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    //标识连接状态
    public static final int STATE_NONE = 0;       //什么都不做
    public static final int STATE_LISTEN = 1;     //监听传入的连接
    public static final int STATE_CONNECTING = 2; //监听传出的连接
    public static final int STATE_CONNECTED = 3;  //连接远程设备

    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothChatService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        mState = state;
        //用于UI更新状态
        mHandler.obtainMessage(KeyActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
        mHandler.obtainMessage(HidActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg1 = mHandler.obtainMessage(KeyActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle1 = new Bundle();
        bundle1.putString(KeyActivity.DEVICE_NAME, device.getName());
        msg1.setData(bundle1);
        mHandler.sendMessage(msg1);

        Message msg2 = mHandler.obtainMessage(HidActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle2 = new Bundle();
        bundle2.putString(HidActivity.DEVICE_NAME, device.getName());
        msg2.setData(bundle2);
        mHandler.sendMessage(msg2);

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        Message msg1 = mHandler.obtainMessage(KeyActivity.MESSAGE_TOAST);
        Bundle bundle1 = new Bundle();
        bundle1.putString(KeyActivity.TOAST, "连接失败");
        msg1.setData(bundle1);
        mHandler.sendMessage(msg1);

        Message msg2 = mHandler.obtainMessage(HidActivity.MESSAGE_TOAST);
        Bundle bundle2 = new Bundle();
        bundle2.putString(HidActivity.TOAST, "连接失败");
        msg2.setData(bundle2);
        mHandler.sendMessage(msg2);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        Message msg1 = mHandler.obtainMessage(KeyActivity.MESSAGE_TOAST);
        Bundle bundle1 = new Bundle();
        bundle1.putString(KeyActivity.TOAST, "蓝牙已断开连接");
        msg1.setData(bundle1);
        mHandler.sendMessage(msg1);

        Message msg2 = mHandler.obtainMessage(HidActivity.MESSAGE_TOAST);
        Bundle bundle2 = new Bundle();
        bundle2.putString(HidActivity.TOAST, "蓝牙已断开连接");
        msg2.setData(bundle2);
        mHandler.sendMessage(msg2);
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                //listen() failed
            }
            mmServerSocket = tmp;
        }

        public void run() {
            setName("AcceptThread");
            BluetoothSocket socket;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothChatService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    //Could not close unwanted socket
                                }
                                break;
                        }
                    }
                }
            }
            //END mAcceptThread
        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                //close() of server failed
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                //create() failed
            }
            mmSocket = tmp;
        }

        public void run() {
            setName("ConnectThread");
            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    //unable to close() socket during connection failure
                }
                // Start the service over to restart listening mode
                BluetoothChatService.this.start();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothChatService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                //close() of connect socket failed
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                //temp sockets not created
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            byte[] buffer2 = new byte[1024];
            int bytes;
            int i;
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    if(bytes>0){
                        buffer2=buffer.clone();
                        for(i=0;i<buffer.length;i++)buffer[i]=0;
                        // Send the obtained bytes to the UI Activity

                    }
                } catch (IOException e) {
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(KeyActivity.MESSAGE_WRITE, buffer.length, -1, buffer)
                        .sendToTarget();

                mHandler.obtainMessage(HidActivity.MESSAGE_WRITE, buffer.length, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                //Exception during write
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                //close() of connect socket failed
            }
        }
    }
}
