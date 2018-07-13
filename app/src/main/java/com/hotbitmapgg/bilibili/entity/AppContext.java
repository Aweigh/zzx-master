package com.hotbitmapgg.bilibili.entity;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

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
    public static String Account = null;//app系统账号信息
    public static String Channel = null;//渠道号
    public static String ServerBaseURL = null;//蜘蛛寻服务器BaseURL,在模拟器中调试不要使用localhost,因为模拟器是另一台设备
    public static String HttpUserAgent = null;//HTTP请求的UA

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
    public static long ClientID = 0x0;//用于表示当前客户端的ID
    public static String HttpCookies = null;//HTTP请求的Cookie
    public static String ClientSecretKey = null;//客户端密钥(每个客户端拥有不同密钥),根据客户端设备信息生成的。
    /*设备信息
      {
        "model":"xxx",//手机型号
        "brand":"xxx",//手机品牌
        "andrID":"xxxxx",//在设备首次启动时,系统会随机生成一个64位的数字,缺点是设备恢复出厂设置会重置,不需要权限
        "serialNo":"xxxxx",//Android系统2.3版本以上可以通过Build.SERIAL获取，且非手机设备也可以，不需要权限，通用性也较高，但我测试发现红米手机返回的是0123456789ABCDEF 明显是一个顺序的非随机字符串，也不一定靠谱。
        "ver":xxx,//客户端版本号VersionCode
      }*/
    public static JSONObject DeviceInfo = null;//设备信息

    ///<summary>程序上下文初始化</summary>
    public static void Initialize(Context context)
    {
        try
        {
            JSONObject configure = JsonUtil.ParseAssertFile(context.getAssets(),"configuration.json",new JSONObject());
            Account = JsonUtil.GetString(configure,"Account",Const.EMPTY);
            Channel = JsonUtil.GetString(configure,"Channel",Const.EMPTY);
            ServerBaseURL = JsonUtil.GetString(configure,"ServerURL",Const.ZZX_SERVER_URL);
            HttpUserAgent = JsonUtil.GetString(configure,"HttpUserAgent",Const.EMPTY);

            /*by="Aweigh" date="2018/7/11 16:51"
              这里获取设备信息,不再获取手机IMEI号和手机IMSI号,因为这两个信息需要需要android.permission.READ_PHONE_STATE权限，它在6.0+系统中是需要动态申请的。
              如果需求要求App启动时上报设备标识符的话，那么第一会影响初始化速度，第二还有可能被用户拒绝授权。
            */
            String serialNum = Build.SERIAL;
            String androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            ClientSecretKey = MD5.Hash(androidID +"##" + serialNum);//通过设备信息计算出客户端密钥
            ClientID = CRC32.HashByText(androidID +"##" + serialNum);//通过设备信息计算出客户端ID

            DeviceInfo = new JSONObject();
            DeviceInfo.put("model",Build.MODEL);
            DeviceInfo.put("brand",Build.BRAND);
            DeviceInfo.put("cid",ClientID);
            DeviceInfo.put("ver", CommonUtil.getVersionCode(context));
            String deviceInfoStr = DeviceInfo.toString();

            /*Token信息
              {
                "account":"xxx",//app系统账号,密文数据
                "channel":"xxx",//渠道号
            }*/
            JSONObject stoken = new JSONObject();
            stoken.put("account",Account);
            stoken.put("channel",Channel);
            String stokenStr = stoken.toString();

            RC4 rc4 = new RC4(Const.DEFAULT_RC4_KEY);
            HttpCookies = Const.COOKIE_ZZXUSS + "=" + rc4.EncryptToBase64String(deviceInfoStr) + ";" +
                          Const.COOKIE_STOKEN + "=" + rc4.EncryptToBase64String(stokenStr) + ";" ;

            Log.d(Const.LOG_TAG,
                    "AppContext.Initialize=>{\n" +
                            "\tAccount:" + Account + "\n" +
                            "\tChannel:\"" + Channel + "\"\n" +
                            "\tSeverBaseURL:\"" + ServerBaseURL + "\"\n" +
                            "\tHttpUserAgent:\"" + HttpUserAgent + "\"\n" +
                            "\tClientSecretKey:\"" + ClientSecretKey + "\"\n" +
                            "\tdeviceInfo:" + deviceInfoStr + "\n" +
                            "\tstoken:" + stokenStr + "\n" +
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


