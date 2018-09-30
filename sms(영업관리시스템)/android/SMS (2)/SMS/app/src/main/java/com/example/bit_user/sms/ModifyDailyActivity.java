package com.example.bit_user.sms;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ModifyDailyActivity extends AppCompatActivity {
    public static Activity mdaActivity;
    SharedPreferences preferences;
    String fcmkey;
    private int flag;

    Button btnaddConsult;
    ListView listConsultation;
    ArrayList<Consultation> consultItems; //현재 가지고있는 상담일지 목록
    ArrayAdapter<Consultation> consultationArrayAdapter;

    ArrayList<String> arrayIndex=new ArrayList<>();//추가된 상담일지 목록
    ArrayList<String> deleteIndex=new ArrayList<>(); //삭제된 상담일지 목록

    LayoutInflater inflater;

    private static final String TAG = "filessss";
    ArrayList<ListInterview> items;

    private String userToken;
    private static final int PICK_FROM_Interview = 2;
    private static final int PICK_FROM_FM = 4;
    private static final int PICK_FROM_CAMERA=3;

    private String userNo;
    private String index;
    private String regDate;
    private String defaultDate;
    private String price;

    double d_sale;
    double d_priceGoal;
    double d_rate;
    double d_startDis;
    double d_endDis;
    double d_dis;

    EditText editTitle;
    EditText editSale;
    EditText editStartMeasure;
    EditText editEndMeasure;

    EditText editDailyReport;
    EditText editTxtGoal;

    TextView txtDistance;

    TextView department;
    TextView name;
    TextView leader;
    TextView today;

    String title;//제목
    String sale; //매출액
    String rate=""; //달성율
    String startDis;
    String endDis;

    String description;

    String strDate;
    String sendDate;

    TextView txtGoal;//목표액표시(주간일지로부터 가져옴)
    TextView txtRate;
    String priceGoal; //실제 목표 액수
    String dis;

    Button btnCancel;
    Button btnSubmit;
    Button btnCalendar;
    Button btnChoiceDate;

    Spinner spinAddFile;

    String mCurrentPhotoPath;//현재사진 경로
    String img_url;
    Uri albumURI =null;
    Uri mImageCaptureUri;
    Uri fileURI;

    Cursor mCursor;

    //캘린더 호출시 필요한 정보들
    static final int DATE_DIALOG_ID=1;
    private int mYear;
    private int mMonth;
    private int mDay;

    TextView calDate;

    long now = System.currentTimeMillis();
    // 현재시간을 date 변수에 저장한다.
    Date date = new Date(now);
    // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
    SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    // nowDate 변수에 값을 저장한다.
    String formatDate = sdfNow.format(date);

    //달성률계산
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            try{
                sale = editSale.getText().toString(); //입력한 매출액액
               System.out.println("sale : "+sale);

                if(sale!=null || !"".equals(sale) || sale.isEmpty()==false || editSale.getText().toString().isEmpty()==false){

                    d_sale = Double.parseDouble(sale);
                    System.out.println("ddd"+d_sale);
                    System.out.println("DDDDDDDDDDDDDDDd"+priceGoal); //null

                    if(priceGoal==null || "".equals(priceGoal)){

                        price=editTxtGoal.getText().toString();
                        d_priceGoal = Double.parseDouble(price);

                    }
                    else{
                        d_priceGoal = Double.parseDouble(priceGoal);
                    }

                    if (d_sale < 0 || d_priceGoal < 0) {
                        Toast.makeText(getApplicationContext(),"금액을 다시 입력해 주세요.",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(d_sale==0 && d_priceGoal>0){
                        d_rate=100;
                    }
                    else if(d_sale==0 && d_priceGoal==0){
                        d_rate=0;
                    }
                    else{
                        d_rate = (d_sale/d_priceGoal)*100;
                        if(d_rate>100)
                            d_rate=100;
                    }

                    System.out.println(d_rate);
                    rate = String.format("%.2f",d_rate );
                    txtRate.setText("달성률 :  "+rate+" % "); //목표달성비율
                }
            }catch (NumberFormatException e){
                txtRate.setText("달성률 :  0.00 % ");
                return;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };


    TextWatcher textWatcher2 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            try{
                startDis = editStartMeasure.getText().toString();
                endDis = editEndMeasure.getText().toString();

                if(startDis != null || endDis != null || startDis != "" || endDis != ""){

                    d_startDis = Double.parseDouble(startDis);
                    System.out.println("startDis"+d_startDis);

                    d_endDis = Double.parseDouble(endDis);
                    System.out.println("endDis"+d_endDis);

                    d_dis = Math.abs(d_endDis-d_startDis);
                    dis=String.format("%.1f",d_dis);
                    txtDistance.setText("주행거리 :  "+dis+" km "); //주행거리
                }
            }catch (NumberFormatException e){
                txtDistance.setText("주행거리 :  0.0 km ");
                return;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_daily);

        mdaActivity=ModifyDailyActivity.this;

        preferences= getSharedPreferences("tokenAndHome",MODE_PRIVATE);
        fcmkey = preferences.getString("fcmkey","0");

        userToken = getIntent().getStringExtra("userToken");
        userNo = getIntent().getStringExtra("userNo");
        regDate = getIntent().getStringExtra("regDate");
        index = getIntent().getStringExtra("index");
        sendDate=regDate;

        consultItems=(ArrayList<Consultation>) getIntent().getSerializableExtra("consultItems");

        department = (TextView)findViewById(R.id.txtDep);
        name = (TextView)findViewById(R.id.txtName);
        leader = (TextView)findViewById(R.id.txtLeader);
        today = (TextView)findViewById(R.id.txtDate);
        today.setText(" 수정일 : "+formatDate);

        txtGoal=(TextView)findViewById(R.id.txtGoal);
        editTxtGoal=(EditText)findViewById(R.id.editTxtGoal);

        editTitle=(EditText)findViewById(R.id.editTitle);
        editSale=(EditText)findViewById(R.id.editSale);
        editSale.setText(getIntent().getStringExtra("saleTotal"));
        editTitle.setText(getIntent().getStringExtra("title"));
        txtGoal.setText(" [ 목표액 : "+getIntent().getStringExtra("saleGoal")+" 원 ]");

        calDate=(TextView)findViewById(R.id.calDate);
        calDate.setText("[ "+getIntent().getStringExtra("reportDate")+" ]");
        defaultDate=getIntent().getStringExtra("reportDate");
        //달성율
        editSale.addTextChangedListener(textWatcher);
        editTxtGoal.addTextChangedListener(textWatcher);

        txtRate = (TextView)findViewById(R.id.txtRate);

        //주행거리
        editStartMeasure = (EditText)findViewById(R.id.editStartMeasure);
        editEndMeasure = (EditText)findViewById(R.id.editEndMeasure);

        editStartMeasure.setText(getIntent().getStringExtra("startDis"));
        editEndMeasure.setText(getIntent().getStringExtra("endDis"));

        startDis =  getIntent().getStringExtra("startDis");
        endDis = getIntent().getStringExtra("endDis");

        txtDistance = (TextView)findViewById(R.id.txtDistance);

        //수정없을 경우 그대로 넘겨야함
        dis=getIntent().getStringExtra("dis");
        editStartMeasure.addTextChangedListener(textWatcher2);
        editEndMeasure.addTextChangedListener(textWatcher2);

        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnSubmit = (Button)findViewById(R.id.btnSubmit);
        btnCalendar=(Button)findViewById(R.id.btnCalendar);
        btnChoiceDate = (Button)findViewById(R.id.btnChoiceDate);

        btnaddConsult=(Button)findViewById(R.id.btnaddConsult);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        editDailyReport = (EditText)findViewById(R.id.editDailyReport);
        editDailyReport.setText(getIntent().getStringExtra("description"));
        //본문 가져오기

        final Calendar calendar = Calendar.getInstance();
        mYear=calendar.get(Calendar.YEAR);
        mMonth=calendar.get(Calendar.MONTH);
        mDay=calendar.get(Calendar.DATE);

        listConsultation=(ListView)findViewById(R.id.listConsultation);


        /*editSale.setEnabled(false);
        editStartMeasure.setEnabled(false);
        editEndMeasure.setEnabled(false);
        editDailyReport.setEnabled(false);
        btnaddConsult.setEnabled(false);
        btnSubmit.setEnabled(false);*/
        btnSubmit.setTextColor(Color.rgb(148,148,148));

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        new SendRequestinfo().execute();

    }


    @Override
    protected void onResume() {
        super.onResume();
        onbtnClick();


        consultationArrayAdapter=new ModifyConsultationAdapter(getBaseContext(),consultItems,deleteIndex,userToken);
        listConsultation.setAdapter(consultationArrayAdapter);
        //setListViewHeightBasedOnChildren(listConsultation);
        listConsultation.setChoiceMode(listConsultation.CHOICE_MODE_SINGLE);
        consultationArrayAdapter.notifyDataSetChanged();

        //#############################################################Adapter에서 삭제한후 리스트 사이즈 해결
        /* *삭제&추가 목록 같이*
        consultItems.trimToSize();
        arrayIndex.clear();
        for(int i=0; i<consultItems.size(); i++){
            arrayIndex.add(String.valueOf(consultItems.get(i).getNo()));
        }
        System.out.println("결국 상담일지 인덱스는 "+arrayIndex);*/

        //###############################################################
        consultItems.trimToSize();
        deleteIndex.trimToSize();

        System.out.println("현재목록 개수 : "+consultItems.size());
        System.out.println("삭제목록  : "+deleteIndex.toString());
        System.out.println("추가목록  : "+arrayIndex.toString());

    }



    public void onbtnClick(){
        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnSubmit = (Button)findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag=1;
                new Uid(userToken).execute();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ModifyDailyActivity.this,DialogCancelActivity.class);
                intent.putExtra("userToken",userToken);
                intent.putExtra("flag","ModifyDaily");
                startActivity(intent);
            }
        });

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
                System.out.println(sendDate);
                if(sendDate==null)
                    sendDate=defaultDate;
                System.out.println(sendDate);
                new Uid(userToken).execute();
            }
        });

        //상담일지 첨부 클릭
        btnaddConsult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                items = new ArrayList<ListInterview>();
                new SendRequestList().execute();
            }
        });

    }


    //캘린더 선택
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




    //가져온 사진,리스트 뿌리기기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK){

            switch (requestCode) {
                case PICK_FROM_Interview: {

                    //상담일지 리스트에서 선택된 아이템의 인덱스정보를 담은 배열
                    arrayIndex = data.getStringArrayListExtra("arrayIndex");
                    ArrayList<Consultation> con = new ArrayList<>();
                    con =(ArrayList<Consultation>)data.getSerializableExtra("consultations");
                    //추가된 객체들
                    System.out.println("리스트로부터 전달받은 상담일지 인덱스번호 배열: "+arrayIndex);

                    //##############################################################################

                    //원래있던 상담일지 목록에 새로선택한 것도 추가
                    for(int i=0; i<con.size(); i++){
                        consultItems.add(con.get(i));
                    }

                    break;
                }

            }
        }
    }


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //수정내용 보내기
    // 일일보고서 내용 넘김
    private class SendRequestModifyDailyReport extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url= new ApiHost().getApi()+"modify/dayreport";//"http://192.168.1.21:9990/modify/dayreport";
        String message;

        @Override
        protected String doInBackground(Void... params) {

            try {

                HttpClient httpclient = new DefaultHttpClient();
                HttpPut put = new HttpPut(url);

                System.out.println("추가 상담일지  "+arrayIndex);
                System.out.println("삭제 상담일지  "+deleteIndex);
                if(arrayIndex==null)
                    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~``");
                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(10);


                //상담일지 추가 있을 경우
                if(arrayIndex!=null)
                    nameValuePairs.add(new BasicNameValuePair("addConsulList",arrayIndex.toString()));
                //상담일지 삭제 있을 경우
                if(deleteIndex!=null)
                    nameValuePairs.add(new BasicNameValuePair("delConsulList",deleteIndex.toString()));

                nameValuePairs.add(new BasicNameValuePair("token",userToken));
                nameValuePairs.add(new BasicNameValuePair("confirmStatus","0"));
                nameValuePairs.add(new BasicNameValuePair("userNo",userNo));
                nameValuePairs.add(new BasicNameValuePair("index",index));
                nameValuePairs.add(new BasicNameValuePair("title",title));
                nameValuePairs.add(new BasicNameValuePair("saleGoal",priceGoal)); //전달받은 목표액 다시넘김
                nameValuePairs.add(new BasicNameValuePair("rate",rate));
                nameValuePairs.add(new BasicNameValuePair("saleTotal",sale)); //매출액
                nameValuePairs.add(new BasicNameValuePair("startDis",startDis));
                nameValuePairs.add(new BasicNameValuePair("endDis",endDis));
                nameValuePairs.add(new BasicNameValuePair("totalDis",dis));
                nameValuePairs.add(new BasicNameValuePair("description",description));
                nameValuePairs.add(new BasicNameValuePair("newreportDate",sendDate));
                //상담일지


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
            if("{\"changed\":\"changed\"}".equals(message)){
                Toast.makeText(getApplicationContext(),"일일 보고서가 수정되었습니다.",Toast.LENGTH_LONG).show();

                finish();
            }
            else{
                Toast.makeText(getApplicationContext(),"입력된 정보가 올바르지 않습니다.\n다시 작성해주세요.",Toast.LENGTH_LONG).show();
            }
        }

    }






    //상담일지 리스트
    //list 가져오기
    private class SendRequestList extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url= new ApiHost().getApi()+"write/dayreport/attach/consultation";//"http://192.168.1.21:9990/write/dayreport/attach/consultation";
        String message;

        @Override
        protected String doInBackground(Void... params) {

            String status=NetworkUtil.getConnectivityStatusString(ModifyDailyActivity.this);
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
                ActivityCompat.finishAffinity(ModifyDailyActivity.this);
            }else if(message.equals("wifi로 바꾸어 주세요.")){
                Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
            }
            else{

                JSONArray jsonArray = null;
                try {
                    ListInterview listInterview;

                    jsonArray = new JSONArray(message);
                    for(int i=0; i<jsonArray.length(); i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        listInterview = new ListInterview(
                                String.valueOf(jsonObject.getString("no")),
                                jsonObject.getString("title"),
                                jsonObject.getString("firstCustomerName"),
                                jsonObject.getString("secondCustomerName"));
                        items.add(listInterview);
                    }
                    System.out.println("dddddddddddddd"+items);

                    Intent intentlist = new Intent(ModifyDailyActivity.this,ListInterviewActivity.class);
                    intentlist.putExtra("items",items);

                    //list를 다시 돌려받으려고 startActivity 대신 씀
                    //돌려받는 메서드는 onActivityReenter()
                    startActivityForResult(intentlist,PICK_FROM_Interview);

                    System.out.println("lis333333333333333");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        }
    }


    //해당 날짜 목표액 가져오기
    private class SendRequestSalesGoal extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url= new ApiHost().getApi()+"write/dayreport/salesgoal";//"http://192.168.1.21:9990/write/dayreport/salesgoal";
        String message;

        @Override
        protected String doInBackground(Void... params) {

            try {
                SimpleDateFormat sdfNow2 = new SimpleDateFormat("yyyy-MM-dd");
                // nowDate 변수에 값을 저장한다.
                String formatDate2 = sdfNow2.format(date);

                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url);

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


        @Override
        protected void onPostExecute(String s) {

            JSONArray jsonArray = null;
            try {
                if ("weekplanner not exist".equals(message)){
                    Toast.makeText(getApplicationContext(),"작성된 주간계획서가 없습니다.",Toast.LENGTH_SHORT).show();
                    txtGoal.setVisibility(View.GONE);
                    editTxtGoal.setVisibility(View.VISIBLE);
                    priceGoal=price;//null

                    calDate.setTextColor(Color.rgb(192,0,64));

                    editSale.setEnabled(true);
                    editStartMeasure.setEnabled(true);
                    editEndMeasure.setEnabled(true);
                    editDailyReport.setEnabled(true);
                    btnaddConsult.setEnabled(true);
                    btnSubmit.setEnabled(true);
                    btnSubmit.setTextColor(Color.rgb(136,0,0));
                }
                else {
                    calDate.setTextColor(Color.rgb(192,0,64));
                    jsonArray = new JSONArray(message);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    if(jsonObject.has("saleGoal")) {
                        priceGoal = jsonObject.getString("saleGoal");
                        System.out.println("목표액" + priceGoal);

                        txtGoal.setVisibility(View.VISIBLE);
                        editTxtGoal.setVisibility(View.GONE);

                        txtGoal.setText(" [ 목표액 : " + jsonObject.getString("saleGoal") + " 원 ]");

                        editSale.setEnabled(true);
                        editStartMeasure.setEnabled(true);
                        editEndMeasure.setEnabled(true);
                        editDailyReport.setEnabled(true);
                        btnaddConsult.setEnabled(true);
                        btnSubmit.setEnabled(true);
                        btnSubmit.setTextColor(Color.rgb(136,0,0));

                    }
                    else{
                        Toast.makeText(getApplicationContext(),"해당 날짜의 "+jsonObject.getString("already exist")+"가 이미 있습니다.",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

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

        public Uid(String validationToken) {
            token = validationToken;
            fcmkey = preferences.getString("fcmkey","0");
            System.out.println("checkauth fcmy"+fcmkey);
        }


        @Override
        protected String doInBackground(Void... params) {
            String status=NetworkUtil.getConnectivityStatusString(ModifyDailyActivity.this);
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
                ActivityCompat.finishAffinity(ModifyDailyActivity.this);
            }else if(message.equals("wifi로 바꾸어 주세요.")){
                Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                //message에 따라서 결과과
                if(message.equals("accept")){

                    if(flag==0){
                        new SendRequestSalesGoal().execute();
                    }
                    else if(flag==1){ //통신
                        if(d_startDis>d_endDis){
                            Toast.makeText(getApplicationContext(),"계기판을 다시 입력해 주세요.",Toast.LENGTH_SHORT).show();
                            editStartMeasure.setText("");
                            editEndMeasure.setText("");
                            return;
                        }

                        price=editTxtGoal.getText().toString();
                        title = editTitle.getText().toString();
                        sale = editSale.getText().toString();
                        description = editDailyReport.getText().toString();

                        //작성된 주간계획이 없을경우 목표액 직접설정
                        if (price != null && editTxtGoal.getVisibility()==View.VISIBLE) {
                            priceGoal=price;
                        }

                        //일일보고서 작성부분 넘김
                        new SendRequestModifyDailyReport().execute();

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
}