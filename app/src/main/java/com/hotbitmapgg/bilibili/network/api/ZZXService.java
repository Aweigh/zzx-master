package com.hotbitmapgg.bilibili.network.api;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

///<summary>蜘蛛寻服务接口集合</summary>
public interface ZZXService
{
    ///<summary>获取配置信息接口</summary>
    ///<param name="aid">账号ID</param>
    ///<result>返回Json对象</result>
    @GET("Application/Service/Configuration?t=2")
    Observable<ResponseBody> getConfiguration(@Query("aid") int aid);

    ///<summary>视频首页接口</summary>
    ///<param name="cid">视频类目</param>
    ///<result>返回Json对象</result>
    @GET("Application/Service/VideoHome?t=2")
    Observable<ResponseBody> getVideoHome(@Query("cid") int cid);

    ///<summary>视频详细信息接口</summary>
    ///<param name="vid">视频ID</param>
    ///<result>返回Json对象</result>
    @GET("Application/Service/GetVideoDetail?t=2")
    Observable<ResponseBody> getVideoDetail(@Query("vid") int vid);
}
