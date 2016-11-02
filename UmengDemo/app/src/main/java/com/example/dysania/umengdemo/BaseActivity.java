package com.example.dysania.umengdemo;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.umeng.analytics.MobclickAgent;

/**
 * Created by dysaniazzz on 2016/10/31.
 */

public class BaseActivity extends AppCompatActivity {

    public Context mContext;
    public String mClassName = getClass().getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mClassName);  //统计页面（仅有Activity的应用中SDK自动调用，不需要单独写）
        MobclickAgent.onResume(mContext);       //统计时长
    }

    @Override
    protected void onPause() {
        super.onPause();
        //保证onPageEnd在onPause之前调用，因为onPause中会保存信息
        MobclickAgent.onPageEnd(mClassName);
        MobclickAgent.onPause(mContext);
    }

}
