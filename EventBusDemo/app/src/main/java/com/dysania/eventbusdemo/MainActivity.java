package com.dysania.eventbusdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by DysaniazzZ on 2016/11/3.
 */

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        findViewById(R.id.btn_main_tosecond).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, SecondActivity.class));
            }
        });
    }
}
