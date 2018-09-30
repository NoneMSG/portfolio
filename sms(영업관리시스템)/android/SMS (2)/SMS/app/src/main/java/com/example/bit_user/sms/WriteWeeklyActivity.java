package com.example.bit_user.sms;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WriteWeeklyActivity extends AppCompatActivity {
    public static Activity wwaActivity;
    private String userToken;
    SharedPreferences preferences;
    String fcmkey;
    private int flag;


    DayPlan dayPlan;

    TextView department;
    TextView name;
    TextView leader;
    TextView today;

    EditText editTitle;
    TextView editGoal;

    String title;
    String goal;

    String mon;
    String tues;
    String wed;
    String thurs;
    String fri;
    String sun;

    EditText editMon;
    EditText editTues;
    EditText editWed;
    EditText editThurs;
    EditText editFri;

    EditText goalMon;
    EditText goalTues;
    EditText goalWed;
    EditText goalThurs;
    EditText goalFri;

    String monPlan;
    String monTotal;

    String tuesPlan;
    String tuesTotal;

    String wedPlan;
    String wedTotal;

    String thursPlan;
    String thursTotal;

    String friPlan;
    String friTotal;

    String strDate;
    String sendDate;


    Button btnCancel;
    Button btnSubmit;
    Button btnCalendar;
    Button btnChoiceDate;

    TextView dateMon;
    TextView dateTues;
    TextView dateWed;
    TextView dateThurs;
    TextView dateFri;

    LayoutInflater inflater;

    GetDayAndWeek getDayAndWeek = new GetDayAndWeek();

    // 현재시간을 msec 으로 구한다.
    long now = System.currentTimeMillis();
    // 현재시간을 date 변수에 저장한다.
    Date date = new Date(now);
    // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
    SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    // nowDate 변수에 값을 저장한다.
    String formatDate = sdfNow.format(date);

    //캘린더 호출시 필요한 정보들
    static final int DATE_DIALOG_ID=1;
    private int mYear;
    private int mMonth;
    private int mDay;

    TextView calDate;


    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {



        }

        @Override
        public void afterTextChanged(Editable s) {

            try{
                int sum;

                monTotal = goalMon.getText().toString();
                tuesTotal = goalTues.getText().toString();
                wedTotal = goalWed.getText().toString();
                thursTotal = goalThurs.getText().toString();
                friTotal = goalFri.getText().toString();

                if("".equals(monTotal))
                    monTotal="0";
                if("".equals(tuesTotal))
                    tuesTotal="0";
                if("".equals(wedTotal))
                    wedTotal="0";
                if("".equals(thursTotal))
                    thursTotal="0";
                if("".equals(friTotal))
                    friTotal="0";



                sum=Integer.parseInt(monTotal)+
                        Integer.parseInt(tuesTotal)+
                        Integer.parseInt(wedTotal)+
                        Integer.parseInt(thursTotal)+
                        Integer.parseInt(friTotal);

                System.out.println(sum);
                editGoal.setText(String.valueOf(sum));

            }catch (NumberFormatException e){
                 editGoal.setText("");
                return;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_weekly);

        wwaActivity=WriteWeeklyActivity.this;
        preferences= getSharedPreferences("tokenAndHome",MODE_PRIVATE);
        fcmkey = preferences.getString("fcmkey","0");

        userToken = getIntent().getStringExtra("userToken");

        department = (TextView)findViewById(R.id.txtDep);
        name = (TextView)findViewById(R.id.txtName);
        leader = (TextView)findViewById(R.id.txtLeader);
        today = (TextView)findViewById(R.id.txtDate);
        today.setText(" 작성일 : "+formatDate);

        calDate=(TextView)findViewById(R.id.calDate);

        editTitle=(EditText)findViewById(R.id.editTitle);
        editGoal=(TextView)findViewById(R.id.editGoal);


        dateMon=(TextView)findViewById(R.id.dateMon);
        dateTues=(TextView)findViewById(R.id.dateTues);
        dateWed=(TextView)findViewById(R.id.dateWed);
        dateThurs=(TextView)findViewById(R.id.dateThurs);
        dateFri=(TextView)findViewById(R.id.dateFri);

        editMon=(EditText)findViewById(R.id.editMon);
        editTues=(EditText)findViewById(R.id.editTues);
        editWed=(EditText)findViewById(R.id.editWed);
        editThurs=(EditText)findViewById(R.id.editThurs);
        editFri=(EditText)findViewById(R.id.editFri);

        goalMon=(EditText)findViewById(R.id.goalMon);
        goalTues=(EditText)findViewById(R.id.goalTues);
        goalWed=(EditText)findViewById(R.id.goalWed);
        goalThurs=(EditText)findViewById(R.id.goalThurs);
        goalFri=(EditText)findViewById(R.id.goalFri);

        //일일목표액 실시간 반영
        goalMon.addTextChangedListener(textWatcher);
        goalTues.addTextChangedListener(textWatcher);
        goalWed.addTextChangedListener(textWatcher);
        goalThurs.addTextChangedListener(textWatcher);
        goalFri.addTextChangedListener(textWatcher);

        btnCalendar=(Button)findViewById(R.id.btnCalendar);
        btnChoiceDate = (Button)findViewById(R.id.btnChoiceDate);
        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnSubmit = (Button)findViewById(R.id.btnSubmit);

        btnSubmit.setEnabled(false);
        btnSubmit.setTextColor(Color.rgb(148,148,148));

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        new SendRequestinfo().execute();

        onbtnClick();

        final Calendar calendar = Calendar.getInstance();
        mYear=calendar.get(Calendar.YEAR);
        mMonth=calendar.get(Calendar.MONTH);
        mDay=calendar.get(Calendar.DATE);

        updateDate();
        //서버로부터 받아온 날짜와 휴무일 정보로 dateMon~dateFri setText

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void updateDate(){
        strDate = "[ "+mYear+"-"+(mMonth+1)+"-"+mDay+" ]";

        if(mMonth<9 && mDay<10)
            sendDate=mYear+"-0"+(mMonth+1)+"-0"+mDay;
        else if(mMonth<9)
            sendDate=mYear+"-0"+(mMonth+1)+"-"+mDay;
        else if(mDay<10)
            sendDate=mYear+"-"+(mMonth+1)+"-0"+mDay;
        else
            sendDate=mYear+"-"+(mMonth+1)+"-"+mDay;
        calDate.setText(strDate);
       // calDate.setTextColor(880000);
    }


    //호출되는 캘린더 다이얼로그
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    mYear=year;
                    mMonth=monthOfYear;
                    mDay=dayOfMonth;
                    updateDate();
                }
            };

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id){
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,mDateSetListener,mYear,mMonth,mDay);
        }

        return null;
    }

    //액티비티 하단 Cancel,Submit ,캘린더 다이얼로그 버튼 클릭 이벤트
    public void onbtnClick(){
        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnSubmit = (Button)findViewById(R.id.btnSubmit);
        //캘린더 호출
        btnCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });
        //캘린더 정보 전송
        btnChoiceDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag=0;
                new Uid(userToken).execute();
            }
        });


        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag=1;

                if("".equals(monTotal))
                    monTotal="0";
                if("".equals(tuesTotal))
                    tuesTotal="0";
                if("".equals(wedTotal))
                    wedTotal="0";
                if("".equals(thursTotal))
                    thursTotal="0";
                if("".equals(friTotal))
                    friTotal="0";

                new Uid(userToken).execute();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WriteWeeklyActivity.this,DialogCancelActivity.class);
                intent.putExtra("userToken",userToken);
                intent.putExtra("flag","WriteWeekly");
                startActivity(intent);
            }
        });

    }


    //캘린더에서 선택된 날짜 전송
    private class SendRequestDateHoliday extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url=new ApiHost().getApi()+"getweek";
        String message;

        @Override
        protected String doInBackground(Void... params) {

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url);

                System.out.println(sendDate);
                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("token",userToken));
                nameValuePairs.add(new BasicNameValuePair("date",sendDate));

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

        //###########전달받은 요일별 날짜와 휴무일 정보 나누기
        @Override
        protected void onPostExecute(String s) {
            changeJsonDate(message);
        }
    }


    public void changeJsonDate(String jsonMessage){

        try {


            if ("data already exist".equals(jsonMessage)){
                Toast.makeText(getApplicationContext(),"이미 주간계획서가 있습니다.",Toast.LENGTH_SHORT).show();
                dateMon.setText("월 ");
                dateTues.setText("화 ");
                dateWed.setText("수 ");
                dateThurs.setText("목 ");
                dateFri.setText("금 ");
            }
            else{
                calDate.setTextColor(Color.rgb(192,0,64));
                btnSubmit.setEnabled(true);
                btnSubmit.setTextColor(Color.rgb(136,0,0));

                JSONArray jsonArray = new JSONArray("["+jsonMessage+"]");
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                mon=jsonObject.getString("mon");
                tues=jsonObject.getString("tue");
                wed=jsonObject.getString("wed");
                thurs=jsonObject.getString("thu");
                fri=jsonObject.getString("fri");
                sun=jsonObject.getString("sun");

                dateMon.setText("월 "+mon);
                dateTues.setText("화 "+tues);
                dateWed.setText("수 "+wed);
                dateThurs.setText("목 "+thurs);
                dateFri.setText("금 "+fri);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



    private class SendRequestWeekPlan extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url=new ApiHost().getApi()+"write/weekplan";
        String message;


        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd");


        //DayPlan 객체생성하여 Gson을 이용해 Json 변경
        Gson gson = new Gson();

        //##########오늘날짜 formatDate 기준으로 다음주 월~금 날짜 가져오기

        DayPlan dayPlan = new DayPlan("월",mon,monPlan,monTotal,0);
        String toJson0=gson.toJson(dayPlan);

        DayPlan dayPlan1 = new DayPlan("화",tues,tuesPlan,tuesTotal,0);
        String toJson1=gson.toJson(dayPlan1);

        DayPlan dayPlan2 = new DayPlan("수",wed,wedPlan,wedTotal,0);
        String toJson2=gson.toJson(dayPlan2);

        DayPlan dayPlan3 = new DayPlan("목",thurs,thursPlan,thursTotal,0);
        String toJson3=gson.toJson(dayPlan3);

        DayPlan dayPlan4 = new DayPlan("금",fri,friPlan,friTotal,0);
        String toJson4=gson.toJson(dayPlan4);

        @Override
        protected String doInBackground(Void... params) {


            //미입력시 에러날경우 메서드 따로 떼어서 null->0 으로 변환해주어서 계산

                System.out.println("월==========" + toJson0);
                System.out.println("화==========" + toJson1);
                System.out.println("수==========" + toJson2);
                System.out.println("목==========" + toJson3);
                System.out.println("금==========" + toJson4);
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost post = new HttpPost(url);


                    //아이디와 비밀번호 묶음
                    List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(10);
                    nameValuePairs.add(new BasicNameValuePair("token", userToken));
                    nameValuePairs.add(new BasicNameValuePair("title", title));
                    nameValuePairs.add(new BasicNameValuePair("saleGoal", goal));
                    nameValuePairs.add(new BasicNameValuePair("firstday", mon));
                    nameValuePairs.add(new BasicNameValuePair("endday", sun));
                    nameValuePairs.add(new BasicNameValuePair("rate", "0.0"));
                    nameValuePairs.add(new BasicNameValuePair("saleTotal", "0"));
                    nameValuePairs.add(new BasicNameValuePair("list0", toJson0));
                    nameValuePairs.add(new BasicNameValuePair("list1", toJson1));
                    nameValuePairs.add(new BasicNameValuePair("list2", toJson2));
                    nameValuePairs.add(new BasicNameValuePair("list3", toJson3));
                    nameValuePairs.add(new BasicNameValuePair("list4", toJson4));

                    UrlEncodedFormEntity ent = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
                    post.setEntity(ent);

                    //전송
                    HttpResponse httpResponse = httpclient.execute(post);

                    //응답
                    HttpEntity resEntity = httpResponse.getEntity();

                    //토큰 혹은 false를 message에 담고서 비교
                    message = EntityUtils.toString(resEntity);
                    System.out.println("결과 : " + message);


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
            JSONArray jsonArray2;
            try {
                //##############어떻게 분류해서 비교하지?
                if("inserted".equals(message)){
                    //################w저장 후 인텐트
                    Toast.makeText(getApplicationContext(),"주간 계획서가 저장되었습니다.",Toast.LENGTH_SHORT).show();

                    finish();
                }
                else if("tranjection".equals(message)){
                    Toast.makeText(getApplicationContext(),"목표액입력이 잘못되었습니다.",Toast.LENGTH_SHORT).show();
                }
                else if("이번주에 이번주 계획서 작성불가".equals(message)){
                    Toast.makeText(getApplicationContext(),"주간계획서 작성기간이 지났습니다.",Toast.LENGTH_SHORT).show();
                }
                else{ //이미 존재하는 주간계획의 경우 타이틀 가져와 알려줌
                    jsonArray2 = new JSONArray("["+message+"]");
                    JSONObject jsonObject = jsonArray2.getJSONObject(0);
                    String existTitle = jsonObject.getString("alreadyexist");

                    Toast.makeText(getApplicationContext(),existTitle+"는 이미 존재하는 주간계획서 입니다.",Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }



    private class SendRequestinfo extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url=new ApiHost().getApi()+"write/info";
        String message;

        @Override
        protected String doInBackground(Void... params) {
            String status=NetworkUtil.getConnectivityStatusString(WriteWeeklyActivity.this);
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
            }catch (IOException e) {
                e.printStackTrace();
            }


            return message;
        }


        @Override
        protected void onPostExecute(String s) {
            if(message.equals("네트워크가 연결되지 않아 종료됩니다.")){
                Toast.makeText(getApplicationContext(), "네트워크가 연결되지 않아 종료됩니다.", Toast.LENGTH_SHORT).show();
                ActivityCompat.finishAffinity(WriteWeeklyActivity.this);
            }else if(message.equals("wifi로 바꾸어 주세요.")){
                Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                changeJsonInfo(message);
            }
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

            name.setText(" 이름 : "+user.getName());
            department.setText(" 소속 : "+user.getDepName()+" 팀");
            leader.setText(" ("+user.getTeamleader()+")");


        } catch (JSONException e) {
            e.printStackTrace();
        }


        return user;
    }

    //###############################################################
    //###############액티비티만들어서 커스텀다이얼로그 새로만들기######################
    //##################레이아웃덮어쓴 다이얼로그가 지워지지않음 -스타일변경해서 기본버튼 사용하든지#######################
   /* public void cancelCheckDialog( View view ) {

        try{

            inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.activity_dialog_cancel,null);

            btnOk = (Button)layout.findViewById(R.id.btnOk);
            btnRealCancel = (Button)layout.findViewById(R.id.btnRealCancel);

            new AlertDialog.Builder( WriteWeeklyActivity.this ).
                setIcon( android.R.drawable.ic_dialog_alert ).
                setView(layout)*//*.
                    setPositiveButton(null, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d("dddd","156");
                    dialog.dismiss();
                }
            }).setNegativeButton(null, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            })*//*.show();




            *//* AlertDialog.Builder adb = new AlertDialog.Builder( WriteWeeklyActivity.this );
            adb.setIcon( android.R.drawable.ic_dialog_alert );
            adb.setView(layout);
            adb.show();*//*


           btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("dddd","156");

                    layout.setVisibility(View.GONE);

               }
            });


            btnRealCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }

    }*/

    private void setDismiss(Dialog dialog){
        if(dialog!=null&&dialog.isShowing())
            dialog.dismiss();
    }

    public String getWeekdate(){
        //이번주 월요일
        String monday = getDayAndWeek.getCurMonday();
        //다음주 월요일
        String nextMonday = getDayAndWeek.get7DayAgoDate(monday,7);

        return nextMonday;
    }


    //###############################################r기기확인
    private class Uid extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url=new ApiHost().getApi()+"uidcheck";
        String message;
        String token;


        public Uid(String validationToken) {
            token = validationToken;
            fcmkey = preferences.getString("fcmkey","0");
            System.out.println("checkauth fcmy"+fcmkey);
        }


        @Override
        protected String doInBackground(Void... params) {

            String status=NetworkUtil.getConnectivityStatusString(WriteWeeklyActivity.this);
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
                ActivityCompat.finishAffinity(WriteWeeklyActivity.this);
            }else if(message.equals("wifi로 바꾸어 주세요.")){
                Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                if(message.equals("accept")){
                    //finish();
                    if(flag==0){//날짜 결정
                        new SendRequestDateHoliday().execute();
                    }
                    else if(flag==1){//전송
                        title = editTitle.getText().toString();
                        goal = editGoal.getText().toString();

                        monPlan = editMon.getText().toString();
                        monTotal = goalMon.getText().toString();

                        tuesPlan = editTues.getText().toString();
                        tuesTotal = goalTues.getText().toString();

                        wedPlan = editWed.getText().toString();
                        wedTotal = goalWed.getText().toString();

                        thursPlan = editThurs.getText().toString();
                        thursTotal = goalThurs.getText().toString();

                        friPlan = editFri.getText().toString();
                        friTotal = goalFri.getText().toString();
                        //주간계획 작성부분 넘김
                        new SendRequestWeekPlan().execute();

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
            //message에 따라서 결과과

        }
    }
}
