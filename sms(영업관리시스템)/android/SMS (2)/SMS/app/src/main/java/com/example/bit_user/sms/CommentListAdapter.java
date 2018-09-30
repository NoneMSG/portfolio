package com.example.bit_user.sms;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by bit-user on 2017-08-28.
 */

public class CommentListAdapter extends ArrayAdapter<Comment>{
    ArrayList<Comment> commentItems = new ArrayList<Comment>();
    LayoutInflater inflater;
    private String userToken;
    private int pos;

    TextView txtCmtName;
    TextView txtComment;
    TextView txtCmtDate;
    Button btnDeleteComment;

    Comment comment;
    Comment cmt;


    public CommentListAdapter(Context context, ArrayList<Comment> object,String userToken) {
        super(context, 0, object);

        commentItems = object;
        this.userToken=userToken;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        pos = position;
        final Context context = parent.getContext();

        if (convertView == null) {

            convertView = inflater.inflate(R.layout.list_comment, null);
            txtCmtName = (TextView) convertView.findViewById(R.id.txtCmtName);
            txtComment = (TextView) convertView.findViewById(R.id.txtComment);
            txtCmtDate = (TextView) convertView.findViewById(R.id.txtCmtDate);
            btnDeleteComment = (Button) convertView.findViewById(R.id.btnDeleteComment);


            comment = commentItems.get(pos);

            txtCmtName.setText(comment.getName()+" : ");
            txtComment.setText(comment.getComment());
            txtCmtDate.setText(" ( "+comment.getDate().substring(0,10)+" )");

            btnDeleteComment.setOnClickListener((View.OnClickListener)context);
        }
        btnDeleteComment.setTag(getItem(position));//comment객체를 넘김

        //코멘트삭제
      /*btnDeleteComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //코멘트 번호를 받아와 어레이 리스트안에서 remove
                //삭제는 어떻게? 번호를 넘겨?여기서 처리해?
                cmt=commentItems.get(position);
                System.out.println(position);

                new SendRequestDeleteComment().execute();
            }
        });*/

        return  convertView;
    }



    //#############################통신부분 전부 상세보기로 넘김 (ReadDailyActivity)

    /*private class SendRequestDeleteComment extends AsyncTask<Void,Void,String> { //background,progress,execcute
        String url="http://192.168.1.21:9990/delete/comment";
        String message;

        @Override
        protected String doInBackground(Void... params) {

            try {
                System.out.println("userNo  "+cmt.getUserNo()+" / no  "+cmt.getNo());
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(url);


                //아이디와 비밀번호 묶음
                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(3);
                nameValuePairs.add(new BasicNameValuePair("token",userToken));
                nameValuePairs.add(new BasicNameValuePair("userNo",String.valueOf(cmt.getUserNo())));
                nameValuePairs.add(new BasicNameValuePair("no",String.valueOf(cmt.getNo())));


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
                Toast.makeText(getContext().getApplicationContext(), "본인이 작성한 글 외에는 삭제할 수 없습니다.", Toast.LENGTH_SHORT).show();
            else{
                commentItems.remove(pos);
                notifyDataSetChanged();
            }
        }
    }*/
}
