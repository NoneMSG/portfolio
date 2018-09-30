package com.example.admin.gabizo3;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "블루투스";

    // Request Code
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private static BluetoothService btService = null;


    ImageButton goBtn;
    ImageButton backBtn;
    ImageButton rightBtn;
    ImageButton leftBtn;
    ImageButton btconnectBtn;
    ImageButton toDrawBtn;
    Button standBtn;
    Button sitBtn;
    Button speedBtn1;
    Button speedBtn2;
    Button speedBtn3;
    Button speedBtn4;
    Button speedBtn5;
    Button modeBtn;
    Button tailupBtn;
    Button taildownBtn;
    TextView currentview;
    TextView speedview;
    int mode = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Button
        goBtn = (ImageButton) findViewById(R.id.go_Btn);
        backBtn = (ImageButton) findViewById(R.id.back_Btn);
        rightBtn = (ImageButton) findViewById(R.id.right_Btn);
        leftBtn = (ImageButton) findViewById(R.id.left_Btn);
        toDrawBtn = (ImageButton) findViewById(R.id.toDraw_Btn);
        standBtn = (Button) findViewById(R.id.stand_Btn);
        sitBtn = (Button) findViewById(R.id.sit_Btn);
        btconnectBtn = (ImageButton) findViewById(R.id.btconnect_Btn);
        speedBtn1 = (Button) findViewById(R.id.speed_Btn1);
        speedBtn2 = (Button) findViewById(R.id.speed_Btn2);
        speedBtn3 = (Button) findViewById(R.id.speed_Btn3);
        speedBtn4 = (Button) findViewById(R.id.speed_Btn4);
        speedBtn5 = (Button) findViewById(R.id.speed_Btn5);
        modeBtn = (Button) findViewById(R.id.mode_Btn);
        tailupBtn = (Button) findViewById(R.id.tailup_Btn);
        taildownBtn = (Button) findViewById(R.id.taildown_Btn);
        currentview = (TextView) findViewById(R.id.currentstate);
        speedview = (TextView) findViewById(R.id.speedstate);

        final Drawable speedbuttonimage = getResources().getDrawable(R.drawable.speedbutton_image);
        final Drawable speedbuttonimageclicked = getResources().getDrawable(R.drawable.speedbutton_imageclicked);
        final Drawable drive = getResources().getDrawable(R.drawable.button_image);
        final Drawable obstacle = getResources().getDrawable(R.drawable.obstacle_image);
        final Drawable wall = getResources().getDrawable(R.drawable.wall_image);

        final String textcolor = "#FFFFFF"; //검
        final String textclickedcolor = "#000000"; //흰

        speedBtn1.setBackground(speedbuttonimage);
        speedBtn1.setTextColor(Color.parseColor(textcolor));
        speedBtn2.setBackground(speedbuttonimage);
        speedBtn2.setTextColor(Color.parseColor(textcolor));
        speedBtn3.setBackground(speedbuttonimageclicked);
        speedBtn3.setTextColor(Color.parseColor(textclickedcolor));
        speedBtn4.setBackground(speedbuttonimage);
        speedBtn4.setTextColor(Color.parseColor(textcolor));
        speedBtn5.setBackground(speedbuttonimage);
        speedBtn5.setTextColor(Color.parseColor(textcolor));

        speedview.setText(R.string.s3);

        modeBtn.setText(R.string.drive);
        modeBtn.setBackground(drive);

        // btService 객체 없을시 새로운 btService객체 생성
        if(btService == null) {
            btService = new BluetoothService(this, mHandler);
        }

        // btconnect Button onClick
        btconnectBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("메시지", "Bluetooth Connect");
                if(btService.getDeviceState()) {
                    btService.enableBluetooth();
                } else {
                    finish();
                }
            }
        });

        // Go Button onClick
        goBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                currentview.setText(R.string.go);
                currentview.setTextSize(15);
                sendData("\n"+"m 1 2"+"\n");
                Log.d("메시지", "Go");
            }
        });

        // Back Button onClick
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                currentview.setText(R.string.back);
                currentview.setTextSize(15);
                sendData("\n"+"m 2 2"+"\n");
                Log.d("메시지", "Back");
            }
        });

        // Left Button onClick
        leftBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                currentview.setText(R.string.left);
                currentview.setTextSize(15);
                sendData("\n"+"m 3 2"+"\n");
                Log.d("메시지", "Left");
            }
        });

        // Right Button onClick
        rightBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                currentview.setText(R.string.right);
                currentview.setTextSize(15);
                sendData("\n" + "m 4 2" + "\n");
                Log.d("메시지", "Right");
            }
        });

        // ModeChange(toDraw) Button onClick
        toDrawBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("메시지", "ChangeMode(toDraw)");
                Intent intent = new Intent(getApplicationContext(), DrawActivity.class);
                startActivity(intent);
            }
        });

        // Stand Button onClick
        standBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                currentview.setText(R.string.stand);
                currentview.setTextSize(15);
                sendData("\n" + "m 0 1" + "\n");
                Log.d("메시지", "Stand");
            }
        });

        // Sit Button onClick
        sitBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                currentview.setText(R.string.sit);
                currentview.setTextSize(15);
                sendData("\n"+"m 0 0"+"\n");
                Log.d("메시지", "Sit");
            }
        });

        // ObstacleMode Button onClick
        modeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(mode == 1) {
                    currentview.setText(R.string.drive);
                    currentview.setTextSize(14);
                    modeBtn.setText(R.string.drive);
                    modeBtn.setBackground(drive);
                    mode = 2;
                    Log.d("메시지", "Drive Mode");
                }
                else if( mode == 2 ) {
                    currentview.setText(R.string.ob);
                    currentview.setTextSize(14);
                    modeBtn.setText(R.string.ob);
                    modeBtn.setBackground(obstacle);
                    mode = 3;
                    Log.d("메시지", "Obstacle Mode");
                }
                else if( mode == 3) {
                    currentview.setText(R.string.wall);
                    currentview.setTextSize(14);
                    modeBtn.setText(R.string.wall);
                    modeBtn.setBackground(wall);
                    mode = 1;
                    Log.d("메시지", "Wall Mode");
                }
                sendData("\n" + "c" + "\n");

            }
        });

        tailupBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                currentview.setText(R.string.tailup);
                currentview.setTextSize(14);
                sendData("\n"+"t b"+"\n");
                Log.d("메시지", "Tail up");
            }
        });

        taildownBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                currentview.setText(R.string.taildown);
                currentview.setTextSize(14);
                sendData("\n" + "t s" + "\n");
                Log.d("메시지", "Tail down");
            }
        });

        speedBtn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                speedview.setText(R.string.s1);
                sendData("\n" + "s 1" + "\n");
                speedBtn1.setBackground(speedbuttonimageclicked);
                speedBtn1.setTextColor(Color.parseColor(textclickedcolor));
                speedBtn2.setBackground(speedbuttonimage);
                speedBtn2.setTextColor(Color.parseColor(textcolor));
                speedBtn3.setBackground(speedbuttonimage);
                speedBtn3.setTextColor(Color.parseColor(textcolor));
                speedBtn4.setBackground(speedbuttonimage);
                speedBtn4.setTextColor(Color.parseColor(textcolor));
                speedBtn5.setBackground(speedbuttonimage);
                speedBtn5.setTextColor(Color.parseColor(textcolor));
                Log.d("메시지", "Speed 1");
            }
        });

        speedBtn2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                speedview.setText(R.string.s2);
                sendData("\n"+"s 2"+"\n");
                speedBtn1.setBackground(speedbuttonimage);
                speedBtn1.setTextColor(Color.parseColor(textcolor));
                speedBtn2.setBackground(speedbuttonimageclicked);
                speedBtn2.setTextColor(Color.parseColor(textclickedcolor));
                speedBtn3.setBackground(speedbuttonimage);
                speedBtn3.setTextColor(Color.parseColor(textcolor));
                speedBtn4.setBackground(speedbuttonimage);
                speedBtn4.setTextColor(Color.parseColor(textcolor));
                speedBtn5.setBackground(speedbuttonimage);
                speedBtn5.setTextColor(Color.parseColor(textcolor));
                Log.d("메시지", "Speed 2");
            }
        });

        speedBtn3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                speedview.setText(R.string.s3);
                sendData("\n"+"s 3"+"\n");
                speedBtn1.setBackground(speedbuttonimage);
                speedBtn1.setTextColor(Color.parseColor(textcolor));
                speedBtn2.setBackground(speedbuttonimage);
                speedBtn2.setTextColor(Color.parseColor(textcolor));
                speedBtn3.setBackground(speedbuttonimageclicked);
                speedBtn3.setTextColor(Color.parseColor(textclickedcolor));
                speedBtn4.setBackground(speedbuttonimage);
                speedBtn4.setTextColor(Color.parseColor(textcolor));
                speedBtn5.setBackground(speedbuttonimage);
                speedBtn5.setTextColor(Color.parseColor(textcolor));
                Log.d("메시지", "Speed 3");
            }
        });

        speedBtn4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                speedview.setText(R.string.s4);
                sendData("\n"+"s 4"+"\n");
                speedBtn1.setBackground(speedbuttonimage);
                speedBtn1.setTextColor(Color.parseColor(textcolor));
                speedBtn2.setBackground(speedbuttonimage);
                speedBtn2.setTextColor(Color.parseColor(textcolor));
                speedBtn3.setBackground(speedbuttonimage);
                speedBtn3.setTextColor(Color.parseColor(textcolor));
                speedBtn4.setBackground(speedbuttonimageclicked);
                speedBtn4.setTextColor(Color.parseColor(textclickedcolor));
                speedBtn5.setBackground(speedbuttonimage);
                speedBtn5.setTextColor(Color.parseColor(textcolor));
                Log.d("메시지", "Speed 4");
            }
        });

        speedBtn5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                speedview.setText(R.string.s5);
                sendData("\n"+"s 5"+"\n");
                speedBtn1.setBackground(speedbuttonimage);
                speedBtn1.setTextColor(Color.parseColor(textcolor));
                speedBtn2.setBackground(speedbuttonimage);
                speedBtn2.setTextColor(Color.parseColor(textcolor));
                speedBtn3.setBackground(speedbuttonimage);
                speedBtn3.setTextColor(Color.parseColor(textcolor));
                speedBtn4.setBackground(speedbuttonimage);
                speedBtn4.setTextColor(Color.parseColor(textcolor));
                speedBtn5.setBackground(speedbuttonimageclicked);
                speedBtn5.setTextColor(Color.parseColor(textclickedcolor));
                Log.d("메시지", "Speed 5");
            }
        });


    }

    // 메인쓰레드를 제외한 UI 부분에서 Toast를 사용할수 없기 때문에 핸들러를 사용했다.
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 1:
                    Toast.makeText(getApplicationContext(), "블루투스 연결에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(getApplicationContext(), "블루투스 연결에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(getApplicationContext(), "블루투스 연결이 끊어졌습니다.",Toast.LENGTH_SHORT).show();
                default:
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // DeviceListActivity에서  장치와 연결을 선택하였을 경우
                if (resultCode == Activity.RESULT_OK) {
                    btService.getDeviceInfo(data);
                }
                break;
            case REQUEST_ENABLE_BT:
                // 블루투스 권한 요청에 대한 대답
                if (resultCode == Activity.RESULT_OK) {
                    // 확인 눌렀을 때
                    btService.scanDevice();
                } else {
                    // 취소 눌렀을 때
                    Log.d(TAG, "Bluetooth is not enabled");
                }
                break;
        }
    }

    private void sendData(String message) {

        Log.d(TAG, "sendData");
        // 데이터 보내기 전 블루투스가 실제로 연결 되어 있는지 확인
        if (btService.getState() != BluetoothService.STATE_CONNECTED) {
            Log.d(TAG, "Not connected with Device");
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // 데이터 보내기전 실제로 보낼 데이터가 있는지 확인
        if (message.length() > 0) {
            // 메세지의 byte 수를 얻고 BluetoothService의 write메소드를 실행한다.
            byte[] send = message.getBytes();
            btService.write(send);

        }
    }


    public BluetoothService getBluetoothState() {

        return btService;
    }

}
