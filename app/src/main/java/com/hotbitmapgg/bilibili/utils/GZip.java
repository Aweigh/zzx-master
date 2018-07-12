package com.hotbitmapgg.bilibili.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

///<summary>gzip解压缩</summary>
public class GZip
{
    /**
     * String 压缩至gzip 字节数据
     */
    public static byte[] Compress(String str)
    {
        return Compress(str, Const.UTF8);
    }

    /**
     * String 压缩至gzip 字节数组，可选择encoding配置
     */
    public static byte[] Compress(String str, String encoding)
    {
        if (str == null || str.length() == 0)
            return null;

        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzipInputStream;
            gzipInputStream = new GZIPOutputStream(out);
            gzipInputStream.write(str.getBytes(encoding));
            gzipInputStream.close();
            return out.toByteArray();
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * 字节数组解压
     */
    public static byte[] Decompress(byte[] bytes)
    {
        if (bytes == null || bytes.length == 0)
            return null;

        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);

            GZIPInputStream gzipInputStream = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = gzipInputStream.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * 字节数组解压至string
     */
    public static String DecompressToString(byte[] bytes)
    {
        return DecompressToString(bytes, Const.UTF8);
    }

    /**
     * 字节数组解压至string，可选择encoding配置
     */
    public static String DecompressToString(byte[] bytes, String encoding)
    {
        if (bytes == null || bytes.length == 0)
            return null;

        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            GZIPInputStream ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toString(encoding);
        }
        catch (Exception e)
        {
            return null;
        }
    }

/*    *//**
     * 判断请求头是否存在gzip
     *//*
    public static boolean isGzip(Headers headers) {
        boolean gzip = false;
        for (String key : headers.names()) {
            if (key.equalsIgnoreCase("Accept-Encoding") && headers.get(key).contains("gzip") || key.equalsIgnoreCase("Content-Encoding") && headers.get(key).contains("gzip")) {
                gzip = true;
                break;
            }
        }
        return gzip;
    }*/
}
