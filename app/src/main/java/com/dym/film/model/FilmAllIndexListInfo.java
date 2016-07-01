package com.dym.film.model;

import java.util.ArrayList;

/**
 * Created by wbz360 on 2016/1/4.
 */
public class FilmAllIndexListInfo extends BaseRespInfo
{
    public AssessModel assess=null;

    public static class AssessModel{
        public CinecismModel cinecism=null;
        public BoxModel box=null;
        public StubModel stub=null;
    }
    public static class CinecismModel{
        public int criticNum=0;
        public ArrayList<CriticsModel> critics=null;
    }
    public static class CriticsModel{
        public int criticID=0;
        public int opinion=0;
        public String name="";
        public String avatar="";
        public String title="";
    }
    public static class StubModel{
        public String stubIndex="";
        public int positiveNum=0;
        public int negativeNum=0;
    }
    public static class BoxModel{
        public String sum="";
        public ArrayList<ShardsModel> shards=null;
    }
    public static class ShardsModel{
        public String date="";
        public float box=0;
    }

}
