package com.example.bit_user.sms;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
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

import static java.lang.String.valueOf;

public class SearchActivity extends AppCompatActivity {

    int position;

    SharedPreferences preferences;
    String fcmkey;
    private boolean flag0=false;

    private int index;
    private int index1;//주간
    private int index2;//일간
    private int index3;//상담
    private String uidFlag="";

    private int pos;

    private String flag;
    private String userToken;
    private String regDate;

    ArrayList<Home_List> items = new ArrayList<Home_List>();
    ArrayAdapter<Home_List> listAdapter;

    Spinner spinner;
    EditText editSearch;
    Button btnSearch;
    Button btnCancel;
    ListView searchListView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        preferences= getSharedPreferences("tokenAndHome",MODE_PRIVATE);
        fcmkey = preferences.getString("fcmkey","0");

        userToken = getIntent().getStringExtra("userToken");
        spinner=(Spinner)findViewById(R.id.spinner);
        editSearch=(EditText)findViewById(R.id.editSearch);
        btnSearch=(Button)findViewById(R.id.btnSearch);
        btnCancel=(Button)findViewById(R.id.btnCancel);
        editSearch.setSingleLine();
        editSearch.setMaxLines(1);
        editSearch.setEnabled(false);
        editSearch.setFocusable(false);
        searchListView=(ListView) findViewById(R.id.searchListView);

        ArrayList<String> spinnerItem = new ArrayList<String>();
        spinnerItem.add("[ 계획서 선택 ]");
        spinnerItem.add("주간 계획서");
        spinnerItem.add("일일 보고서");
        spinnerItem.add("상담 일지");
        ArrayAdapter adapter = new ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,spinnerItem);
        spinner.setAdapter(adapter);

        new Uid(userToken).execute();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onSpinnerClick(position);
                if(position!=0){
                    editSearch.setFocusable(true);
                    editSearch.setFocusableInTouchMode(true);
                    editSearch.requestFocus();
                }
                if(position==0){
                    editSearch.setFocusable(false);
                    editSearch.setFocusableInTouchMode(false);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(),0);
                    editSearch.setText(null);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        editSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(v,0);
            }
        });
        editSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getKeyCode()==KeyEvent.KEYCODE_ENTER){
                    System.out.println("endtered!");
                    btnSearch.performClick();
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home,menu);
        getMenuInflater().inflate(R.menu.homemenu,menu);

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
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onbtnClick();
    }

    public void onSpinnerClick(int position){
        this.position = position;
        index=0;
        if(position==0)
            return;
        else{
            //주간
            if(position==1){
                index1=0;
                flag="0";
                items = new ArrayList<Home_List>();
            }
            //일일보고
            else if(position==2){
                index2=0;
                flag="1";
                items = new ArrayList<Home_List>();
            }
            else if(position==3){//상담일지
                index3=0;
                flag="2";
                items = new ArrayList<Home_List>();
            }
        }
        editSearch.setEnabled(true);
    }


    public void onbtnClick(){


        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if("".equals(editSearch.getText().toString())) {
                    return;
                }
                else {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                    new Uid(userToken).execute();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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

                MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;
                mainActivity.finish();

                Intent intent = new Intent(SearchActivity.this,LoginActivity.class);
                startActivity(intent);

                finish();
            }
            else if("can not logout".equals(message)){
                Toast.makeText(getApplicationContext(),"다른 기기에서 로그인되어있습니다. 새로 로그인 해주세요",Toast.LENGTH_LONG).show();
                editor.clear();
                editor.commit();

                MainActivity mainActivity = (MainActivity)MainActivity.mainActivity;
                mainActivity.finish();

                Intent intent = new Intent(SearchActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }




    private class SendRequestHomeList extends AsyncTask<Void,Void,String> { //background,progress,execcute

        String message;
        String keyword = editSearch.getText().toString();
        @Override
        protected String doInBackground(Void... params) {
            index=index1;
            String url= new ApiHost().getApi()+"view/searching";//"http://192.168.1.21:9990/view/searching";


            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url);
                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("token",userToken));
                nameValuePairs.add(new BasicNameValuePair("flag",flag));
                nameValuePairs.add(new BasicNameValuePair("keyword",keyword));


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
            //키워드 해당 리스트 뿌려주기

            JSONArray jsonArray = null;
            Home_List homelistObject;
            if("no data".equals(message)) {
                Toast.makeText(getApplicationContext(),"불러올 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                items.clear();
                listAdapter = new HomeListAdapter(getBaseContext(),items);
                searchListView.setAdapter(listAdapter);
                return;
            }


            try {

                jsonArray = new JSONArray(message);

                if(position==2){//일일보고서의 경우 승인여부 포함

                    for(int i=0; i<jsonArray.length(); i++){

                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        Object obj = jsonObject.getString("reg_date");
                        regDate = String.valueOf(obj);

                        homelistObject = new Home_List(
                                jsonObject.getInt("no"),
                                jsonObject.getString("title"),
                                regDate.substring(0,10),
                                jsonObject.getString("confirm"));

                        System.out.println(homelistObject);
                        //index갱신하면서 마지막 번호 가져옴
                        index2=jsonObject.getInt("no");
                        items.add(homelistObject);
                    }System.out.println("일간 인덱스 : "+index2);
                }
                else if(position==3){

                    for(int i=0; i<jsonArray.length(); i++){

                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        Object obj = jsonObject.getString("reg_date");
                        regDate = valueOf(obj);

                        homelistObject = new Home_List(
                                jsonObject.getInt("no"),
                                jsonObject.getString("title"),
                                regDate.substring(0,10));

                        System.out.println(homelistObject);
                        //index갱신하면서 마지막 번호 가져옴
                        index3=jsonObject.getInt("no");

                        items.add(homelistObject);
                        System.out.println("모든 items"+items);
                    }
                    System.out.println("상담 인덱스 : "+index3);
                }
                else{

                    for(int i=0; i<jsonArray.length(); i++){

                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        Object obj = jsonObject.getString("reg_date");
                        regDate = valueOf(obj);

                        homelistObject = new Home_List(
                                jsonObject.getInt("no"),
                                jsonObject.getString("title"),
                                regDate.substring(0,10));

                        System.out.println(homelistObject);
                        //index갱신하면서 마지막 번호 가져옴
                        index1=jsonObject.getInt("no");

                        items.add(homelistObject);

                        System.out.println("모든 items"+items);
                    }
                    System.out.println("주간 인덱스 : "+index1);
                }

                System.out.println("dddddddddddddd"+position);
                pos=position;

                listAdapter = new HomeListAdapter(getBaseContext(),items);
                searchListView.setAdapter(listAdapter);
                searchListView.setChoiceMode(searchListView.CHOICE_MODE_SINGLE);

                searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //System.out.println("주간일지 상세보기 ㄱㄱㄱㄱ");
                        String itemNo = valueOf(listAdapter.getItem(position).getNo());
                        //regDate=homelistObject.getWriteDate();
                        //번호 넘기고 정보에대한 상세보기 페이지 넘기기

                        System.out.println(pos);
                        switch (pos){
                            case 1:{//week
                                System.out.println("주간일지 상세보기 ㄱ");
                                Intent intent;
                                intent = new Intent(SearchActivity.this,ReadWeeklyActivity.class);
                                intent.putExtra("token",userToken);
                                intent.putExtra("index",itemNo);
                                intent.putExtra("regDate",regDate);
                                System.out.println("index : "+itemNo+" date : "+regDate);
                                startActivity(intent);

                                break;
                            }
                            case 2:{//day
                                System.out.println("일일보고서 상세보기 ㄱ");
                                Intent intent;
                                intent = new Intent(SearchActivity.this,ReadDailyActivity.class);
                                intent.putExtra("token",userToken);
                                intent.putExtra("index",itemNo);
                                intent.putExtra("regDate",regDate);
                                startActivity(intent);

                                break;
                            }
                            case 3:{//consultation
                                System.out.println("상담일지 상세보기 ㄱ");
                                Intent intent;
                                intent = new Intent(SearchActivity.this,ReadInterviewActivity.class);
                                intent.putExtra("index",itemNo);
                                intent.putExtra("token",userToken);
                                intent.putExtra("regDate",regDate);
                                startActivity(intent);

                                break;
                            }
                        }

                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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

            String status=NetworkUtil.getConnectivityStatusString(SearchActivity.this);
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
            }catch (IOException e) {
                e.printStackTrace();
            }


            return message;
        }


        @Override
        protected void onPostExecute(String s) {
            if(message.equals("네트워크가 연결되지 않아 종료됩니다.")){
                Toast.makeText(getApplicationContext(), "네트워크가 연결되지 않아 종료됩니다.", Toast.LENGTH_SHORT).show();
                ActivityCompat.finishAffinity(SearchActivity.this);
            }else if(message.equals("wifi로 바꾸어 주세요.")){
                Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                //message에 따라서 결과과
                if(message.equals("accept")){
                    if("action_MyHome".equals(uidFlag)){
                        uidFlag="";
                        Intent intent = new Intent(SearchActivity.this,MyhomeActivity.class);
                        intent.putExtra("userToken",userToken);
                        startActivity(intent);
                    }
                    else if("action_Profile".equals(uidFlag)){
                        uidFlag="";
                        Intent intent = new Intent(SearchActivity.this,ProfileActivity.class);
                        intent.putExtra("userToken",userToken);
                        startActivity(intent);
                    }
                    else{
                        if(!items.isEmpty()){
                            items.clear();
                            new SendRequestHomeList().execute();
                        }
                        else if(!editSearch.getText().toString().isEmpty())
                            new SendRequestHomeList().execute();

                    }


                    //finish();
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
}
