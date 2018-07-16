package com.hotbitmapgg.bilibili.utils;

///<summary>应用程序常量</summary>
public class Const {
    public static final String LOG_TAG = "AW";
    public static final String MODULE_PARAMS = "module_params";//页面参数键名称
    public static final String EMPTY = "";//空字符串
    public static final long ACCOUNT_UNKNWON = 0x0;//未知账号ID
    public static final String ZZX_SERVER_URL = "http://www.zhizhuxun.com/";//蜘蛛寻服务器地址
    public static final String DEFAULT_RC4_KEY = "0123456789!@#$abcdefg";//默认的RC4密钥,这是与服务器交互的密钥。

    //如下cookie由APP客户端创建,提交给服务器时是经过RC4+Base64编码后的密文数据
    public static final String COOKIE_ZZXUSS = "ZZXUSS";
    public static final String COOKIE_STOKEN = "STOKEN";

    public static final String UTF8 = "UTF-8";//utf-8编码或字符集名称

    public static final int MillSecond = 1000;//1秒钟的毫秒数
}
