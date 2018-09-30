package com.example.bit_user.sms;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ReadInterviewActivity extends AppCompatActivity {
    SharedPreferences preferences;
    String fcmkey;
    private int flag;
    private String ROLE;

    public static Activity riaActivity;

    private String userToken;
    private String index;
    private String regDate;
    private Object dayIndex;
    private String dayIndexNull;

    private int userNo;
    private String position="interview";


    TextView department;
    TextView name;
    TextView leader;
    TextView today;

    TextView txtTitle;
    TextView txtcstName;
    TextView txtownName;

    TextView txtAddress;
    TextView txtsecName;

    TextView txtDiscription;

    private String customerName;
    private String secondcustomerName;

    Button btnCancel;
    Button btnModify;
    Button btnDelete;
    Button btnDayReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_interview);

        riaActivity=ReadInterviewActivity.this;

        preferences= getSharedPreferences("tokenAndHome",MODE_PRIVATE);
        fcmkey = preferences.getString("fcmkey","0");

        userToken = getIntent().getStringExtra("token");
        index = getIntent().getStringExtra("index");//목록에서 선택시 index넘겨주고 받은 indexNo를 여기서 서버에 전송함 해당 상담일지정보 불러옴

        department = (TextView)findViewById(R.id.txtDep);
        name = (TextView)findViewById(R.id.txtName);
        leader = (TextView)findViewById(R.id.txtLeader);
        today = (TextView)findViewById(R.id.txtDate);

        txtTitle=(TextView)findViewById(R.id.txtTitle);
        txtcstName=(TextView)findViewById(R.id.txtcstName);
        txtownName = (TextView)findViewById(R.id.txtownName);

        txtAddress=(TextView)findViewById(R.id.txtAddress);
        txtsecName=(TextView)findViewById(R.id.txtsecName);

        txtDiscription=(TextView)findViewById(R.id.txtDiscription);

        new SendRequestLeaderinfo().execute(); //권한 검사 후 기본정보 가져오기
        new SendRequestConsultation().execute();


    }

    @Override
    protected void onResume() {
        super.onResume();
        btnDayReport=(Button)findViewById(R.id.btnDayReport);
        if(!"not null".equals(dayIndexNull)){
            btnDayReport.setVisibility(View.VISIBLE);
        }
        else if("not null".equals(dayIndexNull)){
             btnDayReport.setVisibility(View.GONE);
        }
        onbtnClick();
    }

    //onclick메서드
    public void onbtnClick(){

        btnCancel=(Button)findViewById(R.id.btnCancel);
        btnModify=(Button)findViewById(R.id.btnModify);
        btnDelete=(Button)findViewById(R.id.btnDelete);


        btnDayReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag=0;
                new Uid(userToken).execute();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag=1;
                new Uid(userToken).execute();
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag=2;
                new Uid(userToken).execute();
            }
        });
    }

    /*//일일보고서 연결 바로가기
    private class SendRequestLinkDayReport extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url="http://192.168.1.21:9990/write/info";
        String message;

        @Override
        protected String doInBackground(Void... params) {

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
    }*/

    private class SendRequestConsultation extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url= new ApiHost().getApi()+"view/consultation/content";//"http://192.168.1.21:9990/view/consultation/content";
        String message;

        @Override
        protected String doInBackground(Void... params) {
            String status=NetworkUtil.getConnectivityStatusString(ReadInterviewActivity.this);
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
                HttpPost post = new HttpPost(url);

                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("token",userToken));
                nameValuePairs.add(new BasicNameValuePair("index",index));

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
            }catch (IOException e) {
                e.printStackTrace();
            }


            return message;
        }


        @Override
        protected void onPostExecute(String s) {
            if(message.equals("네트워크가 연결되지 않아 종료됩니다.")){
                Toast.makeText(getApplicationContext(), "네트워크가 연결되지 않아 종료됩니다.", Toast.LENGTH_SHORT).show();
                ActivityCompat.finishAffinity(ReadInterviewActivity.this);
            }else if(message.equals("wifi로 바꾸어 주세요.")){
                Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                changeJsonConsultation(message);
            }
        }
    }

    public void changeJsonConsultation(String jsonMessage){

        //가져온 name,depName,email,teamleader 저장
        try {

            JSONArray jsonArray = new JSONArray(jsonMessage);
            JSONObject jsonObject = jsonArray.getJSONObject(0);

            if("LEADER".equals(ROLE)){
                name.setText(" 이름 : "+jsonObject.getString("author"));
            }

            userNo=jsonObject.getInt("userNo");
            regDate =jsonObject.getString("reg_date");

            txtTitle.setText(jsonObject.getString("title"));

            txtcstName.setText(" 1차 고객명 : "+jsonObject.getString("customer1Name"));
            txtownName.setText("  [ 대표 : "+jsonObject.getString("customer1ownerName")+" ]");

            txtAddress.setText(" 주소 : "+jsonObject.getString("customer1adress"));
            txtsecName.setText(" 2차 고객명 : "+jsonObject.getString("customer2Name"));
            today.setText(" 등록일 : "+regDate.substring(0,10));

            customerName=jsonObject.getString("customer1Name");
            secondcustomerName=jsonObject.getString("customer2Name");


            //null담기위해 Object타입으로 선언
            dayIndex =jsonObject.opt("dayNo");
            if(dayIndex.toString()=="null"){
                dayIndexNull="null";
            }
            else{
                dayIndexNull="not null";
            }


            System.out.println("dayindexxxxxx"+dayIndex);

            String removeHtmlText = Html.fromHtml(jsonObject.getString("description")).toString();
            txtDiscription.setText(removeHtmlText);


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private class SendRequestinfo extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url= new ApiHost().getApi()+"write/info";//"http://192.168.1.21:9990/write/info";
        String message;

        @Override
        protected String doInBackground(Void... params) {

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
            user.setDepName(jsonObject.getString("depName"));
            user.setTeamleader(jsonObject.getString("teamleader"));

            if("EMPLOYEE".equals(ROLE)){
                name.setText(" 이름 : "+user.getName());
                department.setText(" 소속 : "+user.getDepName()+" 팀");
                leader.setText(" ("+user.getTeamleader()+")");
            }
            else{
                //팀장의 경우 사원의 이름을 가져와야함
                department.setText(" 소속 : "+user.getDepName()+" 팀");
                leader.setText(" ("+user.getTeamleader()+")");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return user;
    }

    //###############################################r기기확인
    private class Uid extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url= new ApiHost().getApi()+"uidcheck";//"http://192.168.1.21:9990/uidcheck";
        String message;
        String token;


        public Uid(String validationToken) {
            token = validationToken;
            fcmkey = preferences.getString("fcmkey","0");
            System.out.println("checkauth fcmy"+fcmkey);
        }


        @Override
        protected String doInBackground(Void... params) {
            String status=NetworkUtil.getConnectivityStatusString(ReadInterviewActivity.this);
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
            }catch (HttpHostConnectException e) {
                e.printStackTrace();
            }  catch (IOException e) {
                e.printStackTrace();
            }


            return message;
        }


        @Override
        protected void onPostExecute(String s) {
            if(message.equals("네트워크가 연결되지 않아 종료됩니다.")){
                Toast.makeText(getApplicationContext(), "네트워크가 연결되지 않아 종료됩니다.", Toast.LENGTH_SHORT).show();
                ActivityCompat.finishAffinity(ReadInterviewActivity.this);
            }else if(message.equals("wifi로 바꾸어 주세요.")){
                Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                //message에 따라서 결과과
                if(message.equals("accept")){

                    if(flag==0){ //d일일보고서 연결
                        if("not null".equals(dayIndexNull)){ //연결된 일일보고서 있음

                            System.out.println("일일보고서  "+dayIndex);

                            Intent intent = new Intent(ReadInterviewActivity.this,ReadDailyActivity.class);
                            intent.putExtra("token",userToken);
                            intent.putExtra("index",String.valueOf(dayIndex));
                            intent.putExtra("regDate",regDate);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"연결된 보고서가 없습니다.",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    else if(flag==1){ //수정

                        Intent intent = new Intent(ReadInterviewActivity.this,ModifyInterviewActivity.class);
                        intent.putExtra("userToken",userToken);
                        intent.putExtra("userNo",String.valueOf(userNo));
                        intent.putExtra("index",index);
                        intent.putExtra("regDate",regDate.substring(0,10));
                        intent.putExtra("description",txtDiscription.getText());
                        intent.putExtra("title",txtTitle.getText());
                        intent.putExtra("customerName",customerName);
                        intent.putExtra("secondcustomerName",secondcustomerName);
                        if("not null".equals(dayIndexNull)){
                            intent.putExtra("dayNo",String.valueOf(dayIndex));
                        }
                        else{
                            intent.putExtra("dayNo","no data");
                        }

                        startActivity(intent);
                        finish();
                    }
                    else if(flag==2){ //삭제
                        Intent intent = new Intent(ReadInterviewActivity.this,DialogDeleteActivity.class);
                        intent.putExtra("userToken",userToken);
                        intent.putExtra("userNo",String.valueOf(userNo));
                        intent.putExtra("index",index);
                        intent.putExtra("position",position);
                        startActivity(intent);
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"다른 장치에서 로그인되어있습니다. 새로 로그인 해주세요",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    //finish();
                }
                //finish();
                return;
            }

        }
    }

    //role 구분 : 팀장1 사원0
    private class SendRequestLeaderinfo extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url= new ApiHost().getApi()+"checkauthority";//"http://192.168.1.21:9990/checkauthority";
        String message;

        @Override
        protected String doInBackground(Void... params) {
            String status=NetworkUtil.getConnectivityStatusString(ReadInterviewActivity.this);
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
                ActivityCompat.finishAffinity(ReadInterviewActivity.this);
            }else if(message.equals("wifi로 바꾸어 주세요.")){
                Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                if("0".equals(message))//사원일경우 사원의 정보 가져옴
                    ROLE="EMPLOYEE";
                else{
                    ROLE="LEADER";

                    btnModify.setVisibility(View.GONE);
                    btnDelete.setVisibility(View.GONE);
                    btnDayReport.setVisibility(View.GONE);
                }
                //onResume();
                new SendRequestinfo().execute();
            }



        }
    }
}
