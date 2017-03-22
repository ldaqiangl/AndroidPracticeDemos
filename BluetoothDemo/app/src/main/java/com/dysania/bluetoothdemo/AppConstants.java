package com.dysania.bluetoothdemo;

import java.util.HashMap;

/**
 * Created by DysaniazzZ on 20/03/2017.
 */
public class AppConstants {

    public static final String DEVICE_NAME = "Smart pill box";

    private static HashMap<String, String> mAttributes = new HashMap<>();

    public static final String PRIMARY_DESCRIPTOR_CLIENT_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";                   //设备的配置项

    /**
     * Battery Level
     */
    public static final String PRIMARY_SERVICE_BATTERY_UUID = "0000180f-0000-1000-8000-00805f9b34fb";                       //设备电量
    public static final String PRIMARY_CHARACTERISTIC_BATTERY_LEVEL_UUID = "00002a19-0000-1000-8000-00805f9b34fb";          //电量百分比

    /**
     * Manufacture Name
     */
    public static final String PRIMARY_SERVICE_DEVICE_INFORMATION_UUID = "0000180a-0000-1000-8000-00805f9b34fb";            //设备信息
    public static final String PRIMARY_CHARACTERISTICS_DEVICE_INFORMATION_UUID = "00002a29-0000-1000-8000-00805f9b34fb";    //制造商信息
    public static final String PRIMARY_CHARACTERISTICS_DEVICE_MODEL_NUMBER_UUID = "00002a24-0000-1000-8000-00805f9b34fb";   //模型编号
    public static final String PRIMARY_CHARACTERISTICS_DEVICE_SYSTEM_ID_UUID = "00002a23-0000-1000-8000-00805f9b34fb";      //系统ID

    /**
     * Custom Data Translation
     */
    public static final String CUSTOM_SERVICE_SMART_PILLBOX_UUID = "00000001-1212-efde-1523-785feabcd123";                               //小药盒
    public static final String CUSTOM_CHARACTERISTICS_SMART_PILLBOX_WRITE_DATA_UUID = "00000002-1212-efde-1523-785feabcd123";            //小药盒写数据
    public static final String CUSTOM_CHARACTERISTICS_SMART_PILLBOX_NOTIFICATION_UUID = "00000003-1212-efde-1523-785feabcd123";          //小药盒收通知

    static {
        //Services
        mAttributes.put(PRIMARY_SERVICE_BATTERY_UUID, "Battery Service");
        mAttributes.put(PRIMARY_SERVICE_DEVICE_INFORMATION_UUID, "Device Information Service");

        mAttributes.put(CUSTOM_SERVICE_SMART_PILLBOX_UUID, "Smart Pillbox Service");
        //Characteristics
        mAttributes.put(PRIMARY_CHARACTERISTIC_BATTERY_LEVEL_UUID, "Battery Level");
        mAttributes.put(PRIMARY_CHARACTERISTICS_DEVICE_INFORMATION_UUID, "Manufacturer Name String");
        mAttributes.put(PRIMARY_CHARACTERISTICS_DEVICE_MODEL_NUMBER_UUID, "Model Number String");
        mAttributes.put(PRIMARY_CHARACTERISTICS_DEVICE_SYSTEM_ID_UUID, "System ID");

        mAttributes.put(CUSTOM_CHARACTERISTICS_SMART_PILLBOX_WRITE_DATA_UUID, "Write Data");
        mAttributes.put(CUSTOM_CHARACTERISTICS_SMART_PILLBOX_NOTIFICATION_UUID, "Medicine Notification");
        //Descriptors
        mAttributes.put(PRIMARY_DESCRIPTOR_CLIENT_CONFIG, "Notification Descriptor Config");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = mAttributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
