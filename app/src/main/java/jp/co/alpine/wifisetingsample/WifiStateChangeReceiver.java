package jp.co.alpine.wifisetingsample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.util.Log;

import jp.co.alpine.wificonfiglibs.MyWifiManager;
import jp.co.alpine.wificonfiglibs.OnWifiStateChangeListener;


public class WifiStateChangeReceiver extends BroadcastReceiver {
        private final String TAG = WifiStateChangeReceiver.class.getSimpleName();

        private OnWifiStateChangeListener mWifiStateListener;
        private static boolean firstConnect = true;

        @Override
        public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null)
            return;

        if (android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            NetworkInfo networkInfo = intent.getParcelableExtra(android.net.wifi.WifiManager.EXTRA_NETWORK_INFO);
            Log.d(TAG, "WiFi state is changed, network info: " + networkInfo);

            if (networkInfo == null || !networkInfo.isConnected() || !firstConnect)
                return;

            // Wifi is connected
            firstConnect = false;
            android.net.wifi.WifiManager wifiManager = (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();

            Log.d(TAG, "Network extra info: " + networkInfo.getExtraInfo());
            Log.d(TAG, "WiFi SSID: " + ssid + ", networkID: " + wifiInfo.getNetworkId());

            if (MyWifiManager.UNKNOWN_SSID.equals(ssid) ||
                    MyWifiManager.UNKNOWN_SSID.equals(networkInfo.getExtraInfo())) {
                Log.d(TAG, "Ignored WiFi state changed for unknown SSID ...");
                return;
            }

            if (mWifiStateListener == null)
                return;

            mWifiStateListener.onWifiStateChanged(ssid,
                    networkInfo.getState(),
                    wifiInfo.getNetworkId());
            return;
        }

        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            firstConnect = true;
            NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI && !networkInfo.isConnected()) {
                // Wifi is disconnected
                Log.d(TAG, "Wifi disconnected: " + networkInfo);
            }
            return;
        }
    }

    public void setOnWifiStateChangeListener(OnWifiStateChangeListener listener) {
        mWifiStateListener = listener;
    }

}
