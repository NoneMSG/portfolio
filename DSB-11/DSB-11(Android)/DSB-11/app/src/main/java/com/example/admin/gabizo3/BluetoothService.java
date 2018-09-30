package com.example.admin.gabizo3;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService {
    private static final String TAG = "블루투스";

    // Intent 요청코드
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // RFCOMM Protocol (고유 ID)
    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter btAdapter;

    private Activity mActivity;
    private Handler mHandler;

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private int mState;

    // 상태를 나타내는 상태 변수
    public static final int STATE_NONE = 0; // 아무것도 하고 있지 않음
    public static final int STATE_LISTEN = 1; // 연결대기
    public static final int STATE_CONNECTING = 2; // 연결중
    public static final int STATE_CONNECTED = 3; // 연결완료

    public BluetoothService(Activity ac, Handler h) {
        mActivity = ac;
        mHandler = h;

        btAdapter = BluetoothAdapter.getDefaultAdapter(); // 기본 BluetoothAdapter
    }


    // 블루투스가 지원 여부
    public boolean getDeviceState() {
        Log.d(TAG, "Check the Bluetooth support");

        if(btAdapter == null) {
            Log.d(TAG, "Bluetooth is not available");

            return false;

        } else {
            Log.d(TAG, "Bluetooth is available");

            return true;
        }
    }

    // 블루투스 연결 시도
    public void enableBluetooth() {
        Log.d(TAG, "Check the enabled Bluetooth");

        if(btAdapter.isEnabled()) {
            // 기기의 블루투스 상태가 On인 경우
            Log.d(TAG, "Bluetooth Enable");
            scanDevice();

        }else {
            // 기기의 블루투스 상태가 Off인 경우
            Log.d(TAG, "Bluetooth Enable Request");
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); // 블루투스권한요청 화면
            mActivity.startActivityForResult(i, REQUEST_ENABLE_BT);
        }
    }

    // 장치 탐색
    public void scanDevice() {
        Log.d(TAG, "Scan Device");

        Intent intent = new Intent(mActivity, DeviceListActivity.class);
        mActivity.startActivityForResult(intent, REQUEST_CONNECT_DEVICE);

    }

    // 장치의 정보를 얻어옴
    public void getDeviceInfo(Intent data) {
        // 장치의 MAC Address를 얻어온다.
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        // 블루투스 장치 객체를 얻어온다.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        Log.d(TAG, "Get Device Info \n" + "address : " + address);

        connect(device);
    }

    // Bluetooth 상태 설정
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
    }

    // Bluetooth 상태 얻어오기
    public synchronized int getState() {
        return mState;
    }


    // 모든 쓰레드를 없앤다.
    public synchronized void start() {
        Log.d(TAG, "start");

        // 연결을 시도하는 쓰레드를 초기화시킨다.
        if (mConnectThread == null) {

        } else {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // 연결이 완료되어있는 쓰레드를 초기화시킨다.
        if (mConnectedThread == null) {

        } else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    // 장치와 연결
    public synchronized void connect(BluetoothDevice device) { // synchronized 는 동기화
        Log.d(TAG, "connect to: " + device);

        // 연결을 시도하는 쓰레드를 초기화시킨다.
        if (mState == STATE_CONNECTING) {
            if (mConnectThread == null) {

            } else {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // 연결이 완료되어있는 쓰레드를 초기화시킨다.
        if (mConnectedThread == null) {

        } else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // 연결하기 위한 쓰레드를 생성한다. (장치 정보를 넘겨준다.)
        mConnectThread = new ConnectThread(device);

        // 쓰레드 동작
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    // 장치와 데이터 통신 시도도
   public synchronized void connected(BluetoothSocket socket,
                                       BluetoothDevice device) {
        Log.d(TAG, "connected");

        // 연결을 시도하는 쓰레드를 초기화시킨다.
        if (mConnectThread == null) {

        } else {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // 연결이 완료되어있는 쓰레드를 초기화시킨다.
        if (mConnectedThread == null) {

        } else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // 데이터 송수신을 위한 쓰레드를 생성
        mConnectedThread = new ConnectedThread(socket);
       // 쓰레드 동작
        mConnectedThread.start();

        setState(STATE_CONNECTED);
        Log.d(TAG, "단말이 연결되었습니다.");
       // UI부분에선 Toast메세지를 실행할수 없기 때문에 핸들러 사용
        mHandler.sendEmptyMessage(1);
    }

    // 모든 쓰레드 reset
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }

    // 값을 쓰는 부분(보내는 부분)
    public void write(byte[] out) { // Create temporary object
        Log.d(TAG, "btService write");
        ConnectedThread r; // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED)
                return;
            r = mConnectedThread;
        }
        r.write(out);
    }

    // 연결 실패했을때
    private void connectionFailed() {
        setState(STATE_LISTEN);
        mHandler.sendEmptyMessage(2);
        //stop();
    }

    // 연결을 잃었을 때
    private void connectionLost() {
        mHandler.sendEmptyMessage(3);
        setState(STATE_LISTEN);
        //stop();
    }



    // 장치와 장치간의 연결을 위한 쓰레드
    private class ConnectThread extends Thread {
        // 소켓
        private final BluetoothSocket mmSocket;
        // 장치
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // 장치 정보를 얻어서 BluetoothSocket 생성
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        // 실행 내용
        public void run() {
            Log.d(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // 연결을 시도하기 전에는 항상 기기 검색을 중지한다.
            // 기기 검색이 계속되면 연결속도가 느려지기 때문이다.
            btAdapter.cancelDiscovery();

            // BluetoothSocket 연결 시도
            try {
                // BluetoothSocket 연결 시도에 대한 return 값은 succes 또는 exception이다. 성공시 try를 실행하지만 실패시 catch를 실행한다.
                mmSocket.connect();
                Log.d(TAG, "Connect Success");

            } catch (IOException e) {
                connectionFailed(); // 연결 실패시 불러오는 메소드
                Log.d(TAG, "Connect Fail");

                // socket을 닫는다.
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG,
                            "unable to close() socket during connection failure",
                            e2);
                }
                // 연결에 실패 했기 때문에 모든 쓰레드를 제거한다.
                BluetoothService.this.start();
                return;
            }

            // ConnectThread 클래스를 reset한다.
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            // 데이터 통신을 위한 메소드 실행
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }


    // 데이터 송수신을 위한 쓰레드
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // BluetoothSocket의 inputstream 과 outputstream을 얻는다.
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.d(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // 연결하고 있는동안 InputStream의 상태를 유지한다. 왜냐하면 데이터가 들어오기까지 Block하는 함수 이기 때문에
            while (true) {
                try {
                    Log.d(TAG,"READ 대기");
                    // InputStream으로부터 값을 받는 읽는 부분(값을 받는다)
                    bytes = mmInStream.read(buffer);

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        // 데이터 전송 함수
        public void write(byte[] buffer) {
            Log.d(TAG, "connected thread write");
            try {
                // 값을 쓰는 부분(값을 보낸다)
                Log.d(TAG,"데이터 전송 완료");
                mmOutStream.write(buffer);

            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        // 데이터 전송을 위한 소켓 close
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
