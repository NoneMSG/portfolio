package com.example.bit_user.sms;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;


public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //타이틀바 숨기기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //풀 스크린
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                finish();
            }
        }, 1000);// 2 초
    }
}
