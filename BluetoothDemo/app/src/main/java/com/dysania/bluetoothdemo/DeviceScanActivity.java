package com.dysania.bluetoothdemo;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanFilter.Builder;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by DysaniazzZ on 14/03/2017.
 * 蓝牙设备扫描页面
 */
public class DeviceScanActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtnBluetoothState;
    private ListView mLvAvailableDevices;

    private Handler mHandler = new Handler();
    private boolean mScanning = false;
    private boolean mBluetoothEnabled = false;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private CustomScanCallback mCustomScanCallback;
    private AvailableDevicesAdapter mAvailableDevicesAdapter;

    private static final int REQUEST_PERMISSION_ACL = 0x01;
    private static final int REQUEST_ENABLE_BT = 0x02;
    private static final int SCAN_DEVICE_DURATION = 10000;
    private static final String TAG = DeviceScanActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);

        //检查设备是否支持BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            UIUtil.createToast(this, R.string.ble_not_support);
            finish();
        }

        //检查位置权限是否授予(6.0需要）
        checkAclPermission();

        //检查设备是否支持蓝牙
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            UIUtil.createToast(this, R.string.bluetooth_not_support);
            finish();
            return;
        }

        mBtnBluetoothState = (Button) findViewById(R.id.btn_bluetooth_state);
        mLvAvailableDevices = (ListView) findViewById(R.id.lv_available_devices);
        mBtnBluetoothState.setOnClickListener(this);
        mAvailableDevicesAdapter = new AvailableDevicesAdapter(this);
        mLvAvailableDevices.setAdapter(mAvailableDevicesAdapter);
        mLvAvailableDevices.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice bluetoothDevice = mAvailableDevicesAdapter.getItem(position);
                DeviceControlActivity.actionStart(DeviceScanActivity.this, bluetoothDevice.getName(), bluetoothDevice.getAddress());
            }
        });

        //检查蓝牙是否开启
        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        } else {
            changeBleState(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //注册蓝牙状态变化的广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothStateReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        //停止扫描
        try {
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                scanLeDeviceWithFilter(false);
            } else {
                scanLeDevice(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //清空数据
        mAvailableDevicesAdapter.clear();
    }

    @Override
    protected void onStop() {
        super.onStop();

        //注销广播，防止内存泄露
        if (mBluetoothStateReceiver != null) {
            unregisterReceiver(mBluetoothStateReceiver);
            mBluetoothStateReceiver = null;
        }
    }

    private void checkAclPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
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
                    changeBleState(true);
                } else {
                    //蓝牙未开启
                    changeBleState(false);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_bluetooth_state:
                if (mBluetoothEnabled) {
                    mAvailableDevicesAdapter.clear();
                    mAvailableDevicesAdapter.notifyDataSetChanged();
                    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                        scanLeDeviceWithFilter(true);
                    } else {
                        scanLeDevice(true);
                    }
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
            switch (bluetoothState) {
                case BluetoothAdapter.STATE_ON:
                    changeBleState(true);
                    UIUtil.createToast(context, R.string.bluetooth_is_on);
                    break;
                case BluetoothAdapter.STATE_OFF:
                    changeBleState(false);
                    UIUtil.createToast(context, R.string.bluetooth_is_off);
                    mAvailableDevicesAdapter.clear();
                    mAvailableDevicesAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    private void changeBleState(boolean enable) {
        mBluetoothEnabled = enable;
        if (enable) {
            mBtnBluetoothState.setText(R.string.bluetooth_start_scan);
            //判断设备版本，决定是否使用过滤扫描
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                mCustomScanCallback = new CustomScanCallback();
            }
        } else {
            mBtnBluetoothState.setText(R.string.bluetooth_open_bt);
        }
    }

    private void stopScanLeDevice() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            return;
        }
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            mBluetoothLeScanner.stopScan(mCustomScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    /**
     * 扫描蓝牙设备
     */
    private void scanLeDevice(boolean enable) {
        if (enable) {
            //经过预定扫描期后停止扫描
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBtnBluetoothState.setEnabled(true);
                    stopScanLeDevice();
                }
            }, SCAN_DEVICE_DURATION);
            mScanning = true;
            mBtnBluetoothState.setEnabled(false);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBtnBluetoothState.setEnabled(true);
            stopScanLeDevice();
        }
    }

    /**
     * 扫描蓝牙设备(可以定义扫描规则)
     */
    @TargetApi(VERSION_CODES.LOLLIPOP)
    private void scanLeDeviceWithFilter(boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBtnBluetoothState.setEnabled(true);
                    stopScanLeDevice();
                }
            }, SCAN_DEVICE_DURATION);
            mScanning = true;
            mBtnBluetoothState.setEnabled(false);
            ScanFilter scanFilter = new Builder().setDeviceName(AppConstants.DEVICE_NAME).build();
            ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
            ArrayList<ScanFilter> filters = new ArrayList<>();
            filters.add(scanFilter);
            mBluetoothLeScanner.startScan(filters, scanSettings, mCustomScanCallback);
        } else {
            mScanning = false;
            mBtnBluetoothState.setEnabled(true);
            stopScanLeDevice();
        }
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    public class CustomScanCallback extends ScanCallback {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            mAvailableDevicesAdapter.addItem(device);
            mAvailableDevicesAdapter.notifyDataSetChanged();
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
                BluetoothDevice device = result.getDevice();
                mAvailableDevicesAdapter.addItem(device);
            }
            mAvailableDevicesAdapter.notifyDataSetChanged();
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            UIUtil.createToast(DeviceScanActivity.this, getString(R.string.bluetooth_scan_error, errorCode));
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (AppConstants.DEVICE_NAME.equals(device.getName())) {
                        mAvailableDevicesAdapter.addItem(device);
                        mAvailableDevicesAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };
}
