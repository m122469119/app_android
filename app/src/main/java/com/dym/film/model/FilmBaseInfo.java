package com.dym.film.model;

import java.util.ArrayList;

/**
 * Created by wbz360 on 2016/1/4.
 */
public class FilmBaseInfo extends BaseRespInfo
{
    public FilmModel film=null;

    public static class FilmModel{
        public int filmID=0;
        public String name="";
        public String country="";
        public String releaseDate="";
        public String director="";
        public int length=0;
        public String cast="";
        public String summary="";
        public String post="";
        public int followed=0;
        public String dymIndex="";
        public ArrayList<String> trailers=null;
        public ArrayList<String> photos=null;
        public int status=0;
        public int trailerSum=0;
        public int photoSum=0;
        public int sellingTicket=0;
    }

}
