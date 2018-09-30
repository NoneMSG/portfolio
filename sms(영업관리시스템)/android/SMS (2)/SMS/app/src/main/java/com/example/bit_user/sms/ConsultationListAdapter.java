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
 * Created by bit-user on 2017-09-01.
 */

public class ConsultationListAdapter extends ArrayAdapter<Consultation> {
    ArrayList<Consultation> consultItems;
    LayoutInflater inflater;
    private String userToken;
    private int pos;
    String date;

    TextView listConTitle;
    Button btnGoCon;

    public ConsultationListAdapter(Context context, ArrayList<Consultation> object, String userToken) {
        super(context, 0, object);

        consultItems = object;
        this.userToken=userToken;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Consultation consultation = consultItems.get(position);
        if(convertView==null){
            final Context context = parent.getContext();

            convertView = inflater.inflate(R.layout.list_consultation, null);
            listConTitle = (TextView) convertView.findViewById(R.id.listConTitle);
            btnGoCon = (Button) convertView.findViewById(R.id.btnGoCon);



            listConTitle.setText(consultation.getTitle());
            pos=consultation.getNo();
            date=consultation.getRegDate();
            //삭제
            btnGoCon.setOnClickListener((View.OnClickListener)context);

        }btnGoCon.setTag(getItem(position));

        return  convertView;
    }

}
