package com.example.bit_user.sms;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MyhomeActivity extends AppCompatActivity {
    private String userToken;
    private String flag;

    RadioButton radioGraph;
    RadioButton radioCalendar;
    RadioButton radioList;

    Button btnCancel;
    Button btnApply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myhome);

        userToken = getIntent().getStringExtra("userToken");
        onClickRadio();
        onClickbtn();
    }

    public void onClickRadio(){
        radioGraph=(RadioButton)findViewById(R.id.radioGraph);
        radioCalendar=(RadioButton)findViewById(R.id.radioCalendar);
        radioList=(RadioButton)findViewById(R.id.radioList);

        radioGraph.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                flag="0";

            }
        });
        radioCalendar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                flag="1";

            }
        });
        radioList.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                flag="2";

            }
        });
    }

    public void onClickbtn(){
        btnCancel=(Button)findViewById(R.id.btnCancel);
        btnApply = (Button)findViewById(R.id.btnApply);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SendRequestChangeHome().execute();
            }
        });
    }


    private class SendRequestChangeHome extends AsyncTask<Void,Void,String> { //background,progress,execcute

        String url= new ApiHost().getApi()+"profile/home";//"http://192.168.1.21:9990/profile/home";
        String message;
        String token;
        String mainPosition;

        @Override
        protected String doInBackground(Void... params) {
            String status=NetworkUtil.getConnectivityStatusString(MyhomeActivity.this);
            if("네트워크가 연결되지 않아 종료됩니다.".equals(status)){
                //Toast.makeText(getApplicationContext(), "네트워크가 연결되지 않았습니다.", Toast.LENGTH_SHORT).show();
                //android.os.Process.killProcess(android.os.Process.myPid());
                //ActivityCompat.finishAffinity(MainActivity.this);
                message = status;
                return message;
            }
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPut put = new HttpPut(url);

                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("token",userToken));
                nameValuePairs.add(new BasicNameValuePair("flag",flag));

                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
                put.setEntity(ent);

                //전송
                HttpResponse httpResponse = httpclient.execute(put);

                //응답
                HttpEntity resEntity = httpResponse.getEntity();


                //토큰 혹은 false를 message에 담고서 비교
                message= EntityUtils.toString(resEntity);
                System.out.println(message);



            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (HttpHostConnectException e) {
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }

            return message;
        }


        @Override
        protected void onPostExecute(String s) {
            if(message.equals("네트워크가 연결되지 않아 종료됩니다.")){
                Toast.makeText(getApplicationContext(), "네트워크가 연결되지 않아 종료됩니다.", Toast.LENGTH_SHORT).show();
                ActivityCompat.finishAffinity(MyhomeActivity.this);
            }else if(message.equals("wifi로 바꾸어 주세요.")){
                Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                SharedPreferences preferences;
                SharedPreferences.Editor editor;

                preferences = getSharedPreferences("tokenAndHome", Context.MODE_PRIVATE);
                editor = preferences.edit();

                editor.putString("home",flag);
                editor.commit();

                Intent intent = new Intent(MyhomeActivity.this,MainActivity.class);
                intent.putExtra("userToken",userToken);
                startActivity(intent);

                MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;
                mainActivity.finish();

                finish();
            }


        }
    }

}
