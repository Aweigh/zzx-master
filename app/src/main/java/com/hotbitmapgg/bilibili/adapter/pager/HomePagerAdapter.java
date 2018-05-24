package com.hotbitmapgg.bilibili.adapter.pager;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.hotbitmapgg.bilibili.entity.ServerReply;
import com.hotbitmapgg.bilibili.entity.video.VideoCommentInfo;
import com.hotbitmapgg.bilibili.module.home.attention.HomeAttentionFragment;
import com.hotbitmapgg.bilibili.module.home.bangumi.HomeBangumiFragment;
import com.hotbitmapgg.bilibili.module.home.discover.HomeDiscoverFragment;
import com.hotbitmapgg.bilibili.module.home.live.HomeLiveFragment;
import com.hotbitmapgg.bilibili.module.home.recommend.HomeRecommendedFragment;
import com.hotbitmapgg.bilibili.module.home.region.HomeRegionFragment;
import com.hotbitmapgg.bilibili.network.RetrofitHelper;
import com.hotbitmapgg.bilibili.network.auxiliary.Const;
import com.hotbitmapgg.bilibili.utils.JsonUtil;
import com.hotbitmapgg.ohmybilibili.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 主界面Fragment模块Adapter
 */
public class HomePagerAdapter extends FragmentPagerAdapter {
    ///<summary>
    /// 从网络获取的类目对象列表
    /// 目录Json格式:[{"ID":xxx,"Name":"xxxx","Type":xxx},{}...]
    ///</summary>
    private List<JSONObject> _catalogArr = null;
    private Fragment[] _fragmentArr = null;

    public HomePagerAdapter(FragmentManager fm,List<JSONObject> catalogArr)
    {
        super(fm);
        if(catalogArr!=null && catalogArr.size()>0)
        {
            _catalogArr = catalogArr;
            _fragmentArr = new Fragment[catalogArr.size()];
        }
          /*by="Aweigh" date="2018/4/27 14:58"
            TITLES:从资源获取顶部导航字符串数组
            _fragmentArr:是导航栏对应的fragment数组
          */
        //TITLES = context.getResources().getStringArray(R.array.sections);
        //_fragmentArr = new Fragment[TITLES.length];
    }

    @Override
    public Fragment getItem(int position) {
        if (_fragmentArr[position] == null) {
          switch (position) {
            case 0://直播
              _fragmentArr[position] = HomeLiveFragment.newIntance();
              break;
            case 1://推荐
              _fragmentArr[position] = HomeRecommendedFragment.newInstance();
              break;
            case 2://番剧
              _fragmentArr[position] = HomeBangumiFragment.newInstance();
              break;
            case 3://分区
              _fragmentArr[position] = HomeRegionFragment.newInstance();
              break;
            case 4://关注
              _fragmentArr[position] = HomeAttentionFragment.newInstance();
              break;
            case 5://发现
              _fragmentArr[position] = HomeDiscoverFragment.newInstance();
              break;
            default:
              break;
          }
        }
        return _fragmentArr[position];
    }


    @Override
    public int getCount() {
        if(_catalogArr==null) return 0;
        return _catalogArr.size();//TITLES.length;
    }
    @Override
    public CharSequence getPageTitle(int position) {
        if(_catalogArr==null) return "";//TITLES[position];
        return  JsonUtil.GetString(_catalogArr.get(position),"Name","");
    }
}
