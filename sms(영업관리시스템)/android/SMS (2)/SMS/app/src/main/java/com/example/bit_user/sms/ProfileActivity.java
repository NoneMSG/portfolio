package com.example.bit_user.sms;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
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

public class ProfileActivity extends AppCompatActivity {

    private String userToken;
    private static final int PICK_FROM_PW = 1;


    String newPassword;
    String newPasswordCheck;
    String pw;
    String changePW;

    String message="";

    EditText np;
    EditText npc;

    TextView userName;
    TextView userDepartment;
    TextView userManager;

    TextView diffPassword;
    TextView passwordLengthCheck;

    Button btnApply;
    Button btnCancel;

    LayoutInflater inflater;
    AlertDialog.Builder adb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userToken = getIntent().getStringExtra("userToken");

        diffPassword = (TextView)findViewById(R.id.password);
        passwordLengthCheck = (TextView)findViewById(R.id.passwordLengthCheck);

        np = (EditText)findViewById(R.id.newPassword);
        npc = (EditText)findViewById(R.id.newPasswordCheck);

        np.setFocusable(false);
        npc.setEnabled(false);

        userDepartment = (TextView)findViewById(R.id.userDepartment);
        userManager = (TextView)findViewById(R.id.userManager);
        //editText.setClickable(false);

        onClickEvent();

        new SendRequestProfile().execute();
    }

    //취소 , 적용 버튼
    public void onClickEvent(){
        btnApply = (Button)findViewById(R.id.apply);
        btnCancel = (Button)findViewById(R.id.cancel);

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                newPassword = np.getText().toString();
                newPasswordCheck = npc.getText().toString();

                if("".equals(newPassword) && "".equals(newPasswordCheck)){
                    Toast.makeText(getApplicationContext(),"변경된 비밀번호가 없습니다.",Toast.LENGTH_LONG).show();
                }

                else if(newPassword.equals(newPasswordCheck)){//비밀번호 일치
                    //##############
                    if(newPassword.length()<6 || newPassword.length()>12 || newPasswordCheck.length()<6 || newPasswordCheck.length()>12){
                        diffPassword.setVisibility(View.GONE);
                        passwordLengthCheck.setVisibility(View.VISIBLE);

                    }
                    else{
                        changePW=newPasswordCheck;
                        new SendPutNewpw().execute();
                    }

                }
                else{
                    diffPassword.setVisibility(View.VISIBLE);
                    passwordLengthCheck.setVisibility(View.GONE);
                    diffPassword.setText(" 비밀번호가 일치하지 않습니다.");
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        np.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEditTouch();
            }
        });
        npc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("correct-pw".equals(message)) {

                    //npc.setFocusableInTouchMode(true);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            }
        });
    }

    public void onEditTouch(){

        inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        System.out.println("비밀번호 텆ㅊ;");

        if ("correct".equals(message) || "correct-pw".equals(message)) {
            System.out.println("crrect-pw");
            message="correct-pw";

        }
        else{
            System.out.println("xjxjxjxjxjxj터터터터터텊ㅍㅍㅊㅊㅊ치");
            /*View layout = inflater.inflate(R.layout.pwcheck_layout, null);

            adb = new AlertDialog.Builder(ProfileActivity.this);
            System.out.println("터치2");
            adb.setTitle("현재 비밀번호 확인").
                    setView(layout).
                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            pwcheck_edit =
                                    (EditText) ((AlertDialog) dialog).findViewById(R.id.currentPW);
                            String pwcheck = pwcheck_edit.getText().toString();

                            //암호화된 현재 비밀번호
                            pw = testSHA256(pwcheck);

                            //검증 = POST로 보내기
                            new SendPost().execute();
                            adb.setCancelable(true);

                        }
                    }).
                    setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).
                    show();*/
            Intent intent = new Intent(ProfileActivity.this,DialogPWCheckActivity.class);
            intent.putExtra("userToken",userToken);
            startActivityForResult(intent,PICK_FROM_PW);
            System.out.println("터치3");

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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK){

            switch (requestCode) {
                case PICK_FROM_PW: {

                    message = data.getStringExtra("message");
                    if("correct".equals(message)){
                        Toast.makeText(getApplicationContext(),"비밀번호 인증이 완료되었습니다.",Toast.LENGTH_LONG).show();
                        np.setFocusable(true);
                        //npc.setFocusable(true);
                        npc.setEnabled(true);
                        np.setFocusableInTouchMode(true);
                        npc.setFocusableInTouchMode(true);
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    }
                    //토큰 만료
                    else if("false".equals(message)){
                        Intent intent = new Intent(ProfileActivity.this,LoginActivity.class);
                        startActivity(intent);
                        Toast.makeText(getApplicationContext(),"인증 시간이 만료되었습니다.",Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(),"다시 로그인하여 주세요.",Toast.LENGTH_LONG).show();
                    }

                    //비번틀림
                    else{
                        Toast.makeText(getApplicationContext(),"비밀번호가 다릅니다.",Toast.LENGTH_LONG).show();

                    }

                    break;
                }
            }
        }
    }


    private class SendPutNewpw extends AsyncTask<Void,Void,String>{ //background,progress,execcute
        SharedPreferences preferences;
        SharedPreferences.Editor editor;


        String url= new ApiHost().getApi()+"profile/pw";//"http://192.168.1.21:9990/profile/pw";
        String result = "";
        String message;
        String token;

        @Override
        protected String doInBackground(Void... params) {

            String status=NetworkUtil.getConnectivityStatusString(ProfileActivity.this);
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
                HttpClient httpclient = new DefaultHttpClient();
                HttpPut put = new HttpPut(url);

                //System.out.println(json);

                //비밀번호 SHA256 암호화
                String sha256_pw = testSHA256(changePW);

                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("password",sha256_pw));
                nameValuePairs.add(new BasicNameValuePair("token",userToken));

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


            return result;
        }


        @Override
        protected void onPostExecute(String s) {
            if(message.equals("네트워크가 연결되지 않아 종료됩니다.")){
                Toast.makeText(getApplicationContext(), "네트워크가 연결되지 않아 종료됩니다.", Toast.LENGTH_SHORT).show();
                ActivityCompat.finishAffinity(ProfileActivity.this);
            }else if(message.equals("wifi로 바꾸어 주세요.")){
                Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getApplicationContext(),"비밀번호가 변경되었습니다. ",Toast.LENGTH_SHORT).show();

                Toast.makeText(getApplicationContext(),"다시 로그인하여 주세요.",Toast.LENGTH_LONG).show();

                new SendRequestLogout().execute();
            }
        }
    }

    private class SendRequestLogout extends AsyncTask<Void,Void,String> { //background,progress,execcute
        SharedPreferences preferences = getSharedPreferences("tokenAndHome",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        String url= new ApiHost().getApi()+"logout"; //"http://192.168.1.21:9990/logout";
        String message;

        @Override
        protected String doInBackground(Void... params) {

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url);

                String fcmkey = preferences.getString("fcmkey","");

                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("token",userToken));
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
            } catch (IOException e) {
                e.printStackTrace();
            }


            return message;
        }


        @Override
        protected void onPostExecute(String s) {
            if("logout".equals(message)){
                System.out.println(preferences.getString("token",""));
                System.out.println(preferences.getString("home",""));

                editor.clear();
                editor.commit();

                Intent intent = new Intent(ProfileActivity.this,LoginActivity.class);
                startActivity(intent);

                finish();
            }
            else if("can not logout".equals(message)){
                Toast.makeText(getApplicationContext(),"다른 기기에서 로그인되어있습니다. 새로 로그인 해주세요",Toast.LENGTH_LONG).show();
                editor.clear();
                editor.commit();
                Intent intent = new Intent(ProfileActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }


    private class SendRequestProfile extends AsyncTask<Void,Void,String>{ //background,progress,execcute
        SharedPreferences preferences;
        SharedPreferences.Editor editor;

        String url= new ApiHost().getApi()+"profile/info";//"http://192.168.1.21:9990/profile/info";
        String message;
        String token;

        @Override
        protected String doInBackground(Void... params) {
            userName = (TextView)findViewById(R.id.userName);

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url);

                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("token",userToken));

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
            } catch (IOException e) {
                e.printStackTrace();
            }


            return message;
        }


        @Override
        protected void onPostExecute(String s) {
            changeJsonInfo(message);
        }
    }

    public User changeJsonInfo(String jsonMessage){
        User user = new User();

        //가져온 name,depName,email,teamleader 저장
        try {

            JSONArray jsonArray = new JSONArray(jsonMessage);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            user.setName(jsonObject.getString("name"));
            user.setEmail(jsonObject.getString("email"));
            user.setDepName(jsonObject.getString("depName"));
            user.setTeamleader(jsonObject.getString("teamleader"));

            userName.setText(user.getName() +" ( "+user.getEmail()+" )");
            userDepartment.setText(user.getDepName());
            userManager.setText("팀장 : "+user.getTeamleader());

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return user;
    }
}
