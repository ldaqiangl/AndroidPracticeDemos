package com.example.dysania.umengdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by dysaniazzz on 2016/10/31.
 */

public class SecondActivity extends BaseActivity implements View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        findViewById(R.id.btn_second_tomain).setOnClickListener(this);
        findViewById(R.id.btn_second_goback).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_second_tomain:
                startActivity(new Intent(mContext, MainActivity.class));
                break;
            case R.id.btn_second_goback:
                finish();
                break;
        }
    }

}
