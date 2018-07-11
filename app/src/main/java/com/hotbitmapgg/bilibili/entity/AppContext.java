package com.hotbitmapgg.bilibili.entity;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.hotbitmapgg.bilibili.utils.Const;
import com.hotbitmapgg.bilibili.utils.JsonUtil;
import com.hotbitmapgg.bilibili.utils.RC4;

import org.json.JSONObject;

import java.util.List;

///<summary>应用程序上下文环境</summary>
public class AppContext
{
    ///保存在本地配置文件(configuration.json)中
    public static long AccountID = 0;//app系统账号ID
    public static String Channel = null;//渠道号
    public static String UserAgent = null;//HTTP请求的UA
    public static String ServerBaseURL = null;//蜘蛛寻服务器BaseURL,在模拟器中调试不要使用localhost,因为模拟器是另一台设备

    ///从网络获取的数据
    /*类目对象列表,在SplashActivity.java->onCreate->loadData中被赋值
        [{
         "ID":xxx,"Name":"xxxx","Type":xxx,
        "IsDef":true|false,//是否默认显示
        },{...},..
       ]*/
    public static List<JSONObject> CatalogArr = null;
    /*视频页面配置
      {
        "hot_title":"正在热播",
        "latest_title":"最新上映",
        "recommend_title":"热门/推荐",
        "newest_ep_index_format":"更新至第%d集",
        "watching_count_format":"%d人在看",
        "more_text":"更多..",
      }*/
    public static JSONObject VideoPageCfg = null;

    ///运行时生成数据
    /*设备信息
      {
        "model":"xxx",//手机型号
        "brand":"xxx",//手机品牌
        "androidID":"xxxxx",//在设备首次启动时,系统会随机生成一个64位的数字,缺点是设备恢复出厂设置会重置,不需要权限
        "serialNum":"xxxxx",//Android系统2.3版本以上可以通过下面的方法得到Serial Number,不需要权限
      }*/
    public static JSONObject DeviceInfo = null;//设备信息
    public static String HttpCookies = null;//HTTP请求的Cookie

    ///<summary>程序上下文初始化</summary>
    public static void Initialize(Context context)
    {
        try
        {
            JSONObject configure = JsonUtil.ParseAssertFile(context.getAssets(),"configuration.json",new JSONObject());
            AccountID = JsonUtil.GetInt64(configure,"AccountID",Const.ACCOUNT_UNKNWON);
            Channel = JsonUtil.GetDefStrIfEmpty(configure,"Channel",Const.EMPTY);
            UserAgent = JsonUtil.GetDefStrIfEmpty(configure,"UserAgent",Const.EMPTY);
            ServerBaseURL = JsonUtil.GetDefStrIfEmpty(configure,"ServerURL",Const.ZZX_SERVER_URL);

            /*by="Aweigh" date="2018/7/11 16:51"
              这里获取设备信息,不再获取手机IMEI号和手机IMSI号,因为这两个信息需要需要android.permission.READ_PHONE_STATE权限，它在6.0+系统中是需要动态申请的。
              如果需求要求App启动时上报设备标识符的话，那么第一会影响初始化速度，第二还有可能被用户拒绝授权。
            */
            String androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            com.hotbitmapgg.bilibili.utils.RC4 rc4 = new com.hotbitmapgg.bilibili.utils.RC4(Const.DEFAULT_RC4_KEY);
            DeviceInfo = new JSONObject();
            DeviceInfo.put("model",Build.MODEL);
            DeviceInfo.put("brand",Build.BRAND);
            DeviceInfo.put("androidID",androidID);
            DeviceInfo.put("serialNum",Build.SERIAL);
            HttpCookies = "ZZXUSS=" + rc4.EncryptToBase64String(DeviceInfo.toString());
            Log.d(Const.LOG_TAG,
                    "AppContext.Initialize=>{\n" +
                            "\tAccountID:" + AccountID + "\n" +
                            "\tChannel:\"" + Channel + "\"\n" +
                            "\tUserAgent:\"" + UserAgent + "\"\n" +
                            "\tSeverBaseURL:\"" + ServerBaseURL + "\"\n" +
                            "\tDeviceInfo:" + DeviceInfo.toString() + "\n" +
                            "\tHttpCookies:" + HttpCookies + "\n" +
                            "}"
            );
        }
        catch (Exception e){
            Log.e(Const.LOG_TAG,"exception:" + e.getMessage());
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    ///java测试代码
    public static void InternalTest()
    {
        String aa = "xxcxc1515";
        String bb = "{\"cc\":\"ccc\",\"bb\":201,\"ee\":\"999\"}";

        //String text = RC4.EncryptToBase64String(aa,"AWEIGH");
    }
}
