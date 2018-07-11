package com.hotbitmapgg.bilibili.module.common;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.hotbitmapgg.bilibili.entity.AppContext;
import com.hotbitmapgg.bilibili.entity.ServerReply;
import com.hotbitmapgg.bilibili.network.RetrofitHelper;
import com.hotbitmapgg.bilibili.utils.Const;
import com.hotbitmapgg.bilibili.utils.ConstantUtil;
import com.hotbitmapgg.bilibili.utils.PreferenceUtil;
import com.hotbitmapgg.bilibili.utils.SystemUiVisibilityUtil;
import com.hotbitmapgg.ohmybilibili.R;
import com.trello.rxlifecycle.components.RxActivity;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

///<summary>APP启动页界面,即启动时的动画或广告页面</summary>
public class SplashActivity extends RxActivity {
    private Unbinder bind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        bind = ButterKnife.bind(this);
        SystemUiVisibilityUtil.hideStatusBar(getWindow(), true);
        setUpSplash();
        loadData();
    }

    private void setUpSplash() {
        Observable.timer(2000, TimeUnit.MILLISECONDS)
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> finishTask());
    }

    private void finishTask() {
        boolean isLogin = PreferenceUtil.getBoolean(ConstantUtil.KEY, false);
        if (isLogin) {//如果已经登录了则进入主界面
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        } else {//转到登录界面,随便输入账号和密码都可以
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }
        SplashActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bind.unbind();
    }
    /*by="Aweigh" date="2018/5/21 16:59"
      在该页面中获取配置信息等网络数据,即在动画页面中请求数据提供给首页等页面使用
    */
    private void loadData()
    {
        RetrofitHelper.getZZXAPI().getConfiguration(AppContext.AccountID) .
                compose(bindToLifecycle()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()) .
                subscribe(response -> {
                    ServerReply reply = new ServerReply(response);
                    if(!reply.IsSucceed()){
                        Log.e(Const.LOG_TAG,"请求类目列表失败," + reply.Message());
                        return;
                    }
                    AppContext.CatalogArr = reply.GetJObjArray("CatalogArr");
                    AppContext.VideoPageCfg = reply.GetJObject("VideoPageCfg",new JSONObject());
                },throwable -> {
                    Log.e(Const.LOG_TAG,throwable.getMessage());
                });
    }
}
