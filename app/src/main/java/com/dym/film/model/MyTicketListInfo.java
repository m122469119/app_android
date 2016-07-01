package com.dym.film.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by wbz360 on 2016/1/4.
 */
public class MyTicketListInfo extends BaseRespInfo
{
    public ArrayList<OrderModel> orders=null;
    public static class OrderModel implements Serializable{
        public int orderID=0;
        public String orderCode="";
        public String film="";
        public String buyTime="";
        public int count=0;
        public int amount=0;
        public int status=0;
    }

}
