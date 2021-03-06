package com.hotbitmapgg.bilibili.entity;

import com.hotbitmapgg.bilibili.utils.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;

///<summary>蜘蛛寻服务接口返回的数据</summary>
public class ServerReply
{
    private int         _flag = 0;
    private String      _message = null;
    private JSONObject  _inner = null;

    public  ServerReply(){}
    public  ServerReply(ResponseBody response)
    {
        try
        {
            String text = response.string();
            _inner = new JSONObject(text);
            _flag = _inner.getInt("Flag");
            _message = _inner.getString("Message");
        }
        catch (Exception e)
        {
            this.Clear();
            e.printStackTrace();
        }
    }
    public void Clear()
    {
        _flag = 0;
        _message = null;
        _inner = null;
    }
    public boolean IsSucceed()
    {
        return  _flag == 1;
    }
    public int Flag()
    {
        return _flag;
    }
    public String Message()
    {
        return _message;
    }
    public int GetInt(String key,int def)
    {
        return JsonUtil.GetInt(_inner,key,def);
    }
    public String GetString(String key,String def)
    {
        return JsonUtil.GetString(_inner,key,def);
    }
    public JSONObject GetJObject(String key,JSONObject def)
    {
        JSONObject result = JsonUtil.GetJObject(_inner,key);
        if(result == null && def != null) return def;
        return result;
    }
    public JSONArray GetJArray(String key,JSONArray def)
    {
        JSONArray result = JsonUtil.GetJArray(_inner,key);
        if(result == null && def != null) return def;
        return result;
    }
    public List<JSONObject> GetJObjArray(String key)
    {
        return JsonUtil.GetJObjArray(_inner,key);
    }
    public List<String> GetStringList(String key)
    {
        JSONArray suggestionArr = GetJArray(key,null);
        return JsonUtil.ToStringList(suggestionArr,null);
    }
}
