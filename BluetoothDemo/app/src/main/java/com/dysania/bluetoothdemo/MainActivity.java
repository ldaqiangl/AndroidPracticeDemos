package com.dysania.bluetoothdemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by DysaniazzZ on 14/03/2017.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtnBluetoothState;
    private ListView mLvAvailableDevices;

    private Handler mHandler = new Handler();
    private boolean mIsBluetoothEnabled = false;
    private List<String> mAvailableDevices = new ArrayList<>();

    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mAvailableDevicesAdapter;

    private static final int REQUEST_PERMISSION_ACL = 0x01;
    private static final int REQUEST_ENABLE_BT = 0x02;
    private static final int SCAN_DEVICE_DURATION = 10000;      //设置蓝牙扫描时长
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
    }

    private void initData() {
        //检查设备是否支持BLE
        if (!BleUtil.checkBleSupport(this)) {
            UIUtil.createToast(this, R.string.ble_not_support);
            finish();
        }
        //检查位置权限是否授予(6.0需要）
        checkAclPermission();
        //检查蓝牙是否开启
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (!BleUtil.checkBleEnabled(mBluetoothAdapter)) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        } else {
            mIsBluetoothEnabled = true;
        }
        //注册蓝牙状态变化的广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothStateReceiver, intentFilter);
    }

    private void initView() {
        mBtnBluetoothState = (Button) findViewById(R.id.btn_bluetooth_state);
        mLvAvailableDevices = (ListView) findViewById(R.id.lv_available_devices);
        mBtnBluetoothState.setOnClickListener(this);
        if (mIsBluetoothEnabled) {
            mBtnBluetoothState.setText(R.string.bluetooth_start_scan);
        } else {
            mBtnBluetoothState.setText(R.string.bluetooth_open_bt);
        }
        mAvailableDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mAvailableDevices);
        mLvAvailableDevices.setAdapter(mAvailableDevicesAdapter);
    }

    private void checkAclPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_ACL);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_ACL:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //权限被授予
                } else {
                    //权限被拒绝
                    UIUtil.createToast(this, R.string.bluetooth_permission_denied);
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    //蓝牙开启
                    mIsBluetoothEnabled = true;
                    mBtnBluetoothState.setText(R.string.bluetooth_start_scan);
                } else {
                    //蓝牙未开启
                    mIsBluetoothEnabled = false;
                    mBtnBluetoothState.setText(R.string.bluetooth_open_bt);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_bluetooth_state:
                if (mIsBluetoothEnabled) {
                    showPairedAndScanDevices();
                } else {
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                }
                break;
        }
    }

    private BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int bluetoothState = intent.getExtras().getInt(BluetoothAdapter.EXTRA_STATE);
            Log.e(TAG, String.valueOf(bluetoothState));
            switch (bluetoothState) {
                case BluetoothAdapter.STATE_ON:
                    mIsBluetoothEnabled = true;
                    mBtnBluetoothState.setText(R.string.bluetooth_start_scan);
                    break;
                case BluetoothAdapter.STATE_OFF:
                    mIsBluetoothEnabled = false;
                    mBtnBluetoothState.setText(R.string.bluetooth_open_bt);
                    mAvailableDevices.clear();
                    mAvailableDevicesAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    /**
     * 显示已配对的设备并开始扫描蓝牙设备
     */
    private void showPairedAndScanDevices() {
        mAvailableDevices.clear();
        mAvailableDevices.add(getString(R.string.bluetooth_paired_list));
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (!pairedDevices.isEmpty()) {
            for (BluetoothDevice pairedDevice : pairedDevices) {
                mAvailableDevices.add(pairedDevice.getName() + "\n" + pairedDevice.getAddress());
            }
        } else {
            mAvailableDevices.add(getString(R.string.bluetooth_no_paired));
        }
        mAvailableDevicesAdapter.notifyDataSetChanged();
        mAvailableDevices.add(getString(R.string.bluetooth_available_list));
        scanLeDevices(true);
    }

    /**
     * 扫描蓝牙设备
     */
    private void scanLeDevices(boolean enable) {
        if (enable) {
            //经过预定扫描期后停止扫描
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_DEVICE_DURATION);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String content = device.getName() + "\n" + device.getAddress();
                    if (!mAvailableDevices.contains(content)) {
                        mAvailableDevices.add(content);
                        mAvailableDevicesAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBluetoothStateReceiver != null) {
            unregisterReceiver(mBluetoothStateReceiver);
            mBluetoothStateReceiver = null;
        }
    }
}
