package com.kdp.wanandroidclient.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.kdp.wanandroidclient.R;
import com.kdp.wanandroidclient.bean.ArticleBean;
import com.kdp.wanandroidclient.bean.BannerBean;
import com.kdp.wanandroidclient.common.Const;
import com.kdp.wanandroidclient.event.Event;
import com.kdp.wanandroidclient.inter.OnArticleListItemClickListener;
import com.kdp.wanandroidclient.manager.UserInfoManager;
import com.kdp.wanandroidclient.ui.adapter.ArticleListAdapter;
import com.kdp.wanandroidclient.ui.adapter.BannerAdapter;
import com.kdp.wanandroidclient.ui.adapter.BaseListAdapter;
import com.kdp.wanandroidclient.ui.base.BaseAbListFragment;
import com.kdp.wanandroidclient.ui.logon.LogonActivity;
import com.kdp.wanandroidclient.ui.tree.TreeActivity;
import com.kdp.wanandroidclient.ui.web.WebViewActivity;
import com.kdp.wanandroidclient.utils.LogUtils;
import com.kdp.wanandroidclient.utils.ToastUtils;
import com.kdp.wanandroidclient.widget.BannerViewPager;

import java.util.ArrayList;
import java.util.List;


/**
 * 首页文章
 * author: 康栋普
 * date: 2018/2/12
 */

public class HomeFragment extends BaseAbListFragment<HomePresenter, HomeContract.IHomeView, ArticleBean> implements HomeContract.IHomeView, OnArticleListItemClickListener {
    private int id;//文章id
    private int position;
    private List<BannerBean> mBannerList = new ArrayList<>();
    private BannerViewPager mViewPager;
    private BannerAdapter mBannerAdapter;

    @Override
    protected HomePresenter createPresenter() {
        return new HomePresenter();
    }

    @Override
    protected boolean isCanLoadMore() {
        return true;
    }

    //初始化HeaderView
    @Override
    protected View initHeaderView() {
        View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.main_header_banner, mRecyclerView, false);
        mViewPager = (BannerViewPager) headerView.findViewById(R.id.viewPager);
        return headerView;
    }


    //设置Banner选中item
    private void setCurrentItem(final int position) {
        mViewPager.setCurrentItem(position, false);
    }

    //加载列表数据
    @Override
    protected void loadDatas() {
        mPresenter.getHomeList();
    }

    @Override
    protected BaseListAdapter getListAdapter() {
        return new ArticleListAdapter(this, Const.LIST_TYPE.HOME);
    }

    //Banner数据
    @Override
    public void setBannerData(List<BannerBean> banner) {
        mBannerList.clear();
        mBannerList.addAll(banner);
    }

    //列表数据
    @Override
    public void setData(List<ArticleBean> data) {
        mListData.addAll(data);
    }

    //显示内容
    @Override
    public void showContent() {
        notifyDatas();
        super.showContent();
    }

    //刷新所有数据
    public void notifyDatas() {
        if (mBannerAdapter == null) {
            mBannerAdapter = new BannerAdapter(mBannerList);
            mViewPager.setAdapter(mBannerAdapter);
            //设置预加载两个页面
            mViewPager.setOffscreenPageLimit(2);
            setCurrentItem(1000 * mBannerList.size());
//            mViewPager.start();
        }
        mBannerAdapter.notifyDatas(mBannerList);
    }


    //收藏结果
    @Override
    public void collect(boolean isCollect, String result) {
        notifyItemData(isCollect, result);
    }

    //刷新单条Item
    private void notifyItemData(boolean isCollect, String result) {
        mListData.get(position).setCollect(isCollect);
        position++;
        mListAdapter.notifyItemDataChanged(position, mRecyclerView);
        ToastUtils.showToast(getActivity(), result);
    }

    //文章id
    @Override
    public int getArticleId() {
        return id;
    }

    //进入详情
    @Override
    public void onItemClick(ArticleBean bean) {
        Intent intent = new Intent(getActivity(), WebViewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Const.BUNDLE_KEY.OBJ, bean);
        bundle.putInt(Const.BUNDLE_KEY.COLLECT_TYPE, 1);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onDeleteCollectClick(int position, int id, int originId) {

    }

    //收藏click
    @Override
    public void onCollectClick(int position, int id) {
        LogUtils.e(id+"");
        if (!UserInfoManager.isLogin())
            startActivity(new Intent(getActivity(), LogonActivity.class));
        this.position = position;
        this.id = id;
        if (mListData.get(this.position).isCollect())
            mPresenter.unCollectArticle();
        else
            mPresenter.collectArticle();
    }

    //分类click
    @Override
    public void onTreeClick(int chapterId, String chapterName) {
        Intent intent = new Intent(getActivity(), TreeActivity.class);
        intent.putExtra(Const.BUNDLE_KEY.INTENT_ACTION_TYPE, Const.BUNDLE_KEY.INTENT_ACTION_LIST);
        intent.putExtra(Const.BUNDLE_KEY.CHAPTER_ID, chapterId);
        intent.putExtra(Const.BUNDLE_KEY.CHAPTER_NAME, chapterName);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewPager.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewPager.stop();
    }

    @Override
    protected void receiveEvent(Object object) {
        Event mEvent = (Event) object;
        if (mEvent.type == Event.Type.ITEM) {
            ArticleBean bean = (ArticleBean) mEvent.object;
            for (int i = 0; i < mListData.size(); i++) {
                if (bean.equals(mListData.get(i))) {
                    position = i;
                    notifyItemData(bean.isCollect(), getString(R.string.collect_success));
                }
            }
        } else {
            refreshData();
        }
    }

    @Override
    protected String registerEvent() {
        return Const.EVENT_ACTION.REFRESH_DATA;
    }


}
