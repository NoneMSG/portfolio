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

public class ModifyAttachmentAdapter extends ArrayAdapter<Attachment> {
    ArrayList<Attachment> attachItems = new ArrayList<Attachment>();
    ArrayList<String> deleteattaIndex;

    LayoutInflater inflater;
    private String userToken;
    private int pos;
    private int index;

    Context context;

    TextView txtTitle;
    Button btnDelete;

    Attachment attachment;

    public ModifyAttachmentAdapter(Context context, ArrayList<Attachment> object, ArrayList<String> deleteattaIndex, String userToken) {
        super(context, 0, object);
        attachItems = object;
        this.deleteattaIndex=deleteattaIndex;
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

        final Attachment data = this.getItem(position);
        if (data != null){
            txtTitle = (TextView) view.findViewById(R.id.txtTitle);
            btnDelete = (Button) view.findViewById(R.id.btnDelete);

            txtTitle.setText(attachItems.get(pos).getOriginalName());

            System.out.println("indexxxxxxxxx"+attachItems.get(pos).getNo());

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    attachment=attachItems.get(pos);

                    System.out.println("dddddddddddddddddddd"+attachItems.get(pos).getNo());

                    //삭제 리스트에 먼저 추가후 현재목록에서 제거
                    deleteattaIndex.add(String.valueOf(attachItems.get(pos).getNo()));
                    attachItems.remove(attachment);

                    notifyDataSetChanged();
                }
            });
        }

        return  view;
    }

}