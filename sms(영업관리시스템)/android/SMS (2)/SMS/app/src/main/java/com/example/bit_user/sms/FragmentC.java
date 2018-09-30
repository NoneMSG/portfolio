package com.example.bit_user.sms;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class FragmentC extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
    private Integer userNumber=-1;
    private String ROLE;
    private String flag;
    private boolean resumeFlag=false;
    ArrayList<String> spinnerTeamUser;
    ArrayList<TeamUser> listTeamUsers;
    TeamUser teamUserSearch;

    private String userToken ;
    private String regDate;

    private int pos;
    int position; //planposition
    int position2; //nameposition

    private LayoutInflater inflater;
    private int index;
    private int index1;//주간
    private int index2;//일간
    private int index3;//상담

    Context ctx;
    Spinner spinName;
    Spinner spinPlan;
    SwipeRefreshLayout refreshlayout;

    ListView listView=null;

    String url;


    ArrayList<Home_List> items;
    ArrayAdapter<Home_List> listAdapter;
    ArrayAdapter<Home_List> listAdapter2;
    boolean lastitemVisibleFlag;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx=getActivity().getBaseContext();
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //토큰값
        userToken = getActivity().getIntent().getStringExtra("userToken");

        System.out.println("========================================CCCCCCCCCCCCCC"+userToken);

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.activity_home_list,container, false);
        //HomeListActivity.this.startActivity();
        //final View header = (ViewGroup) inflater.inflate(R.layout.listview_header, null, false) ;


        spinPlan = (Spinner)rootView.findViewById(R.id.spinPlan);
        spinName = (Spinner)rootView.findViewById(R.id.spinName);

        listView = (ListView)rootView.findViewById(R.id.listView);
        //listView.addHeaderView(header);
        refreshlayout=(SwipeRefreshLayout)rootView.findViewById(R.id.refreshlayout);

        inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ArrayList<String> spinnerItem = new ArrayList<String>();
        spinnerItem.add("[ 계획서 선택 ]");
        spinnerItem.add("주간 계획서");
        spinnerItem.add("일일 보고서");
        spinnerItem.add("상담 일지");

        ArrayAdapter adapter = new ArrayAdapter(getActivity(),R.layout.support_simple_spinner_dropdown_item, spinnerItem);
        spinPlan.setAdapter(adapter);
        spinPlan.setSelection(0);


        spinnerTeamUser=new ArrayList<>();
        //spinnerTeamUser.add("[ 팀원 선택 ]");
        new SendRequestLeaderinfo().execute(); //권한 확인


        spinPlan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==0){
                    listView.setAdapter(null);
                }
                spinName.setSelection(0);
                System.out.println("clicked item"+position);
                System.out.println("clicked item"+listView.getAdapter());

                onSpinnerClick(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0){
                    //선택된 유저 번호 저장
                    userNumber=listTeamUsers.get(position-1).getNo();
                }else{
                    //팀원 선택시 유저번호(팀전체값)
                    userNumber=-1;
                }
                onSpinnerNameClick(position);
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
                    System.out.println("호출 1");
                    new SendRequestHomeList().execute();
                }
            }


            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastitemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);

            }
        });
        refreshlayout.setOnRefreshListener(this);
        //refreshlayout.canChildScrollUp();
        return rootView;
    }



    public void onSpinnerNameClick(int position){
        position2 = position;
        index=0;
        if(position2==0){
            userNumber=-1;
            return;
        }else{
            teamUserSearch = listTeamUsers.get(position2-1);
            new SendRequestHomeListUser().execute();
        }
    }

    public void onSpinnerClick(int position){
        this.position = position;
        index=0;
        if(position==0){
            flag="";
            userNumber=-1;
            items = new ArrayList<Home_List>();
            //return;
        }

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
            spinName.setEnabled(true);
            System.out.println("호출 2");
            new SendRequestHomeList().execute();
        }
    }

    @Override
    public void onRefresh() {

        //새로 가져오기
        index1=0;
        index2=0;
        index3=0;
        //items.clear();

        System.out.println("호출 : "+position+", "+position2);

        if(position==0 || position2==0){
            items.clear();
            flag="";
            userNumber=-1;
            refreshlayout.setRefreshing(false);
            return;
        }
        else{
            if(!items.isEmpty()){
                items.clear();
                listAdapter.notifyDataSetChanged();
                new SendRequestHomeList().execute();
                refreshlayout.setRefreshing(false);
            }
            else{
                items.clear();
                new SendRequestHomeList().execute();
                refreshlayout.setRefreshing(false);
            }

        }


    }

    //d이름 선택시
    //role 구분 : 팀장1 사원0
    private class SendRequestHomeListUser extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url=new ApiHost().getApi()+"view/spinner/serachbyName";//"http://192.168.1.21:9990/view/spinner/serachbyName";
        String message;

        @Override
        protected String doInBackground(Void... params) {
            String status=NetworkUtil.getConnectivityStatusString(getActivity());
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

                System.out.println("flag = "+flag);
                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("token",userToken));
                nameValuePairs.add(new BasicNameValuePair("flag",flag));
                nameValuePairs.add(new BasicNameValuePair("userNo",String.valueOf(teamUserSearch.getNo())));
                if(index>0)
                    nameValuePairs.add(new BasicNameValuePair("index",String.valueOf(index)));

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
                Toast.makeText(getActivity().getApplicationContext(), "네트워크가 연결되지 않아 종료됩니다.", Toast.LENGTH_SHORT).show();
                ActivityCompat.finishAffinity(getActivity());
            }else{
                JSONArray jsonArray = null;
                Home_List homelistObject;

                if("no data".equals(message) || "false".equals(message)) {
                    Toast.makeText(getActivity().getApplicationContext(),"불러올 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                    //spinName.setSelection(0);

                /*items.clear();

                listAdapter2 = new HomeListAdapter(ctx, items);

                listView.setAdapter(listAdapter2);
                listView.setChoiceMode(listView.CHOICE_MODE_SINGLE);*/

                    return;
                }

                try {

                    jsonArray = new JSONArray(message);
                    items.clear();
                    if (flag=="1") {//일일보고서의 경우 승인여부 포함
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            Object obj = jsonObject.getString("regDate");
                            regDate = valueOf(obj);

                            homelistObject = new Home_List(
                                    jsonObject.getInt("no"),
                                    jsonObject.getString("title"),
                                    regDate.substring(0, 10),
                                    jsonObject.getString("confirm"));

                            System.out.println(homelistObject);
                            //index갱신하면서 마지막 번호 가져옴
                            index2 = jsonObject.getInt("no");
                            items.add(homelistObject);
                        }

                        System.out.println("일일 인덱스 : "+index2);
                    } else if(flag=="2"){

                        for(int i=0; i<jsonArray.length(); i++){

                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            Object obj = jsonObject.getString("regDate");
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
                    else if(flag=="0"){//flag 0 주간일지

                        for(int i=0; i<jsonArray.length(); i++){

                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            Object obj = jsonObject.getString("regDate");
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
                    else{
                        items.clear();
                    }

                    System.out.println("dddddddddddddd" + position);


                    listAdapter2 = new HomeListAdapter(ctx, items);

                    listView.setAdapter(listAdapter2);
                    listView.setChoiceMode(listView.CHOICE_MODE_SINGLE);

                    pos = position;

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            resumeFlag=true;
                            String itemNo = String.valueOf(listAdapter2.getItem(position).getNo());
                            //regDate=homelistObject.getWriteDate();
                            //번호 넘기고 정보에대한 상세보기 페이지 넘기기

                            System.out.println(pos);//스피너 position

                            //MainActivity ma = new MainActivity();
                            //ma.onIntent(pos,itemNo,regDate);

                            switch (pos) {
                                case 1: {//week
                                    System.out.println("주간일지 상세보기 ㄱ");
                                    Intent intent;

                                    //현재 액티비티(메인)에서 상세보기로 인텐트를 걸어야 함
                                    //그럼 Adapter에서 해야할까
                                    //리스트어댑터도 여기서 하니까 메인액티비티에서 인텐트를 걸어야할..것같음
                                    //그럼 클릭 리스너로 pos값을 메인으로 가져와서 switch문으로 메인에서 제어하면?

                                    intent = new Intent(getActivity(), ReadWeeklyActivity.class);
                                    intent.putExtra("token", userToken);
                                    intent.putExtra("index", itemNo);
                                    intent.putExtra("regDate", regDate);
                                    System.out.println("index : " + itemNo + " date : " + regDate);
                                    startActivity(intent);

                                    break;
                                }
                                case 2: {//day
                                    System.out.println("일일보고서 상세보기 ㄱ");
                                    Intent intent;
                                    intent = new Intent(getActivity(), ReadDailyActivity.class);
                                    intent.putExtra("token", userToken);
                                    intent.putExtra("index", itemNo);
                                    intent.putExtra("regDate", regDate);
                                    startActivity(intent);

                                    break;
                                }
                                case 3: {//consultation
                                    System.out.println("상담일지 상세보기 ㄱ");
                                    Intent intent;
                                    intent = new Intent(getActivity(), ReadInterviewActivity.class);
                                    intent.putExtra("index", itemNo);
                                    intent.putExtra("token", userToken);
                                    intent.putExtra("regDate", regDate);
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


    private class SendRequestHomeList extends AsyncTask<Void,Void,String> { //background,progress,execcute

        String message;

        @Override
        protected String doInBackground(Void... params) {

            String status=NetworkUtil.getConnectivityStatusString(getActivity());
            if("네트워크가 연결되지 않아 종료됩니다.".equals(status)){
                //Toast.makeText(getApplicationContext(), "네트워크가 연결되지 않았습니다.", Toast.LENGTH_SHORT).show();
                //android.os.Process.killProcess(android.os.Process.myPid());
                //ActivityCompat.finishAffinity(MainActivity.this);
                message = status;
                return message;
            }else{
                if(position==1){
                    index=index1;
                    url=  new ApiHost().getApi()+"view/weekplanner";//"http://192.168.1.21:9990/view/weekplanner";
                }
                //일일보고
                else if(position==2){
                    index=index2;
                    url=  new ApiHost().getApi()+"view/dayreport";//"http://192.168.1.21:9990/view/dayreport";
                }
                else if(position==3){//상담일지
                    index=index3;

                    url=  new ApiHost().getApi()+"view/consultation";//"http://192.168.1.21:9990/view/consultation";
                }
            }



            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url);
                System.out.println("index : " + index);
                //System.out.println("user No "+userNumber);
                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("token", userToken));//보고서 구분 바디
                //선택된 유저번호 서버측에 전달
                nameValuePairs.add(new BasicNameValuePair("userNo", userNumber.toString()));
                if (index > 0) {//리스트 갱신호출
                    ////////////////////////////////////index초기값 0으로 설정할까
                    nameValuePairs.add(new BasicNameValuePair("index", String.valueOf(index)));
                }


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
                Toast.makeText(getActivity().getApplicationContext(), "네트워크가 연결되지 않아 종료됩니다.", Toast.LENGTH_SHORT).show();
                ActivityCompat.finishAffinity(getActivity());
            }else{
                JSONArray jsonArray = null;
                Home_List homelistObject;
                System.out.println("123"+message);
                if("no data".equals(message) || "false".equals(message) || "0".equals(message)) {
                    System.out.println("123post");
                    Toast.makeText(getActivity().getApplicationContext(),"불러올 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
/*
                items.clear();

                listAdapter = new HomeListAdapter(ctx, items);

                listView.setAdapter(listAdapter);
                listView.setChoiceMode(listView.CHOICE_MODE_SINGLE);
                System.out.println("======");
                System.out.println();*/

                }else{

                    try {

                        jsonArray = new JSONArray(message);

                        if (position == 2) {//일일보고서의 경우 승인여부 포함
                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                Object obj = jsonObject.getString("regDate");
                                regDate = String.valueOf(obj);

                                homelistObject = new Home_List(
                                        jsonObject.getInt("no"),
                                        jsonObject.getString("title"),
                                        regDate.substring(0, 10),
                                        jsonObject.getString("confirm"));

                                System.out.println(homelistObject);
                                //index갱신하면서 마지막 번호 가져옴
                                index2 = jsonObject.getInt("no");
                                items.add(homelistObject);
                            }
                            System.out.println(items.toString());

                        } else if(position==3){

                            for(int i=0; i<jsonArray.length(); i++){

                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                Object obj = jsonObject.getString("regDate");
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

                                Object obj = jsonObject.getString("regDate");
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

                        System.out.println("dddddddddddddd" + position);
                        pos = position;


                        listAdapter = new HomeListAdapter(ctx, items);

                        listView.setAdapter(listAdapter);
                        listView.setChoiceMode(listView.CHOICE_MODE_SINGLE);


                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                resumeFlag=true;
                                String itemNo = valueOf(listAdapter.getItem(position).getNo());
                                //regDate=homelistObject.getWriteDate();
                                //번호 넘기고 정보에대한 상세보기 페이지 넘기기

                                System.out.println(pos);//스피너 position

                                //MainActivity ma = new MainActivity();
                                //ma.onIntent(pos,itemNo,regDate);

                                switch (pos) {
                                    case 1: {//week
                                        System.out.println("주간일지 상세보기 ㄱ");
                                        Intent intent;

                                        //현재 액티비티(메인)에서 상세보기로 인텐트를 걸어야 함
                                        //그럼 Adapter에서 해야할까
                                        //리스트어댑터도 여기서 하니까 메인액티비티에서 인텐트를 걸어야할..것같음
                                        //그럼 클릭 리스너로 pos값을 메인으로 가져와서 switch문으로 메인에서 제어하면?

                                        intent = new Intent(getActivity(), ReadWeeklyActivity.class);
                                        intent.putExtra("token", userToken);
                                        intent.putExtra("index", itemNo);
                                        System.out.println("index : " + itemNo + " date : " + regDate);
                                        startActivity(intent);

                                        break;
                                    }
                                    case 2: {//day
                                        System.out.println("일일보고서 상세보기 ㄱ");
                                        Intent intent;
                                        intent = new Intent(getActivity(), ReadDailyActivity.class);
                                        intent.putExtra("token", userToken);
                                        intent.putExtra("index", itemNo);
                                        startActivity(intent);

                                        break;
                                    }
                                    case 3: {//consultation
                                        System.out.println("상담일지 상세보기 ㄱ");
                                        Intent intent;
                                        intent = new Intent(getActivity(), ReadInterviewActivity.class);
                                        intent.putExtra("index", itemNo);
                                        intent.putExtra("token", userToken);
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


    //role 구분 : 팀장1 사원0
    private class SendRequestLeaderinfo extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url=  new ApiHost().getApi()+"checkauthority"; //"http://192.168.1.21:9990/checkauthority";
        String message;

        @Override
        protected String doInBackground(Void... params) {
            String status=NetworkUtil.getConnectivityStatusString(getActivity());
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
                Toast.makeText(getActivity().getApplicationContext(), "네트워크가 연결되지 않아 종료됩니다.", Toast.LENGTH_SHORT).show();
                ActivityCompat.finishAffinity(getActivity());
            }else{
                if("0".equals(message)){//사원일경우 사원의 정보 가져옴
                    ROLE="EMPLOYEE";
                    spinName.setVisibility(View.GONE);
                    position2=-1;
                }
                else{
                    ROLE="LEADER";//팀장의 경우 팀원명단 스피너 구현
                    new SendRequestTeamUser().execute();
                }
            }
        }
    }


    //팀장 : 팀원명단스피너 가져오기
    private class SendRequestTeamUser extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url=  new ApiHost().getApi()+"view/spinner/user";//"http://192.168.1.21:9990/view/spinner/user";
        String message;

        @Override
        protected String doInBackground(Void... params) {
            String status=NetworkUtil.getConnectivityStatusString(getActivity());
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
                Toast.makeText(getActivity().getApplicationContext(), "네트워크가 연결되지 않아 종료됩니다.", Toast.LENGTH_SHORT).show();
                ActivityCompat.finishAffinity(getActivity());
            }else{
                ChangeJsonTeamUserInfo(message);
            }

        }

    }

    public void ChangeJsonTeamUserInfo(String message){
        try {

            JSONArray jsonArray = new JSONArray(message);

            listTeamUsers = new ArrayList<TeamUser>();
            spinnerTeamUser= new ArrayList<String>();

            spinnerTeamUser.add("[ 팀원 선택 ]");

            for(int i=0; i<jsonArray.length(); i++){

                JSONObject jsonObject = jsonArray.getJSONObject(i);

                TeamUser teamUser = new TeamUser(
                        jsonObject.getInt("no"),
                        jsonObject.getString("name"));

                listTeamUsers.add(teamUser);
                spinnerTeamUser.add(teamUser.getName());

            }
            System.out.println(spinnerTeamUser);
            ArrayAdapter adapter = new ArrayAdapter(ctx,R.layout.support_simple_spinner_dropdown_item,spinnerTeamUser);
            spinName.setAdapter(adapter);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinName.setEnabled(false);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}