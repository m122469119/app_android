package com.dym.film.ui.citylist.model;


import com.dym.film.ui.citylist.widget.ContactItemInterface;

import java.util.ArrayList;

public class CityItem implements ContactItemInterface
{
	private String nickName;
	private String fullName;
	private ArrayList<String> regionList;

	public CityItem(String nickName, String fullName,ArrayList<String> regionList)
	{
		super();
		this.nickName = nickName;
		this.setFullName(fullName);
		this.setRegionList(regionList);

	}

	@Override
	public String getItemForIndex()
	{
//		if (fullName.equals("热门")) {
//			return "1" + fullName;
//		}
		return fullName;
	}

	@Override
	public String getDisplayInfo()
	{
		return nickName;
	}

	@Override
	public ArrayList<String> getDisplayList() {
		return regionList;
	}

	public String getNickName()
	{
		return nickName;
	}

	public void setNickName(String nickName)
	{
		this.nickName = nickName;
	}

	public String getFullName()
	{
		return fullName;
	}

	public void setFullName(String fullName)
	{
		this.fullName = fullName;
	}

	public void setRegionList(ArrayList<String> regionList) {
		if(regionList==null||regionList.size()==0){
			this.regionList=new ArrayList<>();
		}else
		{
			this.regionList = regionList;
		}
	}
}
