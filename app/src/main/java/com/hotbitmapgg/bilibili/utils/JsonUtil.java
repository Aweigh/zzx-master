package com.hotbitmapgg.bilibili.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

///<summary>从Json对象操作类</summary>
public class JsonUtil
{
    ///<summary>从Json对象中获取指定属性的Int值</summary>
    ///<param name="key">属性名称</param>
    ///<result></result>
    public static int GetInt(JSONObject obj, String key, int def)
    {
        if(obj==null||key==null||key.isEmpty() || !obj.has(key)) return def;
        return  obj.optInt(key);
    }

    ///<summary>从Json对象中获取指定属性的String值</summary>
    public static String GetString(JSONObject obj,String key,String def)
    {
        if(obj==null || key==null || key.isEmpty() || !obj.has(key)) return def;
        return  obj.optString(key);
    }

    public static JSONObject GetJObject(JSONObject obj,String key)
    {
        if(obj==null || key==null || key.isEmpty() || !obj.has(key)) return null;
        return  obj.optJSONObject(key);
    }

    public static JSONArray GetJArray(JSONObject obj,String key)
    {
        if(obj==null || key==null || key.isEmpty() || !obj.has(key)) return null;
        return obj.optJSONArray(key);
    }

    public static List<JSONObject> GetJObjArray(JSONObject obj,String key)
    {
        JSONArray arrTemp = GetJArray(obj,key);
        if(arrTemp==null) return  null;

        List<JSONObject> lst = new ArrayList<>();
        for (int i=0;arrTemp!=null && i<arrTemp.length();i++)
        {
            JSONObject item = arrTemp.optJSONObject(i);
            if(item!=null)
                lst.add(item);
        }
        return lst;
    }
}
