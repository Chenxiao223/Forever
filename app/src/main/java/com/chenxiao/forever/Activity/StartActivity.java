package com.chenxiao.forever.Activity;

import android.content.Intent;
import android.os.Bundle;

import com.chenxiao.forever.forever.R;

import java.util.Timer;
import java.util.TimerTask;

public class StartActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        initView();
    }

    private void initView() {
        final Intent it = new Intent(this, LoginActivity.class); //你要转向的Activity
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                startActivity(it); //执行
                finish();
            }
        };
        timer.schedule(task, 1000 * 2); //3秒后
    }
}
