package com.dysania.bluetoothdemo;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import com.dysania.bluetoothdemo.BluetoothLeService.LocalBinder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by DysaniazzZ on 20/03/2017.
 * 蓝牙设备操作页面
 */
public class DeviceControlActivity extends AppCompatActivity implements OnClickListener, OnChildClickListener {

    private static final String DEVICE_NAME = "device_name";
    private static final String DEVICE_ADDRESS = "device_address";

    private static final String LIST_NAME = "name";
    private static final String LIST_UUID = "uuid";

    private static final String TAG = DeviceControlActivity.class.getSimpleName();

    private String mDeviceAddress;
    private boolean mConnectionState = false;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;

    private TextView mTvDeviceState;
    private TextView mTvDeviceData;
    private ExpandableListView mElvGattServices;

    public static void actionStart(Context context, String deviceName, String deviceAddress) {
        Intent intent = new Intent(context, DeviceControlActivity.class);
        intent.putExtra(DEVICE_NAME, deviceName);
        intent.putExtra(DEVICE_ADDRESS, deviceAddress);
        context.startActivity(intent);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothLeService = ((LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            //连接设备
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothLeService = null;
        }
    };

    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                updateConnectionState(true);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                updateConnectionState(false);
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                displayGattServices(mBluetoothLeService.getSupportGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        TextView tvDeviceName = (TextView) findViewById(R.id.tv_device_name);
        TextView tvDeviceAddress = (TextView) findViewById(R.id.tv_device_address);
        mTvDeviceState = (TextView) findViewById(R.id.tv_device_state);
        mTvDeviceData = (TextView) findViewById(R.id.tv_device_data);
        mElvGattServices = (ExpandableListView) findViewById(R.id.elv_gatt_services);
        mTvDeviceState.setOnClickListener(this);
        mElvGattServices.setOnChildClickListener(this);

        Intent intent = getIntent();
        String deviceName = intent.getStringExtra(DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(DEVICE_ADDRESS);

        tvDeviceName.setText(TextUtils.isEmpty(deviceName) ? "N/A" : deviceName);
        tvDeviceAddress.setText(mDeviceAddress);

        //bind service
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result = " + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGattUpdateReceiver != null) {
            unregisterReceiver(mGattUpdateReceiver);
            mGattUpdateReceiver = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unbindService(mServiceConnection);
            mBluetoothLeService = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateConnectionState(final boolean connectionState) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState = connectionState;
                mTvDeviceState.setText(connectionState ? R.string.bluetooth_state_connected : R.string.bluetooth_state_disconnected);
            }
        });
    }

    private void clearUI() {
        mElvGattServices.setAdapter((SimpleExpandableListAdapter) null);
        mTvDeviceData.setText(R.string.bluetooth_empty_data);
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) {
            return;
        }
        String uuid = null;
        String unknownServiceString = getString(R.string.bluetooth_unknown_service);
        String unknownCharacteristicString = getString(R.string.bluetooth_unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<>();     //父类条目
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<>();
        mGattCharacteristics = new ArrayList<>();

        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME, AppConstants.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();       //获取一个service下的所有characteristic
            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<>();             //存储一个characteristic下的数据
            ArrayList<BluetoothGattCharacteristic> characteristics = new ArrayList<>();                     //存储characteristic对象本身

            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                characteristics.add(gattCharacteristic);

                HashMap<String, String> currentCharacteristicData = new HashMap<>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharacteristicData.put(LIST_NAME, AppConstants.lookup(uuid, unknownCharacteristicString));
                currentCharacteristicData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharacteristicData);
            }

            mGattCharacteristics.add(characteristics);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(this, gattServiceData,
                android.R.layout.simple_expandable_list_item_2, new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2}, gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2, new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2});

        mElvGattServices.setAdapter(gattServiceAdapter);
    }

    private void displayData(String data) {
        if (!TextUtils.isEmpty(data)) {
            mTvDeviceData.setText(data);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_device_state:
                if (mConnectionState) {
                    mBluetoothLeService.disconnect();
                } else {
                    mBluetoothLeService.connect(mDeviceAddress);
                }
                break;
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        if (mGattCharacteristics != null) {
            BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(groupPosition).get(childPosition);
            int properties = characteristic.getProperties();
            if ((properties | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                if (mNotifyCharacteristic != null) {
                    mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                    mNotifyCharacteristic = null;
                }
                mBluetoothLeService.readCharacteristic(characteristic);
            }
            if ((properties | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                mNotifyCharacteristic = characteristic;
                mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
            }
            return true;
        }
        return false;
    }
}
