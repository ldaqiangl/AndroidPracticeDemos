package com.dysania.umengdemo;

import android.app.Application;

import com.umeng.analytics.MobclickAgent;

/**
 * Created by dysaniazzz on 2016/10/31.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initUmeng();
    }

    private void initUmeng() {
        //在代码里设置AppKey和Channel
        //MobclickAgent.startWithConfigure(new MobclickAgent.UMAnalyticsConfig(getApplicationContext(), appkey, channelId, eType, isCrashEnable));

        //禁止默认的页面统计方式（默认只计算Activity，不计算Fragment和View)
        MobclickAgent.openActivityDurationTrack(false);

        //打开调试模式
        MobclickAgent.setDebugMode(BuildConfig.DEBUG);
    }

}
