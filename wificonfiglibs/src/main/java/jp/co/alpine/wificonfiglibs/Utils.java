package jp.co.alpine.wificonfiglibs;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

public class Utils {
    public static String getWifiName(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = null;
        if (wifiManager != null)
            wifiInfo = wifiManager.getConnectionInfo();
        String ssid = null;
        if (wifiInfo != null)
            ssid = wifiInfo.getSSID();
        if (ssid == null) ssid = "";
        return ssid;
    }

    public static String getMyIp(Context context) {
        WifiManager wManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return Formatter.formatIpAddress(wManager.getConnectionInfo().getIpAddress());
    }
}
