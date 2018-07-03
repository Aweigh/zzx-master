package com.hotbitmapgg.bilibili.entity;

import com.hotbitmapgg.bilibili.utils.JsonUtil;

import org.json.JSONObject;

import java.util.List;

///<summary>应用程序上下文环境</summary>
public class AppContext
{
    ///<summary>app系统账号</summary>
    public static int AccountID = 3;
    ///<summary>
    /// 从网络获取的类目对象列表
    /// 目录Json格式:[{"ID":xxx,"Name":"xxxx","Type":xxx},{}...]
    /// 在SplashActivity.java->onCreate->loadData中被赋值
    ///</summary>
    public static List<JSONObject> CatalogArr = null;


    public static void InternalTest()
    {
        String aa = "{\"aa\":\"aaaa\",\"bb\":101}";
        String bb = "{\"cc\":\"ccc\",\"bb\":201,\"ee\":\"999\"}";

        JSONObject ja = JsonUtil.Parse(aa,null);
        JSONObject jb = JsonUtil.Parse(bb,null);
        String vb = ja.optString("bb");
        int ve = ja.optInt("ee");
//        boolean isSuc = JsonUtil.Combine(ja,jb);
    }
}
