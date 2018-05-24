package com.hotbitmapgg.bilibili.entity;

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
}
