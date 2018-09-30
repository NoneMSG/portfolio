package com.example.bit_user.sms;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    BroadcastReceiver br;
    ApiHost apihost = new ApiHost();
    static String id;
    static String password;
    String[] info = new String[2];

    static EditText editText_id;
    static EditText editText_pw;
    private String uniqueToken;
    @Override
    protected void onResume() {
        super.onResume();
        FirebaseInstanceId.getInstance().getToken();
        uniqueToken = FirebaseInstanceId.getInstance().getToken();
        System.out.println("=======uniqueToken==========================="+uniqueToken);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //splashActivity
        //타이틀바 숨기기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //풀 스크린
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        startActivity(new Intent(this, SplashActivity.class));
        setTheme(R.style.SMSTheme);

        super.onCreate(savedInstanceState);

        //#############네트워크 검사#####################
        String status=NetworkUtil.getConnectivityStatusString(this);
        if("네트워크가 연결되지 않아 종료됩니다.".equals(status)){
            Toast.makeText(getApplicationContext(), "네트워크가 연결되지 않아 종료됩니다.", Toast.LENGTH_SHORT).show();
            //android.os.Process.killProcess(android.os.Process.myPid());
            ActivityCompat.finishAffinity(this);
            return;
        }
        else if("wifi로 바꾸어 주세요.".equals(status)){
            //Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
        }


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setDisplayShowHomeEnabled(true);


        SharedPreferences token =  getSharedPreferences("tokenAndHome",MODE_PRIVATE);
        String validationToken = token.getString("token","no");
        System.out.println("토큰이 있음  "+validationToken);

        if( !validationToken.equals("no") ){

            new Uid(validationToken).execute();
        }


        editText_id = (EditText) findViewById(R.id.id);
        editText_pw = (EditText) findViewById(R.id.password);

        Button btnSend = (Button)findViewById(R.id.send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                id = editText_id.getText().toString();
                password = editText_pw.getText().toString();
                if(password.isEmpty()==true) {
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요.", Toast.LENGTH_LONG).show();
                }
                else if(id.isEmpty()==true) {
                    Toast.makeText(getApplicationContext(), "이메일을 입력해주세요.", Toast.LENGTH_LONG).show();
                }
                else{
                    //############비밀번호 길이 제한
                    if(password.length()<6 || password.length()>12)
                        Toast.makeText(getApplicationContext(), "비밀번호는 6~12자로 제한됩니다.", Toast.LENGTH_LONG).show();
                    else
                        new SendPost().execute();
                }

            }
        });
    }

    private class Uid extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url= apihost.getApi()+"uidcheck"; //"http://192.168.1.21:9990/uidcheck";
        String message;
        String token;
        String fcmkey;
        SharedPreferences preferences;

        public Uid(String validationToken) {
            token = validationToken;
            preferences = getSharedPreferences("tokenAndHome", Context.MODE_PRIVATE);
            fcmkey = preferences.getString("fcmkey","0");
            System.out.println("checkauth fcmy"+fcmkey);
        }

        @Override
        protected String doInBackground(Void... params) {
            String status=NetworkUtil.getConnectivityStatusString(LoginActivity.this);
            if("네트워크가 연결되지 않아 종료됩니다.".equals(status)){
                //Toast.makeText(getApplicationContext(), "네트워크가 연결되지 않았습니다.", Toast.LENGTH_SHORT).show();
                //android.os.Process.killProcess(android.os.Process.myPid());
                //ActivityCompat.finishAffinity(MainActivity.this);
                message = status;
                return message;
            }
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url);

                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("token",token));
                nameValuePairs.add(new BasicNameValuePair("fcmkey",fcmkey));

                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
                post.setEntity(ent);

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
                ActivityCompat.finishAffinity(LoginActivity.this);
            }else{
                //message에 따라서 결과과
                if(message.equals("accept")){
                    Toast.makeText(getApplicationContext(),"로그인 되었습니다.",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("userToken",token);
                    startActivity(intent);
                    System.exit(0);
                }else{
                    Toast.makeText(getApplicationContext(),"다른 기기에서 로그인되어있습니다. 새로 로그인 해주세요",Toast.LENGTH_LONG).show();

                }
            }

        }
    }



    public static String testSHA256(String pw) {
        String SHA ="";

        try {

            MessageDigest mdSHA = MessageDigest.getInstance("SHA-256");

            mdSHA.update(pw.getBytes());
            byte byteData[] = mdSHA.digest();

            StringBuffer stringBuffer = new StringBuffer();

            for(int i=0; i<byteData.length; i++) {
                stringBuffer.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
            }

            SHA = stringBuffer.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            SHA=null;
        }

        return SHA;
    }


    private class SendPost extends AsyncTask<Void,Void,String>{ //background,progress,execcute
        SharedPreferences preferences;
        SharedPreferences.Editor editor;

        String url=apihost.getApi()+"login";
        String result = "";
        String message;
        String token;
        String mainPosition;

        SendPost(){
            uniqueToken = FirebaseInstanceId.getInstance().getToken();
            System.out.println("uniqueToken    "+uniqueToken);
        }

        @Override
        protected String doInBackground(Void... params) {
            //#############네트워크 검사#####################
            String status=NetworkUtil.getConnectivityStatusString(LoginActivity.this);
            if("네트워크가 연결되지 않아 종료됩니다.".equals(status)){
                //Toast.makeText(getApplicationContext(), "네트워크가 연결되지 않았습니다.", Toast.LENGTH_SHORT).show();
                //android.os.Process.killProcess(android.os.Process.myPid());
                //ActivityCompat.finishAffinity(MainActivity.this);
                message = status;
                return message;
            }else if("wifi로 바꾸어 주세요.".equals(status)){
                //Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
                //return status;
            }
            System.out.println("url!!!!!!!!!!!!"+url);
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url);

                //System.out.println(json);
                System.out.println("222222");

                //비밀번호 SHA256 암호화
                String sha256_pw = testSHA256(password);
                System.out.println("333333");
                System.out.println(sha256_pw);
                System.out.println("44444");

                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("id",id));
                nameValuePairs.add(new BasicNameValuePair("password",sha256_pw));
                nameValuePairs.add(new BasicNameValuePair("uniqueToken",uniqueToken));

                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
                post.setEntity(ent);
                System.out.println("55555");
                //전송
                HttpResponse httpResponse = httpclient.execute(post);
                System.out.println("66666");
                //응답
                HttpEntity resEntity = httpResponse.getEntity();
                System.out.println("77777");

                //토큰 혹은 false를 message에 담고서 비교
                message= EntityUtils.toString(resEntity);
                System.out.println(message);


                //인증안됨 로그인 힛수
                if("1".equals(message)||"2".equals(message)||"3".equals(message)||"4".equals(message)||"5".equals(message)){
                    result=message;
                }
                else if("no id in the server".equals(message)){
                    result=message;
                }

                else{//인증성공경우 토큰값 받아옴 , 토큰 저장해야함

                    //토큰 , 홈번호 JSON 풀기
                    changeJsonInfo(message);

                    //토큰 저장
                    //String message= EntityUtils.toString(resEntity);
                    Token userToken = new Token();
                    userToken.setToken(info[0]);

                    Log.w("Responese", userToken.getToken());
                    result="success";

                    //System.out.println(userToken.getToken());
                    preferences = getSharedPreferences("tokenAndHome", Context.MODE_PRIVATE);
                    editor = preferences.edit();

                    //SharedPreference에 토큰 저장
                    editor.putString("token", userToken.getToken());
                    editor.putString("home",info[1]);
                    editor.putString("fcmkey",uniqueToken);
                    editor.commit();


                    //저장된 토큰 가져오기
                    token = preferences.getString("token","");
                    mainPosition = preferences.getString("home","");

                    System.out.println("토큰확인 : "+token);
                    System.out.println("Home position : "+mainPosition);
                }


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (HttpHostConnectException e) {
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }


        @Override
        protected void onPostExecute(String s) {
            if(message.equals("네트워크가 연결되지 않아 종료됩니다.")){
                Toast.makeText(getApplicationContext(), "네트워크가 연결되지 않아 종료됩니다.", Toast.LENGTH_SHORT).show();
                ActivityCompat.finishAffinity(LoginActivity.this);
            }else if(message.equals("wifi로 바꾸어 주세요.")){
                //Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                editText_id.setText("");
                editText_pw.setText("");

                if(result=="success"){
                    Toast.makeText(getApplicationContext(),"로그인 되었습니다.",Toast.LENGTH_SHORT).show();
                    //Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("userToken",token);
                    //intent.putExtra("mainPosition",mainPosition);
                    startActivity(intent);

                    System.exit(0);

                }
                else if("5".equals(result)){
                    Toast.makeText(getApplicationContext(),"5회이상 틀렸습니다. 관리자에게 문의하여주세요.",Toast.LENGTH_LONG).show();
                /*Intent intent = new Intent(LoginActivity.this,LoginActivity.class);
                startActivity(intent);*/
                }
                else if("blocked".equals(result)){
                    Toast.makeText(getApplicationContext(),"접근이 제한된 사용자 입니다. 관리자에게 문의하여주세요.",Toast.LENGTH_LONG).show();
                /*Intent intent = new Intent(LoginActivity.this,LoginActivity.class);
                startActivity(intent);*/
                }
                else {
                    Toast.makeText(getApplicationContext(),"인증된 사원이 아닙니다.",Toast.LENGTH_LONG).show();
                /*Intent intent = new Intent(LoginActivity.this,LoginActivity.class);
                startActivity(intent);*/


                }
            }

        }
    }

    public String[] changeJsonInfo(String jsonMessage){

        //가져온 name,depName,email,teamleader 저장
        try {

            JSONArray jsonArray = new JSONArray(jsonMessage);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            info[0]=jsonObject.getString("token");
            info[1]=jsonObject.getString("home");


        } catch (JSONException e) {
            e.printStackTrace();
        }


        return info;
    }

}
