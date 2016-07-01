package com.dym.film.model;

import java.util.ArrayList;

/**
 * Created by wbz360 on 2016/1/4.
 */
public class FilmHotListInfo extends BaseRespInfo
{
    public ArrayList<NewsModel> news=null;
    public static class NewsModel{
        public int newsID=0;
        public String title="";
        public String logo="";
        public String url="";
        public String quoteUrl="";
        public long publishTime=0;
    }

}
