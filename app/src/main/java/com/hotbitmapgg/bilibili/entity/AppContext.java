package com.hotbitmapgg.bilibili.entity;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import com.hotbitmapgg.bilibili.network.RetrofitHelper;
import com.hotbitmapgg.bilibili.utils.CommonUtil;
import com.hotbitmapgg.bilibili.utils.Const;
import com.hotbitmapgg.bilibili.utils.JsonUtil;
import com.hotbitmapgg.bilibili.utils.MD5;
import com.hotbitmapgg.bilibili.utils.RC4;
import com.hotbitmapgg.bilibili.utils.CRC32;

import org.json.JSONObject;

import java.util.List;

///<summary>应用程序上下文环境</summary>
public class AppContext
{
    ///保存在本地配置文件(configuration.json)中
    public static JSONObject GlobalCfg = null;
    public static JSONObject VideoPageCfg = null;
    public static JSONObject TotalStationSearchCfg = null;
    public static String ServerBaseURL = null;//蜘蛛寻服务器BaseURL,在模拟器中调试不要使用localhost,因为模拟器是另一台设备

    ///从网络获取的数据
    /*类目对象列表,在SplashActivity.java->onCreate->loadData中被赋值
        [{
         "ID":xxx,"Name":"xxxx","Type":xxx,
        "IsDef":true|false,//是否默认显示
        },{...},..
    ]*/
    public static List<JSONObject> CatalogArr = null;

    ///运行时生成数据
    public static long ClientID = 0x0;//用于表示当前客户端的ID
    public static String ClientSecretKey = null;//客户端密钥(每个客户端拥有不同密钥),根据客户端设备信息生成的。

    ///<summary>程序上下文初始化</summary>
    public static void Initialize(Context context)
    {
        JSONObject cfgJson = JsonUtil.ParseAssertFile(context.getAssets(),"configuration.json",new JSONObject());
        GlobalCfg = JsonUtil.GetJObject(cfgJson,"Global");
        VideoPageCfg = JsonUtil.GetJObject(cfgJson,"VideoPage");
        TotalStationSearchCfg = JsonUtil.GetJObject(cfgJson,"TotalStationSearch");
        ServerBaseURL = JsonUtil.GetString(GlobalCfg,"ServerURL",Const.ZZX_SERVER_URL);

        /*by="Aweigh" date="2018/7/11 16:51"
          这里获取设备信息,不再获取手机IMEI号和手机IMSI号,因为这两个信息需要需要android.permission.READ_PHONE_STATE权限，它在6.0+系统中是需要动态申请的。
          如果需求要求App启动时上报设备标识符的话，那么第一会影响初始化速度，第二还有可能被用户拒绝授权。
        */
        String serialNum = Build.SERIAL;
        String androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        ClientSecretKey = MD5.Hash(androidID +"##" + serialNum);//通过设备信息计算出客户端密钥
        ClientID = CRC32.HashByText(androidID +"##" + serialNum);//通过设备信息计算出客户端ID

        Log.d(Const.LOG_TAG,
                "AppContext.Initialize=>{\n" +
                        "\tSeverBaseURL:\"" + ServerBaseURL + "\"\n" +
                        "\tClientID:\"" + ClientID + "\"\n" +
                        "\tClientSecretKey:\"" + ClientSecretKey + "\"\n" +
                        "}"
        );

        RetrofitHelper.initialize(context);
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    ///java测试代码
    public static void InternalTest()
    {
        String aa = "xxcxc1515";
        String bb = "{\"cc\":\"ccc\",\"bb\":201,\"ee\":\"999\"}";
        com.hotbitmapgg.bilibili.utils.RC4 rc4 = new com.hotbitmapgg.bilibili.utils.RC4(Const.DEFAULT_RC4_KEY);


/*        try
        {
            byte[] buffer = test.getBytes("UTF-8");
            //code = CRC32.calculate(buffer);
            java.util.zip.CRC32 crc32 = new java.util.zip.CRC32();
            crc32.update(buffer);
            code = crc32.getValue();
        }
        catch (Exception e)
        {
            return;
        }*/
    }
}


