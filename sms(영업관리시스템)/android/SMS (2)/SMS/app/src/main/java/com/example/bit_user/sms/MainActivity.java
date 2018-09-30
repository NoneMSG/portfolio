package com.example.bit_user.sms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
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

import static com.example.bit_user.sms.R.drawable.ic_add_white;
import static com.example.bit_user.sms.R.drawable.ic_clear_white;
import static com.example.bit_user.sms.R.drawable.ic_main_calendar_selected;
import static com.example.bit_user.sms.R.drawable.ic_main_calendar_unselected;
import static com.example.bit_user.sms.R.drawable.ic_main_graph_selected;
import static com.example.bit_user.sms.R.drawable.ic_main_graph_unselected;
import static com.example.bit_user.sms.R.drawable.ic_main_list_selected;
import static com.example.bit_user.sms.R.drawable.ic_main_list_unselected;
import static com.example.bit_user.sms.R.drawable.main_list_selected;
import static com.example.bit_user.sms.R.drawable.main_list_unselected;
import static com.example.bit_user.sms.R.id.action_Weather;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    MenuItem menuItem;

    public static Activity mainActivity;
    static private Context context;
    private String ROLE;
    SharedPreferences preferences;

    Menu menu;
    LocationManager locationManager;

    boolean isGPSEnabled=false; //현재 GPS 사용유무
    boolean isNetworkEnabled=false; //현재 네트워크 사용유무
    boolean isGetLocation=false; //GPS 상태값값

    Location location;
    double lat; //위도
    double lon; //경도

    private GpsInfo gps;

    private String userToken;
    private String mainPosition;
    private String uidFlag="";

    private int flag=1;
    private int weatherflag=0;
    private String weathercode;


    FloatingActionButton fab[]=new FloatingActionButton[5];
    TextView txv[] = new TextView[3];
    //Button fab4;
    ImageButton btn[] = new ImageButton[3];
    ViewPager viewPager = null;

    LinearLayout fablayoutinner;

    Handler handler = null;
    int p;    //페이지번호
    int v = 1;    //화면 전환 뱡향

    protected void onCreate(Bundle savedInstanceState) {

//        if(getSharedPreferences("setting",MODE_PRIVATE).getString("key","0").equals("0")){
//            startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
//        }
//        SharedPreferences config = getSharedPreferences("setting",Context.MODE_PRIVATE);
//        SharedPreferences.Editor configeditor;
//        configeditor = config.edit();
//        configeditor.putString("key","garbage");
//        configeditor.commit();

        preferences = getSharedPreferences("tokenAndHome",MODE_PRIVATE);
        super.onCreate(savedInstanceState);

      /*  //#############네트워크 검사#####################
        String status=NetworkUtil.getConnectivityStatusString(this);
        if("네트워크가 연결되지 않아 종료됩니다.".equals(status)){
            Toast.makeText(getApplicationContext(), "네트워크가 연결되지 않아 종료됩니다.", Toast.LENGTH_SHORT).show();
            //android.os.Process.killProcess(android.os.Process.myPid());
            ActivityCompat.finishAffinity(this);
            return;
        }
        else if("wifi로 바꾸어 주세요.".equals(status)){
            Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
        }*/

        setContentView(R.layout.activity_main);
        context = this;
        mainActivity=MainActivity.this;

        userToken = getIntent().getStringExtra("userToken");
        //mainPosition = getIntent().getStringExtra("mainPosition");
        p=Integer.parseInt(preferences.getString("home","0"));
        System.out.println("user Hom position : =====" + p);
        System.out.println("fcmkey==== "+preferences.getString("fcmkey","0"));

        fablayoutinner=(LinearLayout)findViewById(R.id.fablayoutinner);
        fablayoutinner.setVisibility(View.GONE);

        fab[0]=(FloatingActionButton) findViewById(R.id.fab1);
        fab[1]=(FloatingActionButton) findViewById(R.id.fab2);
        fab[2]=(FloatingActionButton) findViewById(R.id.fab3);
        fab[3]=(FloatingActionButton) findViewById(R.id.fab4);
        fab[4]=(FloatingActionButton) findViewById(R.id.fab5);

        txv[0] = (TextView) findViewById(R.id.weeklable);
        txv[1] = (TextView) findViewById(R.id.dailylable);
        txv[2] = (TextView) findViewById(R.id.consullable);

        for(int i = 0 ; i <fab.length; i++){
            fab[i].setOnClickListener(this);
        }

        //viewPager
        viewPager = (ViewPager) findViewById(R.id.viewPager);
//        viewPager.setOnClickListener(new View.OnClickListener(){
//
//            @Override
//            public void onClick(View v) {
//                findViewById(R.id.fablayoutinner).setVisibility(View.GONE);
//            }
//        });

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(p);


        btn[0] = (ImageButton)findViewById(R.id.btn_a);
        btn[1] = (ImageButton) findViewById(R.id.btn_b);
        btn[2] = (ImageButton) findViewById(R.id.btn_c);

        for (int i = 0; i < btn.length; i++) {
            btn[i].setOnClickListener(this);
        }

        setViewPagerButton(p);
        /*handler = new Handler() {

            public void handleMessage(android.os.Message msg) {
                if (p == 0) {
                    viewPager.setCurrentItem(1);
                    p++;
                    v = 1;
                }
                if (p == 1 && v == 0) {
                    viewPager.setCurrentItem(1);
                    p--;
                }
                if (p == 1 && v == 1) {
                    viewPager.setCurrentItem(2);
                    p++;
                }
                if (p == 2) {
                    viewPager.setCurrentItem(1);
                    p--;
                    v = 0;
                }
            }
        };*/

        fablayoutinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("!!!!!!!!!");
                fablayoutinner.setVisibility(View.GONE);
                fablayoutinner.setBackgroundColor(Color.alpha(225));
                fab[0].setImageDrawable(getDrawable(ic_add_white));
                flag=1;
            }
        });

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //System.out.println("position"+position);
            }

            @Override
            public void onPageSelected(int position) {
                setViewPagerButton(position);
                if( position==2 ){
                    ((Spinner) viewPager.getRootView().findViewById(R.id.spinPlan)).setSelection(0);
                    ((ListView) viewPager.getRootView().findViewById(R.id.listView)).setAdapter(null);
                }
                if(flag==0){
                    fablayoutinner.setVisibility(View.GONE);
                    fablayoutinner.setBackgroundColor(Color.alpha(225));
                    fab[0].setImageDrawable(getDrawable(ic_add_white));
                    flag=1;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // System.out.println("state"+state);
            }
        });


        gps = new GpsInfo(MainActivity.this);
        // GPS 사용유무 가져오기
        if (gps.isGetLocation()) {

            lat = gps.getLatitude();
            lon = gps.getLongitude();

            System.out.println("*****************************lat : "+lat);
            System.out.println("*****************************lon : "+lon);
        }
        new Weather().execute();
        //new Uid(userToken).execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu=menu;

        //##################################날씨 아이콘 바꾸기##################################################
        if(weatherflag==1){
            menuItem = menu.getItem(0);

            switch (weathercode){
                case "SKY_D01" : //맑음
                    menuItem.setIcon(R.drawable.sun);
                    break;
                case "SKY_D02" : //구름 조금
                    menuItem.setIcon(R.drawable.partly_cloudy);
                    break;
                case "SKY_D03" : //구름 많음
                    menuItem.setIcon(R.drawable.clouds);
                    break;
                case "SKY_D04" : //흐림
                    menuItem.setIcon(R.drawable.clouds);
                    break;
                case "SKY_D05" : //비
                    menuItem.setIcon(R.drawable.rain);
                    break;
                case "SKY_D06" : //눈
                    menuItem.setIcon(R.drawable.snow);
                    break;
                case "SKY_D07" : //비 또는 눈
                    menuItem.setIcon(R.drawable.snoworrain);
                    break;
            }
        }
        else{
            getMenuInflater().inflate(R.menu.weather,menu);
            getMenuInflater().inflate(R.menu.home,menu);
            getMenuInflater().inflate(R.menu.homemenu,menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //########################마이홈 설정########################
        if(id==R.id.action_MyHome){
            uidFlag="action_MyHome";
            new Uid(userToken).execute();

        }
        //########################프로필 보기 및 비밀번호 변경########################
        else if(id==R.id.action_Profile){
            uidFlag="action_Profile";
            new Uid(userToken).execute();

        }
        //##################로그아웃########################
        else if(id==R.id.action_Logout){
            new SendRequestLogout().execute();
        }

        else if(id==R.id.action_Gohome){
           /* 불안정
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.putExtra("userToken",userToken);
            startActivity(intent);
            finish();*/
            //onResume();
            viewPager.setCurrentItem(p);
            setViewPagerButton(p);
        }
        else if(id== action_Weather){
            //new Weather().execute();
        }

        return super.onOptionsItemSelected(item);
    }
    public void setViewPagerButton(int p){
        switch (p){
            case 0:
                btn[0].setBackground(getDrawable(main_list_selected));
                btn[1].setBackground(getDrawable(main_list_unselected));
                btn[2].setBackground(getDrawable(main_list_unselected));

                btn[1].setImageDrawable(getDrawable(ic_main_calendar_unselected));
                btn[0].setImageDrawable(getDrawable(ic_main_graph_selected));
                btn[2].setImageDrawable(getDrawable(ic_main_list_unselected));
                break;
            case 1:
                btn[0].setBackground(getDrawable(main_list_unselected));
                btn[1].setBackground(getDrawable(main_list_selected));
                btn[2].setBackground(getDrawable(main_list_unselected));

                btn[1].setImageDrawable(getDrawable(ic_main_calendar_selected));
                btn[0].setImageDrawable(getDrawable(ic_main_graph_unselected));
                btn[2].setImageDrawable(getDrawable(ic_main_list_unselected));
                break;
            case 2:
                btn[0].setBackground(getDrawable(main_list_unselected));
                btn[1].setBackground(getDrawable(main_list_unselected));
                btn[2].setBackground(getDrawable(main_list_selected));

                btn[1].setImageDrawable(getDrawable(ic_main_calendar_unselected));
                btn[0].setImageDrawable(getDrawable(ic_main_graph_unselected));
                btn[2].setImageDrawable(getDrawable(ic_main_list_selected));
                break;
        }
    }

    @Override
    protected void onResumeFragments() {
        System.out.println("111111111111111111111111111111111111111111111111");
        super.onResumeFragments();
    }


    @Override
    public void onClick(View v) {
        Intent intent =null;
        new Uid(userToken).execute();
        switch (v.getId()) {

            case R.id.btn_a:
                viewPager.setCurrentItem(0);
                fablayoutinner.setVisibility(View.GONE);
                fablayoutinner.setBackgroundColor(Color.alpha(225));

                setViewPagerButton(0);

                break;
            case R.id.btn_b:
                viewPager.setCurrentItem(1);
                fablayoutinner.setVisibility(View.GONE);
                fablayoutinner.setBackgroundColor(Color.alpha(225));

                setViewPagerButton(1);

                break;
            case R.id.btn_c:
                viewPager.setCurrentItem(2);
                fablayoutinner.setVisibility(View.GONE);
                fablayoutinner.setBackgroundColor(Color.alpha(225));

                setViewPagerButton(2);

                break;
            case R.id.fab1:
                if(flag==1){ //작성 메뉴 버튼 클릭
                    fablayoutinner.setVisibility(View.VISIBLE);
                    fablayoutinner.setBackgroundColor(Color.argb(210,0,0,0));
                    //+였던 아이콘 x로 변경
                    fab[0].setImageDrawable(getDrawable(ic_clear_white));
                    flag=0;
                }
                else if(flag==0){
                    fablayoutinner.setVisibility(View.GONE);
                    fablayoutinner.setBackgroundColor(Color.alpha(225));
                    fab[0].setImageDrawable(getDrawable(ic_add_white));
                    flag=1;
                }
                break;
            case R.id.fab2:
                intent = new Intent(MainActivity.this, WriteWeeklyActivity.class);
                intent.putExtra("userToken",userToken);
                startActivity(intent);
                onClick(fab[0]);
                break;
            case R.id.fab3:
                intent = new Intent(MainActivity.this, WriteDailyActivity.class);
                intent.putExtra("userToken",userToken);
                startActivity(intent);
                onClick(fab[0]);
                break;
            case R.id.fab4:
                intent = new Intent(MainActivity.this, WriteInterviewActivity.class);
                intent.putExtra("userToken",userToken);
                startActivity(intent);
                onClick(fab[0]);
                break;
            case R.id.fab5:
                intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra("userToken",userToken);
                startActivity(intent);
                onClick(fab[0]);
                break;

            default:
                break;

        }

    }


    private class SendRequestLogout extends AsyncTask<Void,Void,String> { //background,progress,execcute
        SharedPreferences preferences = context.getSharedPreferences("tokenAndHome",context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        String url= new ApiHost().getApi()+"logout"; //"http://192.168.1.21:9990/logout";
        String message;

        @Override
        protected String doInBackground(Void... params) {

            String status=NetworkUtil.getConnectivityStatusString(MainActivity.this);
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
                ActivityCompat.finishAffinity(MainActivity.this);
            }else if(message.equals("wifi로 바꾸어 주세요.")){
                Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                if("logout".equals(message)){
                    System.out.println(preferences.getString("token",""));
                    System.out.println(preferences.getString("home",""));

                    editor.clear();
                    editor.commit();

                    Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                    startActivity(intent);

                    finish();
                }
                else if("can not logout".equals(message)){
                    Toast.makeText(getApplicationContext(),"다른 기기에서 로그인되어있습니다. 새로 로그인 해주세요",Toast.LENGTH_LONG).show();
                    editor.clear();
                    editor.commit();
                    Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }
    }


    private class Uid extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url=new ApiHost().getApi()+"uidcheck"; //"http://192.168.1.21:9990/uidcheck";
        String message;
        String token;
        String fcmkey;
        SharedPreferences preferences;

        public Uid(String validationToken) {
            token = validationToken;
            preferences = getSharedPreferences("tokenAndHome", Context.MODE_PRIVATE);
            fcmkey = preferences.getString("fcmkey", "0");
            System.out.println("checkauth fcmy" + fcmkey);
        }

        /*@Override
        protected void onPreExecute() {
            //#############네트워크 검사#####################
            String status=NetworkUtil.getConnectivityStatusString(MainActivity.this);
            if("네트워크가 연결되지 않아 종료됩니다.".equals(status)){
                Toast.makeText(getApplicationContext(), "네트워크가 연결되지 않아 종료됩니다.", Toast.LENGTH_SHORT).show();
                //android.os.Process.killProcess(android.os.Process.myPid());
                ActivityCompat.finishAffinity(MainActivity.this);
                return;
            }
            else if("wifi로 바꾸어 주세요.".equals(status)){
                Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

        }*/

        @Override
        protected String doInBackground(Void... params) {

            String status=NetworkUtil.getConnectivityStatusString(MainActivity.this);
            if("네트워크가 연결되지 않아 종료됩니다.".equals(status)){
                //Toast.makeText(getApplicationContext(), "네트워크가 연결되지 않았습니다.", Toast.LENGTH_SHORT).show();
                //android.os.Process.killProcess(android.os.Process.myPid());
                //ActivityCompat.finishAffinity(MainActivity.this);
                message = status;
                return message;
            }
            System.out.println(" fcmkey : "+fcmkey);
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
                ActivityCompat.finishAffinity(MainActivity.this);
            }else if(message.equals("wifi로 바꾸어 주세요.")){
                //Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                //message에 따라서 결과과
                if(message.equals("accept")){
                    if("action_MyHome".equals(uidFlag)){
                        uidFlag="";
                        Intent intent = new Intent(MainActivity.this,MyhomeActivity.class);
                        intent.putExtra("userToken",userToken);
                        startActivity(intent);
                    }
                    else if("action_Profile".equals(uidFlag)){
                        uidFlag="";
                        Intent intent = new Intent(MainActivity.this,ProfileActivity.class);
                        intent.putExtra("userToken",userToken);
                        startActivity(intent);
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(),"다른 장치에서 로그인되어있습니다. 새로 로그인 해주세요.",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
                //finish();
                new SendRequestLeaderinfo().execute();
                return;
            }

        }
    }


    //back button방지
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK && flag==0){
            System.out.println("backKey");
            fablayoutinner.setVisibility(View.GONE);
            fablayoutinner.setBackgroundColor(Color.alpha(225));
            fab[0].setImageDrawable(getDrawable(ic_add_white));
            flag=1;
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
    private class SendRequestLeaderinfo extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url= new ApiHost().getApi()+"checkauthority";//"http://192.168.1.21:9990/checkauthority";
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
                System.out.println("요청내용 전달받음4 : "+message);


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
            if("0".equals(message)) {//사원일경우 사원의 정보 가져옴
                ROLE = "EMPLOYEE";
            }
            else{
                ROLE="LEADER";
                fab[1].setVisibility(View.GONE);
                fab[2].setVisibility(View.GONE);
                fab[3].setVisibility(View.GONE);
                txv[0].setVisibility(View.GONE);
                txv[1].setVisibility(View.GONE);
                txv[2].setVisibility(View.GONE);
            }
        }
    }


    //######################날씨정보 가져오기
    private class Weather extends AsyncTask<Void,Void,String> {
        //"http://apis.skplanetx.com/weather/summary?version=1&lat=37.57140000000&lon=126.9658000000";

        String url= "http://apis.skplanetx.com/weather/summary?version=1&lat=37.497980&lon=127.027529";
        String sktkey = "5979a075-1740-3144-82f0-4fabcee96e50";
        String message;

        @Override
        protected String doInBackground(Void... params) {

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(url);

                httpget.addHeader("appKey",sktkey);
                //httpget.setHeader("appKey",appkey);

                //전송
                HttpResponse httpResponse = httpclient.execute(httpget);

                //응답
                HttpEntity resEntity = httpResponse.getEntity();

                //토큰 혹은 false를 message에 담고서 비교
                message= EntityUtils.toString(resEntity);
                System.out.println("요청내용 전달받음5 : "+message);


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
            try {

                JSONObject jo = new JSONObject(message);
                //JSONArray ja = new JSONArray(jo.get("weather"));
                System.out.println("요청내용 전달받음6 : "+jo.get("weather"));
                String temp = jo.get("weather").toString();

                JSONObject jo1 = new JSONObject(temp);
                String temp2 = jo1.get("summary").toString();
                //JSONArray ja = new JSONArray(jo1.get("summary"));
                System.out.println("요청내용 전달받음7 : "+temp2);
                JSONArray ja = new JSONArray(temp2);
                System.out.println("요청내용 전달받음8 : "+ja.get(0));
                JSONObject jo2 = new JSONObject(ja.get(0).toString());

                //System.out.println("요청내용 전달받음9 : "+jo2.get("today"));

                String todayweather = jo2.get("today").toString();
                JSONObject jo3 = new JSONObject(todayweather);
                //System.out.println(jo3.get("sky"));
                JSONObject sky =new JSONObject(jo3.get("sky").toString());

                System.out.println("날씨 : "+sky.get("name"));
                System.out.println("날씨코드 : "+sky.get("code"));

                weatherflag=1;
                weathercode=String.valueOf(sky.get("code"));
                onCreateOptionsMenu(menu);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }


   public void selectWeather(String code){
       MenuItem menuItem;
       int id;


        switch (weathercode){
            case "SKY_D01" : //맑음
                System.out.println("===========================================================================================================");
                id = R.id.action_Weather;
                menuItem = menu.getItem(2);
                menuItem.setIcon(R.drawable.sun);
                //menuItem.setIcon(getResources().getDrawable(R.drawable.sun));
                //getMenuInflater().inflate(R.menu.weather, menu);
                break;
            case "SKY_D02" : //구름 조금
                id = R.id.action_Weather;
                menu.getItem(id).setIcon(getDrawable(R.drawable.partly_cloudy));
                break;
            case "SKY_D03" : //구름 많음
                id = R.id.action_Weather;
                menu.getItem(id).setIcon(getDrawable(R.drawable.clouds));
                break;
            case "SKY_D04" : //흐림
                id = R.id.action_Weather;
                menu.getItem(id).setIcon(getDrawable(R.drawable.clouds));
                break;
            case "SKY_D05" : //비
                id = R.id.action_Weather;
                menu.getItem(id).setIcon(getDrawable(R.drawable.rain));
                break;
            case "SKY_D06" : //눈
                id = R.id.action_Weather;
                menu.getItem(id).setIcon(getDrawable(R.drawable.snow));
                break;
            case "SKY_D07" : //비 또는 눈
                id = R.id.action_Weather;
                menu.getItem(id).setIcon(getDrawable(R.drawable.snoworrain));
                break;
        }
       weatherflag=0;
    }
}