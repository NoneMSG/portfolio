package com.example.bit_user.sms;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by bit-user on 2017-09-06.
 */

public class AttachmentListAdapter extends ArrayAdapter<Attachment> {
    ArrayList<Attachment> attachItems = new ArrayList<Attachment>();
    LayoutInflater inflater;
    private String userToken;
    private int pos;
    private int index;

    Context context;

    TextView listTitle;
    Button btnGoatta;
    Button btnDelatta;

    Attachment attachment;
    ImageView image;
    File file;

    public AttachmentListAdapter(Context context, ArrayList<Attachment> object, String userToken) {
        super(context, 0, object);
        attachItems = object;
        this.userToken=userToken;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        pos = position;
        context = parent.getContext();

        if (convertView == null) {

            convertView = inflater.inflate(R.layout.list_attachment, null);
            listTitle = (TextView) convertView.findViewById(R.id.listTitle);
            btnGoatta = (Button) convertView.findViewById(R.id.btnGoatta);
            btnDelatta= (Button) convertView.findViewById(R.id.btnDelatta);
            image = (ImageView)convertView.findViewById(R.id.image);

            listTitle.setText(attachItems.get(pos).getOriginalName());

            System.out.println("indexxxxxxxxx"+attachItems.get(pos).getNo());
            btnGoatta.setOnClickListener((View.OnClickListener)context);
            btnDelatta.setOnClickListener((View.OnClickListener)context);
        }
        btnGoatta.setTag(getItem(position));//선택 첨부파일 객체 넘김
        btnDelatta.setTag(getItem(position));
       /* btnGoatta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                attachment=attachItems.get(pos);

                System.out.println("dddddddddddddddddddd"+attachItems.get(pos).getNo());
                loadingBar.setVisibility(View.VISIBLE);
                new ImageDownload().execute();

            }
        });*/
        return  convertView;
    }



/*
    private class ImageDownload extends AsyncTask<String, Void, Void> {
        private final String SAVE_FOLDER = "/SMSproject";
        byte[] tmpByte=null;
        String filePath;

        //웹 서버 쪽 파일이 있는 경로
        String fileUrl = "http://192.168.1.21:9990/download";

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

                System.out.println("넘어온 값은 ? " + Arrays.toString(tmpByte));

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
                Intent intent = new Intent(getContext(), ImageActivity.class);
                intent.putExtra("filePath",filePath);
                context.startActivity(intent);
                loadingBar.setVisibility(View.GONE);
            }
            else{
                Toast.makeText(getContext().getApplicationContext(), filePath+"에 저장되었습니다.", Toast.LENGTH_SHORT).show();
                loadingBar.setVisibility(View.GONE);
            }

            */
/*Bitmap bitmap = BitmapFactory.decodeByteArray( tmpByte, 0, tmpByte.length ) ;
            image.setImageBitmap(bitmap);
            loadingBar.setVisibility(View.GONE);*//*



        }

    }
*/

}
