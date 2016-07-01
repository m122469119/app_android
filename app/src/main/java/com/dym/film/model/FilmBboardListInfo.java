package com.dym.film.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by wbz360 on 2016/1/4.
 */
public class FilmBboardListInfo extends BaseRespInfo
{
    public ArrayList<BillboardModel> billboards=null;
    public static class BillboardModel implements Serializable{
        public int bdID=0;
        public String logo="";
        public String title="";
    }

}
