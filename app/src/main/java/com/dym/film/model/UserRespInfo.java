package com.dym.film.model;

public class UserRespInfo extends BaseRespInfo{
	
	public UserModel user=null;
	
	public static class UserModel{
		public int userID=0;
		public String name="";
		public int gender=0;
		public String mobile="";
		public String avatar="";
		public String token="";
		public String createTime="";
		public LocationModel location=null;
		
	}
	
	public static class LocationModel{
		public String province="";
		public String city="";
		public String district="";
		public String longitude="";
		public String latitude="";
	}
	
}
