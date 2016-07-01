package com.dym.film.manager;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/9
 */

import android.content.Context;
import android.util.Log;

import com.dym.film.entity.DaoMaster;
import com.dym.film.entity.DaoSession;

/**
 * 单例模式, 用于管理和获取GreenDao ORM
 */
public class DatabaseManager
{
    public final static String TAG = "DatabaseManager";

    private final static String DB_NAME = "dym-db";

    private final static DatabaseManager mInstance = new DatabaseManager();
    public static DatabaseManager getInstance()
    {
        return mInstance;
    }

    private Context mContext = null;

    private DaoMaster mDaoMaster = null;

    public void initializeDaoMaster(Context context)
    {
        mContext = context;

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, DB_NAME, null);
        mDaoMaster = new DaoMaster(helper.getWritableDatabase());
    }

    /**
     * 获取DaoMaster
     * @return
     */
    public DaoMaster getDaoMaster()
    {
        if (mDaoMaster == null) {
            if (mContext == null) {
                //Loge(TAG, "DatabaseManager Context is Null");
                return null;
            }
            else {
                DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(mContext, DB_NAME, null);
                mDaoMaster = new DaoMaster(helper.getWritableDatabase());
            }
        }

        return mDaoMaster;
    }

    /**
     * 获取DaoSession
     * @return
     */
    public DaoSession getDaoSession()
    {
        DaoMaster master = getDaoMaster();
        if (master == null) {
            //Loge(TAG, "DaoMaster is null, get Dao Session Failed!");
            return null;
        }

        return master.newSession();
    }
}
