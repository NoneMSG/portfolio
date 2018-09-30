package com.example.bit_user.sms;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ListInterviewActivity extends AppCompatActivity{
    Button listbtnCancel;
    Button listbtnChoice;
    ListView listView;

    ArrayList<Consultation> consultations = new ArrayList<>();

    ArrayList<ListInterview> items;
    ArrayAdapter<ListInterview> listAdapter;

    int itemCount=0;
    ArrayList<String> arrayIndex=new ArrayList<String>();
    ArrayList<String> arrayIndexName=new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_interview);

        items=(ArrayList<ListInterview>)getIntent().getSerializableExtra("items");
        System.out.println("listinterviewactivity : "+items);

        listView = (ListView)findViewById(R.id.listView);
        listbtnCancel = (Button)findViewById(R.id.listbtnCancel);
        listbtnChoice = (Button)findViewById(R.id.listbtnChoice);

        listAdapter = new ListDataAdapter(getBaseContext(),items);
        //listAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_multiple_choice,items);
        listView.setAdapter(listAdapter);
        //listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        listView.setChoiceMode(listView.CHOICE_MODE_MULTIPLE);

       listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                System.out.println(listAdapter.getItem(position).getNo());

                    boolean same=false;
                    int index=0;

                    //같은것 클릭할 경우 삭제##########################################
                    for(int i=0; i<itemCount; i++){
                        if(arrayIndex.get(i)==listAdapter.getItem(position).getNo()){
                            Toast.makeText(getApplicationContext(),"상담일지 선택 취소",Toast.LENGTH_SHORT).show();
                            same=true;
                            index=i;
                        }
                    }
                    if(same==false){//더블클릭 없음

                            Toast.makeText(getApplicationContext(),"상담일지 추가",Toast.LENGTH_SHORT).show();
                            arrayIndex.add(listAdapter.getItem(position).getNo());
                            itemCount++;

                            Consultation con = new Consultation(
                                    Integer.parseInt(listAdapter.getItem(position).getNo()),
                                    listAdapter.getItem(position).getTitle());

                            consultations.add(con);
                            System.out.println("count::"+itemCount);
                            view.setSelected(true);

                    }
                    else {//더블클릭=선택취소
                        arrayIndex.remove(index);
                        itemCount--;

                    }

            }
        });
        onbtnClick();

    }

    public void onbtnClick(){
        listbtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        listbtnChoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("최종 선택arrayIndex : "+arrayIndex);

                Intent intent = new Intent();
                intent.putExtra("arrayIndex",arrayIndex);
                intent.putExtra("consultations",consultations);
                setResult(RESULT_OK,intent);

                finish();
            }
        });
    }
}
