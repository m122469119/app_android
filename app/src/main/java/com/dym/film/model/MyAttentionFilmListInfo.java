package com.dym.film.model;

import java.util.ArrayList;

/**
 * Created by wbz360 on 2016/1/4.
 */
public class MyAttentionFilmListInfo extends BaseRespInfo
{
    public ArrayList<FilmModel> films=null;
    public static class FilmModel{
        public int filmID=0;
        public String name="";
        public String country="";
        public String releaseDate="";
        public String director="";
        public String cast="";
        public String post="";
        public String stubIndex="";
        public String dymIndex="";
        public int status=0;
    }

}
