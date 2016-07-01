package com.dym.film.model;

import java.util.ArrayList;

/**
 * Created by wbz360 on 2016/1/4.
 */
public class BannerListInfo extends BaseRespInfo
{
    public ArrayList<BannerModel> banners=null;
    public static class BannerModel{
        public String img="";
        public String url="";
    }

}
