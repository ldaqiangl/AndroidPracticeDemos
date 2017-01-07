package com.dysania.umengdemo;

import android.support.v4.app.Fragment;

import com.umeng.analytics.MobclickAgent;

/**
 * Created by dysaniazzz on 2016/10/31.
 */

public class BaseFragment extends Fragment {

    public String mClassName = getClass().getSimpleName();

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mClassName);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mClassName);
    }

}
