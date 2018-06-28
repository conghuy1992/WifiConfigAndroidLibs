package jp.co.alpine.wifisetingsample;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import jp.co.alpine.wificonfiglibs.MyWifiManager;
import jp.co.alpine.wificonfiglibs.OnWifiStateChangeListener;

public abstract class WifiSettingBase extends BaseActivity {
	protected Button mBtnOK;
	protected MyWifiManager mWifiManager;
	private WifiStateChangeReceiver mWifiReceiver;

	private boolean mIsRegisteredReceiver;

	private Runnable mTimeoutCallback;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setup();
	}

	private void setup() {
		mWifiManager = new MyWifiManager(this);

		mTimeoutCallback = new Runnable() {
			@Override
			public void run() {
				synchronized (WifiSettingBase.this) {
					Log.d(TAG, "WiFi connection timeout ...");

					if (mIsRegisteredReceiver) {
						unregisterReceiver(mWifiReceiver);
						mIsRegisteredReceiver = false;
					}

					try {
						hiddenProgressDialog();
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
					MyWifiManager.WifiData wifiInfo = mWifiManager.getWiFiInfo();
					Toast.makeText(getApplicationContext(), "Can't connect, WiFi connection timeout ...!", Toast.LENGTH_LONG).show();
			}
			}
		};

		// Instantiates and register receiver for wifi state
		mWifiReceiver = new WifiStateChangeReceiver();
		mWifiReceiver.setOnWifiStateChangeListener(new OnWifiStateChangeListener() {
			@Override
			public void onWifiStateChanged(String ssid, NetworkInfo.State state, int networkId) {
				if (state != NetworkInfo.State.CONNECTED)
					return;;

				MyWifiManager.WifiData wifiInfo = mWifiManager.getWiFiInfo();
				if (wifiInfo == null || wifiInfo.ssid == null)
					return;
				if (ssid == null ||
						!ssid.replaceAll("\"", "").equals(wifiInfo.ssid))
					return;

				// stop timer
				synchronized (WifiSettingBase.this) {
					mBtnOK.removeCallbacks(mTimeoutCallback);
				}

				Log.d(TAG, "Connected to (WiFi) network: " + wifiInfo.ssid);

				Toast.makeText(getApplicationContext(), "\"Connected to (WiFi) network: "+ wifiInfo.ssid, Toast.LENGTH_LONG).show();
				if (mIsRegisteredReceiver) {
					unregisterReceiver(mWifiReceiver);
					mIsRegisteredReceiver = false;
				}

				hiddenProgressDialog();

				// disconnect in case connect wifi with static IP
				if (wifiInfo.isStatic)
					mWifiManager.disconnectWifi(networkId);
			}
		});
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION);
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mWifiReceiver, intentFilter);
		mIsRegisteredReceiver = true;
	}

	protected void connectWiFi(MyWifiManager.WifiData wifiInfo) {
		if (mBtnOK == null)
			return;

		if (wifiInfo == null
				|| wifiInfo.ssid == null
				|| wifiInfo.security == null)
			return;

		if (!mIsRegisteredReceiver) {
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION);
			intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			registerReceiver(mWifiReceiver, intentFilter);
			mIsRegisteredReceiver = true;
		}

		showProgressDialog(this,"connecting");

		mWifiManager.setWiFiInfo(wifiInfo);
		mWifiManager.connectToWifi();
		mBtnOK.postDelayed(mTimeoutCallback, 10000);
	}

	@Override
	protected void onPause() {
		super.onPause();

		synchronized (this) {
			if (mBtnOK != null)
				mBtnOK.removeCallbacks(mTimeoutCallback);
		}

		if (mIsRegisteredReceiver) {
			unregisterReceiver(mWifiReceiver);
			mIsRegisteredReceiver = false;
		}

		hiddenProgressDialog();
	}
}
