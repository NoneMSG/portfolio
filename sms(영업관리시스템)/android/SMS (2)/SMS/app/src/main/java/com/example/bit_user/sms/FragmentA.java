package com.example.bit_user.sms;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;


public class FragmentA extends Fragment{
    private String userToken ;
    private View view;
    private Context ctx;
    private String ROLE;

    //graph data
    private double maxYvalue1 =100000 ;
    private LineGraphSeries<DataPoint> lSeries1;
    private BarGraphSeries<DataPoint> bSeries1;
    //private LineGraphSeries<DataPoint> lSeries2;
    private BarGraphSeries<DataPoint> bSeries2;
    StaticLabelsFormatter slaf ;
    StaticLabelsFormatter slaf2 ;

    //graph list
    private GraphView[] graphList = new GraphView[2];

    //spinner list
    private Spinner[] spinnerList = new Spinner[4];

    //spinner name list
    ArrayList listTeamUsers;
    ArrayList spinnerTeamUser;

    //spinner period list
    ArrayList<String> yearList;
    ArrayList<String> monthList;
    ArrayList<String> weekList;

    private String year;
    private String month;
    private String userNo;

    private String monthyear;
    private String nameYear;
    private String nameMonth;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //token
        userToken = getActivity().getIntent().getStringExtra("userToken");

        //view
        view = inflater.inflate(R.layout.fragment_a,container,false);
        ctx = getActivity().getBaseContext();
        //spinners
        spinnerList[0] = (Spinner) view.findViewById(R.id.graphSpinnerYear); //2010~
        yearList = new ArrayList<>();
        monthList = new ArrayList<>();
        weekList = new ArrayList<>();
        Calendar now = Calendar.getInstance();

        yearList.add("년도");
        for(int i = now.get(Calendar.YEAR); i>=now.get(Calendar.YEAR)-10 ; --i){
            yearList.add(String.valueOf(i));
        }
        addItemInSpinner(spinnerList[0],yearList);

        monthList.add("월");
        spinnerList[1] = (Spinner) view.findViewById(R.id.graphSpinnerMonth); //1~12
        for(int i = 1; i < 13; ++i){
            if(String.valueOf(i).length()==1){
                monthList.add("0"+String.valueOf(i));
            }else{
                monthList.add(String.valueOf(i));
            }
        }
        addItemInSpinner(spinnerList[1],monthList);
        spinnerList[1].setEnabled(false);

        spinnerList[3] = (Spinner) view.findViewById(R.id.graphSpinnerName); //팀원들
        //new SendRequestTeamUser().execute();
        //권한 검증  리더일 때 스피너 데이터 가져옴
        new SendRequestLeaderinfo().execute();

        graphList[0] = (GraphView)view.findViewById(R.id.graphSales);
        graphList[1] = (GraphView)view.findViewById(R.id.graphTravel);


        graphList[0].getViewport().setYAxisBoundsManual(true);
        graphList[0].getViewport().setMaxY(maxYvalue1);
        graphList[0].getViewport().setXAxisBoundsManual(true);

        graphList[0].setTitle("[ 판매액 ]"); //그래프 이름

        graphList[1].getViewport().setYAxisBoundsManual(true);
        graphList[1].getViewport().setMaxY(1000);
        graphList[1].getViewport().setXAxisBoundsManual(true);


        //graph
        lSeries1 = new LineGraphSeries<>();
        bSeries1 = new BarGraphSeries<>();
        //lSeries2 = new LineGraphSeries<>();
        bSeries2 = new BarGraphSeries<>();

        bSeries1.setDrawValuesOnTop(true);
        bSeries1.setSpacing(40);
        bSeries2.setSpacing(40);

        graphList[0].setClickable(true);
        graphList[0].getViewport().setScalable(true);
        graphList[0].getViewport().setScalableY(true);
        graphList[0].getViewport().setScrollable(true);
        graphList[0].getViewport().setScrollableY(true);
        graphList[0].setScrollBarSize(10000);


        // graphList[0].getViewport().setMaxXAxisSize(12);

        graphList[0].addSeries(bSeries1); //막대 그래프 추가
        graphList[0].addSeries(lSeries1); //선형 그래프 추가
        lSeries1.setTitle("Goal"); //그래프 타이틀
        lSeries1.setColor(Color.GREEN); //컬러
        bSeries1.setTitle("Total");
        bSeries1.setColor(Color.RED);

        //view graph option
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(1);
        nf.setMinimumFractionDigits(1);
        graphList[0].getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf,nf));
        graphList[1].getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf,nf));
        graphList[0].getGridLabelRenderer().setLabelsSpace(10);
        graphList[1].getGridLabelRenderer().setLabelsSpace(10);
//        graphList[0].getGridLabelRenderer().setHumanRounding(true);
//        graphList[1].getGridLabelRenderer().setHumanRounding(true);

        graphList[1].setTitle("[ 이동거리 ]");
        graphList[1].addSeries(bSeries2);
        bSeries2.setTitle("총 이동거리");
        bSeries2.setColor(Color.RED);

        graphList[1].setClickable(true);
        graphList[1].getViewport().setScalable(true);
        graphList[1].getViewport().setScalableY(true);
        graphList[1].getViewport().setScrollable(true);
        graphList[1].getViewport().setScrollableY(true);
        // graphList[1].setScrollBarSize(10000);


        graphList[0].getLegendRenderer().setVisible(true);
        graphList[0].getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        slaf = new StaticLabelsFormatter(graphList[0]);

        graphList[1].getLegendRenderer().setVisible(true);
        graphList[1].getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        slaf2 = new StaticLabelsFormatter(graphList[1]);

        spinnerList[0].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(position==0 && ROLE=="LEADER"){
                    year="0";
                    System.out.println(year);
                    System.out.println("user번호 : "+userNo);
                }
                if(position!=0 && ROLE=="LEADER") {
                    //String
                    year = parent.getSelectedItem().toString();
                    if (userNo.equals("-1")) {
                        new GetYearData(year).execute();
                    } else if (!userNo.equals("0")) {
                        new GetYearData(year, userNo).execute();
                    } else {
                        Toast.makeText(getActivity(), "선택된 팀이나 유저가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
                if(position!=0 && ROLE=="EMPLOYEE"){
                    year = parent.getSelectedItem().toString();
                    new GetYearData(year).execute();
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerList[1].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                if(position==0 && ROLE=="LEADER"){
                    month="0";
                    System.out.println(month);
                    System.out.println("user번호 : "+userNo);
                }
                if(position!=0 && ROLE=="LEADER"){
                    //String
                    month=parent.getSelectedItem().toString();
                    if(userNo.equals("-1")){
                        new GetMonthData(month).execute();
                    }else if(!userNo.equals("0")){
                        new GetMonthData(month,userNo).execute();
                    }else{
                        Toast.makeText(getActivity(),"선택된 팀이나 유저가 없습니다.",Toast.LENGTH_SHORT).show();
                    }

                }else if(position!=0 && ROLE=="EMPLOYEE"){
                    month=parent.getSelectedItem().toString();
                    new GetMonthData(month).execute();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerList[3].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(position==0 && ROLE=="LEADER"){
                    userNo="0";
                    System.out.println("user번호 : "+userNo);
                }

                if(position!=0 && ROLE=="LEADER"){
                    //listTeamUsers.get(position);
                    System.out.println("clciked"+listTeamUsers.get(position));
                    userNo = listTeamUsers.get(position).toString();

                    //Toast.makeText(getActivity(),parent.getSelectedItem().toString(),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        return view;
    }


    //add period data
    private void addItemInSpinner(Spinner spinner, ArrayList arrList){
        ArrayAdapter ad = new ArrayAdapter(getActivity(),R.layout.support_simple_spinner_dropdown_item,arrList);
        spinner.setAdapter(ad);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onResume() {
        super.onResume();
    }


    private DataPoint[] gnerateData1(ArrayList arrayList){
        System.out.println("list data ="+arrayList);
        int count = arrayList.size();
        DataPoint[] values = new DataPoint[count];
        for(int i = 0 ; i < count ; ++i){
            int x = i;
            double y =  (double) arrayList.get(i);
            DataPoint v = new DataPoint(x,y);
            values[i] = v;

        }
        return values;
    }

    //year data
    private class GetYearData extends AsyncTask<Void,Void,String> {
        String response;
        String year ;
        String uri= new ApiHost().getApi()+"graph/sales/year";  //"http://192.168.1.21:9990/graph/sales/year";

        GetYearData(String str){
            year=str;
            nameYear=str;
        }
        GetYearData(String str, String userNo){
            year=str;
            if(!userNo.equals("-1")){
                userNo = userNo;
            }
        }
        @Override
        protected String doInBackground(Void... params) {
            System.out.println("doin bg "+year);

            String status=NetworkUtil.getConnectivityStatusString(getActivity());
            if("네트워크가 연결되지 않아 종료됩니다.".equals(status)){
                //Toast.makeText(getApplicationContext(), "네트워크가 연결되지 않았습니다.", Toast.LENGTH_SHORT).show();
                //android.os.Process.killProcess(android.os.Process.myPid());
                //ActivityCompat.finishAffinity(MainActivity.this);
                response = status;
                return response;
            }
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(uri);


                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("token",userToken));
                nameValuePairs.add(new BasicNameValuePair("year",year));
                if(ROLE.equals("LEADER")){
                    if(!userNo.equals("0")){
                        nameValuePairs.add(new BasicNameValuePair("userNo",userNo));
                    }
                }


                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
                post.setEntity(ent);

                //전송
                HttpResponse httpResponse = httpclient.execute(post);

                //응답
                HttpEntity resEntity = httpResponse.getEntity();

                //토큰 혹은 false를 message에 담고서 비교
                response= EntityUtils.toString(resEntity);
                System.out.println("요청내용 전달받음1 : "+response);


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (HttpHostConnectException e) {
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }
        @Override
        protected void onPostExecute(String s) {
            if(response.equals("네트워크가 연결되지 않아 종료됩니다.")){
                Toast.makeText(getActivity().getApplicationContext(), "네트워크가 연결되지 않아 종료됩니다.", Toast.LENGTH_SHORT).show();
                ActivityCompat.finishAffinity(getActivity());
            }else{
                monthyear=year; //month에 연도값 추가 해서 넘기기
                spinnerList[1].setEnabled(true);
                JSONObject jo;
                ArrayList<Integer> saleGoalList = new ArrayList<>();
                ArrayList<Integer> saleTotalList = new ArrayList<>();
                ArrayList<Double> totalDisList = new ArrayList<>();

                try {
                    jo = new JSONObject(response);
                    int length = jo.getJSONArray("saleGoal").length();
//                System.out.println("json array length :  "+jo.getJSONArray("saleGoal").length());
//                System.out.println("json array length :  "+jo.getJSONArray("saleTotal").length());

                    for(int i =0 ; i < length ; ++i){
                        saleGoalList.add( (Integer) ( jo.getJSONArray("saleGoal").get(i).equals(null)  ? 0 : jo.getJSONArray("saleGoal").get(i)) );
                        saleTotalList.add( (Integer) ( jo.getJSONArray("saleTotal").get(i).equals(null) ? 0: jo.getJSONArray("saleTotal").get(i)));
                        Object temp = ( jo.getJSONArray("disTotal").get(i).equals(null) ? 0.0: jo.getJSONArray("disTotal").get(i));
                        if(temp instanceof Number){
                            totalDisList.add( ((Number) temp).doubleValue()  );
                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                System.out.println("list "+saleGoalList);
                System.out.println("list "+saleTotalList);
                System.out.println("list "+totalDisList);

//            if(totalDisList.get(7) instanceof Number){
//                Double d = ( (Number)totalDisList.get(7)).doubleValue();
//                System.out.println("dobule vlaue "+d);
//            }
                lSeries1.resetData(gnerateData(saleGoalList));
                bSeries1.resetData(gnerateData(saleTotalList));

                bSeries2.resetData(gnerateData1(totalDisList));

//            bSeries2.setAnimated(true);

                slaf2.setHorizontalLabels(gerateLable(totalDisList));
                graphList[1].getGridLabelRenderer().setLabelFormatter(slaf2);

                slaf.setHorizontalLabels(gerateLable(saleGoalList));
                graphList[0].getGridLabelRenderer().setLabelFormatter(slaf);


                //그래프 최대값 설정
                Integer saleMax = Collections.max(saleTotalList);
                Integer goalMax = Collections.max(saleGoalList);
                maxYvalue1= Math.max(saleMax,goalMax);
                Integer max = Math.max(saleMax,goalMax);

                System.out.println("max값 : "+(saleMax+goalMax));
                graphList[0].getViewport().setMaxY(  max+10.0 );
                Double max2 =( Collections.max(totalDisList) );
                graphList[1].getViewport().setMaxY(  max2+50.0 );
                graphList[0].getViewport().setMaxX(  saleTotalList.size() );
                graphList[1].getViewport().setMaxX(  totalDisList.size() );
                lSeries1.setAnimated(true);
                //bSeries1.setAnimated(true);
                //graphList[0].getViewport().setMaxX(14);
            }



        }
    }

    private class GetMonthData extends AsyncTask<Void,Void,String> {
        String response;
        String uri= new ApiHost().getApi()+"graph/sales/month"; //"http://192.168.1.21:9990/graph/sales/month";
        String month;
        GetMonthData(String str){
            month = monthyear+'-'+str;
            nameMonth = month;
        }
        GetMonthData(String str, String userNo){
            month = monthyear+'-'+str;
            if(!userNo.equals("-1")){
                userNo = userNo;
            }
        }
        @Override
        protected String doInBackground(Void... params) {
            System.out.println("doin bg "+month);
            String status=NetworkUtil.getConnectivityStatusString(getActivity());
            if("네트워크가 연결되지 않아 종료됩니다.".equals(status)){
                //Toast.makeText(getApplicationContext(), "네트워크가 연결되지 않았습니다.", Toast.LENGTH_SHORT).show();
                //android.os.Process.killProcess(android.os.Process.myPid());
                //ActivityCompat.finishAffinity(MainActivity.this);
                response = status;
                return response;
            }

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(uri);


                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("token",userToken));
                nameValuePairs.add(new BasicNameValuePair("year",year));
                nameValuePairs.add(new BasicNameValuePair("month",month));
                if(ROLE.equals("LEADER")){
                    if (!userNo.equals("0")) {
                        nameValuePairs.add(new BasicNameValuePair("userNo", userNo));
                    }
                }

                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
                post.setEntity(ent);

                //전송
                HttpResponse httpResponse = httpclient.execute(post);

                //응답
                HttpEntity resEntity = httpResponse.getEntity();

                //토큰 혹은 false를 message에 담고서 비교
                response= EntityUtils.toString(resEntity);
                System.out.println("요청내용 전달받음2 : "+response);


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (HttpHostConnectException e) {
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }
        @Override
        protected void onPostExecute(String s) {
            if(response.equals("네트워크가 연결되지 않아 종료됩니다.")){
                Toast.makeText(getActivity().getApplicationContext(), "네트워크가 연결되지 않아 종료됩니다.", Toast.LENGTH_SHORT).show();
                ActivityCompat.finishAffinity(getActivity());
            }else{
                JSONObject jo;
                ArrayList<Integer> saleGoalList = new ArrayList<>();
                ArrayList<Integer> saleTotalList = new ArrayList<>();
                ArrayList<Double> totalDisList = new ArrayList<>();

                try {
                    jo = new JSONObject(response);
                    int length = jo.getJSONArray("saleGoal").length();
//                System.out.println("json array length :  "+jo.getJSONArray("saleGoal").length());
//                System.out.println("json array length :  "+jo.getJSONArray("saleTotal").length());

                    for(int i =0 ; i < length ; ++i){
                        saleGoalList.add( (Integer) ( jo.getJSONArray("saleGoal").get(i).equals(null)  ? 0 : jo.getJSONArray("saleGoal").get(i)) );
                        saleTotalList.add( (Integer) ( jo.getJSONArray("saleTotal").get(i).equals(null) ? 0: jo.getJSONArray("saleTotal").get(i)));
                        Object temp = ( jo.getJSONArray("disTotal").get(i).equals(null) ? 0.0: jo.getJSONArray("disTotal").get(i));
                        if(temp instanceof Number){
                            totalDisList.add( ((Number) temp).doubleValue()  );
                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                System.out.println("list "+saleGoalList);
                System.out.println("list "+saleTotalList);
                System.out.println("list "+totalDisList);

                lSeries1.resetData(gnerateData(saleGoalList));
                bSeries1.resetData(gnerateData(saleTotalList));
                bSeries2.resetData(gnerateData1(totalDisList));

                slaf2.setHorizontalLabels(gerateLable(totalDisList));
                graphList[1].getGridLabelRenderer().setLabelFormatter(slaf2);

                slaf.setHorizontalLabels(gerateLable(saleGoalList));
                graphList[0].getGridLabelRenderer().setLabelFormatter(slaf);



                //그래프 최대값 설정
                Integer saleMax = Collections.max(saleTotalList);
                Integer goalMax = Collections.max(saleGoalList);
                maxYvalue1= Math.max(saleMax,goalMax);
                Integer max = Math.max(saleMax,goalMax);
                System.out.println("max값 : "+(saleMax+goalMax));
                graphList[0].getViewport().setMaxX(saleTotalList.size());
                graphList[0].getViewport().setMaxY(  max+10.0 );
                Double max2 =( Collections.max(totalDisList) );
                graphList[1].getViewport().setMaxY(  max2+50.0 );
                graphList[1].getViewport().setMaxX(  totalDisList.size() );
                lSeries1.setAnimated(true);

                // super.onPostExecute(s);
            }

        }
    }

    private String[] gerateLable(ArrayList arrayList){
        String[] temp = new String[arrayList.size()];
        for(int i =0 ; i < (arrayList.size()); i++){
            temp[i] = String.valueOf(i);
            if(arrayList.size()>6){
                temp[i]= i+1+"월 ";
            }else if(arrayList.size()<=5){
                temp[i]= i+1+"주 ";
            }
        }
        return temp;
    }
    private DataPoint[] gnerateData(ArrayList arrayList){
        System.out.println("list data ="+arrayList);
        int count = arrayList.size();
        DataPoint[] values = new DataPoint[count];
        for(int i = 0 ; i < count ; ++i){
            int x = i;
            int y ;
            y = (int) arrayList.get(i);
            DataPoint  v = new DataPoint(x,y);
            values[i] = v;

        }
        return values;
    }

    //get namelist
    private class SendRequestTeamUser extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url=  new ApiHost().getApi()+"view/spinner/user";//http://192.168.1.21:9990/view/spinner/user";
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
                System.out.println("요청내용 전달받음3 : "+message);


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
            ChangeJsonTeamUserInfo(message);
        }
        public void ChangeJsonTeamUserInfo(String message) {
            try {

                JSONArray jsonArray = new JSONArray(message);

                listTeamUsers = new ArrayList<TeamUser>();
                spinnerTeamUser = new ArrayList<String>();

                spinnerTeamUser.add("[ 팀원 선택 ]");
                spinnerTeamUser.add("[ 팀 전체 ]");
                listTeamUsers.add(0);
                listTeamUsers.add(-1);
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    TeamUser teamUser = new TeamUser(
                            jsonObject.getInt("no"),
                            jsonObject.getString("name"));

                    listTeamUsers.add(teamUser.getNo());
                    spinnerTeamUser.add(teamUser.getName());

                }
                System.out.println("listtemauser:  "+listTeamUsers);
                System.out.println(spinnerTeamUser);
                ArrayAdapter adapter = new ArrayAdapter(ctx, R.layout.support_simple_spinner_dropdown_item, spinnerTeamUser);
                spinnerList[3].setAdapter(adapter);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //spinnerList[3].setEnabled(false);
                spinnerList[3].setSelection(1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


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
                System.out.println("요청내용 전달받음4 : "+message);


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
                if("0".equals(message)) {//사원일경우 사원의 정보 가져옴
                    ROLE = "EMPLOYEE";
                    spinnerList[3].setVisibility(View.GONE);
                }
                else{
                    ROLE="LEADER";
                    new SendRequestTeamUser().execute();
                }
            }

        }
    }
}