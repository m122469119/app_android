package com.dym.film.model;

import com.dym.film.manager.NetworkManager;

import java.util.ArrayList;

/**
 * Created by wbz360 on 2016/1/4.
 */
public class SharedTicketListInfo extends BaseRespInfo
{
    public Stubs stubs=null;
    public static class Stubs{
        public int positiveNum=0;
        public int negativeNum=0;
        public ArrayList<NetworkManager.SharedTicketRespModel> list=null;
    }

}
