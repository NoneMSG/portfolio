package com.example.bit_user.sms;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class DialogCancelActivity extends AppCompatActivity {
    SharedPreferences preferences;
    Button btnOk;
    Button btnCancel;

    private String flag;
    private String userToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = getSharedPreferences("tokenAndHome",MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog_cancel);

        userToken = getIntent().getStringExtra("userToken");
        flag=getIntent().getStringExtra("flag");

        btnOk=(Button)findViewById(R.id.btnOk);
        btnCancel=(Button)findViewById(R.id.btnCancel);

        //작성창으로 되돌아 가기
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //작성창도 닫고 그 이전의 화면으로 돌아가기
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (flag){
                    case "WriteWeekly" :{
                        WriteWeeklyActivity wwaActivity = (WriteWeeklyActivity)WriteWeeklyActivity.wwaActivity;
                        wwaActivity.finish();
                        break;
                    }
                    case "WriteDaily" :{
                        WriteDailyActivity wdaActivity = (WriteDailyActivity) WriteDailyActivity.wdaActivity;
                        wdaActivity.finish();
                        break;
                    }
                    case "WriteInterview" :{
                        WriteInterviewActivity wiaActivity = (WriteInterviewActivity)WriteInterviewActivity.wiaActivity;
                        wiaActivity.finish();
                        break;
                    }case "ModifyWeekly" :{
                        ModifyWeeklyActivity mwaActivity = (ModifyWeeklyActivity) ModifyWeeklyActivity.mwaActivity;
                        mwaActivity.finish();
                        break;
                    }case "ModifyDaily" :{
                        ModifyDailyActivity mdaActivity = (ModifyDailyActivity) ModifyDailyActivity.mdaActivity;
                        mdaActivity.finish();
                        break;
                    }case "ModifyInterview" :{
                        ModifyInterviewActivity miaActivity = (ModifyInterviewActivity) ModifyInterviewActivity.miaActivity;
                        miaActivity.finish();
                        break;
                    }

                }

                finish();
            }
        });
    }
}
