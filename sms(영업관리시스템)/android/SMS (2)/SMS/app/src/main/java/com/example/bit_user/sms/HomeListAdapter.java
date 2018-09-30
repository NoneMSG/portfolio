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
 * Created by bit-user on 2017-08-25.
 */

public class HomeListAdapter extends ArrayAdapter<Home_List> {
    ArrayList<Home_List> homelistItemList;
    LayoutInflater inflater;

    TextView txtTitle;
    TextView txtWriteDate;
    TextView txtFixed;

    Home_List home_list;


    public HomeListAdapter(Context context, ArrayList<Home_List> object) {
        super(context, 0, object);

        homelistItemList=object;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();



            convertView = inflater.inflate(R.layout.list_view, null);
            txtTitle = (TextView) convertView.findViewById(R.id.txtTitle);
            txtWriteDate = (TextView) convertView.findViewById(R.id.txtWriteDate);
            txtFixed = (TextView) convertView.findViewById(R.id.txtFixed);


            home_list = homelistItemList.get(position);

            txtTitle.setText(home_list.getTitle());
            txtWriteDate.setText(home_list.getWriteDate());

            if("".equals(home_list.getFixed())==false){
                switch (home_list.getFixed()){
                    case "0":
                        txtFixed.setText("미승인");
                        break;
                    case "1":
                        txtFixed.setText("승인 요청중");
                        break;
                    case "2":
                        txtFixed.setText("반려");
                        break;
                    case "3":
                        txtFixed.setText("승인");
                        break;
                }
            }




        return convertView;
    }
}
