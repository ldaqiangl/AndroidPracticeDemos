package com.dysania.appupgradedemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.tencent.bugly.beta.Beta;

/**
 * Created by DysaniazzZ on 2016/11/4.
 */

public class SettingsActivity extends AppCompatActivity {

    public static final String HAS_NEW = "has_new";

    public static void actionStart(Context context, boolean hasNew) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.putExtra(HAS_NEW, hasNew);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        init();
    }

    private void init() {
        boolean hasNew = getIntent().getBooleanExtra(HAS_NEW, false);
        ImageView ivSettingsReddot = (ImageView) findViewById(R.id.iv_settings_reddot);
        findViewById(R.id.rl_settings_checkupgrade).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Beta.checkUpgrade();
            }
        });
        if(hasNew) {
            ivSettingsReddot.setVisibility(View.VISIBLE);
        } else {
            ivSettingsReddot.setVisibility(View.GONE);
        }
    }
}
