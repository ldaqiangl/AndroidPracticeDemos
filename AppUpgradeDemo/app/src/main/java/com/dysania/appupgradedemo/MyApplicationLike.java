package com.dysania.appupgradedemo;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.support.multidex.MultiDex;
import android.view.View;

import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;
import com.tencent.bugly.beta.ui.UILifecycleListener;
import com.tencent.tinker.loader.app.DefaultApplicationLike;

/**
 * Created by DysaniazzZ on 2016/11/4.
 */

public class MyApplicationLike extends DefaultApplicationLike {

    private static final String APP_ID = "7f9230f956";

    public MyApplicationLike(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent, Resources[] resources, ClassLoader[] classLoader, AssetManager[] assetManager) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent, resources, classLoader, assetManager);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initBugly();
    }

    private void initBugly() {
        /* 一些高级功能 */

        /*自动初始化开关*/
        Beta.autoInit = true;           //true表示app启动自动初始化升级模块，false不会自动初始化。如果担心启动速度，可以设置为false，在后面某个时刻手动调用Beta.init(getApplicationContext(), false);

        /*自动检查更新开关*/
        Beta.autoCheckUpgrade = false;   //true表示初始化自动检查升级，false则不会自动检查升级，需要手动调用Beta.checkUpgrade()方法

        /*升级检查周期设置*/
        Beta.initDelay = 1 * 1000;      //设置启动延迟为1s(默认为3s)，app启动1s后初始化SDK，避免影响app启动速度

        /*设置通知栏大图标*/
        Beta.largeIconId = R.mipmap.ic_launcher;

        /*设置更新弹窗默认展示的banner*/
        Beta.defaultBannerId = R.mipmap.ic_launcher;    //这个配置的默认的本地的图片资源的id，当后台配置的banner拉取失败时显示此banner，默认不设置则展示loading...

        /*设置sd卡的Download为更新资源存储目录*/
        Beta.storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        /*设置开启显示打断策略*/
        Beta.showInterruptedStrategy = true;            //设置点击过确认的弹窗在app下次启动自动检查更新时会再次显示

        /*添加可显示弹窗的Activiy*/
        Beta.canShowUpgradeActs.add(MainActivity.class);//只有添加过的Activity页面上才会出现升级提示的弹窗

        /*设置自定义升级对话框布局*/
        Beta.upgradeDialogLayoutId = R.layout.upgrade_dialog;   //注意为了保持接口统一，需要有统一的命名规则

        /*设置自定义tip弹窗UI布局*/
        Beta.tipsDialogLayoutId = R.layout.tips_dialog;         //同上

        /*设置升级对话框生命周期回调接口*/
        Beta.upgradeDialogLifecycleListener = new UILifecycleListener<UpgradeInfo>() {
            @Override
            public void onCreate(Context context, View view, UpgradeInfo upgradeInfo) {

            }

            @Override
            public void onStart(Context context, View view, UpgradeInfo upgradeInfo) {

            }

            @Override
            public void onResume(Context context, View view, UpgradeInfo upgradeInfo) {
                //注：可通过这个回调方式获取布局的控件，如果设置了id，可通过findViewById方式获取，如果设置了tag，可以通过findViewWithTag，具体参考下面例子:

                //通过id方式获取控件，并更改imageview图片
                //ImageView imageView = (ImageView) view.findViewById(R.id.icon);
                //imageView.setImageResource(R.mipmap.ic_launcher);

                //通过tag方式获取控件，并更改布局内容
                //TextView textView = (TextView) view.findViewWithTag("textview");
                //textView.setText("my custom text");

                //更多的操作：比如设置控件的点击事件
                //imageView.setOnClickListener(new View.OnClickListener() {
                //    @Override
                //    public void onClick(View v) {
                //       Intent intent = new Intent(getApplicationContext(), OtherActivity.class);
                //        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //        startActivity(intent);
                //    }
                //});
            }

            @Override
            public void onPause(Context context, View view, UpgradeInfo upgradeInfo) {

            }

            @Override
            public void onStop(Context context, View view, UpgradeInfo upgradeInfo) {

            }

            @Override
            public void onDestroy(Context context, View view, UpgradeInfo upgradeInfo) {

            }
        };

        /*初始化统一接口，该方法自动检查更新*/
        //arg0:上下文对象；arg1:注册时申请的APPID；arg2:是否开启debug模式
        Bugly.init(getApplication(), APP_ID, false);
        //如需增加自动检测更新的时机可以使用下面的方法
        //arg0:isManual用户手动点击检测；arg1:isSilence是否显示弹框等交互
        Beta.checkUpgrade(false, true);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        //必须先安装多Dex支持
        MultiDex.install(base);
        //安装Tinker
        //TinkerManager.installTinker(this);
        Beta.installTinker(this);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void registerActivityLifecycleCallback(Application.ActivityLifecycleCallbacks callbacks) {
        getApplication().registerActivityLifecycleCallbacks(callbacks);
    }
}
