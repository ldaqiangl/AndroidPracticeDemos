package com.example.dysania.umengdemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.umeng.analytics.MobclickAgent;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by dysaniazzz on 2016/10/31.
 */

public class UmengUtils {

    //EventId，用来自定义一些事件
    public static final String UMENG_ID_XXX = "XXX";

    //Key，比如定义某件事的状态，state、status等等
    public static final String UMENG_KEY_XXX = "XXX";

    //Value，比如定义某件事的结果，success，fail
    public static final String UMENG_VALUE_XXX = "XXX";

    /**
     * 统计自定义事件
     *
     * @param context 上下文
     * @param eventId 事件Id
     */
    public static void onEvent(Context context, String eventId) {
        MobclickAgent.onEvent(context, eventId);
    }

    /**
     * 统计自定义事件
     *
     * @param context 上下文
     * @param eventId 事件Id
     * @param hashMap 参数的Map
     */
    public static void onEvent(Context context, String eventId, HashMap hashMap) {
        MobclickAgent.onEvent(context, eventId, hashMap);
    }

    /**
     * 统计自定义事件
     *
     * @param context 上下文
     * @param eventId 事件的Id
     * @param key     参数的Id
     * @param value   参数的Map
     */
    public static void onEvent(Context context, String eventId, String key, String value) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(key, value);
        onEvent(context, eventId, hashMap);
    }

    //动态检查获取权限
    public static boolean checkPermission(Context context, String permission) {
        boolean result = false;
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Class<?> clazz = Class.forName("android.content.Context");
                Method method = clazz.getMethod("checkSelfPermission", String.class);
                int rest = (Integer) method.invoke(context, permission);
                if (rest == PackageManager.PERMISSION_GRANTED) {
                    result = true;
                } else {
                    result = false;
                }
            } catch (Exception e) {
                result = false;
            }
        } else {
            PackageManager pm = context.getPackageManager();
            if (pm.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                result = true;
            }
        }
        return result;
    }

    //获取 Mac 地址和 IMEI 序列号
    public static String getDeviceInfo(Context context) {
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String device_id = null;
            if (checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                device_id = tm.getDeviceId();
            }
            String mac = null;
            FileReader fstream = null;
            try {
                fstream = new FileReader("/sys/class/net/wlan0/address");
            } catch (FileNotFoundException e) {
                fstream = new FileReader("/sys/class/net/eth0/address");
            }
            BufferedReader in = null;
            if (fstream != null) {
                try {
                    in = new BufferedReader(fstream, 1024);
                    mac = in.readLine();
                } catch (IOException e) {
                } finally {
                    if (fstream != null) {
                        try {
                            fstream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            json.put("mac", mac);
            if (TextUtils.isEmpty(device_id)) {
                device_id = mac;
            }
            if (TextUtils.isEmpty(device_id)) {
                device_id = android.provider.Settings.Secure.getString(context.getContentResolver(),
                        android.provider.Settings.Secure.ANDROID_ID);
            }
            json.put("device_id", device_id);
            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
