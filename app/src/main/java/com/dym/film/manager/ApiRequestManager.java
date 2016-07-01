package com.dym.film.manager;

import android.content.Context;

import com.dym.film.application.ConfigInfo;
import com.dym.film.application.UserInfo;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.model.BannerListInfo;
import com.dym.film.model.BaseRespInfo;
import com.dym.film.model.FilmAllIndexListInfo;
import com.dym.film.model.FilmBaseInfo;
import com.dym.film.model.FilmBboardListInfo;
import com.dym.film.model.FilmHotListInfo;
import com.dym.film.model.FilmListInfo;
import com.dym.film.model.FilmPostEntity;
import com.dym.film.model.FilmRankingListInfo;
import com.dym.film.model.FilmReviewListInfo;
import com.dym.film.model.FilmVideoEntity;
import com.dym.film.model.MyAttentionAuthorListInfo;
import com.dym.film.model.MyAttentionFilmListInfo;
import com.dym.film.model.MyBaseInfo;
import com.dym.film.model.MySharedTicketListInfo;
import com.dym.film.model.MyTicketListInfo;
import com.dym.film.model.SharedTicketListInfo;
import com.dym.film.model.ShowFilmListInfo;
import com.dym.film.model.UserRespInfo;
import com.dym.film.utils.CipherUtils;
import com.dym.film.utils.RandomUtils;
import com.dym.film.utils.RegexUtils;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;


public class ApiRequestManager
{

	private Context mContext;
	private static ApiRequestManager mInstance = null;

	public static ApiRequestManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new ApiRequestManager(context);
		}
		return mInstance;
	}
	private ApiRequestManager(Context context) {
		this.mContext = context;
	}

	/*获取注册验证码请求*/
	public void getRegisterVcode(String mobile,AsyncHttpHelper.ResultCallback<BaseRespInfo> callback)

	{
		String url = ConfigInfo.BASE_URL+"/native/register/vcode";
		JSONObject jObject = new JSONObject();
		try {
			String seed= RandomUtils.getRandomNumbersAndLetters(8);
			jObject.put("mobile", mobile);
			jObject.put("sign", CipherUtils.HmacSHA256(seed,ConfigInfo.hmackey));
			jObject.put("seed", seed);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		Type type = new TypeToken<BaseRespInfo>() {}.getType();
		AsyncHttpHelper.postRequest(mContext,url,jObject,type,callback);
	}

	/*验证注册验证码的有效性*/
	public void setRegisterVcode(String mobile, String vcode,AsyncHttpHelper.ResultCallback<BaseRespInfo> callback)
	{
		String url = ConfigInfo.BASE_URL+"/native/register/vcode/validate";
		JSONObject jObject = new JSONObject();
		try {
			jObject.put("mobile", mobile);
			jObject.put("vCode", vcode);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		Type type = new TypeToken<BaseRespInfo>() {}.getType();
		AsyncHttpHelper.postRequest(mContext,url,jObject,type,callback);
	}

	/*设置登录密码*/
	public void setRegisterPwd(String mobile, String pwd, String jid,AsyncHttpHelper.ResultCallback<UserRespInfo> callback)
	{
		String url = ConfigInfo.BASE_URL+"/native/register/info";
		JSONObject jObject = new JSONObject();
		try {
			JSONObject jLocal = new JSONObject();
			jLocal.put("province", UserInfo.province);
			jLocal.put("city", UserInfo.city);
			jLocal.put("district", UserInfo.district);
			jLocal.put("latitude", UserInfo.latitude);
			jLocal.put("longitude", UserInfo.longitude);

			jObject.put("mobile", mobile);
			jObject.put("jid", jid);
			jObject.put("password", CipherUtils.sha256(pwd));
			jObject.put("deviceType", UserInfo.deviceType);
			jObject.put("location", jLocal);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		Type type = new TypeToken<UserRespInfo>() {}.getType();
		AsyncHttpHelper.postRequest(mContext,url,jObject,type,callback);
	}
	/*登出*/
	public void loginOut(AsyncHttpHelper.ResultCallback<BaseRespInfo> callback)
	{
		String url = ConfigInfo.BASE_URL + "/user/logout?user_id=" + UserInfo.userID + "&user_token=" + UserInfo.token;
		Type type = new TypeToken<BaseRespInfo>() {}.getType();
		AsyncHttpHelper.getRequest(mContext,url,type,callback);
	}
	/*原生用户登录*/
	public void startLogin(String mobile, String pwd,
			AsyncHttpHelper.ResultCallback<UserRespInfo> callback) {

		if (!RegexUtils.isMobilePhoneNumber(mobile)) {
			//ShowMsg("你输入的手机号不正确！");
			callback.onFailure("-1","你输入的手机号不正确！");
			return;
		}
		if (pwd.length() < 6 || pwd.length() > 20) {
			// ShowMsg("你输入的密码长度不正确！");
			callback.onFailure("-1","你输入的密码长度不正确！");
			return;
		}
		String url = ConfigInfo.BASE_URL + "/native/login";
		JSONObject jObject = new JSONObject();
		try {
			JSONObject jLocal = new JSONObject();
			jLocal.put("province", UserInfo.province);
			jLocal.put("city", UserInfo.city);
			jLocal.put("district", UserInfo.district);
			jLocal.put("latitude", UserInfo.latitude);
			jLocal.put("longitude", UserInfo.longitude);

			jObject.put("jid", UserInfo.jid);
			jObject.put("deviceType", UserInfo.deviceType);
			jObject.put("mobile", mobile);
			jObject.put("password", CipherUtils.sha256(pwd));
			jObject.put("location", jLocal);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Type type = new TypeToken<UserRespInfo>() {}.getType();
		AsyncHttpHelper.postRequest(mContext,url,jObject,type,callback);
	}

	public void startPlatformLogin(String platName, String token, String id, String jid,AsyncHttpHelper.ResultCallback<UserRespInfo> callback)
	{
		String url = ConfigInfo.BASE_URL + "/sn/login";
		int category = 0;
		if (platName.equals("Wechat")) {
			category = 1;
		}
		else if (platName.equals("QQ")) {
			category = 2;
		}
		else {//SinaWeibo
			category = 3;
		}
		JSONObject jObject = new JSONObject();
		try {
			JSONObject jLocal = new JSONObject();
			jLocal.put("province", UserInfo.province);
			jLocal.put("city", UserInfo.city);
			jLocal.put("district", UserInfo.district);
			jLocal.put("latitude", UserInfo.latitude);
			jLocal.put("longitude", UserInfo.longitude);

			jObject.put("category", category);
			jObject.put("deviceType", UserInfo.deviceType);
			jObject.put("jid", jid);
			jObject.put("token", token);
			jObject.put("id", id);
			jObject.put("location", jLocal);

		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		Type type = new TypeToken<UserRespInfo>() {}.getType();
		AsyncHttpHelper.postRequest(mContext,url,jObject,type,callback);

	}
	/*获取修改密码验证码请求*/
	public void getUpdatePwdVcode(String mobile,AsyncHttpHelper.ResultCallback<BaseRespInfo> callback)
	{
		String url = ConfigInfo.BASE_URL+"/native/password/modify/vcode";
		JSONObject jObject = new JSONObject();
		try {
			String seed= RandomUtils.getRandomNumbersAndLetters(8);
			jObject.put("mobile", mobile);
			jObject.put("sign", CipherUtils.HmacSHA256(seed,ConfigInfo.hmackey));
			jObject.put("seed", seed);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		Type type = new TypeToken<BaseRespInfo>() {}.getType();
		AsyncHttpHelper.postRequest(mContext,url,jObject,type,callback);

	}

	/*验证验证码的有效性*/
	public void setUpdatePwdVcode(String mobile, String vcode,AsyncHttpHelper.ResultCallback<BaseRespInfo> callback)
	{
		String url = ConfigInfo.BASE_URL+"/native/password/modify/vcode/validate";
		JSONObject jObject = new JSONObject();
		try {
			jObject.put("mobile", mobile);
			jObject.put("vCode", vcode);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		Type type = new TypeToken<BaseRespInfo>() {}.getType();
		AsyncHttpHelper.postRequest(mContext,url,jObject,type,callback);
	}

	/*设置新密码*/
	public void setNewPwd(String mobile, String pwd,AsyncHttpHelper.ResultCallback<BaseRespInfo> callback)
	{
		String url = ConfigInfo.BASE_URL+"/native/password/modify";
		JSONObject jObject = new JSONObject();
		try {
			jObject.put("mobile", mobile);
			jObject.put("newPassword", CipherUtils.sha256(pwd));
			jObject.put("sig",CipherUtils.md5(UserInfo.signToken.substring(8,24)+ mobile));
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		Type type = new TypeToken<BaseRespInfo>() {}.getType();
		AsyncHttpHelper.postRequest(mContext,url,jObject,type,callback);
	}

	/*意见反馈*/
	public void submitOpinion(String mobile,String opinion,AsyncHttpHelper.ResultCallback<BaseRespInfo> callback){
		String url = ConfigInfo.BASE_URL+"/user/suggestion?user_id="+ UserInfo.userID+"&user_token="+ UserInfo.token;
		JSONObject jObject = new JSONObject();
		try {
			jObject.put("suggestion", opinion);
			jObject.put("mobile", mobile);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		Type type = new TypeToken<BaseRespInfo>() {}.getType();
		AsyncHttpHelper.postRequest(mContext,url,jObject,type, (AsyncHttpHelper.ResultCallback) callback);
	}
	public void updatePhotoUrl(String photoUrl,AsyncHttpHelper.ResultCallback<BaseRespInfo> callback)
	{
		String url = ConfigInfo.BASE_URL+"/user/profile/modify?user_id="+ UserInfo.userID+"&user_token="+ UserInfo.token;
		JSONObject jObject = new JSONObject();
		try {
			jObject.put("url", photoUrl);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		Type type = new TypeToken<BaseRespInfo>() {}.getType();
		AsyncHttpHelper.postRequest(mContext,url,jObject,type,callback);
	}

	/*修改昵称*/
	public void updateNickname(String name,AsyncHttpHelper.ResultCallback<BaseRespInfo> callback)
	{
		String url = ConfigInfo.BASE_URL+"/user/nickname/modify?user_id="+ UserInfo.userID+"&user_token="+ UserInfo.token;
		JSONObject jObject = new JSONObject();
		try {
			jObject.put("newName", name);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		Type type = new TypeToken<BaseRespInfo>() {}.getType();
		AsyncHttpHelper.postRequest(mContext,url,jObject,type,callback);
	}
	/*修改性别*/
	public void updateSex(int sex,AsyncHttpHelper.ResultCallback<BaseRespInfo> callback)
	{
		String url = ConfigInfo.BASE_URL+"/user/gender/modify?user_id="+ UserInfo.userID+"&user_token="+ UserInfo.token;
		JSONObject jObject = new JSONObject();
		try {
			jObject.put("gender", sex);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		Type type = new TypeToken<BaseRespInfo>() {}.getType();
		AsyncHttpHelper.postRequest(mContext,url,jObject,type,callback);
	}
	public void getMyAttentionFilmList(int page, int limit,AsyncHttpHelper.ResultCallback<MyAttentionFilmListInfo> callback){
		String url = ConfigInfo.BASE_URL+"/user/films/followed?user_id="+ UserInfo.userID+"&user_token="+ UserInfo.token+"&page="+page+"&limit="+limit;
		Type type = new TypeToken<MyAttentionFilmListInfo>() {}.getType();
		AsyncHttpHelper.getRequest(mContext,url,type,callback);

	}
	/*获取影评人列表数据*/
	public void getMyAttentionAuthorList(int page,  int limit,AsyncHttpHelper.ResultCallback<MyAttentionAuthorListInfo> callback){
		String url = ConfigInfo.BASE_URL+"/user/critics/followed?user_id="+ UserInfo.userID+"&user_token="+ UserInfo.token+"&page="+page+"&limit="+limit;
		Type type = new TypeToken<MyAttentionAuthorListInfo>() {}.getType();
		AsyncHttpHelper.getRequest(mContext,url,type,callback);
	}
	public void getMyShareTicketList(int page,  int limit,AsyncHttpHelper.ResultCallback<MySharedTicketListInfo> callback)
	{
		String url = ConfigInfo.BASE_URL + "/user/" + UserInfo.userID + "/stub/list?user_id=" + UserInfo.userID + "&user_token=" + UserInfo.token + "&page=" + page + "&limit=" + limit;
		Type type = new TypeToken<MySharedTicketListInfo>() {}.getType();
		AsyncHttpHelper.getRequest(mContext,url,type,callback);
	}
	/*删除我的晒票*/
	public void delMySharedTicket(int stubID ,AsyncHttpHelper.ResultCallback<BaseRespInfo> callback)
	{
		String url = ConfigInfo.BASE_URL + "/stub/delete?user_id=" + UserInfo.userID + "&user_token=" + UserInfo.token;
		JSONObject jObject = null;
		try {
			jObject = new JSONObject();
			jObject.put("stubID", stubID);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		Type type = new TypeToken<BaseRespInfo>() {}.getType();
		AsyncHttpHelper.postRequest(mContext,url,jObject,type,callback);
	}
	/*获取我的兑换劵列表*/
	public void getMyTicketList(int page, int limit,AsyncHttpHelper.ResultCallback<MyTicketListInfo> callback)
	{
		String url = ConfigInfo.BASE_URL + "/user/ticket/order/list?user_id=" + UserInfo.userID + "&user_token=" + UserInfo.token + "&page=" + page + "&limit=" + limit;
		Type type = new TypeToken<MyTicketListInfo>() {}.getType();
		AsyncHttpHelper.getRequest(mContext,url,type,callback);
	}
	/*获取我的基本信息*/
	/*获取我的兑换劵列表*/
	public void getMyBaseInfo(AsyncHttpHelper.ResultCallback<MyBaseInfo> callback)
	{
		String url = ConfigInfo.BASE_URL+"/user/action/info?user_id="+ UserInfo.userID+"&user_token="+ UserInfo.token;
		Type type = new TypeToken<MyBaseInfo>() {}.getType();
		AsyncHttpHelper.getRequest(mContext,url,type,callback);
	}

	/*影片详情基本信息*/
	public void getFilmBaseInfo(String filmId,AsyncHttpHelper.ResultCallback<FilmBaseInfo> callback){
		String url = ConfigInfo.BASE_URL+"/film/"+filmId+"/info?user_id="+ UserInfo.userID+"&user_token="+ UserInfo.token;
		Type type = new TypeToken<FilmBaseInfo>() {}.getType();
		AsyncHttpHelper.getRequest(mContext,url,type,callback);
	}

	//获取影评信息列表
	public void getFilmVideoList(String filmId,int page,int limit,AsyncHttpHelper.ResultCallback<FilmVideoEntity> callback){
		String url = ConfigInfo.BASE_URL+"/film/"+filmId+"/trailers?page="+page+"&limit="+limit;
		Type type = new TypeToken<FilmVideoEntity>() {}.getType();
		AsyncHttpHelper.getRequest(mContext,url,type,callback);

	}
	//获取影评信息列表
	public void getFilmPostList(String filmId,int page,int limit,AsyncHttpHelper.ResultCallback<FilmPostEntity> callback){
		String url = ConfigInfo.BASE_URL+"/film/"+filmId+"/photos?page="+page+"&limit="+limit;
		Type type = new TypeToken<FilmPostEntity>() {}.getType();
		AsyncHttpHelper.getRequest(mContext,url,type,callback);

	}
	//获取影评信息列表
	public void getFilmReviewList(String filmId,int page,int limit,AsyncHttpHelper.ResultCallback<FilmReviewListInfo> callback){
		String url = ConfigInfo.BASE_URL+"/film/"+filmId+"/cinecism/list?page="+page+"&limit="+limit;
		Type type = new TypeToken<FilmReviewListInfo>() {}.getType();
		AsyncHttpHelper.getRequest(mContext,url,type,callback);

	}

	//获取该电影晒票列表
	public void getSharedTicketList(String filmId,int page,int limit,AsyncHttpHelper.ResultCallback<SharedTicketListInfo> callback){
		String url = ConfigInfo.BASE_URL+"/film/"+filmId+"/stub/list?page="+page+"&limit="+limit;
		Type type = new TypeToken<SharedTicketListInfo>() {}.getType();
		AsyncHttpHelper.getRequest(mContext,url,type,callback);

	}
	//获取该电影相关资讯信息列表12346
	public void getSingleFilmHotList(String filmId,int page,int limit,AsyncHttpHelper.ResultCallback<FilmHotListInfo> callback){
		String url = ConfigInfo.BASE_URL+"/film/"+filmId+"/news?page="+page+"&limit="+limit;
		Type type = new TypeToken<FilmHotListInfo>() {}.getType();
		AsyncHttpHelper.getRequest(mContext,url,type,callback);
	}
	//获取该电影各种指数表
	public void getFilmAllIndexList(String filmId,AsyncHttpHelper.ResultCallback<FilmAllIndexListInfo> callback){
		String url = ConfigInfo.BASE_URL+"/film/"+filmId+"/assess";
		Type type = new TypeToken<FilmAllIndexListInfo>() {}.getType();
		AsyncHttpHelper.getRequest(mContext,url,type,callback);
	}

	/*关注该电影*/
	public void attentionFilm(String filmId,AsyncHttpHelper.ResultCallback<BaseRespInfo> callback)
	{
		String url = ConfigInfo.BASE_URL+"/user/follow/film?user_id="+ UserInfo.userID+"&user_token="+ UserInfo.token;
		JSONObject jObject =null;
		int id=Integer.valueOf(filmId);
		try {
			jObject = new JSONObject();
			jObject.put("filmID", id);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		Type type = new TypeToken<BaseRespInfo>() {}.getType();
		AsyncHttpHelper.postRequest(mContext,url,jObject,type,callback);
	}
	/*取消电影关注*/
	public void cancelAttentionFilm(String filmId,AsyncHttpHelper.ResultCallback<BaseRespInfo> callback)
	{
		String url = ConfigInfo.BASE_URL+"/user/unfollow/film?user_id="+ UserInfo.userID+"&user_token="+ UserInfo.token;
		JSONObject jObject =null;
		int id=Integer.valueOf(filmId);
		try {
			jObject = new JSONObject();
			jObject.put("filmID", id);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		Type type = new TypeToken<BaseRespInfo>() {}.getType();
		AsyncHttpHelper.postRequest(mContext,url,jObject,type,callback);
	}

	/*获取广告条图片数据*/
	public void getFilmBannerData(int category,AsyncHttpHelper.ResultCallback<BannerListInfo> callback)
	{
		String url =  ConfigInfo.BASE_URL+"/app/main/banner/list?category="+category;
		Type type = new TypeToken<BannerListInfo>() {}.getType();
		AsyncHttpHelper.getRequest(mContext,url,type,callback);

	}

	/*获取全部电影榜单列表页*/
	@Deprecated
	public void getAllFilmListData(int page, int limit, AsyncHttpHelper.ResultCallback<FilmRankingListInfo> callback)
	{
		String url =  ConfigInfo.BASE_URL+"/film/all/list?page="+page+"&limit="+limit;
		Type type = new TypeToken<FilmRankingListInfo>() {}.getType();
		AsyncHttpHelper.getRequest(mContext,url,type,callback);
	}
	/*获取热映电影*/
	public void getFilmListData(int page, int limit, String sort, AsyncHttpHelper.ResultCallback<FilmListInfo> callback)
	{
		String url =  ConfigInfo.BASE_URL+"/film/showing/list?sort_order="+sort+"&page="+page+"&limit="+limit;
		Type type = new TypeToken<FilmListInfo>() {}.getType();
		AsyncHttpHelper.getRequest(mContext,url,type,callback);
	}
	/*获取首页热映电影*/
	public void getShowFilmListData(int page, int limit , AsyncHttpHelper.ResultCallback<ShowFilmListInfo> callback)
	{
		String url =  ConfigInfo.BASE_URL+"/film/showing/list/v2?page="+page+"&limit="+limit;
		Type type = new TypeToken<ShowFilmListInfo>() {}.getType();
		AsyncHttpHelper.getRequest(mContext,url,type,callback);
	}

	/*获取即将上映列表数据*/
	public void getPreFilmListData(int page,int limit,AsyncHttpHelper.ResultCallback<FilmListInfo> callback)
	{
		String url =  ConfigInfo.BASE_URL+"/film/waiting/list?page="+page+"&limit="+limit;
		Type type = new TypeToken<FilmListInfo>() {}.getType();
		AsyncHttpHelper.getRequest(mContext,url,type,callback);
	}

	/*获取首页推荐榜单列表*/
	public void getFilmBillboardListData(int page, int limit,AsyncHttpHelper.ResultCallback<FilmBboardListInfo> callback)
	{
		String url =  ConfigInfo.BASE_URL+"/billboard/list?page="+page+"&limit="+limit;
		Type type = new TypeToken<FilmBboardListInfo>() {}.getType();
		AsyncHttpHelper.getRequest(mContext,url,type,callback);
	}
	/*获取具体推荐电影列表*/
	public void getRankingFilmListData(String bdid, int page, int limit, AsyncHttpHelper.ResultCallback<FilmRankingListInfo> callback)
	{
		String url = ConfigInfo.BASE_URL + "/billboard/" + bdid + "/film/list?page=" + page + "&limit=" + limit;
		Type type = new TypeToken<FilmRankingListInfo>() {}.getType();
		AsyncHttpHelper.getRequest(mContext,url,type,callback);
	}
	/*获取热点数据列表*/
	public void getFilmHotListData(int page,int limit,AsyncHttpHelper.ResultCallback<FilmHotListInfo> callback)
	{
		String url =  ConfigInfo.BASE_URL+"/film/news/list?page="+page+"&limit="+limit;
		Type type = new TypeToken<FilmHotListInfo>() {}.getType();
		AsyncHttpHelper.getRequest(mContext,url,type,callback);

	}



}
