package com.hotbitmapgg.bilibili.entity.bangumi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hcc on 2016/10/2 16:49
 * 100332338@qq.com
 * <p>
 * 首页番剧推荐模型类
 */

public class BangumiRecommendInfo {
    private int code;
    private String message;
    private List<ResultBean> result;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ResultBean> getResult() {
        return result;
    }

    public void setResult(List<ResultBean> result) {
        this.result = result;
    }

    public static class ResultBean
    {
        private String cover;
        private long cursor;
        private String desc;
        private int id;
        private int is_new;
        private String link;
        private String title;

        public ResultBean(JSONObject o)
        {
            if(o==null) return;
            cover = o.optString("cover");
            cursor = o.optLong("cursor");
            desc = o.optString("desc");
            id = o.optInt("id");
            is_new = o.optInt("is_new");
            link = o.optString("link");
            title = o.optString("title");
        }
        ///<summary>将JSONObject对象数组转为实体类数组</summary>
        public static List<ResultBean> From(JSONArray arr)
        {
            if(arr==null) return null;

            List<ResultBean> collection = new ArrayList<>();
            for (int i=0;i<arr.length();i++)
            {
                JSONObject o = arr.optJSONObject(i);
                if(o == null) continue;
                ResultBean item = new ResultBean(o);
                collection.add(item);
            }
            return  collection;
        }

        public String getCover() {
            return cover;
        }

        public void setCover(String cover) {
            this.cover = cover;
        }

        public long getCursor() {
            return cursor;
        }

        public void setCursor(long cursor) {
            this.cursor = cursor;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getIs_new() {
            return is_new;
        }

        public void setIs_new(int is_new) {
            this.is_new = is_new;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
