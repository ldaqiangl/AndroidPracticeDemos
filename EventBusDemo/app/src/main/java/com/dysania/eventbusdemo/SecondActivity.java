package com.dysania.eventbusdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by DysaniazzZ on 2016/11/3.
 */

public class SecondActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        init();
    }

    private void init() {
        findViewById(R.id.btn_second_tomain).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, MainActivity.class));
            }
        });
        //Step 3: Post events
        findViewById(R.id.btn_second_closeall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new MessageEvent("Hello EventBus!"));
            }
        });
    }
}
