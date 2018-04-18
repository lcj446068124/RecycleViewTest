package com.deitel.recycleviewtest.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import com.deitel.recycleviewtest.R;
import com.deitel.recycleviewtest.util.ConfigUtils;
import com.deitel.recycleviewtest.util.SnackbarUtils;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout;

import java.util.List;

import butterknife.BindView;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

public abstract class FixedImageFragment<P extends PVContract.Presenter, A extends BaseAdapter>
        extends BaseFragment implements PVContract.View {


    @BindView(R.id.twinkling_refresh_layout)
    TwinklingRefreshLayout mTwinklingRefreshLayout;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private boolean isRunning;

    private A mAdapter;
    private String mBaseUrl;
    private P mPresenter;
    private SGSpacingItemDecoration mItemDecoration;
    private StaggeredGridLayoutManager mLayoutManager;

    private int mSpanCount;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mBaseUrl = bundle.getString("base_url");
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        init();
        initRecyclerView();
        initRefreshLayout();
    }


    protected abstract P getPresenter();

    protected abstract A getAdapter();

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_recycler_view_with_refresh_layout;
    }

    private void initRecyclerView() {
        mRecyclerView.setItemAnimator(new LandingAnimator());
        mLayoutManager = new StaggeredGridLayoutManager(mSpanCount, StaggeredGridLayoutManager
                .VERTICAL);
        mLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        mItemDecoration = new SGSpacingItemDecoration(mSpanCount, mContext.getResources()
                .getDimensionPixelSize(R.dimen.v4dp));
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.removeItemDecoration(mItemDecoration);
        mRecyclerView.addItemDecoration(mItemDecoration);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void scrollToTop() {
        mRecyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void changeRecyclerViewSpanCount() {
        int spanCount = mLayoutManager.getSpanCount();
        if (spanCount >= ConfigUtils.getSpanCountConfig(aContext)) {
            spanCount = 1;
        } else {
            spanCount++;
        }
        ConfigUtils.saveCurrentSpanCount(aContext, spanCount);
        mLayoutManager.setSpanCount(spanCount);
        mItemDecoration.setSpanCount(spanCount);
    }

    private void init() {
        isRunning = false;
        mSpanCount = ConfigUtils.getCurrentSpanCount(aContext);
        mPresenter = getPresenter();
        mAdapter = getAdapter();
    }

    private void initRefreshLayout() {
        ProgressLayout header = new ProgressLayout(mContext);
        header.setColorSchemeResources(
                R.color.ThemeRed,
                R.color.ThemePink,
                R.color.ThemePurple,
                R.color.ThemeDeepPurple,
                R.color.ThemeIndigo,
                R.color.ThemeBlue,
                R.color.ThemeLightBlue,
                R.color.ThemeCyan,
                R.color.ThemeTeal,
                R.color.ThemeGreen,
                R.color.ThemeLightGreen,
                R.color.ThemeLime,
                R.color.ThemeYellow,
                R.color.ThemeAmber,
                R.color.ThemeOrange,
                R.color.ThemeDeepOrange,
                R.color.ThemeBrown,
                R.color.ThemeGrey,
                R.color.ThemeBlueGrey,
                R.color.ThemeBiliBili,
                R.color.ThemeBlack,
                R.color.ThemeDarkBlack
        );
        mTwinklingRefreshLayout.setFloatRefresh(true);
        mTwinklingRefreshLayout.setOverScrollRefreshShow(false);
        mTwinklingRefreshLayout.setMaxHeadHeight(mContext.getResources().getDimension(R.dimen
                .v48dp));
        mTwinklingRefreshLayout.setOverScrollHeight(mContext.getResources().getDimension(R.dimen
                .v42dp));
        mTwinklingRefreshLayout.setHeaderView(header);
        mTwinklingRefreshLayout.setHeaderHeight(mContext.getResources().getDimension(R.dimen
                .v36dp));
        mTwinklingRefreshLayout.startRefresh();
        mTwinklingRefreshLayout.setEnableLoadmore(false);
        mTwinklingRefreshLayout.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
                if (isRunning) return;
                isRunning = true;
                if (mAdapter != null) {
                    mAdapter.clear();
                }
                if (mPresenter != null) {
                    mPresenter.get(mBaseUrl);
                }
            }
        });
    }


    @Override
    public void success(Object result) {
        List beans = (List) result;
        if (beans == null || beans.size() == 0) {
            SnackbarUtils.create(mContext, "没有更多记录了");
        } else {
            mAdapter.add(beans);
        }
        endTask();
    }

    private void endTask() {
        isRunning = false;
        mTwinklingRefreshLayout.finishRefreshing();
    }


    @Override
    public void error(String error) {
        endTask();
        SnackbarUtils.create(mContext, error);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.unsubscribe();
        }
    }
}
