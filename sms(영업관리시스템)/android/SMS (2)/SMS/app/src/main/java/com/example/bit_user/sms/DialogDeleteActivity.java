package com.example.bit_user.sms;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class DialogDeleteActivity extends AppCompatActivity {
    SharedPreferences preferences;
    private String userToken;
    private String userNo;
    private String index;
    private String position;
    private String flag;
    private String commentNo;

    Button btnOk;
    Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = getSharedPreferences("tokenAndHome",MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_delete);

        userToken = getIntent().getStringExtra("userToken");
        userNo = getIntent().getStringExtra("userNo");
        index = getIntent().getStringExtra("index");
        position= getIntent().getStringExtra("position");
        if ("day".equals(position)) {
            flag=getIntent().getStringExtra("flag");
            commentNo=getIntent().getStringExtra("commentNo");
            System.out.println(flag+"///////"+commentNo);
        }
        System.out.println(position);

        btnOk=(Button)findViewById(R.id.btnOk);
        btnCancel=(Button)findViewById(R.id.btnCancel);

        //상세보기창으로 되돌아 가기
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //삭제하고 메인으로 돌아가기
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SendRequestDeleteWeek().execute();
            }
        });
    }


    // 주간보고서 삭제
    private class SendRequestDeleteWeek extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url;
        String message;

        @Override
        protected String doInBackground(Void... params) {
            String status=NetworkUtil.getConnectivityStatusString(DialogDeleteActivity.this);
            if("네트워크가 연결되지 않아 종료됩니다.".equals(status)){
                //Toast.makeText(getApplicationContext(), "네트워크가 연결되지 않았습니다.", Toast.LENGTH_SHORT).show();
                //android.os.Process.killProcess(android.os.Process.myPid());
                //ActivityCompat.finishAffinity(MainActivity.this);
                message = status;
                return message;
            }else if("wifi로 바꾸어 주세요.".equals(status)){
                Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
                return status;
            }else{
                switch (position){
                    case "week":{
                        url=new ApiHost().getApi()+"delete/weekplanner";
                        break;
                    }
                    case "day":{
                        url= new ApiHost().getApi()+"delete/dayreport";//"http://192.168.1.21:9990/delete/dayreport";
                        break;
                    }
                    case "interview":{
                        url= new ApiHost().getApi()+"delete/consultation";//"http://192.168.1.21:9990/delete/consultation";
                        break;
                    }
                }
            }


            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url);

                //아이디와 비밀번호 묶음

                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(3);
                if("day".equals(position)){

                    System.out.println("Ddddddddddddddddddddddddddd"+flag);
                    nameValuePairs.add(new BasicNameValuePair("flag",flag));
                    nameValuePairs.add(new BasicNameValuePair("commentNo",commentNo));
                }
                nameValuePairs.add(new BasicNameValuePair("token",userToken));
                nameValuePairs.add(new BasicNameValuePair("userNo",userNo));
                nameValuePairs.add(new BasicNameValuePair("no",index));

                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
                post.setEntity(ent);
                System.out.println("삭제2");
                //전송
                HttpResponse httpResponse = httpclient.execute(post);

                //응답
                HttpEntity resEntity = httpResponse.getEntity();

                //토큰 혹은 false를 message에 담고서 비교
                message= EntityUtils.toString(resEntity);
                System.out.println("요청내용 전달받음 : "+message);


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (HttpHostConnectException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return message;
        }


        @Override
        protected void onPostExecute(String s) {
            if(message.equals("네트워크가 연결되지 않아 종료됩니다.")){
                Toast.makeText(getApplicationContext(), "네트워크가 연결되지 않아 종료됩니다.", Toast.LENGTH_SHORT).show();
                ActivityCompat.finishAffinity(DialogDeleteActivity.this);
            }else if(message.equals("wifi로 바꾸어 주세요.")){
                Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
            }else{
                if("linked with dayreport".equals(message)){
                    Toast.makeText(getApplicationContext(),"?곌껐??蹂닿퀬?쒓? ?덉뼱 ??젣?????놁뒿?덈떎.",Toast.LENGTH_SHORT).show();

                }else{
                    Intent intent = new Intent(DialogDeleteActivity.this, MainActivity.class);
                    intent.putExtra("userToken",userToken);
                    //intent.putExtra("mainPosition",preferences.getString("home",""));
                    startActivity(intent);

                    switch (position){
                        case "week":{
                            ReadWeeklyActivity rwaActivity = (ReadWeeklyActivity)ReadWeeklyActivity.rwaActivity;
                            rwaActivity.finish();
                            break;
                        }
                        case "day":{
                            ReadDailyActivity rdaActivity = (ReadDailyActivity)ReadDailyActivity.rdaActivity;
                            rdaActivity.finish();
                            break;
                        }
                        case "interview":{
                            ReadInterviewActivity riaActivity = (ReadInterviewActivity)ReadInterviewActivity.riaActivity;
                            riaActivity.finish();
                            break;
                        }
                    }


                    finish();
                }
            }

        }
    }


}
