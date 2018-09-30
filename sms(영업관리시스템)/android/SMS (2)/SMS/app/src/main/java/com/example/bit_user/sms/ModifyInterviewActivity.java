package com.example.bit_user.sms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ModifyInterviewActivity extends AppCompatActivity {
    public static Activity miaActivity;
    SharedPreferences preferences;
    String fcmkey;
    private String userToken;
    private String userNo;
    private String consultindex;
    private String description;
    private String customerName;
    private String secondcustomerName;
    private String dayNo;
    private String regDate;

    private int flag=1;
    ArrayList<Customer> customers;
    ArrayList<Customer> customers2;

    int index;
    int index2;

    EditText editTitle;
    EditText editInterview;

    String interview;
    String title;

    TextView department;
    TextView name;
    TextView leader;
    TextView today;

    TextView representation;
    TextView address;

    Spinner spinner;
    Spinner spinner2;

    private int defaultSpinner;
    private int defaultSpinner2;

    Button btnCancel;
    Button btnSubmit;

    // 현재시간을 msec 으로 구한다.
    long now = System.currentTimeMillis();
    // 현재시간을 date 변수에 저장한다.
    Date date = new Date(now);
    // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
    SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    // nowDate 변수에 값을 저장한다.
    String formatDate = sdfNow.format(date);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_interview);

        miaActivity=ModifyInterviewActivity.this;
        preferences= getSharedPreferences("tokenAndHome",MODE_PRIVATE);
        fcmkey = preferences.getString("fcmkey","0");

        userToken = getIntent().getStringExtra("userToken");
        userNo = getIntent().getStringExtra("userNo");
        consultindex = getIntent().getStringExtra("index");
        dayNo = getIntent().getStringExtra("dayNo");
        regDate = getIntent().getStringExtra("regDate");

        //description = getIntent().getStringExtra("description");
        customerName = getIntent().getStringExtra("customerName");
        secondcustomerName = getIntent().getStringExtra("secondcustomerName");

        department = (TextView)findViewById(R.id.txtDep);
        name = (TextView)findViewById(R.id.txtName);
        leader = (TextView)findViewById(R.id.txtLeader);
        today = (TextView)findViewById(R.id.txtDate);
        today.setText(" 수정일 : "+formatDate);

        representation=(TextView)findViewById(R.id.cstRepre);
        address=(TextView)findViewById(R.id.cstAdd);

        editTitle=(EditText)findViewById(R.id.editTitle);
        editInterview = (EditText)findViewById(R.id.editInterview);

        editTitle.setText(getIntent().getStringExtra("title"));
        editInterview.setText(getIntent().getStringExtra("description"));

        spinner = (Spinner)findViewById(R.id.spinner);
        spinner.setPrompt("거래처 선택");

        spinner2 = (Spinner)findViewById(R.id.spinner2);
        spinner2.setPrompt("2차 거래처 선택");

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        new SendRequestinfo().execute();

        new SendRequestCustom().execute();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                representation.setText(" 대표자 : "+customers.get(position).getOwnerName());
                address.setText(" 주소 : "+customers.get(position).getAddress());
                index=customers.get(position).getNo();
                new SendRequestCustom().execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        onspinClick();
        onbtnClick();
    }


    public void onspinClick(){

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                index2=customers2.get(position).getNo();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    public void onbtnClick(){
        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnSubmit = (Button)findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Uid(userToken).execute();

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ModifyInterviewActivity.this,DialogCancelActivity.class);
                intent.putExtra("userToken",userToken);
                intent.putExtra("flag","ModifyInterview");
                startActivity(intent);
            }
        });

    }



    //수정 저장
    private class SendRequestModifyInterview extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url=  new ApiHost().getApi()+"modify/consultation";// "http://192.168.1.21:9990/modify/consultation";
        String message;

        @Override
        protected String doInBackground(Void... params) {

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPut put = new HttpPut(url);

                System.out.println("index확인 : "+consultindex);

                //#############################################################################
                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(8);
                nameValuePairs.add(new BasicNameValuePair("token",userToken));
                nameValuePairs.add(new BasicNameValuePair("userNo",userNo));
                nameValuePairs.add(new BasicNameValuePair("index",consultindex)); //게시글 번호
                nameValuePairs.add(new BasicNameValuePair("customerNo",String.valueOf(index)));
                nameValuePairs.add(new BasicNameValuePair("title",title));
                nameValuePairs.add(new BasicNameValuePair("description",interview));
                nameValuePairs.add(new BasicNameValuePair("secondcustomerNo",String.valueOf(index2)));
                if(!"no data".equals(dayNo))
                    nameValuePairs.add(new BasicNameValuePair("dayNo",dayNo));

                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
                put.setEntity(ent);

                //전송
                HttpResponse httpResponse = httpclient.execute(put);

                //응답
                HttpEntity resEntity = httpResponse.getEntity();

                //토큰 혹은 false를 message에 담고서 비교
                message= EntityUtils.toString(resEntity);
                System.out.println("결과 : "+message);


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
            Toast.makeText(getApplicationContext(),"상담일지가 수정되었습니다.",Toast.LENGTH_SHORT).show();

            finish();
        }

    }




    private class SendRequestCustom extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url = new ApiHost().getApi()+"write/consultation/listOfcustomer";//"http://192.168.1.21:9990/write/consultation/listOfcustomer";
        String message;

        @Override
        protected String doInBackground(Void... params) {
            String status=NetworkUtil.getConnectivityStatusString(ModifyInterviewActivity.this);
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
                nameValuePairs.add(new BasicNameValuePair("token", userToken));
                nameValuePairs.add(new BasicNameValuePair("flag",String.valueOf(flag)));

                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
                post.setEntity(ent);

                //전송
                HttpResponse httpResponse = httpclient.execute(post);

                //응답
                HttpEntity resEntity = httpResponse.getEntity();

                //토큰 혹은 false를 message에 담고서 비교
                message = EntityUtils.toString(resEntity);
                System.out.println("요청내용 전달받음 : " + message);


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
                ActivityCompat.finishAffinity(ModifyInterviewActivity.this);
            }else if(message.equals("wifi로 바꾸어 주세요.")){
                Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                ChangeJsonCustomerInfo(message);
            }

        }

    }

    public void ChangeJsonCustomerInfo(String message) {
        try {

            JSONArray jsonArray = new JSONArray(message);

            if(flag==1){
                customers = new ArrayList<Customer>();
                ArrayList<String> spinnerItem = new ArrayList<String>();

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    Customer customer = new Customer();

                    customer.setNo(jsonObject.getInt("no"));
                    customer.setName(jsonObject.getString("name"));
                    customer.setOwnerName(jsonObject.getString("owner_name"));
                    customer.setAddress(jsonObject.getString("address"));

                    if(customerName.equals(jsonObject.getString("name")))
                        defaultSpinner=i;

                    customers.add(customer);
                    spinnerItem.add(customer.getName());
                    flag=2;

                }
                System.out.println(spinnerItem);
                ArrayAdapter adapter = new ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,spinnerItem);
                spinner.setAdapter(adapter);
                spinner.setSelection(defaultSpinner);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            }

            else{
                customers2 = new ArrayList<Customer>();
                ArrayList<String> spinnerItem2 = new ArrayList<String>();

                for(int i=0; i<jsonArray.length(); i++){

                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    Customer customer2 = new Customer();

                    customer2.setNo(jsonObject.getInt("no"));
                    customer2.setName(jsonObject.getString("name"));
                    customer2.setOwnerName(jsonObject.getString("owner_name"));
                    customer2.setAddress(jsonObject.getString("address"));

                    if(secondcustomerName.equals(jsonObject.getString("name")))
                        defaultSpinner2=i;

                    customers2.add(customer2);
                    spinnerItem2.add(customer2.getName());

                }
                System.out.println(spinnerItem2);
                ArrayAdapter adapter = new ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,spinnerItem2);
                spinner2.setAdapter(adapter);
                spinner2.setSelection(defaultSpinner2);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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

            name.setText(" 이름 : "+user.getName());
            department.setText(" 소속 : "+user.getDepName()+" 팀");
            leader.setText(" ("+user.getTeamleader()+")");


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

            String status=NetworkUtil.getConnectivityStatusString(ModifyInterviewActivity.this);
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
                ActivityCompat.finishAffinity(ModifyInterviewActivity.this);
            }else if(message.equals("wifi로 바꾸어 주세요.")){
                Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                //message에 따라서 결과과
                if(message.equals("accept")){
                    //finish();
                    title = editTitle.getText().toString();
                    interview = editInterview.getText().toString();
                    System.out.println("index"+index);
                    System.out.println("index2"+index2);
                    new SendRequestModifyInterview().execute();

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

