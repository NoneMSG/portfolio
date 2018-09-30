package com.example.bit_user.sms;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.bit_user.sms.R.id.uploadFileList;

public class ReadDailyActivity extends AppCompatActivity implements View.OnClickListener{
    public static Activity rdaActivity;

    SharedPreferences preferences;
    String fcmkey;
    private int flag;

    private String leaderConfirm;

    private String userToken;
    private String index;
    private String regDate;
    private int userNo;

    private int commentUserNo;
    private int commentNo;

    private String ROLE;
    private boolean linkConsultation=true;
    private boolean linkAttachment=true;
    private boolean linkComment=true;

    private String dis;

    private String position="day";
    private String confirm;
    private String saleGoal;
    private String reportDate;
    private String saleTotal;

    private String startDis;
    private String endDis;

    private String regDateCmt;
    private String reportIndex;

    TextView txtlistconsultation;
    TextView txtlistfile;

    TextView department;
    TextView name;
    TextView leader;
    TextView today;

    TextView txtTitle;
    TextView txtGoal;
    TextView txtSale;
    TextView txtRate;

    TextView txtStart;
    TextView txtEnd;
    TextView txtDis;

    TextView txtLeaderOp;

    TextView txtDailyReport;

    TextView uploadName;

    ProgressBar loadingBar;

    Button btnComment;
    Button btnCancel;
    Button btnModify;
    Button btnDelete;
    Button btnRequest;
    Button btnApproval;
    Button btnReturn;

    ListView listComment;
    ArrayList<Comment> items;
    ArrayAdapter<Comment> listAdapter;
    Comment comment;

    ListView listConsultation;
    ArrayList<Consultation> consultItems;
    ArrayAdapter<Consultation> consultAdapter;

    LayoutInflater inflater;

    ListView listAttachment;
    ArrayList<Attachment> attachItems;
    ArrayAdapter<Attachment> attachAdapter;
    Attachment attachment;

    Button btnOK;
    Button btnCancelcmt;

    Uri fileURI;
    ArrayList<Uri> uriList = new ArrayList<Uri>();
    //attachment button
    Button attachBtn;
    Button sendBtn;
    ListView uploadList;
    ArrayList<String> temp = new ArrayList();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_daily);
        rdaActivity=ReadDailyActivity.this;

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

        txtStart=(TextView)findViewById(R.id.txtStart);
        txtEnd=(TextView)findViewById(R.id.txtEnd);
        txtDis = (TextView)findViewById(R.id.txtDis);

        txtlistconsultation=(TextView)findViewById(R.id.txtlistconsultation);
        txtlistfile=(TextView)findViewById(R.id.txtlistfile);

        txtDailyReport=(TextView)findViewById(R.id.txtDailyReport);

        txtLeaderOp=(TextView)findViewById(R.id.txtLeaderOp);

        loadingBar = (ProgressBar)findViewById(R.id.loading);

        listComment=(ListView)findViewById(R.id.listComment);
        listConsultation=(ListView)findViewById(R.id.listConsultation);
        listAttachment=(ListView)findViewById(R.id.listAttachment);

        onbtnClick();
        uploadName=(TextView)findViewById(R.id.uploadName);
        new SendRequestLeaderinfo().execute();
        System.out.println("권한1 : "+ROLE);
        //new SendRequestDayplanner().execute();
        System.out.println("권한2 : "+ROLE);
        //리스트가 아닌 주간계획서로부터 승인완료된 일일보고서로 들어올 경우
        uploadList = (ListView)findViewById(uploadFileList);

        uploadList.setVisibility(View.GONE);

        uploadList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        listAttachment.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        listConsultation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        items=new ArrayList<>();


        if (getIntent().getStringExtra("daylist") == null)
            new SendRequestDayplanner().execute();

        else if((getIntent().getStringExtra("daylist")).isEmpty()==false)
            changeJsonConsultation(getIntent().getStringExtra("daylist"));



    }

    public void onbtnClick() {
        attachBtn = (Button)findViewById(R.id.fileExplorer);
        sendBtn = (Button)findViewById(R.id.sendfilebtn);
        btnComment = (Button) findViewById(R.id.btnComment);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        System.out.println("권한3 : "+ROLE);

        btnModify = (Button) findViewById(R.id.btnModify);
        btnDelete = (Button)findViewById(R.id.btnDelete);

        btnApproval= (Button) findViewById(R.id.btnApproval);
        btnReturn= (Button) findViewById(R.id.btnReturn);


        //팀장 : 승인
        btnApproval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag=111;
                new Uid(userToken).execute();
            }
        });

        //팀장 : 반려
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag=222;
                new Uid(userToken).execute();
            }
        });


        btnModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag=1;
                new Uid(userToken).execute();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag=2;
                new Uid(userToken).execute();
            }
        });

        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag=5;
                new Uid(userToken).execute();
            }
        });


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                flag=4;
                new Uid(userToken).execute();
            }
        });

        attachBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                flag=3;
                new Uid(userToken).execute();
            }
        });

    }

    @Override
    public void onClick(View v) {

        //이전 일일보고서 삭제제
        /*ReadDailyActivity rdaActivity = (ReadDailyActivity)ReadDailyActivity.rdaActivity;
        rdaActivity.finish();*/
        System.out.println("getID onclick"+v.getId());
        //Toast.makeText(this,"getID "+v.getId(),Toast.LENGTH_SHORT).show();
        switch(v.getId()){
            case R.id.btnGoCon:
                Consultation consultation = (Consultation) v.getTag();
                System.out.println(consultation.getNo());
                System.out.println(consultation.getRegDate());
                Intent intent = new Intent(ReadDailyActivity.this,ReadInterviewActivity.class);
                intent.putExtra("token",userToken);
                intent.putExtra("index",String.valueOf(consultation.getNo()));
                intent.putExtra("regDate",consultation.getRegDate());
                startActivity(intent);
                break;

            case R.id.btnDeleteComment:
                comment = (Comment) v.getTag();
                commentNo=comment.getNo();
                commentUserNo=comment.getUserNo();
                new SendRequestDeleteComment().execute();
                break;

            case R.id.btnGoatta:
                int permissionWrite = ContextCompat.checkSelfPermission(ReadDailyActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
                System.out.println(permissionWrite);
                if(permissionWrite==-1){
                    ActivityCompat.requestPermissions(ReadDailyActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
                }else{
                    attachment = (Attachment) v.getTag();
                    loadingBar.setVisibility(View.VISIBLE);
                    new ImageDownload().execute();
                }
                break;

            case R.id.btnDelatta:
                attachment = (Attachment) v.getTag();
                loadingBar.setVisibility(View.VISIBLE);
                new SendRequestDeleteAttachment().execute();
                break;
        }



    }

    private class SendRequestDeleteAttachment extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url= new ApiHost().getApi()+"delete/attachment";//"http://192.168.1.21:9990/write/info";
        String message;

        @Override
        protected String doInBackground(Void... params) {

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url);

                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("token",userToken));
                nameValuePairs.add(new BasicNameValuePair("attachNo",String.valueOf(attachment.getNo())));
                nameValuePairs.add(new BasicNameValuePair("author",String.valueOf(userNo)));

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
            if("done".equals(message)){
                loadingBar.setVisibility(View.GONE);
                /*attachAdapter.notifyDataSetChanged();
                listAttachment.setAdapter(attachAdapter);*/
                onResume();
            }
        }
    }




    //추가 첨부파일
    private String getOriginalFileName(Uri url){
        Cursor cursor = getContentResolver().query(url, null, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
        cursor.moveToFirst();
        return cursor.getString(column_index);
        //orginalNList.add(i,cursor.getString(column_index));
    }
    private String getMD5CheckSum(Uri fileURI){
        try {
            InputStream file = getContentResolver().openInputStream(fileURI);
            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead=0;
            while(numRead != -1){
                numRead = file.read(buffer);
                if(numRead>0)
                    digest.update(buffer,0,numRead);
            }
            System.out.println("numRead=============="+numRead);
            System.out.println("numRead=============="+buffer);
            byte[] md5Bytes = digest.digest();
            return convertHashToString(md5Bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }return null;
    }
    private static String convertHashToString(byte[] md5Bytes) {
        String returnVal = "";
        for (int i = 0; i < md5Bytes.length; i++) {
            returnVal += Integer.toString(( md5Bytes[i] & 0xff ) + 0x100, 16).substring(1);
        }
        return returnVal;
    }

    private class UploadFiles extends AsyncTask<Void, Void, String>{
        private static final String TAG = "res";
        String res="";
        String api =new ApiHost().getApi()+"upload";
        @Override
        protected String doInBackground(Void... params) {

            ArrayList<String> mimeList = new ArrayList<>();
            ArrayList<String> orginalNList = new ArrayList<>();
            ArrayList<String> md5List = new ArrayList<>();
            MimeTypeMap mimecheck = MimeTypeMap.getSingleton();
            //contain mime
            for(int i = 0 ; i < uriList.size(); ++i){
                mimeList.add(i,mimecheck.getExtensionFromMimeType(getContentResolver().getType(uriList.get(i))));
            }
            for(int i = 0 ; i < uriList.size(); ++i){
//                Cursor cursor = getContentResolver().query(uriList.get(i), null, null, null, null);
//                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
//                cursor.moveToFirst();
                orginalNList.add(i,getOriginalFileName(uriList.get(i)));
            }
            for(int i = 0 ; i< uriList.size();++i){
                md5List.add(getMD5CheckSum(uriList.get(i)));
            }

            //String attachmentName = "files";
            String crlf = "\r\n";
            String twoHyphens = "--";
            String boundary =  "*****";
            URL connectURL = null;
            HttpURLConnection conn =null;
            DataOutputStream dos= null;

            try {
                connectURL = new URL(api);
                conn = (HttpURLConnection)connectURL.openConnection();

                JSONArray mimeja = new JSONArray(mimeList);
                JSONArray nameja = new JSONArray(orginalNList);
                JSONArray md5ja = new JSONArray(md5List);

                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestProperty("Connection","Keep-Alive");
                conn.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
                conn.setRequestProperty("Content-Type","multipart/form-data;boundary="+boundary);
                conn.setRequestProperty("token",userToken); //token 설정 헤더에
                conn.setRequestProperty("index",index); //token 설정 헤더에
                conn.setRequestProperty("mime",mimeja.toString()); //token 설정 헤더에
                conn.setRequestProperty("name",nameja.toString()); //token 설정 헤더에
                conn.setRequestProperty("md5",md5ja.toString()); //token 설정 헤더에
                conn.setRequestProperty("author",String.valueOf(userNo)); //작성자 번호


                // conn.setRequestProperty("md5",getMD5CheckSum(fileURI)); // md5 값
                // conn.setRequestProperty("mime",mime); // 파일 타입

                conn.setRequestMethod("POST");


                for(int i = 0 ; i<uriList.size();++i){

                    String attachmentName = "files";
                    InputStream file =null;
                    System.out.println("uriList.get(i)  "+uriList.get(i));
                    file = getContentResolver().openInputStream(uriList.get(i));
                    InputStreamReader isr = new InputStreamReader(file);

                    dos = new DataOutputStream(conn.getOutputStream());
                    dos.writeBytes(twoHyphens + boundary + crlf);
                    dos.writeBytes("Content-Disposition: form-data; name=\""+attachmentName+"\";filename=\""+ orginalNList.get(i) +"\"" + crlf); //파일이름설정
                    dos.writeBytes("Content-Type: application/octet-stream"+crlf); //type 설정
                    dos.writeBytes("Content-Transfer-Encoding: binary" + crlf);
                    dos.writeBytes(crlf);

                    int bytesAvailable = file.available(); //파일크기
                    //buffer 크기 파일의 크기에 맞게 설정
                    byte[] buffer = new byte[bytesAvailable]; //파일크기만큼 버퍼생성
                    int bytesRead = file.read(buffer); //버퍼에 파일 데이터 넣음
                    System.out.println(i+"bytesRead========================="+bytesRead);
                    while (bytesRead != -1)
                    {
                        dos.write(buffer);
                        bytesAvailable = file.available();
                        bytesRead = file.read(buffer);
                    }
                    dos.writeBytes(crlf);
                    //file 읽어오기 **********

                    //dos.flush(); //전송

                }
//                for(int i = 0 ; i<uriList.size();++i){
//
//                    dos.writeBytes(twoHyphens+boundary+crlf);
//                    dos.writeBytes("Content-Disposition: form-data; name=\"" + "values" + "\"" + crlf);
//                    dos.writeBytes("Content-Type: text/plain" + crlf);
//                    dos.writeBytes(mimeList.toString());
//                    dos.writeBytes(crlf);
//
//                }

                //mulipart finish flag
                dos.writeBytes(twoHyphens + boundary + twoHyphens + crlf);


                BufferedReader bufreader = null;

                StringBuffer page = new StringBuffer();
                InputStream is = conn.getInputStream();
                bufreader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String oline = null;
                while ((oline = bufreader.readLine()) != null) {
                    page.append(oline);
                }
                bufreader.close();
                res = page.toString();

                Log.d(TAG, "여기를 탐~~~~~~~ = "+res);

                is.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            conn.disconnect();

            return res;
        }

        @Override
        protected void onPostExecute(String s) {
            //super.onPostExecute(s);
            if(res.equals("done")){
                Toast.makeText(getApplicationContext(),"첨부파일 업로드완료",Toast.LENGTH_SHORT).show();
                uploadList.setVisibility(View.GONE);
                txtlistfile.setVisibility(View.VISIBLE);
                listAttachment.setVisibility(View.VISIBLE);
                onResume();
            }else if(res.equals("confirmedReport")){
                Toast.makeText(getApplicationContext(),"첨부파일 업로드실패",Toast.LENGTH_SHORT).show();
            }
            //list clear
            uriList.clear();
            temp.clear();

        }
    }

    //result of fileExplorer
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //get file path in android
        if(resultCode==RESULT_OK){
            System.out.println("onActivityResult");
            //System.out.println(requestCode);
            //System.out.println("onActivityResult"+data.getData());
            fileURI=data.getData();
            System.out.println(fileURI);
            System.out.println(uriList.size());
            //list size limit 5

            if(uriList.size()<=5) {
                uriList.add(fileURI);
                temp.add(getOriginalFileName(fileURI));
            }
            ArrayAdapter adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, temp);
            uploadList.setAdapter(adapter);
            uploadList.setVisibility(View.VISIBLE);
            //System.out.println("uriList  "+uriList);

            //uploadList.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, lv_arr));

        }


    }






    //##########첨부파일 다운,보기
    private class ImageDownload extends AsyncTask<String, Void, Void> {
        private final String SAVE_FOLDER = "/SMSproject";
        byte[] tmpByte=null;
        String filePath;

        //웹 서버 쪽 파일이 있는 경로
        String fileUrl = new ApiHost().getApi()+"download";//"http://192.168.1.21:9990/download";

        DataOutputStream dos = null;

        @Override

        protected Void doInBackground(String... params) {

            try {

                String param1 = "token=" + userToken;
                String param2 = "index=" + String.valueOf(attachment.getNo());
                String param3 = "dayNo=" + String.valueOf(attachment.getDayNo());


                //#################################################################################

                URL imgUrl = new URL(fileUrl);

                //서버와 접속하는 클라이언트 객체 생성

                HttpURLConnection conn = (HttpURLConnection) imgUrl.openConnection();
                conn.setRequestMethod("POST");

                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);

                //파일 저장 스트림 생성
                dos = new DataOutputStream(conn.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(dos, "UTF-8")); //캐릭터셋 설정

                //outputstream & buffer 이용 파라미터 전송
                writer.write(param1 + "&" + param2 + "&" + param3);//요청 파라미터를 입력
                writer.flush();
                writer.close();
                dos.close();
                conn.connect();

                //############################################################################333


                Map<String,List<String>> map = conn.getHeaderFields();
                System.out.println("헤더값 "+map+" 끝 ");

                List<String> listFilename = map.get("orignalname");
                System.out.println("filename : "+listFilename.get(0)); //파일명+확장자
                String filename = "/"+listFilename.get(0);

                //##############################################################

                InputStream is = conn.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);

                ByteArrayOutputStream bos = null;
                bos = new ByteArrayOutputStream();

                int len = conn.getContentLength();

                System.out.println(len);
                tmpByte = new byte[len];


                //입력 스트림을 구한다
                int read = 0;
                String line = null;

                //입력 스트림을 파일로 저장
                while ((read = bis.read()) != -1) {
                    bos.write(read);
                }
                tmpByte = bos.toByteArray();

                //System.out.println("넘어온 값은 ? " + Arrays.toString(tmpByte));

                //################################################################################

                FileOutputStream fop = null;
                String content = "This is the text content";

                filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + filename;

                try {

                    File file = new File(filePath);
                    System.out.println(filePath);

                    // if file doesnt exists, then create it
                    if (!file.exists()) {
                        file.createNewFile();
                        System.out.println("SMS 파일생성");
                    }

                    System.out.println("경로"+file.toString());
                    fop = new FileOutputStream(file);


                    // get the content in bytes
                    byte[] contentInBytes = tmpByte;

                    fop.write(contentInBytes);
                    fop.flush();
                    fop.close();

                    System.out.println("Done");

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fop != null) {
                            fop.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //byteArrayToBitmap(tmpByte);

                is.close();
                bos.close();
                conn.disconnect();

            } catch (Exception e) {

                e.printStackTrace();

            }
            return null;

        }


        @Override

        protected void onPostExecute(Void result) {

            super.onPostExecute(result);

            if(".png".equals(attachment.getExtName()) || ".jpg".equals(attachment.getExtName()) || ".JPG".equals(attachment.getExtName()) || ".PNG".equals(attachment.getExtName())){
                //Toast.makeText(getContext().getApplicationContext(), filePath+"에 저장되었습니다.", Toast.LENGTH_SHORT).show();
                System.out.println("!!!!!!!!!!!!!!!!통신끝!!!!!!!!!!!!!!");
                Intent intent = new Intent(ReadDailyActivity.this, ImageActivity.class);
                intent.putExtra("filePath",filePath);
                startActivity(intent);
                loadingBar.setVisibility(View.GONE);
            }
            else{
                Toast.makeText(getApplicationContext(), filePath+"에 저장되었습니다.", Toast.LENGTH_SHORT).show();
                loadingBar.setVisibility(View.GONE);
            }

            /*Bitmap bitmap = BitmapFactory.decodeByteArray( tmpByte, 0, tmpByte.length ) ;
            image.setImageBitmap(bitmap);
            loadingBar.setVisibility(View.GONE);*/


        }

    }







    //#########Comment 삭제
    private class SendRequestDeleteComment extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url= new ApiHost().getApi()+"delete/comment";//"http://192.168.1.21:9990/delete/comment";
        String message;

        @Override
        protected String doInBackground(Void... params) {

            try {
                System.out.println("userNo  "+commentUserNo+" / no  "+commentNo);
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url);


                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(3);
                nameValuePairs.add(new BasicNameValuePair("token",userToken));
                nameValuePairs.add(new BasicNameValuePair("userNo",String.valueOf(commentUserNo)));
                nameValuePairs.add(new BasicNameValuePair("no",String.valueOf(commentNo)));


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

            if("no permission to delete".equals(message))
                Toast.makeText(getApplicationContext(), "본인이 작성한 글 외에는 삭제할 수 없습니다.", Toast.LENGTH_SHORT).show();
            else{
                //items.remove(comment);
                //listAdapter.notifyDataSetChanged();
                onResume();
            }
        }
    }





    //승인요청 보내기
    private class SendRequestDayplannerRequest extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url= new ApiHost().getApi()+"dayreport/requestconfirm";//"http://192.168.1.21:9990/dayreport/requestconfirm";
        String message;

        @Override
        protected String doInBackground(Void... params) {
            String status=NetworkUtil.getConnectivityStatusString(ReadDailyActivity.this);
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
                nameValuePairs.add(new BasicNameValuePair("userNo",String.valueOf(userNo)));
                nameValuePairs.add(new BasicNameValuePair("reportDate",reportDate));
                System.out.println("일일보고서 인덱스 : "+index);

                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
                post.setEntity(ent);

                //전송
                HttpResponse httpResponse = httpclient.execute(post);

                //응답
                HttpEntity resEntity = httpResponse.getEntity();

                //토큰 혹은 false를 message에 담고서 비교
                message= EntityUtils.toString(resEntity);
                System.out.println("승인 ) 요청내용 전달받음 : "+message);


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            }catch (HttpHostConnectException e) {
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
                ActivityCompat.finishAffinity(ReadDailyActivity.this);
            }else if(message.equals("wifi로 바꾸어 주세요.")){
                Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                if("보고서는 당일만 승인 요청 가능".equals(message))
                    Toast.makeText(getApplicationContext(), "보고서는 당일만 승인 요청 가능합니다.", Toast.LENGTH_SHORT).show();
                else if("already requeseted or already confirmed".equals(message)){
                    Toast.makeText(getApplicationContext(), "이미 요청된 보고서가 있습니다.", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "보고서 승인이 요청 되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

        }
    }





    //주간계획서 기본정보 , 일간계획 부분 가져오기
    private class SendRequestDayplanner extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url=new ApiHost().getApi()+"view/dayreport/content";//"http://192.168.1.21:9990/view/dayreport/content";
        String message;

        @Override
        protected String doInBackground(Void... params) {

            String status=NetworkUtil.getConnectivityStatusString(ReadDailyActivity.this);
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
                System.out.println("일일보고서 인덱스 : "+index);

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
            }catch (IOException e) {
                e.printStackTrace();
            }


            return message;
        }


        @Override
        protected void onPostExecute(String s) {
            if(message.equals("네트워크가 연결되지 않아 종료됩니다.")){
                Toast.makeText(getApplicationContext(), "네트워크가 연결되지 않아 종료됩니다.", Toast.LENGTH_SHORT).show();
                ActivityCompat.finishAffinity(ReadDailyActivity.this);
            }else if(message.equals("wifi로 바꾸어 주세요.")){
                Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                changeJsonConsultation(message);
            }

        }
    }

    public void changeJsonConsultation(String jsonMessage){
        Comment cmtObject;
        Consultation consultation;
        Attachment attachment;

        //가져온 name,depName,email,teamleader 저장
        try {
            JSONObject jo = new JSONObject(jsonMessage);

            JSONArray jsonArray = jo.getJSONArray("dayReport");
            JSONObject jsonObject = jsonArray.getJSONObject(0);

            if("LEADER".equals(ROLE)){
                name.setText(" 이름 : "+jsonObject.getString("author"));
            }

            //일일보고서 정보 뿌리기기
            userNo=jsonObject.getInt("user_no");//작성자 번호

            txtTitle.setText(jsonObject.getString("title"));

            txtGoal.setText(" 목표액 : "+jsonObject.getString("sale_goal")+" 원");
            txtSale.setText(" 판매액 : "+jsonObject.getString("sale_total")+" 원");

            String sale_goal = jsonObject.getString("sale_goal");
            String sale_total= jsonObject.getString("sale_total");
            Double d_sale = Double.parseDouble(sale_goal);
            Double d_sale_total = Double.parseDouble(sale_total);

            String rate;
            if(d_sale_total==0||d_sale==0)
                rate="0.00";
            else
                rate=String.format("%.2f",(d_sale_total/d_sale)*100);

            txtRate.setText("달성률 : "+rate+" %");
            regDate=jsonObject.getString("reg_date");



            txtStart.setText(" 출근시 계기판 : "+jsonObject.getString("start_distance")+" km");
            txtEnd.setText(" 퇴근시 계기판 : "+jsonObject.getString("end_distance")+" km");
            txtDis.setText("주행 거리 : "+jsonObject.getString("total_distance")+" km");

            dis=jsonObject.getString("total_distance");
            //#########################웹에서 작성된 경우 html 태그 제거########################3
            String removeHtmlText = Html.fromHtml(jsonObject.getString("description")).toString();
            txtDailyReport.setText(removeHtmlText);

            startDis = jsonObject.getString("start_distance");
            endDis = jsonObject.getString("end_distance");

            confirm = jsonObject.getString("confirm");
            System.out.println("confirm "+confirm);
            saleGoal=jsonObject.getString("sale_goal");
            saleTotal=jsonObject.getString("sale_total");

            reportDate=jsonObject.getString("report_date");
            today.setText(" 등록일 : "+regDate.substring(0,10)+"("+reportDate+")");

            btnRequest=(Button) findViewById(R.id.btnRequest);
            if(!"0".equals(confirm)) {
                btnRequest.setVisibility(View.GONE);
                //attachBtn.setVisibility(View.GONE);
                //sendBtn.setVisibility(View.GONE);
                //uploadList.setVisibility(View.GONE);
                //uploadName.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
                btnModify.setVisibility(View.GONE);

                if("1".equals(confirm)){

                    txtLeaderOp.setText("승인 요청중 입니다.");
                    if ("LEADER".equals(ROLE)){
                        btnDelete.setVisibility(View.GONE);
                        btnModify.setVisibility(View.GONE);
                        attachBtn.setVisibility(View.GONE);
                        sendBtn.setVisibility(View.GONE);

                        //반려 & 승인 : 이미 처리된경우 버튼 안보이게
                        btnApproval.setVisibility(View.VISIBLE);
                        btnReturn.setVisibility(View.VISIBLE);

                    }


                    if("2".equals(confirm) || "1".equals(confirm)||"3".equals(confirm)){ //반려 & 승인 : 이미 처리된경우 버튼 안보이게
                        System.out.println("onResume"+confirm);

                    }
                }
                else if("2".equals(confirm))
                    txtLeaderOp.setText("반려되었습니다.");
                else//###################################################팀장의견 받아와서 수정해야 함 ******************************************
                    txtLeaderOp.setText("이미 승인된 보고서 입니다.");
            }
            //###############################################################################3

            //상담일지
            JSONArray jsonArraycst = jo.getJSONArray("consultation");
            if(jsonArraycst.isNull(0)){
                linkConsultation=false;
                txtlistconsultation.setVisibility(View.GONE);
                listConsultation.setVisibility(View.GONE);
            }

            consultItems=new ArrayList<>();

            for(int i=0; i<jsonArraycst.length(); i++){

                jsonObject = jsonArraycst.getJSONObject(i);

                consultation = new Consultation(
                        jsonObject.getInt("no"),
                        jsonObject.getString("title"),
                        jsonObject.getString("reg_date"));

                consultItems.add(consultation);
            }
            System.out.println(consultItems.toString());

            consultAdapter=new ConsultationListAdapter(getBaseContext(),consultItems,userToken);
            listConsultation.setAdapter(consultAdapter);
            //setListViewHeightBasedOnChildren(listConsultation);
            listConsultation.setChoiceMode(listConsultation.CHOICE_MODE_SINGLE);
            //consultAdapter.notifyDataSetChanged();

            //###############################################################################3

            //첨부파일
            JSONArray jsonArrayatt = jo.getJSONArray("attachment");
            if(jsonArrayatt.isNull(0)){
                linkAttachment=false;
                txtlistfile.setVisibility(View.GONE);
                listAttachment.setVisibility(View.GONE);
            }

            attachItems=new ArrayList<>();

            for(int i=0; i<jsonArrayatt.length(); i++){

                jsonObject = jsonArrayatt.getJSONObject(i);

                attachment = new Attachment(
                        jsonObject.getInt("no"),
                        jsonObject.getInt("day_no"),
                        jsonObject.getString("path"),
                        jsonObject.getString("original_name"),
                        jsonObject.getString("ext_name"),
                        jsonObject.getString("md5"));

                attachItems.add(attachment);
                System.out.println("연결 첨부파일"+attachment.getNo());
            }
            attachAdapter=new AttachmentListAdapter(getBaseContext(),attachItems,userToken);
            listAttachment.setAdapter(attachAdapter);
            listAttachment.setChoiceMode(listAttachment.CHOICE_MODE_SINGLE);
            //setListViewHeightBasedOnChildren(listAttachment);

            //###############################################################################3

            JSONArray jsonArraycmt = jo.getJSONArray("comment");
            if(jsonArraycmt.isNull(0)){
                linkComment=false;
                listComment.setVisibility(View.GONE);
            }

            for(int i=0; i<jsonArraycmt.length(); i++){
                jsonObject = jsonArraycmt.getJSONObject(i);

                String removeHtmlText2 = Html.fromHtml(jsonObject.getString("description")).toString();

                cmtObject = new Comment(
                        jsonObject.getInt("no"),
                        jsonObject.getInt("user_no"),
                        jsonObject.getString("name"),
                        removeHtmlText2,
                        jsonObject.getString("reg_date"));

                items.add(cmtObject);
            }
            listAdapter = new CommentListAdapter(getBaseContext(),items,userToken);
            listComment.setAdapter(listAdapter);
            setListViewHeightBasedOnChildren(listComment);
            listComment.setChoiceMode(listComment.CHOICE_MODE_SINGLE);


            btnRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new SendRequestDayplannerRequest().execute();
                }
            });



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

    //리스트 크기 조정
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            //listItem.measure(0, 0);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();

        params.height = totalHeight;
        listView.setLayoutParams(params);

        listView.requestLayout();
    }



    //role 구분 : 팀장1 사원0
    private class SendRequestLeaderinfo extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url= new ApiHost().getApi()+"checkauthority";//"http://192.168.1.21:9990/checkauthority";
        String message;

        @Override
        protected String doInBackground(Void... params) {
            String status=NetworkUtil.getConnectivityStatusString(ReadDailyActivity.this);
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
            }  catch (HttpHostConnectException e) {
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
                ActivityCompat.finishAffinity(ReadDailyActivity.this);
            }else if(message.equals("wifi로 바꾸어 주세요.")){
                Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                if("0".equals(message))//사원일경우 사원의 정보 가져옴
                    ROLE="EMPLOYEE";
                else{
                    ROLE="LEADER";

                }
                //onResume();
                new SendRequestinfo().execute();
            }



        }
    }



    //팀장승인 승인1,반려2
    private class SendRequestLeaderConfirm extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url= new ApiHost().getApi()+"dayreport/confirm";//"http://192.168.1.21:9990/dayreport/confirm";
        String message;

        @Override
        protected String doInBackground(Void... params) {

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url);

                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("token",userToken));
                nameValuePairs.add(new BasicNameValuePair("flag",leaderConfirm));
                nameValuePairs.add(new BasicNameValuePair("userNo",String.valueOf(userNo)));/////////팀장번호말고 작성팀원의 번호
                nameValuePairs.add(new BasicNameValuePair("index",index));//게시글 번호
                nameValuePairs.add(new BasicNameValuePair("reportDate",reportDate));//보고날짜
                nameValuePairs.add(new BasicNameValuePair("saleTotal",saleTotal));//매출액

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
            Toast.makeText(getApplicationContext(),"처리 되었습니다.",Toast.LENGTH_SHORT).show();
            finish();
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
            String status=NetworkUtil.getConnectivityStatusString(ReadDailyActivity.this);
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
                ActivityCompat.finishAffinity(ReadDailyActivity.this);
            }else if(message.equals("wifi로 바꾸어 주세요.")){
                Toast.makeText(getApplicationContext(), "wifi로 바꾸어 주세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                //message에 따라서 결과과
                if(message.equals("accept")){
                    if(flag==111){ //팀장 : 승인
                        btnReturn.setEnabled(false);
                        leaderConfirm="1";
                        new SendRequestLeaderConfirm().execute();
                    }

                    else if(flag==222){//팀장 : 반려

                        btnApproval.setEnabled(false);
                        leaderConfirm="2";
                        new SendRequestLeaderConfirm().execute();
                    }

                    else if(flag==1){ //수정
                        if("0".equals(confirm)){
                            Intent intent = new Intent(ReadDailyActivity.this,ModifyDailyActivity.class);
                            intent.putExtra("userToken",userToken);
                            intent.putExtra("userNo",String.valueOf(userNo));
                            intent.putExtra("index",index);//글번호
                            intent.putExtra("title",txtTitle.getText());
                            intent.putExtra("reportDate",reportDate);//지정 날짜
                            intent.putExtra("confirm",confirm);
                            intent.putExtra("saleGoal",saleGoal);
                            intent.putExtra("saleTotal",saleTotal);
                            intent.putExtra("startDis",startDis);
                            intent.putExtra("endDis",endDis);
                            intent.putExtra("dis",dis);
                            intent.putExtra("description",txtDailyReport.getText());
                            intent.putExtra("consultItems",consultItems);
                            intent.putExtra("attachItems",attachItems);

                            startActivity(intent);
                            finish();
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"이미 요청된 보고서는 수정할 수 없습니다.",Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if(flag==2){ //삭제
                        String flag0;
                        String commentNo ="1"; //코멘트 있음
                        if (linkAttachment == true && linkConsultation == false)//첨부파일 있고 상담일지 없음
                            flag0="1";
                        else if (linkAttachment == false && linkConsultation == true)//첨부파일 없고 상담일지 있음
                            flag0="2";
                        else if (linkAttachment == true && linkConsultation == true)//둘다 있음
                            flag0="3";
                        else//둘다 없음
                            flag0="4";

                        if(linkComment == false)
                            commentNo="0";//코멘트 없음
                        Intent intent = new Intent(ReadDailyActivity.this, DialogDeleteActivity.class);
                        intent.putExtra("userToken", userToken);
                        intent.putExtra("userNo", String.valueOf(userNo));
                        intent.putExtra("index", index);
                        intent.putExtra("position", position);
                        intent.putExtra("flag", flag0);
                        intent.putExtra("commentNo", commentNo);
                        startActivity(intent);

                    }

                    else if(flag==4){ //첨부파일 첨부

                        if(uriList.isEmpty()||temp.isEmpty()){
                            Toast.makeText(getApplicationContext(),"첨부할 파일이 선택되지 않았습니다.",Toast.LENGTH_SHORT).show();
                        }else{
                            new UploadFiles().execute();
                        }


                    }
                    else if(flag==5){ //코멘트
                        Intent intent = new Intent(ReadDailyActivity.this,DialogCommentActivity.class);
                        intent.putExtra("userToken",userToken);
                        intent.putExtra("dayreportNo",index);
                        intent.putExtra("regDate",regDate);
                        startActivity(intent);

                        linkComment=true;
                        listComment.setVisibility(View.VISIBLE);

                        listAdapter.notifyDataSetChanged();


                    }
                    else if(flag==3){ //첨부파일 열기
                        System.out.println("attachment !");
                        int permissionRead = ContextCompat.checkSelfPermission(ReadDailyActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE);
                        //int permissionWrite = ContextCompat.checkSelfPermission(ReadDailyActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        System.out.println(permissionRead);

                        if(permissionRead==-1 ){
                            ActivityCompat.requestPermissions(ReadDailyActivity.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                        }else{
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("*/*");
                            //open file explorer
                            startActivityForResult(intent,1);
                        }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 1:{
                if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    //open file explorer
                    startActivityForResult(intent,1);
                }else{
                    return;
                }
            }
            case 2:{
                if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    //onClick();
//                   // attachment = (Attachment) v.getTag();
//                    loadingBar.setVisibility(View.VISIBLE);
//                    new ImageDownload().execute();
                }else{
                    return;
                }
            }
        }
    }
}
