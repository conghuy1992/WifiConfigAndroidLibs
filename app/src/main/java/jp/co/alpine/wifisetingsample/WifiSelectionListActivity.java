package jp.co.alpine.wifisetingsample;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.*;
import android.os.Build;
import android.view.*;
import android.widget.*;

import java.util.*;

import jp.co.alpine.wificonfiglibs.MyWifiManager;


public class WifiSelectionListActivity extends BaseActivity {
    private final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 0;
    private static WifiSelectionListActivity inst;
    private android.net.wifi.WifiManager wifi;
    private ListView lv;
    private List<ScanResult> results;

    private List<String> arraylist = new ArrayList<>();
    private WifiListAdapter adapter;

    private EditText mEditIPAddress;
    private EditText mEditGateway;
    private EditText mEditDNS1;
    private LinearLayout linlaHeaderProgress;
    private CheckBox mDetailCheckbox;
    private BroadcastReceiver mWifiBroadcastReceiver;

    private MyWifiManager.WifiData mWifiInfo;
    private LinearLayout mDetailLinearLayout;

    @Override
    protected void setupView() {
        setContentView(R.layout.activity_wifi_selection_list);

        //set up list view
        lv = (ListView) findViewById(R.id.wifi_listview);
        View footerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.wifi_list_item, null, false);
        TextView tvFooter = (TextView) footerView.findViewById(R.id.wifi_name);
        tvFooter.setText("Other");
        tvFooter.setTextColor(Color.WHITE);
        footerView.setBackgroundColor(Color.BLUE);
        lv.addFooterView(footerView);

        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);

        mEditIPAddress = (EditText) findViewById(R.id.edit_ipAddress);
        mEditGateway = (EditText) findViewById(R.id.edit_gateway);
        mEditDNS1 = (EditText) findViewById(R.id.edit_dns1);

        wifi = (android.net.wifi.WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled())
            wifi.setWifiEnabled(true);

        this.adapter = new WifiListAdapter(WifiSelectionListActivity.this, arraylist);
        lv.setAdapter(this.adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mWifiInfo = new MyWifiManager.WifiData(null);
                mWifiInfo.isStatic = mDetailCheckbox.isChecked();
                if (mDetailCheckbox.isChecked()) {
                    mWifiInfo.ipaddress = mEditIPAddress.getText().toString();
                    mWifiInfo.gateway = mEditGateway.getText().toString();
                    mWifiInfo.dns = mEditDNS1.getText().toString();
                }

                mWifiInfo.ssid = null;
                Class<?> nextActivity = WifiSettingSSIDManualActivity.class;
                if (position < arraylist.size()) {
                    mWifiInfo.ssid = arraylist.get(position);
                    nextActivity = WifiSettingSSIDActivity.class;
                }

                Intent intent = new Intent(getApplicationContext(), nextActivity);
                intent.putExtra("WIFI_INFO", mWifiInfo);
                startActivity(intent);
            }
        });

        mDetailCheckbox = (CheckBox) findViewById(R.id.detail_checkbox);
        mDetailCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mDetailLinearLayout.setVisibility(View.VISIBLE);
                } else {
                    mDetailLinearLayout.setVisibility(View.GONE);
                }
            }
        });
        mWifiBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                linlaHeaderProgress.setVisibility(View.GONE);
                results = wifi.getScanResults();

                arraylist.clear();
                for (int i = 0; i < results.size(); i++) {
                    arraylist.add(results.get(i).SSID);
                }

                lv.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();
            }
        };
        mDetailLinearLayout = (LinearLayout) findViewById(R.id.issp_detail_view);
        mDetailLinearLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mWifiBroadcastReceiver, new IntentFilter(android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        scanWifi();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);

        } else {
            scanWifi();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Do something with granted permission
            scanWifi();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mWifiBroadcastReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        inst = null;
    }

    public static WifiSelectionListActivity getInst() {
        return inst;
    }

    private void scanWifi() {
        arraylist.clear();
        wifi.startScan();
        linlaHeaderProgress.setVisibility(View.VISIBLE);
        lv.setVisibility(View.GONE);
    }

    public void onRefresh(View view) {
        scanWifi();
    }
}
