package com.dym.film.model;

import java.util.ArrayList;

/**
 * Created by wbz360 on 2016/1/4.
 */
public class FilmListInfo extends BaseRespInfo
{
    public FilmList films=new FilmList();
    public static class FilmList{
        public int sum=0;
        public ArrayList<FilmModel> list=new ArrayList<>();
    }
    public static class FilmModel{
        public int filmID=0;
        public String name="";
        public String country="";
        public String releaseDate="";
        public String director="";
        public String cast="";
        public String sum="";
        public String post="";
        public String dymIndex="";
        public String stubIndex="";
        public int cinecismNum=0;
        public int stubNum=0;
        public int status=0;
        public String digest="";
        public int  hasShown=0;
        public int  sellingTicket=0;
        public CinecismModel rcmmCinecism=null;
        public StubModel rcmmStub =null;
        public NewsModel rcmmNews =null;
    }
    public static class CinecismModel{
        public String title="";
        public long  cinecismID=0;
    }
    public static class StubModel{
        public String comment="";
        public long  stubID=0;
    }
    public static class NewsModel{
        public String title="";
        public long  newsID=0;
    }
}
