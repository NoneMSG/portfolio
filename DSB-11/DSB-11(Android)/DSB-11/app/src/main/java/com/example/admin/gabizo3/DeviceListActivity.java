package com.example.admin.gabizo3;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

// 화면이 Dialog처럼 보이게 하기 위해 Menifest에 theme를 @android:style/Theme.DeviceDefault.Dialog로 사용하였다.
public class DeviceListActivity extends Activity {

    private static final String TAG = "블루투스";

    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";


    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 윈도우의 특성을 변경
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);// 타이틀바에 원형 프로그레스바 생성
        setContentView(R.layout.device_list);

        // 뒤로 가기를 할 경우 RESULT_CANCELED를 반환
        setResult(Activity.RESULT_CANCELED);

        // 검색 버튼
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE); // View를 숨긴다 (버튼을 숨긴다)
            }
        });

        // 이미 페어링된 단말과 새로운 단말을 위한 배열을 초기화 시킨다.
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        // paired_devices 리스트 뷰에 페어링된 장치의 배열과 setOnItemClickListener를 설정
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // new_devices 리스트 뷰에 새로운 장치의 배열과 setOnItemClickListener를 설정
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // 새로운 블루투스 장치를 찾았을 경우의 Broadcast Receiver 등록
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // 블루투스 검색이 끝났을 경우의 Broadcast Receiver 등록
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Default Bluetooth adapter를 얻는다
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // 현재 페어링된 장치를 얻는다.
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // 페어링된 장치가 있을 경우 페어링된 배열에 넣는다.
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {  // 확장for문 , iterator를 축약 , jdk 5.0이상에서 추가됨
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString(); // 페어링된 장치가 없을 경우
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 더이상 검색하지 않음
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Broadcast Receiver 등록 해제
        this.unregisterReceiver(mReceiver);
    }

    // 장치를 검색
    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        // 제목을 설정
        setProgressBarIndeterminateVisibility(true); // 프로그레스바를 보여준다
        setTitle(R.string.scanning);

        // 서브 타이틀을 보여준다.(새로운 장치)
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // 이미 검색중이라면 검색 종료
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // BluetoothAdapter에게 검색을 요청한다.
        mBtAdapter.startDiscovery();
    }

    // 리스트뷰에 있는 장치를 선택 했을시의 행동을 설정
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // 검색을 중지한다. 왜냐하면 연결을 해야하기 때문이다.
            mBtAdapter.cancelDiscovery();

            // MAC Address를 얻는다.
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Intent에 MAC Address의 정보를 넣는다
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // Activity가 finish가 됬을 경우의 결과값을 설정
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    // 장치를 검색 하였거나 장치 검색이 끝났을 경우에 실행하는 BroadcastReceiver
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // 장치를 찾았을 경우 실행
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Intent로부터 장치의 정보를 가져온다.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 이미 페어링된 장비일 경우 실행하지 않는다.
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            // 장치 검색이 끝났을 경우 실행
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);// 프로그레스바를 감춘다.
                setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

}
