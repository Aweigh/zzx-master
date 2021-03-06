package com.hotbitmapgg.bilibili.widget.banner;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hcc on 16/8/24 21:37
 * 100332338@qq.com
 * <p>
 * Banner模型类
 */
public class BannerEntity {
    public String title;
    public String img;
    public String link;

    public BannerEntity(String link, String title, String img)
    {
        this.link = link;
        this.title = title;
        this.img = img;
    }
    ///<summary>将JSONObject对象转为实体类</summary>
    public BannerEntity(JSONObject jo)
    {
        if(jo==null) return;
        title = jo.optString("title");
        img = jo.optString("img");
        link = jo.optString("link");
    }
    ///<summary>将JSONObject对象数组转为BannerEntity实体类数组</summary>
    public static List<BannerEntity> From(JSONArray arr)
    {
        if(arr==null) return null;

        List<BannerEntity> collection = new ArrayList<>();
        for (int i=0;i<arr.length();i++)
        {
            JSONObject jo = arr.optJSONObject(i);
            if(jo == null) continue;

            BannerEntity entity = new BannerEntity(jo);
            collection.add(entity);
        }
        return  collection;
    }
}
