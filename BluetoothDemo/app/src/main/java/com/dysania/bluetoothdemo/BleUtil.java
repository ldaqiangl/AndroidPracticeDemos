package com.dysania.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by DysaniazzZ on 17/03/2017.
 */
public class BleUtil {

    /**
     * 检查设备是否支持低功耗蓝牙
     *
     * @param context
     * @return
     */
    public static boolean checkBleSupport(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * 检查设备是否开启蓝牙
     *
     * @param bluetoothAdapter
     * @return
     */
    public static boolean checkBleEnabled(BluetoothAdapter bluetoothAdapter) {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }
}
