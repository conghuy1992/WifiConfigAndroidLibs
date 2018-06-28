package jp.co.alpine.wificonfiglibs;

import android.net.NetworkInfo;

/**
 * Created by macbook on 1/23/18.
 */

public interface OnWifiStateChangeListener {
    void onWifiStateChanged(String ssid, NetworkInfo.State state, int networkId);
}
