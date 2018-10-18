package org.sid.shootin.communication.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Util {
    public static byte[] IntToByteArr(int w) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (w >> (3 * 8));
        bytes[1] = (byte) ((w >> (2 * 8)) & 0x000000ff);
        bytes[2] = (byte) ((w >> 8) & 0x000000ff);
        bytes[3] = (byte) w;
        return bytes;
    }


    public static int ByteArrT2Int(byte[] bys) {
        int sum = 0;
        int end = 4;
        byte len = 4;
        for (int i = 0; i < end; i++) {
            int n = ((int) bys[i]) & 0xff;
            n <<= (--len) * 8;
            sum += n;
        }
        return sum;
    }

    public static boolean openWifiAp(Context context, String apName) {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (manager != null && manager.isWifiEnabled())
            manager.setWifiEnabled(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setWifiApEnabledForAndroidO(context, true);
            return true;
        }

        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = apName;
        wifiConfiguration.preSharedKey = null;
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        try {
            Method method = manager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            return (boolean) method.invoke(manager, wifiConfiguration, true);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.e(Util.class.getName() + ":", "open wifi err " + e.getCause().toString());
            return false;
        }
    }

    public static void setWifiApEnabledForAndroidO(Context context, boolean isEnable) {
        ConnectivityManager connManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        Field iConnMgrField = null;
        try {
            iConnMgrField = connManager.getClass().getDeclaredField("mService");
            iConnMgrField.setAccessible(true);
            Object iConnMgr = iConnMgrField.get(connManager);
            Class<?> iConnMgrClass = Class.forName(iConnMgr.getClass().getName());

            if (isEnable) {
                Method startTethering = iConnMgrClass.getMethod("startTethering", int.class, ResultReceiver.class, boolean.class);
                startTethering.invoke(iConnMgr, 0, null, true);
            } else {
                Method startTethering = iConnMgrClass.getMethod("stopTethering", int.class);
                startTethering.invoke(iConnMgr, 0);
            }

        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static boolean openWifi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) return false;
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        return true;
    }

    public static boolean linkWifi(Context context, String apName, String pwd) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null)
            return false;
        WifiConfiguration configuration = createWifiInfo(apName, pwd, 1);
        wifiManager.addNetwork(configuration);
        wifiManager.enableNetwork(configuration.networkId, true);
        return true;
    }

    public static boolean isWifiOpen(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null)
            return false;
        return wifiManager.isWifiEnabled();
    }

    public static WifiConfiguration createWifiInfo(String SSID, String password, int type) {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.allowedAuthAlgorithms.clear();
        configuration.allowedGroupCiphers.clear();
        configuration.allowedKeyManagement.clear();
        configuration.allowedPairwiseCiphers.clear();
        configuration.allowedProtocols.clear();
        configuration.SSID = "\"" + SSID + "\"";

        switch (type) {
            case 1://不加密
                configuration.wepKeys[0] = "";
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                configuration.wepTxKeyIndex = 0;
                configuration.priority = 20000;
                break;
            case 2://wep加密
                configuration.hiddenSSID = true;
                configuration.wepKeys[0] = "\"" + password + "\"";
                configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

                break;
            case 3: //wpa加密

                configuration.preSharedKey = "\"" + password + "\"";
                configuration.hiddenSSID = true;
                configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                configuration.status = WifiConfiguration.Status.ENABLED;
                break;
        }
        return configuration;
    }

    public static String intToIp(int paramInt) {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
                + (0xFF & paramInt >> 24);
    }
}
