package com.hotbitmapgg.bilibili.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonUtil
{
    ///<summary>从Json对象中获取指定属性的Int值</summary>
    ///<param name="key">属性名称</param>
    ///<result></result>
    public static int GetInt(JSONObject obj, String key, int def)
    {
        if(obj==null||key==null||key.isEmpty()) return def;
        try{
            return  obj.getInt(key);
        }
        catch (JSONException e){
            return  def;
        }
    }

    ///<summary>从Json对象中获取指定属性的String值</summary>
    public static String GetString(JSONObject obj,String key,String def)
    {
        if(obj==null||key==null||key.isEmpty()) return def;
        try{
            return  obj.getString(key);
        }
        catch (JSONException e){
            return  def;
        }
    }

    public static JSONObject GetJObject(JSONObject obj,String key)
    {
        if(obj==null || key==null || key.isEmpty()) return null;
        try{
            return  obj.getJSONObject(key);
        }
        catch (JSONException e){
            return  null;
        }
    }

    public static JSONArray GetJArray(JSONObject obj,String key)
    {
        if(obj==null || key==null || key.isEmpty()) return null;
        try{
            return  obj.getJSONArray(key);
        }
        catch (JSONException e){
            return  null;
        }
    }

    public static List<JSONObject> GetJObjArray(JSONObject obj,String key)
    {
        JSONArray arrTemp = GetJArray(obj,key);
        if(arrTemp==null) return  null;

        List<JSONObject> lst = new ArrayList<>();
        for (int i=0;arrTemp!=null && i<arrTemp.length();i++)
        {
            try{
                JSONObject item = arrTemp.getJSONObject(i);
                lst.add(item);
            }
            catch (JSONException e){
                continue;
            }
        }
        return lst;
    }
}
