package com.example.bit_user.sms;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
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
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class DialogPWCheckActivity extends AppCompatActivity {
    SharedPreferences preferences;
    Button btnOk;
    Button btnCancel;
    EditText currentPW;
    String pw;

    private String userToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = getSharedPreferences("tokenAndHome",MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog_pwcheck);

        userToken = getIntent().getStringExtra("userToken");

        currentPW=(EditText)findViewById(R.id.currentPW);
        btnOk=(Button)findViewById(R.id.btnOk);
        btnCancel=(Button)findViewById(R.id.btnCancel);

        //작성창으로 되돌아 가기
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //작성창도 닫고 그 이전의 화면으로 돌아가기
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwcheck = currentPW.getText().toString();
                //암호화된 현재 비밀번호
                pw = testSHA256(pwcheck);

                //검증 = POST로 보내기
                new SendPost().execute();
            }
        });
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


    private class SendPost extends AsyncTask<Void,Void,String> { //background,progress,execcute
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        String message;
        String url= new ApiHost().getApi()+"profile";//"http://192.168.1.21:9990/profile";

        @Override
        protected String doInBackground(Void... params) {
            String status=NetworkUtil.getConnectivityStatusString(DialogPWCheckActivity.this);
            if("네트워크가 연결되지 않아 종료됩니다.".equals(status)){
                //Toast.makeText(getApplicationContext(), "네트워크가 연결되지 않았습니다.", Toast.LENGTH_SHORT).show();
                //android.os.Process.killProcess(android.os.Process.myPid());
                //ActivityCompat.finishAffinity(MainActivity.this);
                message = status;
                return message;
            }else if("wifi로 바꾸어 주세요.".equals(status)){
                Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
                return status;
            }
            try {
                System.out.println("AsyncTask Start");

                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url);

                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("password",pw));
                nameValuePairs.add(new BasicNameValuePair("token",userToken));

                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
                post.setEntity(ent);

                HttpResponse httpResponse = httpclient.execute(post);

                HttpEntity resEntity = httpResponse.getEntity();

                //log
                message= EntityUtils.toString(resEntity);
                System.out.println(message);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (HttpHostConnectException e) {
                e.printStackTrace();
            } catch (SocketException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (RuntimeException e){
                e.printStackTrace();
            }


            return message;
        }


        @Override
        protected void onPostExecute(String s) {
            if(message.equals("네트워크가 연결되지 않아 종료됩니다.")){
                Toast.makeText(getApplicationContext(), "네트워크가 연결되지 않아 종료됩니다.", Toast.LENGTH_SHORT).show();
                ActivityCompat.finishAffinity(DialogPWCheckActivity.this);
            }else if(message.equals("wifi로 바꾸어 주세요.")){
                Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
            }else{
                System.out.println("message : "+message);
                Intent intent = new Intent();
                intent.putExtra("message",message);
                setResult(RESULT_OK,intent);

                finish();
            }

        }
    }
}
