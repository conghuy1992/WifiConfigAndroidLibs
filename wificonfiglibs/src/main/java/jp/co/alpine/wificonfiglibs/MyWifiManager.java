package jp.co.alpine.wificonfiglibs;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;


public class MyWifiManager {
    private final String TAG = MyWifiManager.class.getSimpleName();

    public static final String UNKNOWN_SSID = "<unknown ssid>";

    public static final String AUTHENTICATION_WPA_PSK = "WPA/WPA2 PSK";
    public static final String AUTHENTICATION_WPA_EAP = "WPA/WPA2 EAP";
    public static final String AUTHENTICATION_WEP = "WEP";
    public static final String AUTHENTICATION_OPEN = "Open";
    public static final String AUTHENTICATION_NONE = "None";

    private android.net.wifi.WifiManager mWifiManager;

	/*---------------------------------------------------*/

    /**
     * WiFi data
     */
    public static class WifiData implements Parcelable {
        public String ssid;
        public String security;
        public String password;
        public boolean isStatic;
        public String ipaddress;
        public String gateway;
        public String dns;

        private static final String NET_KEY_SSID = "00";
        private static final String NET_KEY_SECURITY = "01";
        private static final String NET_KEY_PWD = "02";
        private static final String NET_KEY_IS_STATIC = "03";
        private static final String NET_KEY_IP_ADDRESS = "04";
        private static final String NET_KEY_GATE_WAY = "05";
        private static final String NET_KEY_DNS = "06";

        public WifiData(Parcel in) {
            if (in == null) {
                return;
            }
            ssid = in.readString();
            security = in.readString();
            password = in.readString();
            ipaddress = in.readString();
            gateway = in.readString();
            dns = in.readString();
            isStatic = (in.readInt() == 1);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(ssid);
            dest.writeString(security);
            dest.writeString(password);
            dest.writeString(ipaddress);
            dest.writeString(gateway);
            dest.writeString(dns);
            dest.writeInt(isStatic ? 1 : 0);
        }

        public static final Creator CREATOR = new Creator() {
            public MyWifiManager.WifiData createFromParcel(Parcel in) {
                return new MyWifiManager.WifiData(in);
            }

            public MyWifiManager.WifiData[] newArray(int size) {
                return new MyWifiManager.WifiData[size];
            }
        };
    }
	/*---------------------------------------------------*/

    private WifiData mWifiInfo;

    public MyWifiManager(Context context) {
        mWifiManager = (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!mWifiManager.isWifiEnabled())
            mWifiManager.setWifiEnabled(true);
    }

    public WifiInfo getWifiInfo() {
        if (mWifiManager != null)
            return mWifiManager.getConnectionInfo();

        return null;
    }

    public void setWiFiInfo(WifiData info) {
        mWifiInfo = info;
    }

    public WifiData getWiFiInfo() {
        return mWifiInfo;
    }
    /**
     * Get configuration for specific SSID
     *
     * @param ssid Requested SSID
     * @return null if configuration not yet existed
     */
    private WifiConfiguration getConfiguration(String ssid) {
        WifiConfiguration wConf = null;

        if (mWifiManager == null)
            return  wConf;
        if (ssid == null)
            return  wConf;

        List<WifiConfiguration> confs = mWifiManager.getConfiguredNetworks();
        if (confs == null || confs.isEmpty())
            return  wConf;

        String ssidValue = "\"" + ssid + "\"";
        for (WifiConfiguration conf : confs) {
            if (ssidValue.equals(conf.SSID)) {
                wConf = conf;
            }
        }

        return wConf;
    }

    public int connectToWifi() {
        int netId = -1;

        if (mWifiInfo == null) {
            Log.e(TAG, "WiFi data is not set!");
            return netId;
        }

        if (!mWifiManager.isWifiEnabled())
            mWifiManager.setWifiEnabled(true);

        // first, get existed configuration
        WifiConfiguration conf = getConfiguration(mWifiInfo.ssid);
        if (conf == null) {
            Log.d(TAG, "WiFi configuration not exist, create new ...");

            conf = new WifiConfiguration(); // create new if not yet existed
            conf.SSID = "\"" + mWifiInfo.ssid + "\"";
        }

        if (mWifiInfo.security != null) {
            switch (mWifiInfo.security) {
                case AUTHENTICATION_WEP: {
                    conf.wepKeys[0] = "\"" + mWifiInfo.password + "\"";
                    conf.wepTxKeyIndex = 0;
                    conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                    break;
                }

                case AUTHENTICATION_WPA_PSK: {
                    conf.preSharedKey = "\"" + mWifiInfo.password + "\"";
                    break;
                }

                case AUTHENTICATION_OPEN:
                case AUTHENTICATION_NONE: {
                    conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    break;
                }

                default:
                    break;
            }
        }

        if (mWifiInfo.isStatic) {
            try {
                setIpAssignment("STATIC", conf); //or "DHCP" for dynamic setting
                setIpAddress(InetAddress.getByName(mWifiInfo.ipaddress), 24, conf);
                setGateway(InetAddress.getByName(mWifiInfo.gateway), conf);
                setDNS(InetAddress.getByName(mWifiInfo.dns), conf);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        // add new or update?
        if (conf.networkId < 0)
            netId = mWifiManager.addNetwork(conf);
        else
            netId = mWifiManager.updateNetwork(conf);

        mWifiManager.saveConfiguration();
        Log.d("Wifi", "Wifi config saved - netId: " + netId);

        if (netId != -1) {
            Log.d(TAG, "Connecting to wifi network");
            mWifiManager.disconnect();
            mWifiManager.enableNetwork(netId, true);
            mWifiManager.reconnect();
        }

        return netId;
    }

    /**
     * Return security type as String for a SSID, return null if SSID can not be found
     */
    public String scanSsidSecurity(String ssid) {
        String securityMode = null;

        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
        List<ScanResult> wifiList = mWifiManager.getScanResults();

        for (ScanResult sr : wifiList) {
            Log.d(TAG, sr.SSID);

            if (sr.SSID.compareTo(ssid) == 0)
                securityMode = getSecurity(sr);
            //TODO: stop processing here???
        }

        return securityMode;
    }

    /**
     * Return security type of wifi in ScanResult
     */
    private String getSecurity(ScanResult result) {
        String sType = AUTHENTICATION_OPEN;

        if (result == null || result.capabilities == null)
            return sType;

        if (result.capabilities.contains("WEP")) {
            sType = AUTHENTICATION_WEP;
        }

        else if (result.capabilities.contains("PSK")) {
            sType = AUTHENTICATION_WPA_PSK;
        }

        if (result.capabilities.contains("EAP")) {
            sType = AUTHENTICATION_WPA_EAP;
        }

        return sType;
    }

    public void disconnectWifi(int networkId) {
        mWifiManager.disconnect();
        mWifiManager.disableNetwork(networkId);
    }

    // Methods support assign static ip
    public static void setIpAssignment(String assign, WifiConfiguration wifiConf) throws Exception {
        setEnumField(wifiConf, assign, "ipAssignment");
    }

    public static void setIpAddress(InetAddress addr, int prefixLength, WifiConfiguration wifiConf)
            throws Exception {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null) return;
        Class laClass = Class.forName("android.net.LinkAddress");
        Constructor laConstructor = laClass.getConstructor(new Class[] { InetAddress.class, int.class });
        Object linkAddress = laConstructor.newInstance(addr, prefixLength);

        ArrayList mLinkAddresses = (ArrayList) getDeclaredField(linkProperties, "mLinkAddresses");
        mLinkAddresses.clear();
        mLinkAddresses.add(linkAddress);
    }

    public static void setGateway(InetAddress gateway, WifiConfiguration wifiConf)
            throws Exception {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null) return;
        Class routeInfoClass = Class.forName("android.net.RouteInfo");
        Constructor routeInfoConstructor = routeInfoClass.getConstructor(new Class[] { InetAddress.class });
        Object routeInfo = routeInfoConstructor.newInstance(gateway);

        ArrayList mRoutes = (ArrayList) getDeclaredField(linkProperties, "mRoutes");
        mRoutes.clear();
        mRoutes.add(routeInfo);
    }

    public static void setDNS(InetAddress dns, WifiConfiguration wifiConf) throws Exception {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null) return;

        ArrayList<InetAddress> mDnses = (ArrayList<InetAddress>) getDeclaredField(linkProperties, "mDnses");
        mDnses.clear(); //or add a new dns address , here I just want to replace DNS1
        mDnses.add(dns);
    }

    public static Object getField(Object obj, String name) throws Exception {
        Field f = obj.getClass().getField(name);
        Object out = f.get(obj);
        return out;
    }

    public static Object getDeclaredField(Object obj, String name) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object out = f.get(obj);
        return out;
    }

    public static void setEnumField(Object obj, String value, String name) throws Exception {
        Field f = obj.getClass().getField(name);
        f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
    }
}
