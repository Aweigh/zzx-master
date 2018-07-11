package com.hotbitmapgg.bilibili.utils;
import android.util.Base64;

///<summary>RC4加密算法</summary>
public class RC4 {

    private String _password = null;
    public RC4(String key)
    {
        _password = key;
    }
    // <summary>
    /// 对字符串进行加密
    /// </summary>
    /// <param name="text">加密前的文本</param>
    /// <returns>返回加密之后的缓冲区</returns>
    public byte[] Encrypt(String text)
    {
        if (text == null || _password == null) {
            return null;
        }
        byte b_data[] = text.getBytes();
        return RC4Base(b_data, _password);
    }
    /// <summary>
    /// 加密数据并进行base64编码
    /// </summary>
    public String EncryptToBase64String(String text)
    {
        byte[] buffer = Encrypt(text);
        return Base64.encodeToString(buffer, Base64.DEFAULT);
    }
    // <summary>
    /// 解密给定的加密数据
    /// </summary>
    /// <param name="stream">加密数据</param>
    /// <returns>返回解密后的数据</returns>
    public String Decrypt(byte[] stream) {
        if (stream == null || _password == null) {
            return null;
        }
        return asString(RC4Base(stream, _password));
    }
    /// <summary>
    /// 将讲过base64编码之后的
    /// </summary>
    public String DecryptFromBase64String(String text)
    {
        byte[] enstream = Base64.decode(text, Base64.DEFAULT);
        return  Decrypt(enstream);
    }

    ///////////////////////////////////////////////////////////////////////////////////
    /// internal
    private static String asString(byte[] buf) {
        StringBuffer strbuf = new StringBuffer(buf.length);
        for (int i = 0; i < buf.length; i++) {
            strbuf.append((char) buf[i]);
        }
        return strbuf.toString();
    }

    private static byte[] initKey(String aKey) {
        byte[] b_key = aKey.getBytes();
        byte state[] = new byte[256];

        for (int i = 0; i < 256; i++) {
            state[i] = (byte) i;
        }
        int index1 = 0;
        int index2 = 0;
        if (b_key == null || b_key.length == 0) {
            return null;
        }
        for (int i = 0; i < 256; i++) {
            index2 = ((b_key[index1] & 0xff) + (state[i] & 0xff) + index2) & 0xff;
            byte tmp = state[i];
            state[i] = state[index2];
            state[index2] = tmp;
            index1 = (index1 + 1) % b_key.length;
        }
        return state;
    }

    private static byte[] RC4Base (byte [] input, String mKkey) {
        int x = 0;
        int y = 0;
        byte key[] = initKey(mKkey);
        int xorIndex;
        byte[] result = new byte[input.length];

        for (int i = 0; i < input.length; i++) {
            x = (x + 1) & 0xff;
            y = ((key[x] & 0xff) + y) & 0xff;
            byte tmp = key[x];
            key[x] = key[y];
            key[y] = tmp;
            xorIndex = ((key[x] & 0xff) + (key[y] & 0xff)) & 0xff;
            result[i] = (byte) (input[i] ^ key[xorIndex]);
        }
        return result;
    }
}