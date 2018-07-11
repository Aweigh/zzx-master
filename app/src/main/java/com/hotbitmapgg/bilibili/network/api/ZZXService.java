package com.hotbitmapgg.bilibili.network.api;

import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.POST;
import rx.Observable;

/*蜘蛛寻服务接口集合
    @FormUrlEncoded注解:使用该注解,表示请求正文将使用表单网址编码。字段应该声明为参数，并用@Field注释或FieldMap注释。
    使用FormUrlEncoded注解的请求将具"application / x-www-form-urlencoded"MIME类型。字段名称和值将先进行UTF-8进行编码,再根据RFC-3986进行URI编码.
*/
public interface ZZXService
{
    ///<summary>获取配置信息接口</summary>
    ///<param name="aid">账号ID</param>
    ///<result>返回Json对象</result>
    @FormUrlEncoded //该注解少了会崩溃
    @POST("Application/Service/Configuration?t=2")
    Observable<ResponseBody> getConfiguration(@Field("aid") long aid);

    ///<summary>视频首页接口</summary>
    ///<param name="cid">视频类目</param>
    ///<result>返回Json对象</result>
    @FormUrlEncoded
    @POST("Application/Service/VideoHome?t=3")
    Observable<ResponseBody> getVideoHome(@Field("cid") int cid);

    ///<summary>视频详细信息接口</summary>
    ///<param name="vid">视频ID</param>
    ///<result>返回Json对象</result>
    @FormUrlEncoded
    @POST("Application/Service/GetVideoDetail?t=2")
    Observable<ResponseBody> getVideoDetail(@Field("vid") long vid);

    ///<summary>资源详细信息接口</summary>
    ///<param name="rid">资源ID</param>
    ///<param name="aid">账号ID</param>
    ///<result>返回Json对象</result>
    @FormUrlEncoded
    @POST("Application/Service/GetResourceDetail?t=2")
    Observable<ResponseBody> getResourceDetail(@Field("rid") int rid,@Field("aid") long aid);

    ///<summary>资源数据流接口</summary>
    ///<param name="rid">资源ID</param>
    ///<param name="aid">账号ID</param>
    ///<result>返回Json对象</result>
    @FormUrlEncoded
    @POST("Application/Service/GetResourceStream?t=3")
    Observable<ResponseBody> getResourceStream(@Field("rid") int rid,@Field("aid") long aid);
}
