package ocr;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;
import java.util.UUID;

/**
 * tools
 *
 * @author : Winter
 * adapted by Xianghui on 2019/5/20
 * retrieved from: https://github.com/fwh007/Lib-DeviceId.git
 */


public class DeviceIDUtil {

    /**
     * get Device Id
     *
     * @param context
     * @return
     */
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    public static String getDeviceId(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String deviceId = telephonyManager.getDeviceId();
            return deviceId;
        }
        return "";
    }

    /**
     * get Wifi Mac address, applied for systems under Android 6.0
     *
     * @param context
     * @return
     */
    public static String getWifiMacAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String wifiMacAddress = wifiInfo.getMacAddress();
        return wifiMacAddress;
    }

    /**
     *
     * get Wifi Mac address, applied for system version above Android 6.0
     * @return
     * @throws SocketException
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static String getWifiMacAddress() throws SocketException {
        StringBuilder wifiMacAddressBuild = new StringBuilder();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface anInterface = interfaces.nextElement();
            if (!"wlan0".equals(anInterface.getName())) {
                //wlan0 is the correct Wifi Mac address
                continue;
            }
            byte[] address = anInterface.getHardwareAddress();
            if (address == null || address.length == 0) {
                continue;
            }

            StringBuilder builder = new StringBuilder();
            for (byte b : address) {
                builder.append(String.format("%02X:", b));
            }
            if (builder.length() > 0) {
                builder.deleteCharAt(builder.length() - 1);
            }
            wifiMacAddressBuild.append(anInterface.getName()).append(" -> ").append(builder).append("\n");
        }
        if (wifiMacAddressBuild.length() > 0) {
            wifiMacAddressBuild.deleteCharAt(wifiMacAddressBuild.length() - 1);
        }
        return wifiMacAddressBuild.toString();
    }


    /**
     * get Wifi Mac address by analyzing IP address，applied to Android 6.0+
     *
     * @return
     * @throws SocketException
     */
    public static String getWifiMacAddressByIp() {
        String strMacAddr = null;
        try {
            //get IP address
            InetAddress ip = getLocalInetAddress();
            byte[] b = NetworkInterface.getByInetAddress(ip).getHardwareAddress();
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < b.length; i++) {
                if (i != 0) {
                    buffer.append(':');
                }
                String str = Integer.toHexString(b[i] & 0xFF);
                buffer.append(str.length() == 1 ? 0 + str : str);
            }
            strMacAddr = buffer.toString().toUpperCase();
        } catch (Exception e) {

        }

        return strMacAddr;
    }

    private static InetAddress getLocalInetAddress() {
        InetAddress ip = null;
        try {
            //enumeration
            Enumeration<NetworkInterface> en_netInterface = NetworkInterface.getNetworkInterfaces();
            while (en_netInterface.hasMoreElements()) {//whether there is another element
                NetworkInterface ni = en_netInterface.nextElement();//get next element
                Enumeration<InetAddress> en_ip = ni.getInetAddresses();//get an enumeration of address
                while (en_ip.hasMoreElements()) {
                    ip = en_ip.nextElement();
                    if (!ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1)
                        break;
                    else
                        ip = null;
                }

                if (ip != null) {
                    break;
                }
            }
        } catch (SocketException e) {

            e.printStackTrace();
        }
        return ip;
    }

    /**
     * get Android Id
     *
     * set android Id as the unique device id
     * @param context
     * @return
     */
    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * for database
     * get brand/product/device of the phone as the device name
     * @return
     */
    public static String getDeviceName(){
        StringBuffer buildSB = new StringBuffer();
        buildSB.append(Build.BRAND).append("-");
        buildSB.append(Build.PRODUCT).append("-");
        buildSB.append(Build.DEVICE).append("-");
        return buildSB.toString();
    }

    /**
     * get information of build
     *
     * @return
     */
    public static String getBuildInfo() {
        //these values will not be changed by system update
        StringBuffer buildSB = new StringBuffer();
        buildSB.append(Build.BRAND).append("/");
        buildSB.append(Build.PRODUCT).append("/");
        buildSB.append(Build.DEVICE).append("/");
        buildSB.append(Build.ID).append("/");
        buildSB.append(Build.VERSION.INCREMENTAL);
        return buildSB.toString();
//        return Build.FINGERPRINT;
    }

    /**
     * get unique device ID
     *
     * @param context
     * @return
     */
    public static String getDeviceUUID(Context context) {
        String uuid = loadDeviceUUID(context);
        if (TextUtils.isEmpty(uuid)) {
            uuid = buildDeviceUUID(context);
            saveDeviceUUID(context, uuid);
        }
        return uuid;
    }

    private static String buildDeviceUUID(Context context) {
        String androidId = getAndroidId(context);
        if (!"9774d56d682e549c".equals(androidId)) {
            Random random = new Random();
            androidId = Integer.toHexString(random.nextInt())
                    + Integer.toHexString(random.nextInt())
                    + Integer.toHexString(random.nextInt());
        }
        return new UUID(androidId.hashCode(), getBuildInfo().hashCode()).toString();
    }

    private static void saveDeviceUUID(Context context, String uuid) {
        context.getSharedPreferences("device_uuid", Context.MODE_PRIVATE)
                .edit()
                .putString("uuid", uuid)
                .apply();
    }

    @Nullable
    private static String loadDeviceUUID(Context context) {
        return context.getSharedPreferences("device_uuid", Context.MODE_PRIVATE)
                .getString("uuid", null);
    }
}
