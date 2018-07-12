package com.hotbitmapgg.bilibili.utils;

import android.text.TextUtils;

import java.security.MessageDigest;

/// <summary>
/// MD5是一个具体的算法,散列算法，哈西算法，但非加密算法（因为没有密钥），可用于加密（不可逆）
/// MD5是一种单向散列函数，单向散列函数的作用是将任何长度的一段数据散列成固定长度。常用于生成消息认证码等等，可以与非对称算法一起用于数字签名。
/// 是让大容量信息在用数字签名软件签署私人密匙前被"压缩"成一种保密的格式（就是把一个任意长度的字节串变换成一定长的大整数）
/// 不管是MD2、MD4还是MD5，它们都需要获得一个随机长度的信息并产生一个128位(16byte)的信息摘要。
/// </summary>
public class MD5
{
    // <summary>
    /// 将缓冲区数据散列成16byte的信息摘要
    /// </summary>
    public static String Hash(String text)
    {
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(text.getBytes(Const.UTF8));
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
