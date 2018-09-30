package com.example.admin.gabizo3;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DrawActivity extends AppCompatActivity {
    MainActivity mainActivity = new MainActivity();
    BluetoothService btService = mainActivity.getBluetoothState();

    PaintBoard board;
    ImageButton undoBtn;
    ImageButton clearBtn;
    ImageButton toDriveBtn;
    ImageButton transmitBtn;
    TextView transmitState;
    TextView cordinateNum;

    static final int START_MARK = 1;
    static final int FINISH_MARK = 2;
    static final int TRANSMIT_LIMIT = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        final LinearLayout paintboard = (LinearLayout) findViewById(R.id.paintboard);

        undoBtn = (ImageButton) findViewById(R.id.undoBtn);
        clearBtn = (ImageButton) findViewById(R.id.clearBtn);
        toDriveBtn = (ImageButton) findViewById(R.id.toDrive_Btn);
        transmitBtn = (ImageButton) findViewById(R.id.transmit_Btn);
        transmitState = (TextView) findViewById(R.id.transmit_state);
        cordinateNum = (TextView) findViewById(R.id.cordinate_Num);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);// 리니어 레이아웃의 크기를 파라미터로 지정

        board = new PaintBoard(this,mHandler);
        board.setLayoutParams(params);//board에 파라미터 설정

        paintboard.addView(board);//시행할시 onDraw 두번 실행, onsizechanged가 실행, board가 View이기 때문에  onDraw 실행


        // Undo Button onClick
        undoBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                board.undo();

                /*for(int i = 0; i < board.list.size(); ++i) {
                    String msg = String.format("%.2f , %.2f", board.list.get(i).x, board.list.get(i).y);
                    Log.d("메시지", " " + msg);
                }*/
                Log.d("메시지", " undo 후 좌표의 개수 " + board.list.size());

            }
        });

        // Clear Button onClick
        clearBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                board.list.clear();
                board.undoNum.clear();
                board.clearBoard(paintboard.getWidth(),paintboard.getHeight());

            }
        });

        // ModeChange(toDrive) Button onClick
        toDriveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("메시지", "ChangeMode(toDrive)");
                board.list.clear();
                board.undoNum.clear();
                board.totalPointNum = 0;
                finish();

            }
        });

        // // Transmit Button onClick
        transmitBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCordinate();
                board.list.clear();
                board.undoNum.clear();
                board.totalPointNum = 0;
                transmitState.setText(R.string.transmited);
            }
        });
    }

    private void sendCordinate() {
        String msg;
        int realcordinateNum = board.list.size();
        int totalTranNum = realcordinateNum/TRANSMIT_LIMIT;
        int currentTranNum = 1;
        int cordinateNum = 0;

        msg = String.format("realcordinateNum : %d , totalTranNum : %d , currentTranNum : %d , cordinateNum : %d", realcordinateNum,totalTranNum,currentTranNum,cordinateNum);
        Log.d("메시지", msg);

        /*for( int i = 0; i < totalTranNum; ++i) {
            board.list.get(TRANSMIT_LIMIT*i).lastmark = START_MARK;
            board.list.get((TRANSMIT_LIMIT-1)+TRANSMIT_LIMIT*i).lastmark = FINISH_MARK;
        }
        board.list.get(TRANSMIT_LIMIT*totalTranNum).lastmark = START_MARK;
        board.list.get(board.list.size()-1).lastmark = FINISH_MARK;*/


        while( currentTranNum <= totalTranNum) {
            Log.d("메시지", "while문 안쪽");
            for (int i = cordinateNum; i < TRANSMIT_LIMIT*currentTranNum; ++i) {
                if (board.list.get(i).lastmark == START_MARK || i == (TRANSMIT_LIMIT*(currentTranNum-1))-1) {
                    msg = String.format("\n" + "p %.2f %.2f s" + "\n", board.list.get(i).x, board.list.get(i).y);
                    sendData(msg);
                    msg = String.format("p %.2f %.2f s", board.list.get(i).x, board.list.get(i).y);
                    Log.d("메시지", msg);
                } else if (board.list.get(i).lastmark == FINISH_MARK ||  i == (TRANSMIT_LIMIT*currentTranNum)-1) {
                    msg = String.format("\n" + "p %.2f %.2f f" + "\n", board.list.get(i).x, board.list.get(i).y);
                    sendData(msg);
                    msg = String.format("p %.2f %.2f f", board.list.get(i).x, board.list.get(i).y);
                    Log.d("메시지", msg);

                } else {
                    msg = String.format("\n" + "p %.2f %.2f i" + "\n", board.list.get(i).x, board.list.get(i).y);
                    sendData(msg);
                    msg = String.format("p %.2f %.2f i", board.list.get(i).x, board.list.get(i).y);
                    Log.d("메시지", msg);

                }
                SystemClock.sleep(300);
                cordinateNum = i;
            }

            sendData("\n" + "q" + "\n");
            Log.d("메시지", "q");

            currentTranNum++;
            SystemClock.sleep(10000);
        }

        Log.d("메시지", "while문 끝났다");
        for (int i = cordinateNum; i < board.list.size(); ++i) {

            if (board.list.get(i).lastmark == START_MARK || i == (TRANSMIT_LIMIT*(currentTranNum-1))-1) {
                msg = String.format("\n" + "p %.2f %.2f s" + "\n", board.list.get(i).x, board.list.get(i).y);
                sendData(msg);
                msg = String.format("p %.2f %.2f s", board.list.get(i).x, board.list.get(i).y);
                Log.d("메시지", msg);
            } else if (board.list.get(i).lastmark == FINISH_MARK || i == board.list.size()-1) {
                msg = String.format("\n" + "p %.2f %.2f f" + "\n", board.list.get(i).x, board.list.get(i).y);
                sendData(msg);
                msg = String.format("p %.2f %.2f f", board.list.get(i).x, board.list.get(i).y);
                Log.d("메시지", msg);
            } else {
                msg = String.format("\n" + "p %.2f %.2f i" + "\n", board.list.get(i).x, board.list.get(i).y);
                sendData(msg);
                msg = String.format("p %.2f %.2f i", board.list.get(i).x, board.list.get(i).y);
                Log.d("메시지", msg);
            }
            SystemClock.sleep(300);
        }
        sendData("\n" + "q" + "\n");
        Log.d("메시지", "q");

    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    transmitState.setText("");
                    break;
                case 2:
                    String cord = String.valueOf(board.list.size());
                    cordinateNum.setText(cord);
                default:
                    break;
            }
        }
    };

    // Data 전송
    private void sendData(String message) {
        Log.d("블루투스", "sendData");
        // 데이터 보내기 전 블루투스가 실제로 연결 되어 있는지 확인
        if (btService.getState() != BluetoothService.STATE_CONNECTED ) {
                Log.d("블루투스", "Not connected with Device");
                Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
       // 데이터 보내기전 실제로 보낼 데이터가 있는지 확인
        if (message.length() > 0) {
            //문자열을 매개변수의 인코딩 방식으로 변환해서 바이트 배열로 돌려줍니다.
            byte[] send = message.getBytes();

            btService.write(send);
        }
    }
}
