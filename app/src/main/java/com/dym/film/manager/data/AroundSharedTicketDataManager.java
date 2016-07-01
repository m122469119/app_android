package com.dym.film.manager.data;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/26
 */

import com.dym.film.manager.NetworkManager;
import com.dym.film.views.AroundSharedTicket;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 管理周围晒票的数据
 */
public class AroundSharedTicketDataManager extends BaseSharedTicketDataManager
{
    public final static String TAG = "ASTDataManager";

    public final static AroundSharedTicketDataManager mInstance = new AroundSharedTicketDataManager();

    public ArrayList<AroundSharedTicket> mAroundData = new ArrayList<>();


    @Override
    public void append(NetworkManager.SharedTicketRespModel sharedTicketRespModel)
    {
        AroundSharedTicket ticket = new AroundSharedTicket(sharedTicketRespModel);
        ticket.mPersonIndex = getSize();
        mAroundData.add(ticket);

        super.append(sharedTicketRespModel);
    }

    @Override
    public void set(int position, NetworkManager.SharedTicketRespModel sharedTicketRespModel)
    {
        if (isValidPosition(position)) {
            AroundSharedTicket ticket = new AroundSharedTicket(sharedTicketRespModel);

            mAroundData.set(position, ticket);

            super.set(position, sharedTicketRespModel);
        }
    }

    @Override
    public void setAll(Collection<? extends NetworkManager.SharedTicketRespModel> list)
    {
        mAroundData.clear();
        super.setAll(list);
    }

    @Override
    public void remove(int position)
    {
        if (isValidPosition(position)) {
            mAroundData.remove(position);
            super.remove(position);
        }
    }

    @Override
    public void clear()
    {
        super.clear();
        mAroundData.clear();
    }

    @Override
    public synchronized void onDestroy()
    {
        clear();
        super.onDestroy();
    }

    public AroundSharedTicket getAroundSharedTicket(int pos)
    {
        if (isValidPosition(pos)) {
            return mAroundData.get(pos);
        }

        return null;
    }
}
