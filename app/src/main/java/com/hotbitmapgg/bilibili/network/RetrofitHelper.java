package com.hotbitmapgg.bilibili.network;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.hotbitmapgg.bilibili.BilibiliApp;
import com.hotbitmapgg.bilibili.entity.AppContext;
import com.hotbitmapgg.bilibili.network.api.AccountService;
import com.hotbitmapgg.bilibili.network.api.BangumiService;
import com.hotbitmapgg.bilibili.network.api.BiliApiService;
import com.hotbitmapgg.bilibili.network.api.BiliAppService;
import com.hotbitmapgg.bilibili.network.api.BiliGoService;
import com.hotbitmapgg.bilibili.network.api.Im9Service;
import com.hotbitmapgg.bilibili.network.api.LiveService;
import com.hotbitmapgg.bilibili.network.api.RankService;
import com.hotbitmapgg.bilibili.network.api.SearchService;
import com.hotbitmapgg.bilibili.network.api.UserService;
import com.hotbitmapgg.bilibili.network.api.VipService;
import com.hotbitmapgg.bilibili.network.api.ZZXService;
import com.hotbitmapgg.bilibili.network.auxiliary.ApiConstants;
import com.hotbitmapgg.bilibili.utils.CommonUtil;
import com.hotbitmapgg.bilibili.utils.Const;
import com.hotbitmapgg.bilibili.utils.JsonUtil;
import com.hotbitmapgg.bilibili.utils.RC4;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
  Retrofit帮助类
  使用实例:
 compose(bindToLifecycle()):在子类使用Observable中的compose操作符，完成Observable发布的事件和当前的组件绑定，实现生命周期同步。
 从而实现当前组件生命周期结束时，自动取消对Observable订阅。

 RetrofitHelper.getZZXAPI(Const.C_ZZXUSS|Const.C_STOKEN).getConfiguration() .
     compose(bindToLifecycle()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()) .
     subscribe(response -> {
         ServerReply reply = new ServerReply(response);
         if(!reply.IsSucceed()){
             Log.e(Const.LOG_TAG,"请求类目列表失败," + reply.Message());
             return;
         }
         AppContext.CatalogArr = reply.GetJObjArray("CatalogArr");
         finishTask(0x2);//加载数据完成
     },throwable -> {
         Log.e(Const.LOG_TAG,throwable.getMessage());
         finishTask(0x2);//加载数据完成
     });
 */
public class RetrofitHelper {
    private static OkHttpClient _mOkHttpClient;
    private static String _account = null;//app系统账号信息
    private static String _channel = null;//渠道号
    private static String _userAgent = null;//HTTP请求的UA
    private static String _cookieZZXUSS = null;//HTTP请求的Cookie:ZZXUSS
    private static String _cookieSTOKEN = null;//HTTP请求的Cookie:STOKEN

    static {
        initOkHttpClient();
    }
    ///<summary>初始化</summary>
    public static void initialize(Context context)
    {
        try
        {
            _account = JsonUtil.GetString(AppContext.GlobalCfg,"Account",Const.EMPTY);
            _channel = JsonUtil.GetString(AppContext.GlobalCfg,"Channel",Const.EMPTY);
            _userAgent = JsonUtil.GetString(AppContext.GlobalCfg,"HttpUserAgent",Const.EMPTY);

            /*DeviceInfo：设备信息
              {
                "model":"xxx",//手机型号
                "brand":"xxx",//手机品牌
                "andrID":"xxxxx",//在设备首次启动时,系统会随机生成一个64位的数字,缺点是设备恢复出厂设置会重置,不需要权限
                "serialNo":"xxxxx",//Android系统2.3版本以上可以通过Build.SERIAL获取，且非手机设备也可以，不需要权限，通用性也较高，但我测试发现红米手机返回的是0123456789ABCDEF 明显是一个顺序的非随机字符串，也不一定靠谱。
                "ver":xxx,//客户端版本号VersionCode
              }*/
            RC4 rc4 = new RC4(Const.DEFAULT_RC4_KEY);
            JSONObject DeviceInfo = new JSONObject();
            DeviceInfo.put("model", Build.MODEL);
            DeviceInfo.put("brand",Build.BRAND);
            DeviceInfo.put("cid",AppContext.ClientID);
            DeviceInfo.put("ver", CommonUtil.getVersionCode(context));
            String deviceInfoStr = DeviceInfo.toString();
            _cookieZZXUSS = rc4.EncryptToBase64String(deviceInfoStr);

            /*Token信息
              {
                "account":"xxx",//app系统账号,密文数据
                "channel":"xxx",//渠道号
            }*/
            JSONObject stoken = new JSONObject();
            stoken.put("account", _account);
            stoken.put("channel", _channel);
            String stokenStr = stoken.toString();
            _cookieSTOKEN = rc4.EncryptToBase64String(stokenStr);

            Log.d(Const.LOG_TAG,
                    "RetrofitHelper.initialize=>{\n" +
                            "\t_account:" + _account + "\n" +
                            "\t_channel:\"" + _channel + "\"\n" +
                            "\t_userAgent:\"" + _userAgent + "\"\n" +
                            "\tdeviceInfo:" + deviceInfoStr + "\n" +
                            "\tstoken:" + stokenStr + "\n" +
                            "\t_cookieZZXUSS:" + _cookieZZXUSS + "\n" +
                            "\t_cookieSTOKEN:" + _cookieSTOKEN + "\n" +
                            "}"
            );
        }
        catch (Exception e){
            Log.e(Const.LOG_TAG,"exception:" + e.getMessage());
            e.printStackTrace();
        }
    }
    ///<summary>蜘蛛寻服务接口</summary>
    public static ZZXService getZZXAPI(){
        return createApi(ZZXService.class, AppContext.ServerBaseURL,Const.C_STOKEN);//只发送COOKIE STOKEN
    }
    public static ZZXService getZZXAPI(int flags){
        return createApi(ZZXService.class, AppContext.ServerBaseURL,flags);
    }
    public static LiveService getLiveAPI() {
        return createApi(LiveService.class, AppContext.ServerBaseURL,0x0);//ApiConstants.LIVE_BASE_URL);
    }

    public static BiliAppService getBiliAppAPI() {
        return createApi(BiliAppService.class,AppContext.ServerBaseURL,0x0);//, ApiConstants.APP_BASE_URL);
    }

    public static BiliApiService getBiliAPI() {
        return createApi(BiliApiService.class, ApiConstants.API_BASE_URL,0x0);
    }

    public static BiliGoService getBiliGoAPI() {
        return createApi(BiliGoService.class, ApiConstants.BILI_GO_BASE_URL,0x0);
    }

    public static RankService getRankAPI() {
        return createApi(RankService.class, ApiConstants.RANK_BASE_URL,0x0);
    }

    public static UserService getUserAPI() {
        return createApi(UserService.class, ApiConstants.USER_BASE_URL,0x0);
    }

    public static VipService getVipAPI() {
        return createApi(VipService.class, ApiConstants.VIP_BASE_URL,0x0);
    }

    public static BangumiService getBangumiAPI() {
        return createApi(BangumiService.class, ApiConstants.BANGUMI_BASE_URL,0x0);
    }

    public static SearchService getSearchAPI() {
        return createApi(SearchService.class, ApiConstants.SEARCH_BASE_URL,0x0);
    }

    public static AccountService getAccountAPI() {
        return createApi(AccountService.class, ApiConstants.ACCOUNT_BASE_URL,0x0);
    }

    public static Im9Service getIm9API() {
        return createApi(Im9Service.class, ApiConstants.IM9_BASE_URL,0x0);
    }

    ///<summary>根据传入的baseUrl，和api创建retrofit</summary>
    ///<param name="flags">表示执行标志位
    /// bit0表示是否包含COOKIE ZZXUSS
    /// bit1表示是否包含COOKIE STOKEN
    ///</param>
    ///<result></result>
    private static <T> T createApi(Class<T> clazz, String baseUrl,int flags) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(_mOkHttpClient)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        for (Interceptor it:_mOkHttpClient.interceptors()){
            if(it.getClass() == ReqHeaderInterceptor.class){
                ((ReqHeaderInterceptor)it).setCookieFlags(flags);//设置拦截器标志位
                break;
            }
        }
        return retrofit.create(clazz);
    }

    /**
     * 初始化OKHttpClient,设置缓存,设置超时时间,设置打印日志,设置UA拦截器
     */
    private static void initOkHttpClient()
    {
        /*by="Aweigh" date="2018/7/12 16:28"
          Okhttp3是支持gzip自动解压的,但是自动解压有一个条件就是不能添加"Accept-Encoding"头,
          从okhttp3源码中BridgeInterceptor类有一个transparentGzip表示用来表示是否需要自动解压gzip数据,但transparentGzip只有当HTTP头没有"Accept-Encoding"时才被设置为true.
          当transparentGzip值为true且服务器Response头部包含"Content-Encoding:gzip"则开始解压返回的数据
        */
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        if (_mOkHttpClient == null) {
            synchronized (RetrofitHelper.class) {
                if (_mOkHttpClient == null) {
                    //设置Http缓存
                    Cache cache = new Cache(new File(BilibiliApp.getInstance().getCacheDir(), "HttpCache"), 1024 * 1024 * 10);
                    _mOkHttpClient = new OkHttpClient.Builder() //.proxy(new Proxy(Proxy.Type.HTTP, new java.net.InetSocketAddress("172.16.31.51", 1691)))
                            .cache(cache)
                            .addInterceptor(interceptor)
                            .addNetworkInterceptor(new CacheInterceptor())
                            .addNetworkInterceptor(new StethoInterceptor())
                            .retryOnConnectionFailure(true)
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .addInterceptor(new ReqHeaderInterceptor())
                            .build();
                }
            }
        }
    }//end initOkHttpClient

    /**HTTP请求头部拦截器
     * 1.添加UA拦截器，B站请求API需要加上UA才能正常使用
     * 2.添加cookie
     */
    private static class ReqHeaderInterceptor implements Interceptor {
        private int _cookieFlags = 0x0;//bit0表示COOKIE_ZZXUSS,bit1表示COOKIE_STOKEN
        public void setCookieFlags(int v){
            _cookieFlags = v;
        }

        @Override
        public Response intercept(Chain chain) throws IOException
        {
            String cookies = Const.EMPTY;
            if((_cookieFlags & Const.C_ZZXUSS)>0)//bit0:COOKIE_ZZXUSS
                cookies += Const.COOKIE_ZZXUSS + "=" + _cookieZZXUSS + ";";
            if((_cookieFlags & Const.C_STOKEN)>0)//bit1:COOKIE_STOKEN
                cookies += Const.COOKIE_STOKEN + "=" + _cookieSTOKEN + ";";

            Request originalRequest = chain.request();
            Request requestWithUserAgent = originalRequest.newBuilder()
                    .removeHeader("User-Agent")
                    .removeHeader("Accept-Encoding")   //要开启自动解压gzip数据就不能有这个头信息,此时如果服务器返回"Content-Encoding:gzip"则将自动解压
                    .addHeader("User-Agent", _userAgent)    //使用自定义UA
                    .addHeader("Cookie",cookies)            //添加自定义cookie
                    .build();
            return chain.proceed(requestWithUserAgent);
        }
    }//end class ReqHeaderInterceptor

    /**
     * 为okhttp添加缓存，这里是考虑到服务器不支持缓存时，从而让okhttp支持缓存
     */
    private static class CacheInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            // 有网络时 设置缓存超时时间1个小时
            int maxAge = 10;//60 * 60; //为了测试方便,暂时修改为10s
            // 无网络时，设置超时为1天
            int maxStale = 60 * 60 * 24;
            Request request = chain.request();
            if (CommonUtil.isNetworkAvailable(BilibiliApp.getInstance())) {
                //有网络时只从网络获取
                request = request.newBuilder().cacheControl(CacheControl.FORCE_NETWORK).build();
            } else {
                //无网络时只从缓存中读取
                request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
            }
            Response response = chain.proceed(request);
            if (CommonUtil.isNetworkAvailable(BilibiliApp.getInstance())) {
                response = response.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", "public, max-age=" + maxAge)
                        .build();
            } else {
                response = response.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                        .build();
            }
            return response;
        }
    }//end class CacheInterceptor
}//end class RetrofitHelper
