package com.hotbitmapgg.bilibili.adapter.pager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.hotbitmapgg.bilibili.module.home.bangumi.HomeBangumiFragment;
import com.hotbitmapgg.bilibili.utils.JsonUtil;

import org.json.JSONObject;

import java.util.List;

/**
 * 主界面Fragment模块Adapter
 */
public class HomePagerAdapter extends FragmentPagerAdapter {
    ///<summary>
    /// 从网络获取的类目对象列表
    /// 目录Json格式:[{"ID":xxx,"Name":"xxxx","Type":xxx,"IsDef":0},{}...]
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
    }
    @Override
    public Fragment getItem(int position)
    {
        if(_fragmentArr == null) return null;

        if(_fragmentArr[position] == null)
        {
            int cid = JsonUtil.GetInt(_catalogArr.get(position),"ID",0);
            _fragmentArr[position] = HomeBangumiFragment.newInstance(cid);
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
    ///<summary>返回要默认显示的Fragment下标</summary>
    public int getDefaultIndex()
    {
        if(_catalogArr == null||_catalogArr.size()==0) return 0;
        for (int i=0;i<_catalogArr.size();i++)
        {
            JSONObject item = _catalogArr.get(i);
            if(JsonUtil.GetInt(item,"IsDef",0) == 1) return  i;
        }
        return -1;
    }
}
