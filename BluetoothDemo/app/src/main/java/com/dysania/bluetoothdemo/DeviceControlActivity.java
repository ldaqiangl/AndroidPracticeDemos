package com.dysania.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import com.dysania.bluetoothdemo.BluetoothLeService.LocalBinder;
import java.lang.reflect.Method;
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

    private static final String PIN_CODE = "111111";

    public static final int STATE_UNBONDED = 0;
    public static final int STATE_BONDING = 1;
    public static final int STATE_BONDED = 2;

    private static final String TAG = DeviceControlActivity.class.getSimpleName();

    private String mDeviceAddress;
    private BluetoothDevice mBluetoothDevice;
    private boolean mConnectionState = false;
    private int mBondState = STATE_UNBONDED;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;

    private TextView mTvDeviceState;
    private TextView mTvDeviceData;
    private Button mBtnBondState;
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

            mBluetoothGatt = mBluetoothLeService.getBluetoothGatt();
            mBluetoothDevice = mBluetoothLeService.getBluetoothDevice(mDeviceAddress);
            updateBondState(mBluetoothLeService.getBondState(mDeviceAddress) ? STATE_BONDED : STATE_UNBONDED);
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

    private BroadcastReceiver mBluetoothAndBondStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                switch (mBluetoothDevice.getBondState()) {
                    case BluetoothDevice.BOND_BONDING:
                        Log.e(TAG, "The device is bonding");
                        updateBondState(STATE_BONDING);
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.e(TAG, "The device is bonded");
                        updateBondState(STATE_BONDED);
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Log.e(TAG, "The device is not bonded");
                        updateBondState(STATE_UNBONDED);
                        break;
                }
//            } else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
//                Log.e(TAG, "Pairing request");
//                //本来打算接收这个广播时手动设置PIN码，跳过输入步骤，但是在国产机上不好使...
//                try {
//                    mBluetoothDevice.setPin(PIN_CODE.getBytes("UTF-8"));
//                    abortBroadcast();
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int bluetoothState = intent.getExtras().getInt(BluetoothAdapter.EXTRA_STATE);
                switch (bluetoothState) {
                    case BluetoothAdapter.STATE_OFF:
                        UIUtil.createToast(context, R.string.bluetooth_is_off);
                        finish();
                        break;
                }
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
        mBtnBondState = (Button) findViewById(R.id.btn_bond_state);
        mElvGattServices = (ExpandableListView) findViewById(R.id.elv_gatt_services);
        mTvDeviceState.setOnClickListener(this);
        mBtnBondState.setOnClickListener(this);
        mElvGattServices.setOnChildClickListener(this);

        Intent intent = getIntent();
        String deviceName = intent.getStringExtra(DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(DEVICE_ADDRESS);

        tvDeviceName.setText(TextUtils.isEmpty(deviceName) ? "N/A" : deviceName);
        tvDeviceAddress.setText(mDeviceAddress);

        //bind service
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        //register bluetooth and bond state receiver
        registerReceiver(mBluetoothAndBondStateReceiver, makeBluetoothAndBondStateIntentFilter());

        mBtnBondState.setEnabled(mConnectionState);
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

        if (mBluetoothAndBondStateReceiver != null) {
            unregisterReceiver(mBluetoothAndBondStateReceiver);
            mBluetoothAndBondStateReceiver = null;
        }

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
                mBtnBondState.setEnabled(mConnectionState);
            }
        });
    }

    private void updateBondState(final int bondState) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBondState = bondState;
                switch (bondState) {
                    case STATE_BONDED:
                        mBtnBondState.setEnabled(true);
                        mBtnBondState.setText(R.string.bluetooth_state_bonded);
                        break;
                    case STATE_BONDING:
                        mBtnBondState.setEnabled(false);
                        mBtnBondState.setText(R.string.bluetooth_state_bonding);
                        break;
                    case STATE_UNBONDED:
                        mBtnBondState.setEnabled(true);
                        mBtnBondState.setText(R.string.bluetooth_state_unbonded);
                        break;
                }
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

    private static IntentFilter makeBluetoothAndBondStateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
//        intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
//        intentFilter.setPriority(intentFilter.SYSTEM_HIGH_PRIORITY - 1);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return intentFilter;
    }

    /**
     * 解除绑定，这个方法在国产机上存在较多问题
     */
    private void deleteBondInformation(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
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
            case R.id.btn_bond_state:
                switch (mBondState) {
                    case STATE_BONDED:
                        //解绑
                        //deleteBondInformation(mBluetoothDevice);
                        //这里假解绑，只是将连接断开
                        if (mConnectionState) {
                            mBluetoothLeService.disconnect();
                        }
                        break;
                    case STATE_UNBONDED:
                        //绑定
                        mBluetoothDevice.createBond();
                        break;
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
            if ((properties | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0
                    || (properties | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                if (AppConstants.CUSTOM_SERVICE_SMART_PILLBOX_UUID.equals(characteristic.getService().getUuid().toString())
                        && AppConstants.CUSTOM_CHARACTERISTICS_SMART_PILLBOX_WRITE_DATA_UUID.equals(characteristic.getUuid().toString())) {
                    Log.e(TAG, "permission: " + characteristic.getPermissions());
                    showDialog(characteristic);
                }
            }
            return true;
        }
        return false;
    }

    private void showDialog(final BluetoothGattCharacteristic characteristic) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        String[] options = new String[]{"Beep", "Shark", "Led"};
        dialog.setTitle("BLE Command Options")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                //蜂鸣
                                characteristic.setValue(new byte[]{(byte) 0xAA, 0x04, 0x01, (byte) 0xB3, 0x62, 0x55});
                                break;
                            case 1:
                                //震动
                                characteristic.setValue(new byte[]{(byte) 0xAA, 0x04, 0x00, (byte) 0xB4, 0x62, 0x55});
                                break;
                            case 2:
                                //亮灯
                                characteristic.setValue(new byte[]{(byte) 0xAA, 0x05, 0x00, (byte) 0xB2, 0x03, 0x64, 0x55});
                                break;
                        }
                        mBluetoothGatt.writeCharacteristic(characteristic);
                    }
                })
                .show();
    }
}
