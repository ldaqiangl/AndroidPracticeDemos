package com.dysania.bluetoothdemo;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import java.util.List;
import java.util.UUID;

/**
 * Created by DysaniazzZ on 20/03/2017.
 */

public class BluetoothLeService extends Service {

    private static final String TAG = BluetoothLeService.class.getSimpleName();

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public static final String ACTION_GATT_CONNECTED = "com.dysania.bluetoothdemo.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = "com.dysania.bluetoothdemo.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = "com.dysania.bluetoothdemo.ACTION_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_DATA_AVAILABLE = "com.dysania.bluetoothdemo.ACTION_DATA_AVAILABLE";
    public static final String EXTRA_DATA = "com.dysania.bluetoothdemo.EXTRA_DATA";

    public int mCurrentConnectState = STATE_DISCONNECTED;

    public static final UUID UUID_SMART_PILLBOX_NOTIFICATION = UUID
            .fromString(AppConstants.CUSTOM_CHARACTERISTICS__SMART_PILLBOX_NOTIFICATION_UUID);

    private final IBinder mBinder = new LocalBinder();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;

    public class LocalBinder extends Binder {

        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    private final BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {

        /**
         * 连接外围设备时回调
         *
         * @param gatt
         * @param status
         * @param newState
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mCurrentConnectState = STATE_CONNECTED;
                Log.e(TAG, "Connected to GATT server");
                //扫描包含的service
                boolean discoverStarted = gatt.discoverServices();
                Log.e(TAG, "Attempted to start service discovery: " + discoverStarted);
            } else {
                intentAction = ACTION_GATT_DISCONNECTED;
                mCurrentConnectState = STATE_DISCONNECTED;
                Log.e(TAG, "Disconnected from GATT server");
            }
            broadcastUpdate(intentAction);
        }

        /**
         * 扫描外围设备service时回调
         *
         * @param gatt
         * @param status
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.e(TAG, "onServicesDiscovered received: " + status);
            }
        }

        /**
         * 读取外围设备characteristic时回调
         *
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        /**
         * 外围设备characteristic更新时回调
         *
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        //获取特征值里的数据
        byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            intent.putExtra(EXTRA_DATA, new String(data));
            sendBroadcast(intent);
        }
    }

    /**
     * 初始化蓝牙
     */
    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter");
            return false;
        }

        return true;
    }

    /**
     * 连接到蓝牙设备
     */
    public boolean connect(String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.e(TAG, "BluetoothAdapter not initialized or unspecified address");
            return false;
        }

        //如果之前有连接的设备，尝试重连
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            Log.e(TAG, "Trying to use an existing mBluetoothGatt for connection");
            if (mBluetoothGatt.connect()) {
                mCurrentConnectState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.e(TAG, "Device not found. Unable to connect");
            return false;
        }

        //这里我们希望直接连接到设备，所以将autoConnect参数设置为false
        mBluetoothGatt = device.connectGatt(this, false, mBluetoothGattCallback);
        Log.e(TAG, "Trying to create a new connection");
        mBluetoothDeviceAddress = address;
        mCurrentConnectState = STATE_CONNECTING;
        return true;
    }

    /**
     * 关闭蓝牙连接(包含已经连接或正在连接)，结果将回调到BluetoothGattCallback#onConnectionStateChange
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * 使用完蓝牙设备后，要及时释放资源
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * 读取特征值，结果将回调到BluetoothGattCallback#onCharacteristicRead
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * 设置通知的可用性
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enable) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enable);

        if (UUID_SMART_PILLBOX_NOTIFICATION.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic
                    .getDescriptor(UUID.fromString(AppConstants.PRIMARY_DESCRIPTOR_CLIENT_CONFIG));
            descriptor.setValue(
                    enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * 获取设备上的所有service，要在调用BluetoothGatt#discoverServices()后再获取
     */
    public List<BluetoothGattService> getSupportGattServices() {
        if (mBluetoothGatt == null) {
            return null;
        }
        return mBluetoothGatt.getServices();
    }
}
