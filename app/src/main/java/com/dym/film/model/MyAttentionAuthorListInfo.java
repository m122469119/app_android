package com.dym.film.model;

import java.util.ArrayList;

/**
 * Created by wbz360 on 2016/1/4.
 */
public class MyAttentionAuthorListInfo extends BaseRespInfo
{
    public ArrayList<CriticModel> critics=null;
    public static class CriticModel{
        public int criticID=0;
        public String title="";
        public String name="";
        public String avatar="";
        public int cinecismNum=0;
        public ArrayList<CinecismModel> cinecism=null;
    }
    public static class CinecismModel{
        public int cinecismID=0;
        public String title="";
        public String createTime="";
        public int opinion=0;
        public ArrayList<FilmModel> film=null;
    }
    public static class FilmModel{
        public int filmID=0;
        public String name="";
        public String post="";
    }
}
