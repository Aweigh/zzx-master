package com.hotbitmapgg.bilibili.network.api;

import com.hotbitmapgg.bilibili.entity.AppContext;

import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.POST;
import rx.Observable;

/*蜘蛛寻服务接口集合
    @FormUrlEncoded注解:使用该注解,表示请求正文将使用表单网址编码。字段应该声明为参数，并用@Field注释或FieldMap注释。
    使用FormUrlEncoded注解的请求将具"Content-Type: application/x-www-form-urlencoded"MIME类型。字段名称和值将先进行UTF-8进行编码,再根据RFC-3986进行URI编码.
*/
public interface ZZXService
{
    ///<summary>获取配置信息接口</summary>
    ///<param name="aid">账号ID</param>
    ///<result>返回Json对象</result>
    /*{
        "CatalogArr":[
            {"ID":xxx,"Name":"xxxx","Type":xx,"IsDef":0|1},
            {....},
        ]
     }*/
    //@FormUrlEncoded
    @POST("Application/Service/Configuration?t=2")
    Observable<ResponseBody> getConfiguration();

    ///<summary>视频首页接口</summary>
    ///<param name="cid">视频类目</param>
    ///<result>返回Json对象</result>
    /*{
        "hotItems":{//正在热播
            "list"[
                {
                    "id":xxx,
                    "title":"xxx",//视频标题
                    "cover":"xxx",
                    "newest_ep_index":-1,//更新至第x话,小于0表示不显示
                    "desc":"xxx",
                    "link":"xxx",
                    "watching_count":-1,//x人在观看,小于0表示不显示
                },
                {....},
            ],
            "count":xx
        },
        "latestItems":{//新上映
            "list"[],
            "count":xx
        },
        "recommendItems":{//推荐
            "list"[],
            "count":xx
        },
        "navigations":[],
        "adHead":[//头部广告栏
            {"title":"xxx","img":"xxx","link":"xxx"},
            {....},
         ],
         "adBody":[//内容广告栏
            {"title":"xxx","img":"xxx","link":"xxx","index":x},
            {....},
         ],
    }*/
    @FormUrlEncoded //该注解少了会崩溃
    @POST("Application/Service/VideoHome?t=3")
    Observable<ResponseBody> getVideoHome(@Field("cid") int cid);

    ///<summary>视频详细信息接口</summary>
    ///<param name="vid">视频ID</param>
    ///<result>返回Json对象</result>
    /*{
        "video":{
                "id":xxx,
                "publishDate":"xxx",
                "actors":"xxx",
                "directors":"xxx",
                "description":"xxx",
                "score":"xxx",
                "isFinish":true|false,
                "title":"xxxx",
                "newest_ep_index":"x",//更新到第n集
                "playCount":0,//播放数量
                "favoriteCount":0,//追番数量
                "cover":"xxxx",
                },
         "resArr":[
                    {"id":xxx,"name":"xxx","title":"xxxx","cover":"xxx"},
                    {....},
                ],
          "comments":{
                    "total":xx,
                    "pageSize":xx,
                    "pageCount":xx,
                    "pageIndex":xx,
                    "list":[...]
                },
        "tagArr":["泡面","奇幻","校园"],
        "seasonVerArr":[
                {"id":0,"type":0,"name":"国际版"},
                {"id":0,"type":0,"name":"中文版"},
                {"id":0,"type":0,"name":"粤语版"},
            ]
    }*/
    @FormUrlEncoded
    @POST("Application/Service/GetVideoDetail?t=2")
    Observable<ResponseBody> getVideoDetail(@Field("vid") long vid);

    ///<summary>资源详细信息接口</summary>
    ///<param name="rid">资源ID</param>
    ///<result>返回Json对象</result>
    /*{
        "streamData":"xxxx",//[流数据]
        "streamConfig":"xxxx",//[流配置]
        "resource"://资源记录的详细信息
        {
            "name":"xxxx",//资源名称,例如:"第x话"
            "title":"xxxx",//[资源标题],例如:"超能力者的灾难"
            "description":"xxxx",//[资源简介]
            "playCount":"xxxx",//视频播放数量
            "barrageCount":"xxxx",//视频弹幕数量
        },
        "comments"://评论列表
        {
            "total":0,
            "pageSize":0,
            "pageCount":0,
            "pageIndex":0,
            "list":[]
        }
     }*/
    @FormUrlEncoded
    @POST("Application/Service/GetResourceDetail?t=2")
    Observable<ResponseBody> getResourceDetail(@Field("rid") int rid);

    ///<summary>资源数据流接口</summary>
    ///<param name="rid">资源ID</param>
    ///<result>返回Json对象</result>
    /*{
        "streamData":"xxxx",//流数据
        "streamConfig":{//流配置
            "type":"ffconcat",
            "expire":"yyyy-MM-dd HH:mm:ss"
        }
     }
     */
    @FormUrlEncoded
    @POST("Application/Service/GetResourceStream?t=3")
    Observable<ResponseBody> getResourceStream(@Field("rid") int rid);

    ///<summary>获取搜索建议</summary>
    ///<param name="wd">关键字</param>
    ///<result>返回Json对象</result>
    /*{
        "suggestionArr":["xxxx","yyyy",...],
        "count":xxx
     }*/
    @FormUrlEncoded
    @POST("Application/Service/GetSuggestions?t=1")
    Observable<ResponseBody> getSuggestions(@Field("wd") String wd);

    ///<summary>根据关键字搜索影视或视频资源记录</summary>
    ///<param name="wd">关键字</param>
    ///<param name="scope">查找范围,bit0表示Video,bit1表示Resource</param>
    ///<result>返回Json对象</result>
    /*{
        "itemArr":[
            {
                "id":xxx,
                "type":xxx,         //0x1表示视频,0x2表示自愿
                "title":"xxx",      //标题
                "cover":"xxxx",     //视频封面
                "playCount":0,      //视频或影视播放数量
                "barrageCount":0,   //视频弹幕数量
                "author":"xxx",     //[上传者或作者]
                "duration":"xx:xx", //[视频时长]
            },
            {...},
         ],
        "count":xxxx
    }*/
    @FormUrlEncoded
    @POST("Application/Service/SearchRecordBy?t=1")
    Observable<ResponseBody> searchRecordBy(@Field("wd") String wd,@Field("scope")int scope,
                                           @Field("pageIndex")int pageIndex,@Field("pageSize")int pageSize);
}
