package com.hotbitmapgg.bilibili.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

///<summary>从Json对象操作类</summary>
public class JsonUtil
{
    ///<summary>将Json字符串转为Json对象</summary>
    public static JSONObject Parse(String text,JSONObject def)
    {
        if(text == null||text.isEmpty()) return  def;

        try
        {
            JSONObject obj = new JSONObject(text);
            return obj;
        }
        catch (Exception e)
        {
            return  def;
        }
    }
    ///<summary>从Json对象中获取指定属性的Int值</summary>
    ///<param name="key">属性名称</param>
    ///<result></result>
    public static int GetInt(JSONObject obj, String key, int def)
    {
        if(obj==null||key==null||key.isEmpty() || !obj.has(key)) return def;
        return  obj.optInt(key);
    }
    ///<summary>设置属性值</summary>
    public static boolean SetValue(JSONObject obj,String key,Object value)
    {
        if(obj==null||key==null) return false;
        try
        {
            obj.put(key,value);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
    ///<summary>从Json对象中获取指定属性的String值
    /// 注意:如果给定属性本身是一个数值类型,该函数也能成功返回其对应的字符串类型,例如:
    /// JSONObject ja = JsonUtil.Parse("{\"aa\":\"aaaa\",\"bb\":101}",null);
    /// String vb = ja.optString("bb"); => 这里vb值为"101"
    ///</summary>
    ///<param name="key">键或属性名称</param>
    public static String GetString(JSONObject obj,String key,String def)
    {
        if(obj==null || key==null || key.isEmpty() || !obj.has(key)) return def;
        return  obj.optString(key);
    }
    public static String GetDefStrIfEmpty(JSONObject obj,String key,String def)
    {
        if(obj==null || key==null || key.isEmpty() || !obj.has(key)) return def;

        String value = obj.optString(key);
        if(value.isEmpty()||value.compareToIgnoreCase("null")==0)
            return  def;//空字符串或null字符串则返回默认值
        return  value;
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

        List<JSONObject> lst = new ArrayList<JSONObject>();
        for (int i=0;arrTemp!=null && i<arrTemp.length();i++)
        {
            JSONObject item = arrTemp.optJSONObject(i);
            if(item!=null)
                lst.add(item);
        }
        return lst;
    }
    public static List<JSONObject> ToJObjectList(JSONArray arr)
    {
        if(arr == null) return  null;
        List<JSONObject> lst = new ArrayList<JSONObject>();
        for (int i = 0;i<arr.length();i++)
        {
            JSONObject item = arr.optJSONObject(i);
            if(item==null) continue;
            lst.add(item);
        }
        return  lst;
    }
    public static List<String> ToStringList(JSONArray arr)
    {
        if(arr == null) return  null;
        List<String> lst = new ArrayList<String>();
        for (int i = 0;i<arr.length();i++)
        {
            String item = arr.optString(i);
            if(item==null) continue;
            lst.add(item);
        }
        return  lst;
    }
    ///<summary>将a对象与b对象合并,将改变a对象而b对象不变</summary>
    ///<param name=""></param>
    ///<result>是否合并成功</result>
    public static boolean Combine(JSONObject a,JSONObject b)
    {
        if(a == null || b == null) return false;

        try
        {
            Iterator<String> it = b.keys();
            while (it.hasNext())
            {
                // 获得key
                String key = it.next();
                Object value = b.opt(key);
                a.put(key,value);
            }
            return  true;
        }
        catch (Exception e)
        {
            return  false;
        }
    }
}
