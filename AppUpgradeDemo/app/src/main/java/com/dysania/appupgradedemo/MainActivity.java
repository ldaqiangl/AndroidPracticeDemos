package com.dysania.appupgradedemo;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;

/**
 * Created by DysaniazzZ on 2016/11/4.
 */

public class MainActivity extends AppCompatActivity {

    private int mVersionCode = 0;
    private String mVersionName = null;
    private String mChannel = null;
    private UpgradeInfo mUpgradeInfo;
    private boolean mHasNew = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
    }

    private void initData() {
        try {
            PackageManager packageManager = getPackageManager();
            String packageName = getPackageName();
            int flags = PackageManager.GET_META_DATA;
            //获取应用信息
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, flags);
            //获取版本信息
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            //获取VersionCode和VersionName
            mVersionCode = packageInfo.versionCode;
            mVersionName = packageInfo.versionName;
            //获取渠道信息
            mChannel = applicationInfo.metaData.getString("channel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //获取升级信息（只要没有新版本就是null)
        mUpgradeInfo = Beta.getUpgradeInfo();
    }

    private void initView() {
        TextView tvAppInfo = (TextView) findViewById(R.id.tv_app_info);
        tvAppInfo.setText("VersionCode: " + mVersionCode + "\n" + "VersionName: " + mVersionName + "\n" + "Channel: " + mChannel);
        RelativeLayout rlMainSettins = (RelativeLayout) findViewById(R.id.rl_main_settings);
        ImageView ivMainReddot = (ImageView) findViewById(R.id.iv_main_reddot);

        rlMainSettins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsActivity.actionStart(MainActivity.this, mHasNew);
            }
        });

        if(mUpgradeInfo != null) {
            int netVersionCode = mUpgradeInfo.versionCode;
            if(netVersionCode > mVersionCode) {
                mHasNew = true;
                ivMainReddot.setVisibility(View.VISIBLE);   //显示小红点
                if(netVersionCode - mVersionCode >= 2) {
                    Beta.checkUpgrade();                    //弹出更新提示框
                }
            } else {
                //防止更新版本后进来小红点还是出现的情况
                ivMainReddot.setVisibility(View.GONE);
            }
        } else {
            ivMainReddot.setVisibility(View.GONE);
        }
    }
}
