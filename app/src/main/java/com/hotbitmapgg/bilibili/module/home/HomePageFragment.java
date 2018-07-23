package com.hotbitmapgg.bilibili.module.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.flyco.tablayout.SlidingTabLayout;
import com.hotbitmapgg.bilibili.adapter.pager.HomePagerAdapter;
import com.hotbitmapgg.bilibili.base.RxLazyFragment;
import com.hotbitmapgg.bilibili.entity.AppContext;
import com.hotbitmapgg.bilibili.entity.ServerReply;
import com.hotbitmapgg.bilibili.entity.bangumi.BangumiAppIndexInfo;
import com.hotbitmapgg.bilibili.module.common.MainActivity;
import com.hotbitmapgg.bilibili.module.entry.GameCentreActivity;
import com.hotbitmapgg.bilibili.module.entry.OffLineDownloadActivity;
import com.hotbitmapgg.bilibili.module.search.TotalStationSearchActivity;
import com.hotbitmapgg.bilibili.network.RetrofitHelper;
import com.hotbitmapgg.bilibili.utils.CommonUtil;
import com.hotbitmapgg.bilibili.utils.Const;
import com.hotbitmapgg.bilibili.utils.JsonUtil;
import com.hotbitmapgg.bilibili.widget.CircleImageView;
import com.hotbitmapgg.bilibili.widget.banner.BannerEntity;
import com.hotbitmapgg.ohmybilibili.R;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


///<summary>首页模块:主界面</summary>
public class HomePageFragment extends RxLazyFragment {
    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    @BindView(R.id.sliding_tabs)
    SlidingTabLayout mSlidingTab;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    /*by="Aweigh" date="2018/7/18 17:48"
      MaterialSearchView控件默认的提示信息是@string/search_hint
      MaterialSearchViewStyle样式配置<item name="android:hint">@string/search_hint</item>
      详细:https://github.com/MiguelCatalan/MaterialSearchView
      该控件会进行自动过滤,而且匹配模式是返回以给定字符串作为起始位置的项即匹配"xxx%",
      目前无法修改中任意位置匹配,除非以后重写该控件
    */
    @BindView(R.id.search_view)
    MaterialSearchView mSearchView;
    @BindView(R.id.toolbar_user_avatar)
    CircleImageView mCircleImageView;

    public static HomePageFragment newInstance() {
        return new HomePageFragment();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_home_pager;
    }

    @Override
    public void finishCreateView(Bundle state) {
        setHasOptionsMenu(true);
        initToolBar();
        initSearchView();
        initViewPager();
    }

    private void initToolBar() {
        mToolbar.setTitle("");
        ((MainActivity) getActivity()).setSupportActionBar(mToolbar);
        mCircleImageView.setImageResource(R.drawable.ic_hotbitmapgg_avatar);
    }

    private void initSearchView() {
        //初始化SearchBar
        mSearchView.setVoiceSearch(false);
        mSearchView.setCursorDrawable(R.drawable.custom_cursor);
        mSearchView.setEllipsize(true);
        mSearchView.setSubmitOnClick(true);//添加事件当做提交事件处理
        //mSearchView.setSuggestions(getResources().getStringArray(R.array.query_suggestions));//设置搜索建议
        mSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            //键盘点击"搜索"事件触发,下来菜单点击事件并不会触发该事件
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.e(Const.LOG_TAG,"mSearchView.onQueryTextSubmit=>" + query);
                TotalStationSearchActivity.launch(getActivity(), query);//全站搜索界面
                return false;
            }
            //搜索框文本发生改变时触发,下来菜单点击事件也会触发该事件
            @Override
            public boolean onQueryTextChange(String newText) {
                if(!mSearchView.isSearchOpen()) return false;
                if(CommonUtil.isNullOrWhiteSpace(newText)) return false;

                //实时从服务器获取搜索建议,并设置到控件中
                RetrofitHelper.getZZXAPI().getSuggestions(newText).compose(bindToLifecycle())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            ServerReply reply = new ServerReply(response);
                            if(!reply.IsSucceed()){
                                Log.e(Const.LOG_TAG,"请求搜索建议失败," + reply.Message());
                                return;
                            }
                            List<String> suggestionLst = reply.GetStringList("suggestionArr");
                            int count = suggestionLst!=null && suggestionLst.size()>=0 ? suggestionLst.size() : -1;
                            if(count>=0) {
                                Log.d(Const.LOG_TAG,"mSearchView.onQueryTextChange("+newText+")=>server repsonse " + count + " record");
                                mSearchView.setSuggestions(suggestionLst.toArray(new String[count]));
                            }

                        },throwable -> {
                            Log.e(Const.LOG_TAG,"mSearchView.onQueryTextChange throwable=>" + throwable.getMessage());
                        });
                return false;
            }
        });
    }

    private void initViewPager() {
        HomePagerAdapter mHomeAdapter = new HomePagerAdapter(getChildFragmentManager(), AppContext.CatalogArr);
        mViewPager.setOffscreenPageLimit(5);
        mViewPager.setAdapter(mHomeAdapter);
        mSlidingTab.setViewPager(mViewPager);

        int defaultIndex = mHomeAdapter.getDefaultIndex();
        if(defaultIndex>=0)//设置默认显示项
            mViewPager.setCurrentItem(defaultIndex);//1
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        //创建页面菜单项(游戏中心,离线缓存,搜索)，对应menu_main.xml
        inflater.inflate(R.menu.menu_main, menu);
        // 设置SearchViewItemMenu
        MenuItem item = menu.findItem(R.id.id_action_search);//将搜索按钮和搜索页面绑定在一起
        mSearchView.setMenuItem(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
/*            case R.id.id_action_game:
                //游戏中心
                startActivity(new Intent(getActivity(), GameCentreActivity.class));
                break;*/
            case R.id.id_action_download:
                //离线缓存
                startActivity(new Intent(getActivity(), OffLineDownloadActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.navigation_layout)
    void toggleDrawer() {
        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).toggleDrawer();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == Activity.RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    mSearchView.setQuery(searchWrd, false);
                }
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    public boolean isOpenSearchView() {
        return mSearchView.isSearchOpen();
    }
    public void closeSearchView() {
        mSearchView.closeSearch();
    }
}
