package com.dysania.appupgradedemo;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;

/**
 * Created by DysaniazzZ on 2016/11/4.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTvMainAppInfo;
    private RelativeLayout mRlMainSettings;
    private ImageView mIvMainRedDot;
    private Button mBtnMainShowToast;
    private Button mBtnMainLoadPatch;
    private Button mBtnMainLoadLibrary;
    private Button mBtnMainKillSelf;

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
            //获取应用信息
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            //获取渠道信息
            mChannel = applicationInfo.metaData.getString("channel");
            //获取版本信息
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), packageManager.GET_CONFIGURATIONS);
            //获取VersionCode和VersionName
            mVersionCode = packageInfo.versionCode;
            mVersionName = packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        //获取升级信息（只要没有新版本就是null)
        mUpgradeInfo = Beta.getUpgradeInfo();
    }

    private void initView() {
        mTvMainAppInfo = (TextView) findViewById(R.id.tv_main_appInfo);
        mRlMainSettings = (RelativeLayout) findViewById(R.id.rl_main_settings);
        mIvMainRedDot = (ImageView) findViewById(R.id.iv_main_redDot);
        mBtnMainShowToast = (Button) findViewById(R.id.btn_main_showToast);
        mBtnMainLoadPatch = (Button) findViewById(R.id.btn_main_loadPatch);
        mBtnMainLoadLibrary = (Button) findViewById(R.id.btn_main_loadLibrary);
        mBtnMainKillSelf = (Button) findViewById(R.id.btn_main_killSelf);
        mRlMainSettings.setOnClickListener(this);
        mBtnMainShowToast.setOnClickListener(this);
        mBtnMainLoadPatch.setOnClickListener(this);
        mBtnMainLoadLibrary.setOnClickListener(this);
        mBtnMainKillSelf.setOnClickListener(this);

        mTvMainAppInfo.setText("VersionCode: " + mVersionCode + "\n" + "VersionName: " + mVersionName + "\n" + "Channel: " + mChannel);
        if(mUpgradeInfo != null) {
            int netVersionCode = mUpgradeInfo.versionCode;
            if(netVersionCode > mVersionCode) {
                mHasNew = true;
                mIvMainRedDot.setVisibility(View.VISIBLE);   //显示小红点
                if(netVersionCode - mVersionCode >= 2) {     //规则是比上一个版本大于等于2就认为是大版本更新
                    Beta.checkUpgrade();                     //弹出更新提示框
                }
            } else {
                //防止更新版本后进来小红点还是出现的情况
                mIvMainRedDot.setVisibility(View.GONE);
            }
        } else {
            mIvMainRedDot.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_main_settings:
                SettingsActivity.actionStart(MainActivity.this, mHasNew);
                break;
            case R.id.btn_main_showToast:
                //测试热更新功能
                testToast();
                break;
            case R.id.btn_main_loadPatch:
                //本地加载补丁测试
                Beta.applyTinkerPatch(getApplicationContext(), Environment.getExternalStorageDirectory() + "/patch_signed_7zip.apk");
                break;
            case R.id.btn_main_loadLibrary:
                //本地加载so测试
                //Beta.loadArmLibrary(getApplicationContext(), "stlport_shared");
                break;
            case R.id.btn_main_killSelf:
                //杀死进程
                android.os.Process.killProcess(android.os.Process.myPid());
                break;
        }
    }

    /**
     * 根据应用patch包前后来测试是否应用patch包成功
     *
     * 应用前提示：This is a bug class
     * 应用后提示：The bug has fixed
     */
    private void testToast() {
        Toast.makeText(this, LoadBugClass.getBugString(), Toast.LENGTH_SHORT).show();
    }
}
