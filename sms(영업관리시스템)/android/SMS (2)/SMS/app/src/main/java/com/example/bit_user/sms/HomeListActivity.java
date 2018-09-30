package com.example.bit_user.sms;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

public class HomeListActivity extends AppCompatActivity{

    private String userToken;
    private String regDate;
    private int pos;
    private LayoutInflater inflater;
    private int index;
    private int index1;//주간
    private int index2;//일간
    private int index3;//상담


    Spinner spinDep;
    Spinner spinName;
    Spinner spinPlan;

    ListView listView;

    String url;
    int position;

    ArrayList<Home_List> items;
    ArrayAdapter<Home_List> listAdapter;
    boolean lastitemVisibleFlag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_list);

        userToken = getIntent().getStringExtra("userToken");

        spinPlan = (Spinner)findViewById(R.id.spinPlan);
        spinName = (Spinner)findViewById(R.id.spinName);

        listView = (ListView)findViewById(R.id.listView);

        //footer등록 (setadapter이전에에)
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //listView.addFooterView(inflater.inflate());


        ArrayList<String> spinnerItem = new ArrayList<String>();
        spinnerItem.add("[ 계획서 선택 ]");
        spinnerItem.add("주간 계획서");
        spinnerItem.add("일일 보고서");
        spinnerItem.add("상담 일지");
        ArrayAdapter adapter = new ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,spinnerItem);
        spinPlan.setAdapter(adapter);

        spinPlan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onSpinnerClick(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        lastitemVisibleFlag=false;
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(scrollState== AbsListView.OnScrollListener.SCROLL_STATE_IDLE&&lastitemVisibleFlag){
                    new SendRequestHomeList().execute();
                }
            }


            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastitemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);

            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        listAdapter=new HomeListAdapter(getBaseContext(),items);
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
                items = new ArrayList<Home_List>();
            }
            //일일보고
            else if(position==2){
                index2=0;
                items = new ArrayList<Home_List>();
            }
            else if(position==3){//상담일지
                index3=0;
                items = new ArrayList<Home_List>();
            }
            new SendRequestHomeList().execute();
        }
    }


    private class SendRequestHomeList extends AsyncTask<Void,Void,String> { //background,progress,execcute

        String message;

        @Override
        protected String doInBackground(Void... params) {
            String status=NetworkUtil.getConnectivityStatusString(HomeListActivity.this);
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
                if(position==1){
                    index=index1;
                    url= new ApiHost().getApi()+"view/weekplanner"; //"http://192.168.1.21:9990/view/weekplanner";
                }
                //일일보고
                else if(position==2){
                    index=index2;
                    url= new ApiHost().getApi()+"view/dayreport"; //"http://192.168.1.21:9990/view/dayreport";
                }
                else if(position==3){//상담일지
                    index=index3;
                    url= new ApiHost().getApi()+"view/consultation";//"http://192.168.1.21:9990/view/consultation";
                }
            }

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url);
                System.out.println("index : "+index);
                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("token",userToken));//보고서 구분 바디
                if(index>0){//리스트 갱신호출
                    ////////////////////////////////////index초기값 0으로 설정할까
                    nameValuePairs.add(new BasicNameValuePair("index",String.valueOf(index)));
                }


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
                ActivityCompat.finishAffinity(HomeListActivity.this);
            }else if(message.equals("wifi로 바꾸어 주세요.")){
                Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
            }else{
                JSONArray jsonArray = null;

                Home_List homelistObject;
                if("no data".equals(message)) {
                    Toast.makeText(getApplicationContext(),"더이상 불러올 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {

                    jsonArray = new JSONArray(message);

                    if(position==2){//일일보고서의 경우 승인여부 포함

                        for(int i=0; i<jsonArray.length(); i++){

                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            Object obj = jsonObject.getString("regDate");
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

                            Object obj = jsonObject.getString("regDate");
                            regDate = String.valueOf(obj);

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

                            Object obj = jsonObject.getString("regDate");
                            regDate = String.valueOf(obj);

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
                    listView.setAdapter(listAdapter);
                    listView.setChoiceMode(listView.CHOICE_MODE_SINGLE);

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
                                    intent = new Intent(HomeListActivity.this,ReadWeeklyActivity.class);
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
                                    intent = new Intent(HomeListActivity.this,ReadDailyActivity.class);
                                    intent.putExtra("token",userToken);
                                    intent.putExtra("index",itemNo);
                                    intent.putExtra("regDate",regDate);
                                    startActivity(intent);

                                    break;
                                }
                                case 3:{//consultation
                                    System.out.println("상담일지 상세보기 ㄱ");
                                    Intent intent;
                                    intent = new Intent(HomeListActivity.this,ReadInterviewActivity.class);
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
    }
}
