package com.dym.film.entity;

public class UserMessage implements java.io.Serializable
{

    private long msgID;
    private int notifyID;
    private String title;
    private String content;
    private long pushTime;
    private int readed;
    private int category;
    private String url;
    private long cinecismID;
    private long filmID;
    private long stubID;


    public long getMsgID()
    {
        return msgID;
    }

    public void setMsgID(long msgID)
    {
        this.msgID = msgID;
    }

    public int getNotifyID()
    {
        return notifyID;
    }

    public void setNotifyID(int notifyID)
    {
        this.notifyID = notifyID;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public long getPushTime()
    {
        return pushTime;
    }

    public void setPushTime(long pushTime)
    {
        this.pushTime = pushTime;
    }

    public int getReaded()
    {
        return readed;
    }

    public void setReaded(int readed)
    {
        this.readed = readed;
    }

    public int getCategory()
    {
        return category;
    }

    public void setCategory(int category)
    {
        this.category = category;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public long getCinecismID()
    {
        return cinecismID;
    }

    public void setCinecismID(long cinecismID)
    {
        this.cinecismID = cinecismID;
    }

    public long getFilmID()
    {
        return filmID;
    }

    public void setFilmID(long filmID)
    {
        this.filmID = filmID;
    }

    public long getStubID()
    {
        return stubID;
    }

    public void setStubID(long stubID)
    {
        this.stubID = stubID;
    }
}
