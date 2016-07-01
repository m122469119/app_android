package com.dym.film.manager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xusoku on 2015/12/2.
 */
public class SerachResultDataManager {
    public final static String TAG = "FRDataManager";

    private final static List<Item> mListData = new ArrayList<>();

    public synchronized static void insert(int position, String model)
    {
        if (position < 0) {
            position = 0;
        }

        if (position > mListData.size()) {
            position = mListData.size();
        }

        mListData.add(position, new Item(model));
    }

    public synchronized static void append(String model)
    {
        insert(mListData.size(), model);
    }

    public synchronized static void setData(int position, String model)
    {
        if (position < 0 || position >= mListData.size()) {
            return;
        }

        mListData.get(position).mModel = model;
    }

    public synchronized static void remove(int position)
    {
        if (position < 0 || position >= mListData.size()) {
            return;
        }

        mListData.remove(position);
    }

    public synchronized static void setData(int position, Item item)
    {
        if (position < 0 || position >= mListData.size()) {
            return;
        }

        mListData.set(position, item);
    }

    public synchronized static boolean isValidPosition(int pos)
    {
        return pos >= 0 && pos < mListData.size();
    }


    public synchronized static int getSize()
    {
        return mListData.size();
    }

    public synchronized static Item getItem(int pos)
    {
        if (pos < 0 || pos >= mListData.size()) {
            return null;
        }

        return mListData.get(pos);
    }

    public synchronized static String getModel(int pos)
    {
        if (pos < 0 || pos >= mListData.size()) {
            return null;
        }

        return mListData.get(pos).mModel;
    }

    public static class Item
    {
        public String mModel = null;
        public boolean mIsLoading = false;

        public Item(String model)
        {
            mModel = model;
            mIsLoading = false;
        }
    }
}
