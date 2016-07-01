package com.dym.film.model;

import java.util.ArrayList;

/**
 * Created by wbz360 on 2016/1/4.
 */
public class FilmReviewListInfo extends BaseRespInfo
{
    public CinecismsList cinecisms=new CinecismsList();
    public String sum="";

    public static class CinecismModel{
        public long cinecismID=0;
        public String title="";
        public String summary="";
        public String srcMedia="";
        public String srcUrl="";
        public String srcScore="";
        public String logo="";
        public int opinion=0;
        public String createTime="";
        public Writer writer=new Writer();
    }
    public static class CinecismsList{

        public int positiveNum=0;
        public int negativeNum=0;
        public ArrayList<CinecismModel> list=new ArrayList<>();
    }
    public static class Writer{
        public long criticID=0;
        public String title="";
        public String name="";
        public String avatar="";
    }
}
