package com.example.bit_user.sms;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by bit-user on 2017-09-18.
 */

public class CalendarListAdapter extends ArrayAdapter<CalendarList> {
    ArrayList<CalendarList> calendarLists;
    LayoutInflater inflater;
    private String userToken;
    private int pos;
    private int index;

    TextView txtTitle;
    TextView txtFixed;
    TextView txtWriter;
    TextView txtContent;

    public CalendarListAdapter(Context context, ArrayList<CalendarList> object, String userToken) {
        super(context, 0, object);

        calendarLists = object;
        this.userToken = userToken;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final int pos = position;
        final Context context = parent.getContext();

            convertView = inflater.inflate(R.layout.list_calendar_team, null);

            txtTitle=(TextView)convertView.findViewById(R.id.txtTitle);
            txtFixed=(TextView)convertView.findViewById(R.id.txtFixed);
            txtWriter=(TextView)convertView.findViewById(R.id.txtWriter);
            txtContent=(TextView)convertView.findViewById(R.id.txtContent);

            CalendarList calendarList = calendarLists.get(position);

            txtTitle.setText(calendarList.getTitle());
            txtWriter.setText("작성자 : "+calendarList.getName());
            txtContent.setText(calendarList.getContent());
            if ("1".equals(calendarList.getConfirm())){
                txtFixed.setText("승인 대기중");
            }
            else{
                if("2".equals(calendarList.getConfirm()))
                    txtFixed.setText("반려");
                else if("3".equals(calendarList.getConfirm()))
                    txtFixed.setText("승인 완료");
            }

        //btnGoatta.setTag(getItem(position));//선택 첨부파일 객체 넘김

        return  convertView;
    }
}