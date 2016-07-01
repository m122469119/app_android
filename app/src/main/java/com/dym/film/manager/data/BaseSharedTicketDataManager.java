package com.dym.film.manager.data;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/12/7
 */

import com.dym.film.manager.NetworkManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * 晒票数据管理的基类
 */
public class BaseSharedTicketDataManager extends BaseDataManager<NetworkManager.SharedTicketRespModel>
{
    public final static String TAG = "BaseSTDM";

    /**
     * 需要刷新头像
     */
    protected boolean mNeedRefreshAvatars = false;

    protected List<Integer> mSupportStateChangedPositions = new ArrayList<>();

    protected HashMap<Long, Integer> mHashMap = new HashMap<>();

    @Override
    public void insert(int position, NetworkManager.SharedTicketRespModel sharedTicketRespModel)
    {
        /**
         * 屏蔽掉insert, 因为不能打乱顺序， 否则 HashMap里面的数据无效
         */
    }

    @Override
    public void append(NetworkManager.SharedTicketRespModel sharedTicketRespModel)
    {
        mHashMap.put(sharedTicketRespModel.stubID, getSize());
        mListData.add(sharedTicketRespModel);
    }

    @Override
    public void set(int position, NetworkManager.SharedTicketRespModel sharedTicketRespModel)
    {
        if (isValidPosition(position)) {
            mHashMap.put(sharedTicketRespModel.stubID, position);
            mListData.set(position, sharedTicketRespModel);
        }
    }

    @Override
    public void setAll(Collection<? extends NetworkManager.SharedTicketRespModel> list)
    {
        mHashMap.clear();
        mListData.clear();
        if (list == null || list.isEmpty()) {
            return;
        }

        for (NetworkManager.SharedTicketRespModel model : list) {
            append(model);
        }
    }

    @Override
    public void remove(int position)
    {
        if (isValidPosition(position)) {
            mHashMap.remove(mListData.get(position).stubID);
            mListData.remove(position);
        }
    }

    @Override
    public void clear()
    {
        mHashMap.clear();
        super.clear();
    }

    @Override
    public synchronized void onDestroy()
    {
        clear();
        mSupportStateChangedPositions.clear();
        super.onDestroy();
    }

    //    /**
//     * 是否需要刷新头像
//     * @return
//     */
//    public boolean needRefreshAvatars()
//    {
//        return mNeedRefreshAvatars;
//    }
//
//    /**
//     * 设置是否需要刷新头像
//     * @param need
//     */
//    public void setNeedRefreshAvatars(boolean need)
//    {
//        mNeedRefreshAvatars = need;
//    }
//
//    /**
//     * 判断是否需要更新点赞的状态
//     * @return
//     */
//    public boolean needRefreshSupportState()
//    {
//        return mSupportStateChangedPositions.size() != 0;
//    }
//
//    /**
//     * 获取
//     * @return
//     */
//    public List<Integer> getSupportedPosList()
//    {
//        return mSupportStateChangedPositions;
//    }
//
//
//    /**
//     * 删除一个位置
//     * @param pos
//     */
//    public synchronized void removeSupportedPosition(int pos)
//    {
//        mSupportStateChangedPositions.remove(Integer.valueOf(pos));
//    }
//
//    /**
//     * 根据位置删除
//     * @param index
//     */
//    public synchronized void removeSupportedPositionIndexOf(int index)
//    {
//        mSupportStateChangedPositions.remove(index);
//    }
//
//    public synchronized void clearSupportedPosition()
//    {
//        mSupportStateChangedPositions.clear();
//    }

    public synchronized void supportTicketById(long id, int num, boolean needRefresh)
    {
        if (mHashMap.containsKey(id)) {
            int pos = mHashMap.get(id);
            supportTicketByPos(pos, num, needRefresh);
        }
    }

    /**
     * 改变点赞数据
     */
    public synchronized void supportTicketByPos(int pos, int num, boolean needRefresh)
    {
        NetworkManager.SharedTicketRespModel model = get(pos);

        model.supported = 1;
        model.supportNum = num;

        if (needRefresh) {
            mSupportStateChangedPositions.add(pos);
        }

        /**
         * 需要和MainDataManager同步数据
         */
        if (!(this instanceof MainSharedTicketDataManager)) {
            MainSharedTicketDataManager.mInstance.supportTicketById(model.stubID, num, true);
        }
    }

    public synchronized void unSupportTicketById(long id, int num, boolean needRefresh)
    {
        if (mHashMap.containsKey(id)) {
            unSupportTicketByPos(mHashMap.get(id), num, needRefresh);
        }
    }

    /**
     * 取消点赞
     */
    public synchronized void unSupportTicketByPos(int pos, int num, boolean needRefresh)
    {
        NetworkManager.SharedTicketRespModel model = get(pos);

        model.supported = 0;
        model.supportNum = num;

        if (needRefresh) {
            mSupportStateChangedPositions.add(pos);
        }

        /**
         * 需要和MainDataManager同步数据
         */
        if (!(this instanceof MainSharedTicketDataManager)) {
            MainSharedTicketDataManager.mInstance.unSupportTicketById(model.stubID, num, true);
        }
    }

}
