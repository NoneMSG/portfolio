package com.example.bit_user.sms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by bit-user on 2017-08-22.
 */

public class ListDataAdapter extends ArrayAdapter<ListInterview>{
    private ArrayList<ListInterview> listInterviewItemList = new ArrayList<ListInterview>();
    LayoutInflater inflater;

    TextView listTitle;
    TextView listname1;
    TextView listname2;
    ListInterview listInterview;
    CheckBox listCheck;

    public ListDataAdapter(Context context, ArrayList<ListInterview> object) {
        super(context, 0, object);
        //new ArrayAdapter(this,android.R.layout.simple_list_item_multiple_choice,object);
        listInterviewItemList=object;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        if(convertView ==null){

            convertView=inflater.inflate(R.layout.row,null);
            listTitle = (TextView)convertView.findViewById(R.id.listTitle);
            listname1 = (TextView)convertView.findViewById(R.id.listname1);
            listname2 = (TextView)convertView.findViewById(R.id.listname2);
            //listCheck = (CheckBox)convertView.findViewById(R.id.listCheck);
        }

        System.out.println("adapt");


        listInterview = listInterviewItemList.get(position);
        System.out.println(listInterview);

        //타이틀,거래처 분리
        listTitle.setText(listInterview.getTitle());
        listname1.setText(listInterview.getName1());
        listname2.setText(listInterview.getName2());
        return convertView;
    }
    /*listCheck.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            System.out.println(listInterview.getNo());
        }
    });*/

   /* public CheckBox getCheckView(){
        return listCheck;
    }*/
}
