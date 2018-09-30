package com.example.bit_user.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by bit-user on 2017-10-13.
 */

public class NetworkChangeReceiver extends BroadcastReceiver{
    Context context;
    String status;
    @Override
    public void onReceive(Context context1, Intent intent) {

        try{
            context=context1;
            String action = intent.getAction();
            status = NetworkUtil.getConnectivityStatusString(context1);
            if("네트워크가 연결되지 않아 종료됩니다.".equals(status))
                Toast.makeText(context.getApplicationContext(), "네트워크가 연결되지 않았습니다.", Toast.LENGTH_SHORT).show();
            else if("wifi로 바꾸어 주세요.".equals(status))
                Toast.makeText(context, "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();

            Callee callee = new Callee();
            callee.doWork(mCallback);

            Callee2 callee2 = new Callee2();
            callee2.doWork(mCallback2);

            //toast();
            //run();
        /*if("네트워크가 연결되지 않아 종료됩니다.".equals(status)){


        }
        else if("wifi로 바꾸어 주세요.".equals(status)){

        }*/
        }catch (NullPointerException e){
            Toast.makeText(context.getApplicationContext(), "네트워크가 연결되지 않아 종료됩니다.", Toast.LENGTH_SHORT).show();
        }

    }
    Callback mCallback = new Callback() {
        @Override
        public void callback() {
            //System.exit(0);
        }
    };

    Callback mCallback2 = new Callback() {
        @Override
        public void callback() {
           // android.os.Process.killProcess(android.os.Process.myPid());
        }
    };

   /* @Override
    public void run() {
        try {
            //toast();
            System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
            Thread.sleep(2000);
            android.os.Process.killProcess(android.os.Process.myPid());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }*/

    public void toast(){
        if("네트워크가 연결되지 않아 종료됩니다.".equals(status))
            Toast.makeText(context.getApplicationContext(), "네트워크가 연결되지 않아 종료됩니다.", Toast.LENGTH_SHORT).show();
        else if("wifi로 바꾸어 주세요.".equals(status))
            Toast.makeText(context, "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
    }

    public class Callee{
        public void Callee(){}
        public void doWork(Callback mCallback){
            //Toast.makeText(context.getApplicationContext(), "네트워크가 연결되지 않아 종료됩니다.", Toast.LENGTH_SHORT).show();
            //android.os.Process.killProcess(android.os.Process.myPid());
            //System.exit(0);
            mCallback.callback();
        }
    }

    public class Callee2{
        public void Callee2(){}
        public void doWork(Callback mCallback){
            //android.os.Process.killProcess(android.os.Process.myPid());
            //System.exit(0);
            mCallback2.callback();
        }
    }
}