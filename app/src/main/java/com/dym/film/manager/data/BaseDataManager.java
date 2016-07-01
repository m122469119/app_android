package com.dym.film.manager.data;

import com.dym.film.adapter.base.BaseSimpleRecyclerAdapter;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/12/7
 */
public class BaseDataManager<Model> extends BaseSimpleRecyclerAdapter.BaseSimpleDataDelegate<Model>
{
    protected boolean mNeedRefreshAll = false;

    /**
     * 判断是否刷新所有的数据
     *
     * @return
     */
    public boolean needRefreshAll()
    {
        return mNeedRefreshAll;
    }

    /**
     * 设置是否刷新所有的数据
     *
     * @param need
     */
    public void setNeedRefreshAll(boolean need)
    {
        mNeedRefreshAll = need;
    }

    public synchronized void onDestroy()
    {
        mListData.clear();
    }
}
