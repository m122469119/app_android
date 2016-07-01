package com.dym.film.model;

import java.util.ArrayList;

/**
 * Created by wbz360 on 2016/1/4.
 */
public class FilmVideoEntity extends BaseRespInfo
{
    public ArrayList<FilmVideoInfo> trailers = new ArrayList<FilmVideoInfo>();

    public static class FilmVideoInfo
    {
        public String title = "";
        public String imgUrl = "";
        public String videoUrl = "";
        public int duration = 0;
    }
}
