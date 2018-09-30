package com.example.bit_user.sms;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;

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
import java.util.Locale;
import java.util.TimeZone;



public class FragmentB extends Fragment{
    private String userToken ;
    private String ROLE;
    private String confirm;
    private String confirmDate;

    TextView txtWeek;
    TextView txtWeekDay;
    TextView txtWeekDayGoal;

    TextView txtConfirmDay;
    TextView txtDayReport;
    TextView txtDayReportRate;
    TextView txtMonth;

    LinearLayout layoutEmployee;
    LinearLayout layoutLeader;

    ListView leaderListView=null;
    TextView txtListConfirm;
    ListView leaderListView2=null;

    ArrayList<CalendarList> calendarItems; //승인 대기중인 아이템
    ArrayList<CalendarList> calendarItems2; //승인되어진 리스트 아이템
    ArrayAdapter<CalendarList> calendarListArrayAdapter;
    CalendarList calendarList;

    private String date;
    private String firstDay;
    private String endDay;
    Context ctx;
    private int dayNo;

    private double rate;
    private double d_goal;
    private double d_sale;


    private CompactCalendarView compactCalendarView;
    private ActionBar toolbar;
    private SimpleDateFormat dateFormatForDisplaying = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("MMM - yyyy", Locale.getDefault());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        userToken  =getActivity().getIntent().getStringExtra("userToken");
        //userToken = getActivity().getIntent().getStringExtra("userToken");
        System.out.println("========================================BBBBBBBBBB"+userToken);

        //ctx = getActivity().getBaseContext();
        View view = inflater.inflate(R.layout.fragment_b,container,false);

        //캘린더 라이브러리 바인딩
        compactCalendarView = (CompactCalendarView) view.findViewById(R.id.compactCalendarView);
        //달력 설정
        compactCalendarView.setLocale(TimeZone.getTimeZone("UTC+09:00"),Locale.KOREAN);
        compactCalendarView.setFirstDayOfWeek(Calendar.SUNDAY);
        compactCalendarView.invalidate();

        txtMonth=(TextView)view.findViewById(R.id.txtMonth);

        //팀원경우
        txtWeek=(TextView)view.findViewById(R.id.txtWeek);
        txtWeekDay=(TextView)view.findViewById(R.id.txtWeekDay);
        txtWeekDayGoal=(TextView)view.findViewById(R.id.txtWeekDayGoal);

        txtConfirmDay=(TextView)view.findViewById(R.id.txtConfirmDay);
        txtDayReport=(TextView)view.findViewById(R.id.txtDayReport);
        txtDayReportRate=(TextView)view.findViewById(R.id.txtDayReportRate);

        layoutEmployee=(LinearLayout)view.findViewById(R.id.layoutEmployee);
        layoutLeader=(LinearLayout)view.findViewById(R.id.layoutLeader);

        //팀장경우
        leaderListView=(ListView)view.findViewById(R.id.leaderListView);
        txtListConfirm=(TextView)view.findViewById(R.id.txtListConfirm);
        leaderListView2=(ListView)view.findViewById(R.id.leaderListView2);

        new SendRequestLeaderinfo().execute();


        //액션바 텍스트 설정
        //toolbar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        txtMonth.setText(dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()));


        //캘린더 클릭 이벤트
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener(){

            @Override
            public void onDayClick(Date dateClicked) {
                //날짜를 클릭했을때 해당 주차의 마지막 날 (일요일 날짜)을 구해서 서버로 전달.

                txtWeek.setText("");
                txtWeekDay.setText("");
                txtWeekDayGoal.setText("");

                txtConfirmDay.setText("");
                txtDayReport.setText("");
                txtDayReportRate.setText("");
                System.out.println("[][][][][][][]"+dateFormatForDisplaying.format(dateClicked));
                date = dateFormatForDisplaying.format(dateClicked);
                new GetFirstNEndDayOfWeek().execute();

                //new GetdailyReport().execute();

            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                txtMonth.setText(dateFormatForMonth.format(firstDayOfNewMonth));
            }
        });
        return view;
    }


    //get first week day and end week day in month
    private class GetFirstNEndDayOfWeek extends AsyncTask<Void,Void,String> {
        String message ;
        String url =  new ApiHost().getApi()+"getweek";//"http://192.168.1.21:9990/getweek";
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


                System.out.println("date      "+date);
                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(3);
                nameValuePairs.add(new BasicNameValuePair("token",userToken));
                nameValuePairs.add(new BasicNameValuePair("date",date));
                nameValuePairs.add(new BasicNameValuePair("calFlag","1"));

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
                String satDay="";
                try {
                    JSONObject job = new JSONObject(message);
                    firstDay = job.get("mon").toString();
                    satDay=job.get("sat").toString();
                    endDay = job.get("sun").toString();
                    //System.out.println("monday"+job.get("mon"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                System.out.println("-===============");
                System.out.println(firstDay);
                System.out.println(endDay);
                System.out.println("-===============");

                //클릭날짜가 토or일 경우 다음통신 안넘어감#############################################
                if(satDay.equals(date)||endDay.equals(date)){

                    System.out.println("#############################################################");
                    txtWeek.setText("");
                    txtWeekDay.setText("");
                    txtWeekDayGoal.setText("");

                    txtConfirmDay.setText("");
                    txtDayReport.setText("");
                    txtDayReportRate.setText("");

                    txtListConfirm.setVisibility(View.GONE);

                    calendarItems.clear();
                    calendarItems2.clear();
                    calendarListArrayAdapter.notifyDataSetChanged();

                }

                else{
                    //날자 클릭하여 정보가져옴 (팀장/팀원 구분시점)#######################################################
                    if("EMPLOYEE".equals(ROLE)){
                        System.out.println("EMPLOYEE");
                        new GetdailyWeekPlanner().execute();
                    }
                    else{
                        System.out.println("LEADER");
                        new GetdailyPlanner().execute();
                    }
                }
            }


        }
    }



    private class GetdailyWeekPlanner extends AsyncTask<Void,Void,String>{
        String message ;
        String url = new ApiHost().getApi()+"calendar/weekplanner";//"http://192.168.1.21:9990/calendar/weekplanner";

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url);

                System.out.println("first"+firstDay);
                System.out.println("endDay"+endDay);
                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(3);
                nameValuePairs.add(new BasicNameValuePair("token",userToken));
                nameValuePairs.add(new BasicNameValuePair("firstDay",firstDay));
                nameValuePairs.add(new BasicNameValuePair("endDay",endDay));

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
            try {
                if("denied".equals(message))
                    return;

                JSONObject jo = new JSONObject(message);

                JSONArray jsonArray = jo.getJSONArray("week");
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                //주간계획서 정보 뿌리기
                txtWeek.setText(jsonObject.getString("title"));



                jsonArray=jo.getJSONArray("daily");
                System.out.println("size "+jsonArray.length());//클릭날짜와 비교해 맞는 일일계획서 들고오기

                for(int i=0; i<jsonArray.length(); i++){

                    jsonObject=jsonArray.getJSONObject(i);
                    if(jsonObject.getString("date").substring(0,10).equals(date)){
                        txtWeekDay.setText(jsonObject.getString("plan"));
                        //##########################################################################################
                        d_goal=Double.parseDouble(jsonObject.getString("sale_goal"));
                        txtWeekDayGoal.setText(jsonObject.getString("sale_goal")+" 원");
                        dayNo=jsonObject.getInt("no"); //일일계획서 번호를 가지고 승인된 보고서 가져오기
                        confirmDate=jsonObject.getString("date");
                        System.out.println(jsonObject.getInt("no"));
                        new SendRequestDayplanner().execute();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //주간계획서 기본정보 , 일간계획 부분 가져오기
    private class SendRequestDayplanner extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url=  new ApiHost().getApi()+"view/dayreport/content";//"http://192.168.1.21:9990/view/dayreport/content";
        String message;

        @Override
        protected String doInBackground(Void... params) {

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url);

                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("token",userToken));
                nameValuePairs.add(new BasicNameValuePair("date",confirmDate));

                System.out.println("토큰 : "+userToken);
                System.out.println("일일보고서 날짜 : "+confirmDate);

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

            changeJsonConsultation(message);
        }
    }

    public void changeJsonConsultation(String jsonMessage) {
        Comment cmtObject;
        Consultation consultation;
        Attachment attachment;

        //가져온 name,depName,email,teamleader 저장
        try {
            if("denied".equals(jsonMessage) || "no data".equals(jsonMessage))
                return;

            JSONObject jo = new JSONObject(jsonMessage);

            JSONArray jsonArray = jo.getJSONArray("dayReport");
            JSONObject jsonObject = jsonArray.getJSONObject(0);

            confirm = jsonObject.getString("confirm");
            System.out.println("confirm " + confirm);

            if("3".equals(confirm)) {
                txtConfirmDay.setText(jsonObject.getString("title"));
                d_sale=Double.parseDouble(jsonObject.getString("sale_total"));

                if(d_goal==0 || d_sale==0){
                    txtDayReportRate.setText(0.00 + " %");
                }
                else {

                    rate=(d_sale/d_goal)*100;
                    txtDayReportRate.setText(String.format("%.2f",rate) + " %");
                }

                txtDayReport.setText(jsonObject.getString("description"));
            }

        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //###################팀장 , 팀원 구분
    //role 구분 : 팀장1 사원0
    private class SendRequestLeaderinfo extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url= new ApiHost().getApi()+"checkauthority";//"http://192.168.1.21:9990/checkauthority";
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
                if("0".equals(message)){//사원일경우 사원의 정보 가져옴
                    ROLE="EMPLOYEE";

                }
                else{
                    ROLE="LEADER"; //팀장의 경우 리스트 보여주기

                    layoutEmployee.setVisibility(View.GONE);
                    layoutLeader.setVisibility(View.VISIBLE);

                    //리스트뷰 활성화 후 어댑터연결


                }
            }
        }
    }


    //#####################팀장의 경우 팀원들 보고서 불러옴
    private class GetdailyPlanner extends AsyncTask<Void,Void,String>{
        String message ;
        String url = new ApiHost().getApi()+"calendar/dayreport";//"http://192.168.1.21:9990/calendar/dayreport";

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url);

                System.out.println("first"+firstDay);
                System.out.println("endDay"+endDay);
                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(3);
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
            try {
                if("no data".equals(message)){
                    layoutLeader.setVisibility(View.GONE);
                    return;
                }
                layoutLeader.setVisibility(View.VISIBLE);
                JSONArray jsonArray = new JSONArray(message);
                JSONObject jsonObject;

                calendarItems=new ArrayList<>();
                calendarItems2=new ArrayList<>();

                for(int i=0; i<jsonArray.length(); i++){

                    jsonObject=jsonArray.getJSONObject(i);
                    calendarList=new CalendarList(
                            jsonObject.getInt("no"),
                            jsonObject.getString("title"),
                            jsonObject.getString("name"),
                            jsonObject.getString("description"),
                            jsonObject.getString("confirm"));

                    if("1".equals(jsonObject.getString("confirm"))) //승인대기
                        calendarItems.add(calendarList);
                    else if("0".equals(jsonObject.getString("confirm"))) //요청안한것
                        return;
                    else{ //승인완료
                        calendarItems2.add(calendarList);
                    }
                }

                calendarListArrayAdapter= new CalendarListAdapter(getActivity(),calendarItems,userToken);
                leaderListView.setAdapter(calendarListArrayAdapter);
                leaderListView.setChoiceMode(leaderListView.CHOICE_MODE_SINGLE);

                if(calendarItems2.isEmpty()==false){ //승인완료 보고서 존재
                    txtListConfirm.setVisibility(View.VISIBLE);

                    calendarListArrayAdapter= new CalendarListAdapter(getActivity(),calendarItems2,userToken);
                    leaderListView2.setAdapter(calendarListArrayAdapter);
                    leaderListView2.setChoiceMode(leaderListView2.CHOICE_MODE_SINGLE);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }



}