/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.artoolkit.ar.ARPokemonBattle.Bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import org.artoolkit.ar.ARPokemonBattle.PokemonSelectActivity;
import org.artoolkit.ar.ARPokemonBattle.R;
import org.artoolkit.ar.ARPokemonBattle.Util.GameDefinitions;

/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class BluetoothFragment extends Fragment {

    private static final String TAG = "BluetoothFragment";

    /** Should only be HOST or CLIENT **/
    /** Fragment will not be added to activity if mode is LOCAL **/
    public static final String ARG_MODE = "mode";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    private Toolbar mToolbar;

    private int mMode;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothService mBluetoothService = null;

    private static boolean mIsBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBluetoothService = ((BluetoothService.ServiceBinder)service).getService();
            mBluetoothService.setHandler(mHandler);
            Log.d(TAG, "BT Service connected, state: " + mBluetoothService.getState());
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBluetoothService = null;
            Log.e(TAG, "BT Service disconnected");
        }
    };

    private void doBindService() {
        Log.d(TAG, "doBindService");
        getActivity().bindService(new Intent(getActivity(), BluetoothService.class),
                mConnection, Context.BIND_AUTO_CREATE);

        mIsBound = true;
    }

    private void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            getActivity().unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMode = getArguments().getInt(ARG_MODE);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else if (!mIsBound) {
            // Start BT service and bind
            Intent startBTService = new Intent(getActivity(), BluetoothService.class);
            getActivity().startService(startBTService);
            Log.d(TAG, "BTService started in onStart");

            doBindService();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_toolbar, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mToolbar = (Toolbar) view.findViewById(R.id.bluetoothToolbar);

        switch (mMode) {
            case GameDefinitions.MODE_HOST:
                mToolbar.setTitle(R.string.mode_host_title);
                // Menu set up
                mToolbar.inflateMenu(R.menu.menu_host_mode);
                mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.discoverable_action: {
                                ensureDiscoverable();
                                return true;
                            }
                        }
                        return false;
                    }
                });
                // Service is slow to bind, so handler will not get state change message
                setStatus(R.string.title_host_waiting);
                break;
            case GameDefinitions.MODE_CLIENT:
                mToolbar.setTitle(R.string.mode_connect_title);
                // Menu set up
                mToolbar.inflateMenu(R.menu.menu_connect_mode);
                mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.secure_connect_scan: {
                                // Launch the DeviceListActivity to see devices and do scan
                                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                                return true;
                            }
                        }
                        return false;
                    }
                });
                // Service is slow to bind, so handler will not get state change message
                setStatus(R.string.title_not_connected);
                break;
        }

    }

    /**
     * Makes this device discoverable.
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Send packet is manually created due to the boolean value. Could have changed the boolean into
     * an int, but whatever
     * @param op
     * @param select
     * @param ready
     * @return
     */
    public boolean sendMessage(int op, int select, boolean ready) {
        // Check that we're actually connected before trying anything
        if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return false;
        }

        byte[] send = new byte[3];
        send[0] = Integer.valueOf(op).byteValue();
        send[1] = Integer.valueOf(select).byteValue();
        send[2] = (byte) (ready ? 1 : 0);

        mBluetoothService.write(send);
        return true;
    }

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        if (null == mToolbar) {
            return;
        }
        mToolbar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        if (null == mToolbar) {
            return;
        }
        mToolbar.setSubtitle(subTitle);
    }

    /************************/
    /** BT SERVICE HANDLER **/
    /************************/

    /**
     * The Handler that gets information back from the BluetoothService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case GameDefinitions.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            if (mMode == GameDefinitions.MODE_HOST) {
                                setStatus(R.string.title_host_waiting);
                            }
                            else {
                                // Only other option is MODE_CLIENT
                                setStatus(R.string.title_not_connected);
                            }
                            break;
                    }
                    break;
                case GameDefinitions.MESSAGE_WRITE:
//                    byte[] writeBuf = (byte[]) msg.obj;
                    // don't need to do anything on write
                    break;
                case GameDefinitions.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    // Update status views
                    boolean rdy = readBuf[2] == 1;
                    if (rdy) {
                        setStatus(R.string.title_opp_ready);
                    }
                    else {
                        setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                    }

                    // Propagate message to PokemonSelectActivity handler
                    ((PokemonSelectActivity) getActivity()).read(readBuf);
                    break;
                case GameDefinitions.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(GameDefinitions.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case GameDefinitions.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(GameDefinitions.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    // Initialize the BluetoothService to perform bluetooth connections
                    Intent startBTService = new Intent(getActivity(), BluetoothService.class);
                    getActivity().startService(startBTService);
                    Log.d(TAG, "BTService started in onActivityResult");

                    doBindService();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }

    /**
     * Establish connection with other divice
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     */
    private void connectDevice(Intent data) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mBluetoothService.connect(device);
    }

}
