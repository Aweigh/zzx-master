package com.hotbitmapgg.bilibili.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;

import java.io.File;

/**
 * Created by hcc on 16/8/4 21:18
 * 100332338@qq.com
 * <p/>
 * 通用工具类
 */
public class CommonUtil {

    /**
     * 检查是否有网络
     */
    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return info != null && info.isAvailable();
    }


    /**
     * 检查是否是WIFI
     */
    public static boolean isWifi(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        }
        return false;
    }


    /**
     * 检查是否是移动网络
     */
    public static boolean isMobile(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                return true;
            }
        }
        return false;
    }

    private static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }


    /**
     * 检查SD卡是否存在
     */
    private static boolean checkSdCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }


    /**
     * 获取手机SD卡总空间
     */
    private static long getSDcardTotalSize() {
        if (checkSdCard()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs mStatFs = new StatFs(path.getPath());
            long blockSizeLong = mStatFs.getBlockSizeLong();
            long blockCountLong = mStatFs.getBlockCountLong();
            return blockSizeLong * blockCountLong;
        } else {
            return 0;
        }
    }


    /**
     * 获取SDka可用空间
     */
    private static long getSDcardAvailableSize() {
        if (checkSdCard()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs mStatFs = new StatFs(path.getPath());
            long blockSizeLong = mStatFs.getBlockSizeLong();
            long availableBlocksLong = mStatFs.getAvailableBlocksLong();
            return blockSizeLong * availableBlocksLong;
        } else {
            return 0;
        }
    }


    /**
     * 获取手机内部存储总空间
     */
    public static long getPhoneTotalSize() {
        if (!checkSdCard()) {
            File path = Environment.getDataDirectory();
            StatFs mStatFs = new StatFs(path.getPath());
            long blockSizeLong = mStatFs.getBlockSizeLong();
            long blockCountLong = mStatFs.getBlockCountLong();
            return blockSizeLong * blockCountLong;
        } else {
            return getSDcardTotalSize();
        }
    }


    /**
     * 获取手机内存存储可用空间
     */
    public static long getPhoneAvailableSize() {
        if (!checkSdCard()) {
            File path = Environment.getDataDirectory();
            StatFs mStatFs = new StatFs(path.getPath());
            long blockSizeLong = mStatFs.getBlockSizeLong();
            long availableBlocksLong = mStatFs.getAvailableBlocksLong();
            return blockSizeLong * availableBlocksLong;
        } else
            return getSDcardAvailableSize();
    }

//    /**
//     * 获取手机IMEI号
//     * IMEI(International Mobile Equipment Identity,国际移动身份识别码)：是由15位数字组成的"电子串号"，
//     * 其组成结构为TAC（6位数字）+FAC（两位数字）+SNR（6位数字）+SP （1位数字）。它与每台手机一一对应，而且该码是全世界唯一的。
//     * 每一只手机在组装完成后都将被赋予一个全球唯一的一组号码，这个号码从生产到交付使用都将被制造生产的厂商所记录。
//     * IMEI码贴在手机背面的标志上，并且读写于手机内存中。它也是该手机在厂家的”档案”和”身份证号”。
//     */
//    public static String getIMEI(Context context) {
//        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
//        String imei = telephonyManager.getDeviceId();
//        return imei;
//    }
//    /**
//     * 获取手机IMSI号
//     * IMSI(International Mobile Subscriber Identification Number,国际移动用户识别码)：是区别移动用户的标志，储存在SIM卡中，可用于区别移动用户的有效信息。
//     * 其总长度不超过15位，同样使用0～9的数字。其中MCC是移动用户所属国家代号，占3位数字，中国的MCC规定为460；MNC是移动网号码，最多由两位数字组成，
//     * 用于识别移动用户所归属的移动通信网；MSIN是移动用户识别码，用以识别某一移动通信网中的移动用户
//     */
//    public static String getIMSI(Context context){
//        TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        String imsi = mTelephonyMgr.getSubscriberId();
//        return imsi ;
//    }
}
