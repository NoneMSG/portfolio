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
 * Created by bit-user on 2017-09-08.
 */

public class ModifyConsultationAdapter extends ArrayAdapter<Consultation> {
    ArrayList<Consultation> consultItems;
    ArrayList<String> deleteIndex;

    LayoutInflater inflater;
    private String userToken;
    private int pos;
    private int index;

    Context context;

    TextView txtTitle;
    Button btnDelete;

    Consultation consultation;

    public ModifyConsultationAdapter(Context context, ArrayList<Consultation> object, ArrayList<String> deleteIndex,String userToken) {
        super(context, 0, object);
        consultItems = object;
        this.deleteIndex = deleteIndex;
        this.userToken=userToken;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public ModifyConsultationAdapter(Context context, ArrayList<Consultation> object,String userToken) {
        super(context, 0, object);
        consultItems = object;
        this.userToken=userToken;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        pos = position;
        context = parent.getContext();
        View view = null;
        if (convertView == null) {
            view = inflater.inflate(R.layout.list_modify_daily, null);
        }
        else {
            view = convertView;
        }

        final Consultation data = this.getItem(position);
        if (data != null){
            txtTitle = (TextView) view.findViewById(R.id.txtTitle);
            btnDelete = (Button) view.findViewById(R.id.btnDelete);

            txtTitle.setText(consultItems.get(pos).getTitle());

            System.out.println("indexxxxxxxxx"+consultItems.get(pos).getNo());


        }
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                consultation=consultItems.get(pos);
                System.out.println("dddddddddddddddddddd"+consultItems.get(pos).getNo());

                //삭제 리스트에 먼저 추가후 현재목록에서 제거
                deleteIndex.add(String.valueOf(consultItems.get(pos).getNo()));
                consultItems.remove(consultation);

                notifyDataSetChanged();

            }
        });

        return  view;
    }

}
