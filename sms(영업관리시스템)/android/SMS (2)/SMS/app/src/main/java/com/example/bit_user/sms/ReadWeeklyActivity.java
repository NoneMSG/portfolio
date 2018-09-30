package com.example.bit_user.sms;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReadWeeklyActivity extends AppCompatActivity {
    public static Activity rwaActivity;

    SharedPreferences preferences;
    String fcmkey;
    private int flag;
    private String ROLE;

    private String userToken;
    private String index;
    private String regDate;
    private int userNo;
    private String position="week";

    private String dayReportIndex;
    private String dayReportIndex0;
    private String dayReportIndex1;
    private String dayReportIndex2;
    private String dayReportIndex3;
    private String dayReportIndex4;

    private String date;
    private String date0;
    private String date1;
    private String date2;
    private String date3;
    private String date4;
    private String goal;
    private String endday;


    TextView department;
    TextView name;
    TextView leader;
    TextView today;

    TextView txtTitle;
    TextView txtGoal;
    TextView txtSale;
    TextView txtRate;

    TextView txtMon;
    TextView txtTues;
    TextView txtWed;
    TextView txtThurs;
    TextView txtFri;

    TextView txtMonPlan;
    TextView txtTuesPlan;
    TextView txtWedPlan;
    TextView txtThursPlan;
    TextView txtFriPlan;

    Button btnMonreport;
    Button btnTuesreport;
    Button btnWedreport;
    Button btnThursreport;
    Button btnFrireport;

    Button btnCancel;
    Button btnModify;
    Button btnDelete;

    long now = System.currentTimeMillis();
    Date comdate = new Date(now);
    SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd");
    String formatDate = sdfNow.format(comdate);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_weekly);
        rwaActivity=ReadWeeklyActivity.this;

        preferences= getSharedPreferences("tokenAndHome",MODE_PRIVATE);
        fcmkey = preferences.getString("fcmkey","0");

        userToken = getIntent().getStringExtra("token");
        index = getIntent().getStringExtra("index");//목록에서 선택시 index넘겨주고 받은 indexNo를 여기서 서버에 전송함 해당 상담일지정보 불러옴

        department = (TextView)findViewById(R.id.txtDep);
        name = (TextView)findViewById(R.id.txtName);
        leader = (TextView)findViewById(R.id.txtLeader);
        today = (TextView)findViewById(R.id.txtDate);

        txtTitle=(TextView)findViewById(R.id.txtTitle);
        txtGoal=(TextView)findViewById(R.id.txtGoal);
        txtSale=(TextView)findViewById(R.id.txtSale);
        txtRate = (TextView)findViewById(R.id.txtRate);

        txtMon=(TextView)findViewById(R.id.txtMon);
        txtTues=(TextView)findViewById(R.id.txtTues);
        txtWed=(TextView)findViewById(R.id.txtWed);
        txtThurs=(TextView)findViewById(R.id.txtThurs);
        txtFri=(TextView)findViewById(R.id.txtFri);

        txtMonPlan=(TextView)findViewById(R.id.txtMonPlan);
        txtTuesPlan=(TextView)findViewById(R.id.txtTuesPlan);
        txtWedPlan=(TextView)findViewById(R.id.txtWedPlan);
        txtThursPlan=(TextView)findViewById(R.id.txtThursPlan);
        txtFriPlan=(TextView)findViewById(R.id.txtFriPlan);

        new SendRequestLeaderinfo().execute();
        new SendRequestweekplanner().execute();

        onClickReport();
        onbtnClick();

    }

    //하단버튼 클릭 메서드

    public void onbtnClick() {
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnModify = (Button) findViewById(R.id.btnModify);
        btnDelete = (Button)findViewById(R.id.btnDelete);

        btnDelete.setOnClickListener(new View.OnClickListener() {
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
    }





    public void onClickReport(){
        btnMonreport=(Button)findViewById(R.id.btnMonreport);
        btnTuesreport=(Button)findViewById(R.id.btnTuesreport);
        btnWedreport=(Button)findViewById(R.id.btnWedreport);
        btnThursreport=(Button)findViewById(R.id.btnThursreport);
        btnFrireport=(Button)findViewById(R.id.btnFrireport);



        btnMonreport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayReportIndex=dayReportIndex0;
                date = date0;
                flag=2;
                new Uid(userToken).execute();
            }
        });
        btnTuesreport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayReportIndex=dayReportIndex1;
                date = date1;
                flag=2;
                new Uid(userToken).execute();

            }
        });
        btnWedreport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayReportIndex=dayReportIndex2;
                date = date2;
                flag=2;
                new Uid(userToken).execute();

            }
        });
        btnThursreport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayReportIndex=dayReportIndex3;
                date = date3;
                flag=2;
                new Uid(userToken).execute();

            }
        });
        btnFrireport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayReportIndex=dayReportIndex4;
                date = date4;
                flag=2;
                new Uid(userToken).execute();

            }
        });
    }



    //승인된 일일보고서 확인 >승인된것만 버튼 활성화
    private class SendRequestExistDayReport extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url= new ApiHost().getApi()+"view/dayreport/content"; //"http://192.168.1.21:9990/view/dayreport/content";
        String message;

        @Override
        protected String doInBackground(Void... params) {

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url);

                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("token",userToken));
                nameValuePairs.add(new BasicNameValuePair("date",date));

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
            if("no data".equals(message)){
                Toast.makeText(getApplicationContext(),"승인된 보고서가 없습니다.",Toast.LENGTH_SHORT).show();
            }
            else{
                System.out.println("dayReportIndex"+dayReportIndex);
                Intent intent;
                intent = new Intent(ReadWeeklyActivity.this,ReadDailyActivity.class);
                intent.putExtra("index",dayReportIndex);
                intent.putExtra("token",userToken);
                intent.putExtra("daylist",message);
                startActivity(intent);
            }
        }
    }




    //주간계획서 기본정보 , 일간계획 부분 가져오기
    private class SendRequestweekplanner extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url= new ApiHost().getApi()+"view/weekplanner/content"; //"http://192.168.1.21:9990/view/weekplanner/content";
        String message;

        @Override
        protected String doInBackground(Void... params) {
            String status=NetworkUtil.getConnectivityStatusString(ReadWeeklyActivity.this);
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
            } catch (IOException e) {
                e.printStackTrace();
            }


            return message;
        }


        @Override
        protected void onPostExecute(String s) {
            if(message.equals("네트워크가 연결되지 않아 종료됩니다.")){
                Toast.makeText(getApplicationContext(), "네트워크가 연결되지 않아 종료됩니다.", Toast.LENGTH_SHORT).show();
                ActivityCompat.finishAffinity(ReadWeeklyActivity.this);
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
            JSONObject jo = new JSONObject(jsonMessage);

            JSONArray jsonArray = jo.getJSONArray("week");
            JSONObject jsonObject = jsonArray.getJSONObject(0);

            if("LEADER".equals(ROLE)){
                name.setText(" 이름 : "+jsonObject.getString("author"));
            }
            //주간계획서 정보 뿌리기
            userNo=jsonObject.getInt("user_no");
            txtTitle.setText(jsonObject.getString("title"));

            goal=jsonObject.getString("sale_goal");
            String total = jsonObject.getString("sale_total");
            txtGoal.setText(" 목표액 : "+jsonObject.getString("sale_goal")+" 원");
            txtSale.setText(" 판매액 : "+jsonObject.getString("sale_total")+" 원");

            regDate=jsonObject.getString("reg_date");
            today.setText(" 등록일 : "+regDate.substring(0,10));
            double rate;

            if(Double.parseDouble(total)!=0){
                rate=Double.parseDouble(total)/Double.parseDouble(goal);
                txtRate.setText("달성률 : "+String.format("%.2f",(rate*100))+" %");
            }
            else
                txtRate.setText("달성률 : 0.00 %");

            endday = jsonObject.getString("end_weekday").toString();
            jsonArray=jo.getJSONArray("daily");
            System.out.println("size "+jsonArray.length());

            for(int i=0; i<jsonArray.length(); i++){

                jsonObject=jsonArray.getJSONObject(i);

                switch (i){
                    case 0:{
                        date0=jsonObject.getString("date").substring(0,10);
                        dayReportIndex0=jsonObject.getString("no");
                        txtMon.setText(jsonObject.getString("day")+" ( "+jsonObject.getString("date").substring(0,10)+" )");
                        txtMonPlan.setText(jsonObject.getString("plan"));
                        today.setText(today.getText()+
                                " ("+jsonObject.getString("date").substring(5,7)+"/"+
                                jsonObject.getString("date").substring(8,10)+"~");
                        break;
                    }
                    case 1:{
                        date1=jsonObject.getString("date").substring(0,10);
                        dayReportIndex1=jsonObject.getString("no");
                        txtTues.setText(jsonObject.getString("day")+" ( "+jsonObject.getString("date").substring(0,10)+" )");
                        txtTuesPlan.setText(jsonObject.getString("plan"));
                        break;
                    }
                    case 2:{
                        date2=jsonObject.getString("date").substring(0,10);
                        dayReportIndex2=jsonObject.getString("no");
                        txtWed.setText(jsonObject.getString("day")+" ( "+jsonObject.getString("date").substring(0,10)+" )");
                        txtWedPlan.setText(jsonObject.getString("plan"));
                        break;
                    }
                    case 3:{
                        date3=jsonObject.getString("date").substring(0,10);
                        dayReportIndex3=jsonObject.getString("no");
                        txtThurs.setText(jsonObject.getString("day")+" ( "+jsonObject.getString("date").substring(0,10)+" )");
                        txtThursPlan.setText(jsonObject.getString("plan"));
                        break;
                    }
                    case 4:{
                        date4=jsonObject.getString("date").substring(0,10);
                        dayReportIndex4=jsonObject.getString("no");
                        txtFri.setText(jsonObject.getString("day")+" ( "+jsonObject.getString("date").substring(0,10)+" )");
                        txtFriPlan.setText(jsonObject.getString("plan"));
                        today.setText(today.getText()+
                                jsonObject.getString("date").substring(5,7)+"/"+
                                jsonObject.getString("date").substring(8,10)+")");
                        break;
                    }
                }
            }

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
            String status=NetworkUtil.getConnectivityStatusString(ReadWeeklyActivity.this);
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
            }  catch (HttpHostConnectException e) {
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
                ActivityCompat.finishAffinity(ReadWeeklyActivity.this);
            }else if(message.equals("wifi로 바꾸어 주세요.")){
                Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                //message에 따라서 결과과
                if(message.equals("accept")){
                    //finish();

                    if(flag==1){//수정
                        try {

                            SimpleDateFormat dateFormat2 = new  SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                            Date date_reg = dateFormat2.parse(date0);
                            Date date_modif = dateFormat2.parse(formatDate);

                            if(formatDate.equals(date0) || date_modif.after(date_reg)){
                                Toast.makeText(getApplicationContext(),"날짜가 지나 수정할 수 없습니다.",Toast.LENGTH_SHORT).show();
                            }
                            else{

                                Intent intent = new Intent(ReadWeeklyActivity.this,ModifyWeeklyActivity.class);
                                intent.putExtra("userToken",userToken);
                                intent.putExtra("userNo",String.valueOf(userNo));
                                intent.putExtra("index",index);//글번호
                                intent.putExtra("title",txtTitle.getText());
                                intent.putExtra("goal",goal);//전체 목표액

                                intent.putExtra("date0",date0);
                                intent.putExtra("date1",date1);
                                intent.putExtra("date2",date2);
                                intent.putExtra("date3",date3);
                                intent.putExtra("date4",date4);

                                intent.putExtra("dayReportIndex0",dayReportIndex0);
                                intent.putExtra("dayReportIndex1",dayReportIndex1);
                                intent.putExtra("dayReportIndex2",dayReportIndex2);
                                intent.putExtra("dayReportIndex3",dayReportIndex3);
                                intent.putExtra("dayReportIndex4",dayReportIndex4);

                                intent.putExtra("endday",endday.substring(0,10));

                                startActivity(intent);
                                finish();

                            }

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    else if(flag==0){
                        Intent intent = new Intent(ReadWeeklyActivity.this,DialogDeleteActivity.class);
                        intent.putExtra("userToken",userToken);
                        intent.putExtra("userNo",String.valueOf(userNo));
                        intent.putExtra("index",index);
                        intent.putExtra("position",position);
                        startActivity(intent);
                    }
                    else if(flag==2){
                        new SendRequestExistDayReport().execute();
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
            String status=NetworkUtil.getConnectivityStatusString(ReadWeeklyActivity.this);
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
                ActivityCompat.finishAffinity(ReadWeeklyActivity.this);
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
                }
                //onResume();
                new SendRequestinfo().execute();
            }



        }
    }

}