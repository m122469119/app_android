package com.dym.film.model;

import java.util.ArrayList;

/**
 * Created by wbz360 on 2016/1/4.
 */
public class MyBaseInfo extends BaseRespInfo
{
    public MyInfo info=null;
    public static class MyInfo{
        public Msg message;
        public Following following;
        public Ticket ticket;
        public MyStubs myStubs;
    }
    public static class Msg
    {
        public int msgNum = 0;
        public int hasNew = 0;
    }
    public static class Following
    {
        public int followingNum = 0;
        public String activeProfile = "";
    }
    public static class MyStubs
    {
        public int myStubNum = 0;
        public int newSupported =0;
        public ArrayList<StubModel> stubs=null;
    }
    public static class StubModel
    {
        public long stubID = 0;
        public String stubImageUrl = "";
        public int opinion =0;
    }
    public static class Ticket
    {
        public int ticketNum = 0;
    }
}
