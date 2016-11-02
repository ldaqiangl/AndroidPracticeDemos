package com.example.dysania.umengdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

/**
 * Created by dysaniazzz on 2016/10/31.
 */

public class MainActivity extends BaseActivity implements View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_main_tosecond).setOnClickListener(this);
        findViewById(R.id.btn_main_showfragment).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_main_tosecond:
                startActivity(new Intent(mContext, SecondActivity.class));
                break;
            case R.id.btn_main_showfragment:
                displayFragment();
                break;
        }
    }

    private void displayFragment() {
        DemoFragment fragment = new DemoFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_main_replace, fragment);
        fragmentTransaction.commit();
    }

}
