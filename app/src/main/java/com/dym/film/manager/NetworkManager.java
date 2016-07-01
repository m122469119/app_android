package com.dym.film.manager;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/12
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.dym.film.application.ConfigInfo;
import com.dym.film.application.UserInfo;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.entity.UserMessage;
import com.dym.film.utils.CipherUtils;
import com.dym.film.utils.LogUtils;
import com.dym.film.utils.MatStatsUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * 所有的网络API接口都在这个Manager里面定义
 * 单例模式
 * TODO: 使用Https
 */
public class NetworkManager
{
    private final static String TAG = "NetworkManager";


    private  static  Context mContext=null;
    private final static NetworkManager mInstance = new NetworkManager();

    public  static void init(Context context){
        mContext=context;
    }
    public static NetworkManager getInstance()
    {
        return mInstance;
    }

    /**
     * URLs
     */
    public final static String URL_BASE = ConfigInfo.BASE_URL;

    /**
     * 默认的字符集
     */
    public final static String DEF_CHARSET = "UTF-8";

    /**
     * Http Method
     */
    public final static String M_POST = "POST";

    public final static String M_GET = "GET";


    /**
     * Gson
     */
    public final static Gson GSON = new GsonBuilder().setDateFormat("yyyy.MM.dd HH:mm:ss").create();

    /**
     * OkHttpClient
     */
    private final static MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final static MediaType IMAGE_PNG_TYPE = MediaType.parse("image/jpeg");

    public final static int DEF_HTTP_TIMEOUT_SEC = 10; // 10s

    private OkHttpClient mOkHttpClient = null;


    private NetworkManager()
    {
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(DEF_HTTP_TIMEOUT_SEC, TimeUnit.SECONDS)
                .readTimeout(DEF_HTTP_TIMEOUT_SEC, TimeUnit.SECONDS)
                .build();
    }

    public OkHttpClient getOkHttpClient()
    {
        return mOkHttpClient;
    }

    public static String encode(String str)
    {
        String res = null;
        try {
            res = URLEncoder.encode(str, DEF_CHARSET);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (res == null) {
                res = str;
            }
        }

        return res;
    }

    public static String getShareUrl(String url)
    {
        return URL_BASE + url + "?dl=dymShare";
    }

    /**
     * Json的通用的请求接口
     * @param url
     * @param method
     * @param callback
     * @return
     */
    public Call query(String url, String method, RequestBody body, Callback callback)
    {
        if (callback == null) {
            callback = new Callback()
            {
                @Override
                public void onFailure(Call call, IOException e)
                {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException
                {

                }
            };
        }

        if (TextUtils.isEmpty(url)) {
            url = URL_BASE;
        }
        else if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            if (url.startsWith("/")) {
                url = URL_BASE + url;
            }
            else {
                url = URL_BASE + "/" + url;
            }
        }
        LogUtils.e(TAG, "URL: " + url);

        if(mContext!=null){
            MatStatsUtil.interfaceTest(mContext,url);
        }
        Request request = new Request.Builder().url(url).method(method, body).build();

        Call call = mOkHttpClient.newCall(request);

        call.enqueue(callback);

        return call;
    }

    public Call post(String url, String json, Callback callback)
    {
        if (json == null) {
            json = "";
        }

        LogUtils.e(TAG, "Body: " + json);
        RequestBody body = RequestBody.create(JSON_TYPE, json);
        return query(url, M_POST, body, callback);
    }

    public Call get(String url, Callback callback)
    {
        return query(url, M_GET, null, callback);
    }


    /**
     * 工具方法，产生url
     * @param params
     * @return
     */
    public static String generateUrl(String baseUrl, String... params)
    {
        if (params == null || params.length == 0) {
            return baseUrl == null ? "" : baseUrl;
        }
        String result = baseUrl + "?";
        result += (params[0] == null ? "" : params[0]) + "=" + (params[1] == null ? "" : params[1]);

        int length = params.length;
        for (int i = 2; i < length; i+=2) {
            result += "&" + (params[i] == null ? "" : params[i]) + "=";

            result += (i+1 < length ? (params[i+1] == null ? "" : params[i+1]): "");
        }

        return result;
    }

    public static String generateUrl(boolean needUser, String baseUrl, String... params)
    {
        String result = baseUrl;
        if (needUser && UserInfo.isLogin) {
            result = generateUrl(result, "user_id",
                    String.valueOf(UserInfo.userID), "user_token", UserInfo.token);
        }
        else {
            result += "?";
        }

        if (params != null && params.length >= 2) {
            result += "&" + (params[0] == null ? "" : params[0]) + "=" + (params[1] == null ? "" : params[1]);

            int length = params.length;
            for (int i = 2; i < length; i += 2) {
                result += "&" + (params[i] == null ? "" : params[i]) + "=";

                result += (i + 1 < length ? (params[i + 1] == null ? "" : params[i + 1]) : "");
            }
        }


        return result;
    }

    /*************************************** A P I ********************************************/

    /**
     * Http请求模型的基类
     */
    public static class BaseReqModel implements Serializable
    {
        //
    }

    /**
     * Http返回模型的基类
     */
    public static class BaseRespModel implements Serializable
    {
        /**
         * 返回码
         */
        public int code = 0;

        /**
         * 返回信息
         */
        public String message = "";


        public ArrayList<String> dates=new ArrayList<>();
    }

    /**
     * 通用的位置模型
     */
    public static class LocationModel
    {
        public String province = "";
        public String city = "";
        public String district = "";
        public double longitude = 0;
        public double latitude = 0;
    }

    /**
     * 通用的图片模型
     */
    public static class StubImageModel
    {
        public String url = "";
        public int width = 0;
        public int height = 0;
    }

    /**
     * 通常的用户模型
     */
    public static class UserModel
    {
        public long userID = 0;

        public String name = "";

        public String mobile = "";

        public String avatar = "";

        public int gender = 1;

        public LocationModel location = null;

        public Date createTime = null;
    }

    public static class MyUserModel extends UserModel
    {
        public String token = "";
    }


    /**
     * (POST) 获取验证码
     * url:
     *  /native/register/vcode
     请求:
     {
         “mobile”: “xxxxx”
     }
     返回
     {
         "code": 0,
         "message": "xxxx"
     }
     */
    public static class ReqGetRegVerCode extends BaseReqModel
    {
        public String mobile = "";
    }

    public static class RespGetRegVerCode extends BaseRespModel
    {
        //
    }

    public Call getRegisterVerCode(@NonNull ReqGetRegVerCode reqModel,
                                   @NonNull HttpRespCallback<RespGetRegVerCode> callback)
    {
        callback.setRespModelClass(RespGetRegVerCode.class);
        return post("/native/register/vcode", GSON.toJson(reqModel), callback);
    }


    /**
     * (POST) 检验验证码有效接口
     * URL:
     *  /native/register/vcode/validate
     请求：
     {
         "mobile": "1300000000", //手机号
         “vcode”: "xxxx"
     }
     返回
     {
         "code": 0,
         "message": "xxxx"
     }
     */
    public static class ReqValidateRegVerCode extends BaseReqModel
    {
        /**
         * 手机号
         */
        public String mobile = "";

        /**
         * 验证码
         */
        public String vcode = "";
    }

    public static class RespValidateRegVerCode extends BaseRespModel
    {
        //
    }

    public Call validateVerCode(@NonNull ReqValidateRegVerCode reqModel,
                                @NonNull HttpRespCallback<RespValidateRegVerCode> callback)
    {
        callback.setRespModelClass(RespValidateRegVerCode.class);
        return post("/native/register/vcode/validate", GSON.toJson(reqModel), callback);
    }


    /**
     * (POST) 原生用户注册
     * URL:
     *  /native/register/info
     请求:
     {
     “mobile”: “xxxx”,
     “password”: “xxxxx”  //sha256(原始密码)
     “deviceType”: 1 | 2 (1 ios 2 android)
     “deviceToken” : “xxxx” // ios设备必填
     “jid”:”xxxxx” //极光id
     “location”: {  //可选字段
         “province”: “xxx”,
         “city”: “xxx”,
         “area”: “xxx”
         “longitude”: xxx.xxxxxx
         “latitude”: xxx.xxxxxxx
     }
     }

     返回: 返回的userID和token会作为以后会用在登录用户才能访问的接口中，必须本地保留。

     {

     “code”: 0,

     “message”: “success”,

     “user”:{

     “userID” :xxx,

     “name”: “xxx”,

     “mobile” :”xxxx”,

     “avatar”: “xxxx”,

     “token”: “xxxxx”，

     “location”: {  //可选字段

     “province”: “xxx”,

     “city”: “xxx”,

     “area”: “xxx”

     “longitude”: xxx.xxxxxx

     “latitude”: xxx.xxxxxxx

     },

     "createTime": "xxxx"

     }

     }
     */
    public static class ReqNativeRegister extends BaseReqModel
    {
        public String mobile = "";

        public String password = "";

        public final int deviceType = 2;

        public String deviceToken = "";

        public String jid = ""; // 极光ID

        public LocationModel location = null;
    }

    public static class RespNativeRegister extends BaseRespModel
    {
        public MyUserModel user = null;
    }

    public Call registerNative(@NonNull ReqNativeLogin reqModel,
                               @NonNull HttpRespCallback<RespNativeLogin> callback)
    {
        callback.setRespModelClass(RespNativeLogin.class);
        return post("/native/register/info", GSON.toJson(reqModel), callback);
    }

    /**
     * 原生用户登录
     method: POST
     url: /native/login

     请求：

     {
     “mobile”: “xxxx”,
     “password”: “xxxxx”  //sha256(原始密码)
     “location”: {  //可选字段
         “province”: “xxx”,
         “city”: “xxx”,
         “area”: “xxx”
         “longitude”: xxx.xxxxxx
         “latitude”: xxx.xxxxxxx
     }
     }
     返回：
     {
     “code”: 0,
     “message”: “success”,
     “user”:{
     “userID” :xxx,
     “name”: “xxx”,
     “mobile” :”xxxx”,
     “avatar”: “xxxx”,
     “token”: “xxxxx”,
     “location”: {  //可选字段
         “province”: “xxx”,
         “city”: “xxx”,
         “area”: “xxx”
         “longitude”: xxx.xxxxxx
         “latitude”: xxx.xxxxxxx
     },
     "createTime": "xxxx"
     }
     }
     */
    public static class ReqNativeLogin extends BaseReqModel
    {
        public String mobile = "";

        public String password = ""; // sha256

        public LocationModel location = null;
    }

    public static class RespNativeLogin extends BaseRespModel
    {
        public MyUserModel user = null;
    }

    public Call loginNative(@NonNull ReqNativeLogin reqModel,
                            @NonNull HttpRespCallback<RespNativeLogin> callback)
    {
        callback.setRespModelClass(RespNativeLogin.class);
        return post("/native/login", GSON.toJson(reqModel), callback);
    }


    /**
     * 第三方sn注册登录
     method: POST
     url: /sn/login

     请求:
     {
     “category” : 1 | 2 | 3(1 表示微信，2. QQ  3. weibo)
     “id”: “xxxx”
     “token”: “xxxxx”,
     “deviceType”: 1 | 2 (1 ios 2 android)
     “deviceToken” : “xxxx” // ios设备必填
     “jid”:”xxxxx” //极光id
     “location”: {  //可选字段
     “province”: “xxx”,
     “city”: “xxx”,
     “area”: “xxx”
     “longitude”: xxx.xxxxxx
     “latitude”: xxx.xxxxxxx
     }
     }

     返回
     {
     “code”: 0,
     “message”: “success”,
     “user”:{
     “userID” :xxx,
     “name”: “xxx”,
     “mobile” :”xxxx”, //可选，第三方登录不一定能获取手机号
     “avatar”: “xxxx”,
     “token”: “xxxxx”,
     “location”: {  //可选字段
     “province”: “xxx”,
     “city”: “xxx”,
     “area”: “xxx”
     “longitude”: xxx.xxxxxx
     “latitude”: xxx.xxxxxxx
     },
     "createTime": "xxxx"
     }
     }
     */
    public static class ReqSNLogin extends BaseReqModel
    {
        public int category = 0;

        public String id = "";

        public String token = "";

        public final int deviceType = 2;

        public String deviceToken = "";

        public String jid = ""; // 极光ID

        public LocationModel location = null;
    }

    public static class RespSNLogin extends BaseRespModel
    {
        public MyUserModel user = null;
    }

    public Call loginSN(@NonNull ReqSNLogin reqModel,
                        @NonNull HttpRespCallback<RespSNLogin> callback)
    {
        callback.setRespModelClass(RespSNLogin.class);
        return post("/sn/login", GSON.toJson(reqModel), callback);
    }


    /**
     * 获取修改密码验证码
     url: /native/password/modify/vcode?user_id=xxxx&user_token=xxxx
     method: GET

     返回
     {
     "code": 0,
     "message": "xxxx"
     }
     */
    public static class RespPwdModifyVerCode extends BaseRespModel
    {
        //
    }

    public Call getPwdModifyVerCode(@NonNull HttpRespCallback<RespPwdModifyVerCode> callback)
    {
        callback.setRespModelClass(RespPwdModifyVerCode.class);

        String url = generateUrl("/native/password/modify/vcode", "user_id", String.valueOf(!UserInfo.isLogin ? "" : !UserInfo.isLogin ? "" : UserInfo.userID), "user_token", UserInfo.token);

        return get(url, callback);
    }

    /**
     * 验证修改密码验证码

     url: /native/password/modify/vcode/validate?user_id=xxxx&user_token=xxxx

     method: POST

     请求

     {

     "vCode": "xxxx"
     }
     */
    public static class ReqPwdModifyValVerCode extends BaseReqModel
    {
        public String vCode = "";
    }

    public static class RespPwdModifyValVerCode extends BaseRespModel
    {
        //
    }

    public Call validatePwdModifyVerCode(@NonNull ReqPwdModifyValVerCode reqModel,
                                         @NonNull HttpRespCallback<RespPwdModifyValVerCode> callback)
    {
        callback.setRespModelClass(RespPwdModifyValVerCode.class);

        String url = generateUrl("/native/password/modify/vcode/validate", "user_id", String.valueOf(!UserInfo.isLogin ? "" : !UserInfo.isLogin ? "" : UserInfo.userID), "user_token", UserInfo.token);

        return post(url, GSON.toJson(reqModel), callback);
    }


    /**
     * url: /native/password/modify?user_id=xxx&user_token=xxx
     method: POST

     新密码要经过sha256加密
     {
     “newPassword”: “xxxx”
     }

     返回:
     {
     “code”： 0,
     “message”: “success”
     }
     */
    public static class ReqPwdModify extends BaseReqModel
    {
        public String newPassword = "";
    }

    public static class RespPwdModify extends BaseRespModel
    {
        //
    }

    public Call modifyPassword(@NonNull ReqPwdModify reqModel,
                               @NonNull HttpRespCallback<RespPwdModify> callback)
    {
        callback.setRespModelClass(RespPwdModify.class);
        String url = generateUrl("/native/password/modify", "user_id", String.valueOf(!UserInfo.isLogin ? "" : UserInfo.userID), "user_token", UserInfo.token);

        return post(url, GSON.toJson(reqModel), callback);
    }


    /**
     * 修改头像接口
     url: /user/profile/modify?user_id=xxx&user_token=xxx
     method: multipart post

     请求
     file key: profile

     返回:

     {
     “code”: 0,
     “message”: “success”,
         “user”:{
             “userID” :xxx,
             “name”: “xxx”,
             “mobile” :”xxxx”,
             “avatar”: “xxxx”
         }
     }
     */
    public static class RespModifyUserProfile extends BaseRespModel
    {
        public UserModel user = null;
    }

    public Call modifyUserProfile(@NonNull File profile,
                                  @NonNull HttpRespCallback<RespModifyUserProfile> callback)
    {
        callback.setRespModelClass(RespModifyUserProfile.class);

        String url = generateUrl("/user/profile/modify", "user_id", String.valueOf(!UserInfo.isLogin ? "" : UserInfo.userID), "user_token", UserInfo.token);

        RequestBody fileBody = RequestBody.create(IMAGE_PNG_TYPE, profile);

        RequestBody requestBody =new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addPart(Headers.of("Content-Disposition", "form-data; name=\"profile\"; filename=\"profile.jpg\""), fileBody).build();

        return query(url, M_POST, requestBody, callback);
    }


    /**
     * 修改昵称接口

     url: /user/nickname/modify?user_id=xxx&user_token=xxx
     method post

     请求：
     {
     “newName”: “xxx”
     }

     返回:
     {
     “code”: 0,
     “message”: “success”,
         “user”:{
             “userID” :xxx,
             “name”: “xxx”,
             “mobile” :”xxxx”,
             “avatar”: “xxxx”
         }
     }
     */
    public static class ReqModifyUserNickname extends BaseReqModel
    {
        public String newName = "";
    }

    public static class RespModifyUserNickname extends BaseRespModel
    {
        public UserModel user = null;
    }

    public Call modifyNickname(@NonNull ReqModifyUserNickname req,
                               @NonNull HttpRespCallback<RespModifyUserNickname> callback)
    {
        callback.setRespModelClass(RespModifyUserNickname.class);

        String url = generateUrl("/user/nickname/modify", "user_id", String.valueOf(!UserInfo.isLogin ? "" : UserInfo.userID), "user_token", UserInfo.token);

        return post(url, GSON.toJson(req), callback);
    }


    /**
     * 获取我的信息的接口

     url: /user/action/info? user_id=xxx&user_token=xxx
     method: GET

     {
         “code” 0,
         “message”: “success”,
         “info”: {
             “message”：{
                 “number”:xx,
                 “hasNew”: 0 | 1
             },
             “following”:{
                 “number”: xxx,
                 “activeProfile”: “xxxx”
             },

             “ticket”: {
                 “number”: x
             },

             “myStubs”:{
                 “number”:xx,
                 “stubs”:[
                     {
                         “stubID”: xx,
                         “stubImageUrl”: “xxx”, //票根照片地址
                         “opinion”: -1 | 0 |1, //态度
                     }
                ]
             }
        }
     }
     */
    public static class MessageRespModel
    {
        public long number = 0;
        public int hasNew = 0;
    }

    public static class FollowingRespModel
    {
        public long number = 0;
        public String activeProfile = "";
    }

    public static class TicketRespModel
    {
        public long number = 0;
    }

    public static class StubsRespModel
    {
        public long stubID = 0;
        public String stubImageUrl = "";
        public int opinion = 0;
    }

    public static class MyStubsRespModel
    {
        public long number = 0;

        public ArrayList<StubsRespModel> stubs = null;
    }

    public static class InfoRespModel
    {
        public MessageRespModel message = null;
        public FollowingRespModel following = null;
        public TicketRespModel ticket = null;
        public MyStubsRespModel myStubs = null;
    }

    public static class RespGetUserInfo extends BaseRespModel
    {
        public InfoRespModel info = null;
    }

    public Call getUserInfomation(@NonNull HttpRespCallback<RespGetUserInfo> callback)
    {
        callback.setRespModelClass(RespGetUserInfo.class);

        String url = generateUrl("/user/action/info", "user_id", String.valueOf(!UserInfo.isLogin ? "" : UserInfo.userID), "user_token", UserInfo.token);

        return get(url, callback);
    }

    /**
     * 建议接口

     url: /user/suggestion? user_id=xxx&user_token=xxx
     method: POST

     请求:
     {
     “suggestion”: “xxx”
     }

     返回:
     {
     “code”:0,
     “message”:”success”
     }
     */
    public static class ReqUserSuggestion extends BaseReqModel
    {
        public String suggestion = "";
    }

    public static class RespUserSuggestion extends BaseRespModel
    {
        //
    }

    public Call getUserSuggestion(@NonNull ReqUserSuggestion req,
                                  @NonNull HttpRespCallback<RespUserSuggestion> callback)
    {
        callback.setRespModelClass(RespUserSuggestion.class);
        String url = generateUrl("/user/suggestion", "user_id", String.valueOf(!UserInfo.isLogin ? "" : UserInfo.userID), "user_token", UserInfo.token);

        return post(url, GSON.toJson(req), callback);
    }


    /**
     * 晒票列表接口

     url: /user/stub/list? user_id=xxx&user_token=xxx&page=xx&limit=xx 这里user_id和user_token是可选的
     method: GET

     返回
     {
     "code" : 0,
     "message": “success”,
     "stubs":
     [
     {
     “stubID”: xx,
     “stubImageUrl”: “xxx”, //票根照片地址
     “opinion”: -1 | 0 |1, //态度
     “writer”:{
         “userID” :xxx,
         “name”: “xxx”,
         “mobile” :”xxxx”, //可选
         “avatar”: “xxxx”,
         “location”: { //位置
             “province”: “xxx”,
             “city”: “xxx”,
             “area”: “xxx”
             “longitude”: xxxx,
             “latitude”: xxxx
         }
     }
     “showOffTime": xxxxx, //晒票根的时间戳
     “tags”: [“xx”, “xx”], //包含的标签
     “content” : “xxxx”, //内容
     “supportNum” : xx, //支持的数量
     “supported”: 0 | 1 // 是否支持过
     }

     ….
     ]

     }
     */

    public static class SharedTicketRespModel
    {
        public long                 stubID = 0;
        public StubImageModel       stubImage = null;
        public int                  opinion = 0;
        public UserModel            writer = null;
        public ArrayList<String>    tags = new ArrayList<>();
        public long                 showOffTime = 0;
        public String               content = "";
        public int                  supportNum = 0;
        public int                  supported = 0;
        public String               city = "";
        public String               district = "";
        public String               shareUrl = "";
        public int commentsNum=0;
    }

    public static class RespSharedTicketList extends BaseRespModel
    {
        public ArrayList<SharedTicketRespModel> stubs = null;
    }

    public Call getSharedTicketList(int page,
                                    int limit,
                                    @NonNull HttpRespCallback<RespSharedTicketList> callback)
    {
        callback.setRespModelClass(RespSharedTicketList.class);

        String url = generateUrl("/user/stub/list", "user_id", String.valueOf(!UserInfo.isLogin ? "" : UserInfo.userID), "user_token", UserInfo.token, "page", String.valueOf(page), "limit", String.valueOf(limit));

        return get(url, callback);
    }

    public static class StubEventRespModel
    {
        public String url = "";
        public String title = "";
    }
    /**
     * 获取tag列表

     url: /tags/list
     method: GET

     返回:
     [“xxxx”, “xxx”,…]
     */
    public static class FilmTag {
        public long filmID = 0;
        public String post = "";
    }
    public static class RespTagsList extends BaseRespModel
    {
        public ArrayList<String> tags = new ArrayList<>();
        public ArrayList<FilmTag>  films = new ArrayList<>();
        public StubEventRespModel stubEvent = null;
    }

    public Call getTagsList(HttpRespCallback<RespTagsList> callback)
    {
        callback.setRespModelClass(RespTagsList.class);

        return get("tags/list", callback);
    }

    public Call getReviewTagsList(HttpRespCallback<RespTagsList> callback)
    {
        callback.setRespModelClass(RespTagsList.class);

        return get("tags/list?forCinecism=1", callback);
    }

    /**
     * 根据tag获取晒票列表

     url: /user/stub/list? user_id=xxx&user_token=xxx&page=xx&limit=xx&tag=xxx user_id和user_token非必须
     method: GET

     返回
     {

     "code" : 0,
     "message": “success”,

     [
     {
     “stubID”: xx,
     “stubImageUrl”: “xxx”, //票根照片地址
     “opinion”: -1 | 0 |1, //态度
     “writer”:{
         “userProfileUrl”: “xxx”, //用户头像地址
         “userName”: “xxx”, //用户名
         “location”: { //位置
             “province”: “xxx”,

             “city”: “xxx”,

             “area”: “xxx”

             “longitude”: xxxx,

             “latitude”: xxxx
         }
     }

     “tags”: [“xx”, “xx”], //包含的标签
     “content” : “xxxx”, //内容
     “supportNum” : xx, //支持的数量
     “supported”: 0 | 1 // 是否支持过
     }

     ….
     ]

     }
     */
    public Call getTagSharedTicketList(int page,
                                       int limit,
                                       String tag,
                                       HttpRespCallback<RespSharedTicketList> callback)
    {
        callback.setRespModelClass(RespSharedTicketList.class);

        String url = generateUrl("/user/stub/list",
                "user_id", String.valueOf(!UserInfo.isLogin ? "" : UserInfo.userID),
                "user_token", UserInfo.token,
                "page", String.valueOf(page),
                "limit", String.valueOf(limit),
                "tag", encode(tag));

        return get(url, callback);
    }

    /**
     * 获取电影票房信息

     url: /film/box/info?tag=xxx
     method: GET
     {
     “code”: 0,
     “message”: “success”,
     “boxInfo”:
     {
     “rank”:xx,
     “supportRatio”: xx,
     “followNum”: xx
     }
     }
     */
    public static class FilmInfoRespModel
    {
        public int rank = 0;
        public int supportRatio = 0;
        public int followNum = 0;
    }

    public static class RespFilmBoxInfo extends BaseRespModel
    {
        public FilmInfoRespModel boxInfo =  null;
    }

    public Call getFilmBoxInfo(String tag,
                               @NonNull HttpRespCallback<RespFilmBoxInfo> callback)
    {
        callback.setRespModelClass(RespFilmBoxInfo.class);

        return get(generateUrl("/film/box/info", "tag", encode(tag)), callback);
    }

    /**
     * 对晒票进行点赞

     url: /stub/support?user_id=xxx&user_token=xxx
     method: POST

     请求{
     “stubID” :xx
     }

     返回
     {
     “supportNum”: xx
     }
     */
    public static class ReqSupportSharedTicket extends BaseReqModel
    {
        public long stubID = 0;
    }

    public static class RespSupportSharedTicket extends BaseRespModel
    {
        public int supportNum = 0;
    }

    public Call supportSharedTicket(@NonNull ReqSupportSharedTicket req,
                                    @NonNull HttpRespCallback<RespSupportSharedTicket> callback)
    {
        callback.setRespModelClass(RespSupportSharedTicket.class);

        String url = generateUrl("/stub/support", "user_id", String.valueOf(!UserInfo.isLogin ? "" : UserInfo.userID), "user_token", UserInfo.token);

        return post(url, GSON.toJson(req), callback);
    }

    /**
     * 获取周围晒票列表

     url: /stub/nearby/list?user_id=xxx&user_token=xxx&longitude=xxx&latitude=xxx  user_id和user_token不是必须的
     method: GET

     {
     “code”: 0,
     “message”: “success”
     “neighbours”：[{
         “userID” :xxx,
         “name”: “xxx”,
         “mobile” :”xxxx”, //可选
         “avatar”: “xxxx”,
         “location”: { //位置
             “province”: “xxx”,
             “city”: “xxx”,
             “area”: “xxx”
             “longitude”: xxxx,
             “latitude”: xxxx
         },

         “stubs”[{
             “stubID”: xx,
             “stubImageUrl”: “xxx”, //票根照片地址
             “opinion”: -1 | 0 |1, //态度
             “tags”: [“xx”, “xx”], //包含的标签
             “content” : “xxxx”, //内容
             “supportNum” : xx, //支持的数量
             “supported”: 0 | 1 // 是否支持过
             “publishTime”: xxxxxx//时间戳，前台去算已发表的天数，到毫秒级
         }…]

     }….

     ]
     }
     */
    public static class ReqGetAroundSharedTicket extends BaseReqModel
    {
        public String province = "";
        public String city = "";
        public String district = "";
        public double longitude = 0;
        public double latitude = 0;
    }

//    public static class NeighboursStubRespModel
//    {
//        public long stubID = 0;
//        public StubImageModel stubImage = null;
//        public int opinion = 0;
//        public ArrayList<String> tags = null;
//        public String content = "";
//        public int supportNum = 0;
//        public int supported = 0;
//        public long publishTime = 0;
//    }

    public static class NeighboursRespModel
    {
        public long userID = 0;

        public String name = "";

        public String mobile = "";

        public String avatar = "";

        public int gender = 1;

        public LocationModel location = null;

        public ArrayList<SharedTicketRespModel> stubs = null;

        public SharedTicketRespModel toSharedTicketRespModel()
        {
            if (stubs == null || stubs.isEmpty()) {
                return null;
            }

            SharedTicketRespModel model = stubs.get(0);

            model.writer = new UserModel();
            model.writer.userID = userID;
            model.writer.name = name;
            model.writer.mobile = mobile;
            model.writer.avatar = avatar;
            model.writer.gender = gender;
            model.writer.location = location;

            return model;
        }
    }

    public static class RespGetAroundSharedTicket extends BaseRespModel
    {
        public ArrayList<NeighboursRespModel> neighbours = null;
    }

    public Call getAroundSharedTicket(int page, int limit,
                                      @NonNull ReqGetAroundSharedTicket req,
                                      @NonNull HttpRespCallback<RespGetAroundSharedTicket> callback)
    {
        callback.setRespModelClass(RespGetAroundSharedTicket.class);

        String url = generateUrl(true, "/stub/nearby/list",
                "page", String.valueOf(page), "limit", String.valueOf(limit));

        return post(url, GSON.toJson(req), callback);
    }

    /**
     * 晒票接口
     url: /user/stub/publish
     method: multipart form POST

     请求
     {
     "film": "xxx",
     "opinion": 0 | 1,
     "stubUrl" : "xxxxx",
     "stubImageWidth": xxx,
     "stubImageHeight": xxx,
     "comment" : "xxx"  //可选
     “city": "xxx"
     "district" : "xxx” //可选 哪个区
     }
     返回:
     {
     "shareUrl" : "xxxx"
     }
     */
    public static class ReqTicketShare extends BaseReqModel
    {
        public String film = "";
        public int opinion = 0;
        public String stubUrl = "";
        public int stubImageWidth = 0;
        public int stubImageHeight = 0; // 图片的宽高
        public String comment = "";
        public LocationModel location = null;
    }

    public static class RespTicketShare extends BaseRespModel
    {
        public String shareUrl = "";
    }

    public Call shareTicket(@NonNull ReqTicketShare req,
                            @NonNull HttpRespCallback<RespTicketShare> callback)
    {
        callback.setRespModelClass(RespTicketShare.class);

        String url = generateUrl("/user/stub/publish", "user_id", String.valueOf(!UserInfo.isLogin ? "" : UserInfo.userID), "user_token", UserInfo.token);

        return post(url, GSON.toJson(req), callback);
    }

    /**
     * 获取某人晒票列表
     url:/user/:userid/stub/list?user_id=xxx&user_token=xxx&page=xxx&limit=xxxx         url中:userid为用户的id，如果是自己的就和user_id参数一样，这里是为了和以后获取别人的晒票列表兼容
     method: GET
     {
     “code": xxx,
     "message": "xxx"
     “stubs”:[{
     “stubID”: xx,
     "stubImage": {
     “url”: “xxx”, //票根照片地址
     "width": xxx,
     "height":xxx
     },
     "city": "xxx",
     "district": "xxx",
     “opinion”: 0 |1, //态度
     “tags”: [“xx”, “xx”], //包含的标签
     “content” : “xxxx”, //内容
     “supportNum” : xx, //支持的数量
     “supported”: 0 | 1 // 是否支持过
     “showOffTime": xxxxx, //晒票根的时间戳
     }…]
     }
     */
    public Call getUserSharedTicketList(int userID, int page, int limit,
                                        @NonNull HttpRespCallback<RespSharedTicketList> callback)
    {
        callback.setRespModelClass(RespSharedTicketList.class);

        String url = "/user/" + String.valueOf(userID) + "/stub/list";

        url = generateUrl(true, url, "page", String.valueOf(page), "limit", String.valueOf(limit));

        return get(url, callback);
    }

    /**
     * 获取万象优图签名的接口

     url: /qcloud/signature

     method: GET

     返回:
     {
     "code" : 0,
     "message": "success",
     "sign": {
     "signature": "xxxx",
     "expired": xxxxx  //长整形，这两个值可以本地保存，上传前检查是否过期，过期后就重新请求一下
     }

     }
     */
    public static class QCloudSignRespModel
    {
        public String signature = "";
        public long expired = 0;
    }

    public static class RespGetQCloudSign extends BaseRespModel
    {
        public QCloudSignRespModel sig = null;
    }

    public Call getQCloudSign(@NonNull HttpRespCallback<RespGetQCloudSign> callback)
    {
        callback.setRespModelClass(RespGetQCloudSign.class);

        String url = generateUrl("/qcloud/signature", "user_id", String.valueOf(!UserInfo.isLogin ? "" : UserInfo.userID), "user_token", UserInfo.token);
        return get(url, callback);
    }

    /**
     *  获取用户消息接口
     url: /user/message/list?user_id&user_token=xxx
     method: GET
     返回:
     [
         {
             "msgID" : xxx,
             "title" : "xxx",
             "content": "xxx",
             "pushTime": xxxx      //长整形，推送时间戳
             "readed": 0 | 1 是否已读
         }
         ....
     ]
     */
    public static class UserMessageModel
    {
        public long msgID = 0;
        public String title = "";
        public String content = "";
        public long pushTime = 0;
        public int readed = 0;
        public int category = 0;

        public String url = "";

        public long cinecismID = 0;
        public long filmID = 0;
        public long stubID = 0;

        public UserMessage toUserMessage()
        {
            UserMessage message = new UserMessage();

            message.setMsgID(msgID);
            message.setTitle(title);
            message.setContent(content);
            message.setPushTime(pushTime);
            message.setReaded(readed);
            message.setCategory(category);
            message.setUrl(url);
            message.setCinecismID(cinecismID);
            message.setFilmID(filmID);
            message.setStubID(stubID);

            return message;
        }
    }

    public static class RespUserMessage extends BaseRespModel
    {
        public ArrayList<UserMessageModel> messages = null;
    }

    public Call getUserMessageList(int page, int limit,
                                   @NonNull HttpRespCallback<RespUserMessage> callback)
    {
        callback.setRespModelClass(RespUserMessage.class);

        String url = generateUrl(true, "/user/message/list",
                "page", String.valueOf(page), "limit", String.valueOf(limit));

        return get(url, callback);
    }

    /**
     * 去除消息未读接口
     url: /user/message/read?user_id&user_token=xxx
     method: POST
     请求:
     {
        "msgID": xx
     }
     返回:
     {
         "code": 0,
         "message": "xxx"
     }
     */
    public static class ReqReadMessage extends BaseReqModel
    {
        public long msgID = 0;
    }

    public static class RespReadMessage extends BaseRespModel
    {
        //
    }

    public Call readedMessage(@NonNull ReqReadMessage req,
                              @NonNull HttpRespCallback<RespReadMessage> callback)
    {
        callback.setRespModelClass(RespReadMessage.class);

        String url = generateUrl(true, "/user/message/read");

        return post(url, GSON.toJson(req), callback);
    }


    /**
     * 推荐影评人列表
     url: /critic/list?page=xx&limit=xxx
     method: GET

     返回
     {
     "code": 0,
     "message": "xxxx",
     "critics":{
         "sum": xxxx,
         "list":
         [
            {
             "criticID":xxx,
             "title":“xxx",   //头衔
             "name": "xxxx",
             "avatar": "xxxx"
             "cinecismNum": xxx
             "cinecism": [
         {
         "title": "xxx",
         "opinion": 0 | 1,
         "film": [{
             "filmID": xxx
             "name": "xxx",
             "post": "xxx"
         }.....]
        "createTime": "xxx"
        }
     ]
     }
     ......
     ]
     }
     }
     */
    public static class StubFilmReviewRespModel
    {
        public long cinecismID = 0;
        public String title = "";
        public int opinion = 0;
        public ArrayList<FilmRespModel> film = null;
        public Date createTime = null;
    }

    public static class CriticRespModel extends BaseRespModel
    {
        public long criticID = 0;
        public String title = "";
        public String name = "";
        public String avatar = "";
        public int cinecismNum = 0;
        public int followed = 0;
        public ArrayList<StubFilmReviewRespModel> cinecism = null;
    }

    public static class CriticSumRespModel
    {
        public int sum = 0;
        public ArrayList<CriticRespModel> list = null;
    }

    public static class RespGetCritic extends BaseRespModel
    {
        public CriticSumRespModel critics = null;
    }

    public Call getCriticList(int page, int limit, @NonNull HttpRespCallback<RespGetCritic> callback)
    {
        callback.setRespModelClass(RespGetCritic.class);

        String url = generateUrl(true, "/critic/list", "page", String.valueOf(page), "limit", String.valueOf(limit));

        return get(url, callback);
    }

    /**
     *推荐影评列表
     url: /cinecism/list?page=xx&limit=xxx
     method: GET

     返回
     {
     "code": 0,
     "message": "xxxx",
     "cinecisms":
     [
     {
     "cinecismID":xxx,
     "title":“xxx",
     "srcMedia": "xxx", //媒体来源
     "srcUrl" : "xxx" //原文链接
     "srcScore": xxxx //原媒体评分
     "film": [{
     "filmID": xxx
     "name": "xxx",
     "post": "xxx"
     }.....]
     "writer": {
     "criticID":xxx,
     "title":“xxx",   //头衔
     "name": "xxxx",
     "avatar": "xxxx"
     }
     }
     ......
     ]
     }
     */
    public static class FilmRespModel
    {
        public long filmID = 0;
        public String name = "";
        public String post = "";

        public String cast = "";
        public String country = "";
        public String releaseDate = "";
        public String index = "";
    }

    public static class FilmReviewRespModel extends BaseRespModel
    {
        public long cinecismID = 0;
        public String title = "";
        public String srcMedia = "";
        public String srcUrl = "";
        public String srcScore = "";
        public String logo = "";
        public String summary = "";
        public int opinion = 0;

        public Date createTime = new Date();

        public ArrayList<FilmRespModel> film = new ArrayList<>();
        public CriticRespModel writer = new CriticRespModel();
    }

    public static class RespGetFilmReview extends BaseRespModel
    {
        public ArrayList<FilmReviewRespModel> cinecisms = null;
    }

    public Call getFilmReviewList(int page, int limit,
                                  @NonNull HttpRespCallback<RespGetFilmReview> callback)
    {
        callback.setRespModelClass(RespGetFilmReview.class);

        String url = generateUrl("/cinecism/list", "page", String.valueOf(page), "limit", String.valueOf(limit));

        return get(url, callback);
    }

    /**
     * 26 获取影评人详情接口
     url:/critic/:crid/details?user_id=xxxx&user_token=xxxx
     method: GET
     返回：
     {
     "criticID": xxx,
     "name": "xxxx",
     "title": "xxx",
     "avatar": "xxx“，
     ”summary": "xxx",
     "introduction": "xxx",
     "followed": 0 | 1 //是否关注
     }
     */
    public static class CriticDetailRespModel
    {
        public long criticID = 0;
        public String name = "";
        public String title = "";
        public String avatar = "";
        public String summary = "";
        public String introduction = "";
        public int followed = 0;
    }
    public static class RespGetCriticDetail extends BaseRespModel
    {
        public CriticDetailRespModel critic = null;
    }

    public Call getCriticDetail(long criticID, @NonNull HttpRespCallback<RespGetCriticDetail> callback)
    {
        callback.setRespModelClass(RespGetCriticDetail.class);

        String url = generateUrl(true, "/critic/" + criticID + "/details");

        return get(url, callback);
    }


    /**
     * TODO: 获取电影相关影评
     url: /film/:fid/cinecism/list?page=xxx&limit=xxxx
     method:GET
     返回{
     "code" : xxx,
     "message": "xxxx",
     "cinecisms": [
     {
     "positiveSum" :xxx,
     "negtiveSum": xxx,
     "list": [
     {
     "cinecismID":xxx,
     "title":“xxx",
     "srcMedia": "xxx", //媒体来源
     "srcUrl" : "xxx" //原文链接
     "srcScore": xxxx //原媒体评分
     "film": [{
     "filmID": xxx
     "name": "xxx",
     "post": "xxx"
     }.....]
     "writer": {
     "criticID":xxx,
     "title":“xxx",   //头衔
     "name": "xxxx",
     "avatar": "xxxx"
     }
     }
     ]
     }
     ]
     }
     */

    /**
     * TODO: 获取电影相关晒票列表
     url: /film/:fid/stub/list?page=xxx&limit=xxx
     method: GET
     返回

     {
     "code": 0,
     "method": "xxxx"
     "stubs": {
     “positiveSum”:xx,
     "negtiveSum": xx,
     “list”:[
     {
     “stubID”: xx,
     “stubImageUrl”: “xxx”, //票根照片地址
     “opinion”: -1 | 0 |1, //态度
     }
     ]
     }
     }
     */

    /**
     *TODO: 30 获取影片相关资讯
     url: /film/:fid/news?page=xxx&limit=xxxx
     method: GET
     返回：
     {
     "code": 0,
     "message": "xxxx",
     "news" [
     {
     “newsID": xx,
     "title": "xxx",
     "logo"： ”xxxx",
     "url": "xxx",
     "publishTime": xxxxxx 时间戳
     }
     ]
     }
     */


    /**
     *TODO: 31 获取影评人的影评列表
     url: /critic/:cid/cinecism/list?page=xxx&limit=xxxx
     method: GET
     返回
     {
     "code": 0,
     "message": "xxxx",
     "cinecisms":
     [
     {
     "cinecismID":xxx,
     "title":“xxx",
     "srcMedia": "xxx", //媒体来源
     "srcUrl" : "xxx" //原文链接
     "srcScore": xxxx //原媒体评分
     "film": [{
     "filmID": xxx
     "name": "xxx",
     "post": "xxx"
     }.....]
     }
     ......
     ]
     }
     */
    public Call getCriticFilmReviewList(long cid, int page, int limit,
                                        @NonNull HttpRespCallback<RespGetFilmReview> callback)
    {
        callback.setRespModelClass(RespGetFilmReview.class);

        String url = "/critic/" + cid + "/cinecism/list";
        url = generateUrl(url, "page", String.valueOf(page), "limit", String.valueOf(limit));

        return get(url, callback);
    }


    /**
     *TODO: 32 和电影相关的影评
     url: /film/:fid/cinecism/list?page=xxx&limit=xxx
     method: GET
     返回
     {
     "code": 0,
     "message": "xxxx",
     "cinecisms":
     [
     {
         "cinecismID":xxx,
         "title":“xxx",
         "srcMedia": "xxx", //媒体来源
         "srcUrl" : "xxx" //原文链接
         "srcScore": xxxx //原媒体评分
         "writer": {
             "criticID":xxx,
             "title":“xxx",   //头衔
             "name": "xxxx",
             "avatar": "xxxx"
         }

     }
     ......
     ]
     }
     */
//    public static class ReviewAboutFilmRespModel
//    {
//        public long cinecismID = 0;
//        public String title = "";
//        public String srcMedia = "";
//        public String srcUrl = "";
//        public int srcScore = 0;
//        public long createTime = 0;
//
//        public CriticRespModel writer = null;
//    }

    public static class RespReviewAboutFilm extends BaseRespModel
    {
        public ArrayList<FilmReviewRespModel> cinecisms = null;
    }

    public Call getReviewAboutFilm(long fid, int page, int limit,
                                   @NonNull HttpRespCallback<RespReviewAboutFilm> callback)
    {
        callback.setRespModelClass(RespReviewAboutFilm.class);

        String url = "/film/" + String.valueOf(fid) + "/cinecism/list";

        url = generateUrl(url, "page", String.valueOf(page), "limit", String.valueOf(limit));

        return get(url, callback);
    }


    /**
     * 33 获取影评详情接口
     url: /cinecism/:cnid/details   :cnid为影评id
     method: GET
     返回:
     {
     "code": 0,
     "message": "xxxx",

     "cinecism": {
         "cinecismID":xxx,
         "title":“xxx",
         "content": "xxx",
         "srcMedia": "xxx", //媒体来源
         "srcUrl" : "xxx" //原文链接
         "srcScore": xxxx //原媒体评分

         "critic": {
             "criticID":xxx,
             "title":“xxx",   //头衔
             "name": "xxxx",
             "avatar": "xxxx" ,
             "cinecismNum": xxx
         },
         "supporters":{
             "sum": xxx,
             "avatars":["xxx", "xx", "xxx"]
         }
     }

     }
     */
    public static class SupportersRespModel
    {
        public int sum = 0;
        public ArrayList<String> avatars = null;
    }

    public static class ReviewDetailRespModel
    {
        public long cinecismID = 0;
        public String title = "";
        public String content = "";
        public String summary = "";
        public String srcMedia = "";
        public String srcUrl = "";
        public String srcScore = "";
        public long createTime = 0;
        public String logo = "";
        public int supported = 0;
        public String shareUrl = "";

        public CriticRespModel critic = null;

        public SupportersRespModel supporters = new SupportersRespModel();
    }

    public static class RespGetReviewDetail extends BaseRespModel
    {
        public ReviewDetailRespModel cinecism = null;
    }

    public Call getReviewDetail(long cnid, String filmID,@NonNull HttpRespCallback<RespGetReviewDetail> callback)
    {
        callback.setRespModelClass(RespGetReviewDetail.class);
        String url;
        if (filmID==null){
            url = "/cinecism/" + cnid + "/details";
        }else{
            url = "/film/" + filmID + "/cinecism/from/critic/" + cnid;
        }

        url = generateUrl(true, url);

        LogUtils.i("123",url);
        return get(url, callback);
    }

    /**
     * 34 影评详情界面获取简单电影介绍的接口
     url: /cinecism/:cnid/film/info
     method: GET
     返回：
     {
     "code": 0,
     "message": "xxx",
     "film": {
     "filmID“: xxxx,
     "name": "xxxx",
     "post": "xxx",
     "cast": "xxx",
     "country": "xxx",
     "releaseDate": "xxx"
     "index":xx //大银幕指数
     }
     }
     */
    public static class RespGetReviewFilmInfo extends BaseRespModel
    {
        public ArrayList<FilmRespModel> films = null;
    }

    public Call getReviewFilmInfo(long cnid, @NonNull HttpRespCallback<RespGetReviewFilmInfo> callback)
    {
        callback.setRespModelClass(RespGetReviewFilmInfo.class);

        String url = "/cinecism/" + cnid + "/film/info";

        return get(url, callback);
    }


//     获取城市列表
//    {
//        "code": 0,
//            "message": "xxx"
//        "cities" : [
//        "name": "xxx",
//            "districts":["xxx", "xxx"]
//        ]
//    }

    public static class RespCityList extends BaseRespModel
    {

        public ArrayList<CityRespModel> cities = null;
    }

    public static class CityRespModel implements Serializable
    {
        public String name = "";
        public ArrayList<String> districts = null;
    }

    public Call getCityList(@NonNull HttpRespCallback<RespCityList> callback)
    {
        callback.setRespModelClass(RespCityList.class);

        String url = generateUrl("/ticket/lbs/info");

        return get(url, callback);
    }

    /**
     * 36 删除晒票
     url /stub/delete?user_id=xxx&user_token=xxx
     method: POST
     请求：
     {
     "stubID": xxx
     }
     返回：
     {
     "code": xxx,
     "message":"xxx"
     }
     */
    public static class ReqDeleteSharedTicket extends BaseReqModel
    {
        public long stubID = 0;
    }

    public static class RespDeleteSharedTicket extends BaseRespModel
    {
        //
    }

    public Call deleteSharedTicket(@NonNull ReqDeleteSharedTicket req,
                                   @NonNull HttpRespCallback<RespDeleteSharedTicket> callback)
    {
        callback.setRespModelClass(RespDeleteSharedTicket.class);

        String url = generateUrl(true, "/stub/delete");

        return post(url, GSON.toJson(req), callback);
    }

//    获取影院列表
//    {
//        "code":xx,
//            "message": "xxx",
//            "cinemas":[
//        {
//            "cinemaID": xxx,
//                "name": "xxx",
//                "address": "xxxx",
//                "tel": "xxxx"
//            "minPrice": xxxx //当前时段该影院的最低价
//        }
//        ]
//    }
//    http://api-test.dymfilm.com/cinema/list?city=上海市&district=长宁区&lng=&lat=
//    url:                       /cinema/list/showing/film/:filmID?date=yyyyMMdd&city=xxxx&district=xxx&lng=xxxx&lat=xxxx &page=xxx&limit=xxx4个参数都是可选项
    public static class RespCinemaList extends BaseRespModel
    {

        public ArrayList<CinemaRespModel> cinemas = null;
    }
    public static class CinemaRespModel
    {
        public String cinemaID = "";
        public String name = "";
        public String address = "";
        public String tel = "";
        public String longitude = "";
        public String latitude = "";
        public String minPrice = "";
        public String showingThisFilm = "";
    }

    public Call getCinemaList(boolean flag,String filmId,String date,String city,String district,String lng,String lat, int page,
                              int limit,
            @NonNull HttpRespCallback<RespCinemaList> callback)
    {
        callback.setRespModelClass(RespCinemaList.class);

        String str="";
        if(!TextUtils.isEmpty(city)){
            str+="city="+(city);
        }
        str+="&date="+date;
        if(!TextUtils.isEmpty(district)){
            str+="&district="+(district);
        }
        if(!TextUtils.isEmpty(lat)){
            str+="&lat="+lat;
        }
        str+="&limit="+limit;
        if(!TextUtils.isEmpty(lng)){
            str+="&lng="+lng;
        }
        str+="&page="+page;

        if(str.charAt(0)=='&'){
            str=str.substring(1,str.length());
        }
        String sign= CipherUtils.HmacSHA256(str,ConfigInfo.hmackey);
        String url="";
        if(flag){
            //从底部tab进去

            String str1="";
            if(!TextUtils.isEmpty(city)){
                str1+="city="+(city);
            }
            if(!TextUtils.isEmpty(district)){
                str1+="&district="+(district);
            }
            if(!TextUtils.isEmpty(lat)){
                str1+="&lat="+lat;
            }
            str1+="&limit="+limit;
            if(!TextUtils.isEmpty(lng)){
                str1+="&lng="+lng;
            }
            str1+="&page="+page;

            if(str1.charAt(0)=='&'){
                str1=str1.substring(1,str1.length());
            }
            String sign1= CipherUtils.HmacSHA256(str1,ConfigInfo.hmackey);

            url= generateUrl("/cinema/list/v2","city", encode(city), "district", encode(district),"lng", String.valueOf(lng), "lat", String.valueOf(lat),
                   "page", String.valueOf(page),"limit", String.valueOf(limit),"sign",sign1);
        }else{
            //从影片详情进去
            url= generateUrl("/cinema/list/showing/film/"+filmId+"/v3","date",date,"city", encode(city), "district", encode(district),
                    "lng", String.valueOf(lng), "lat", String.valueOf(lat),"page", String.valueOf(page),"limit", String.valueOf(limit),"sign",sign);
        }

        return get(url, callback);
    }
    //第一次默认显示的影院
    public Call getCinemaList(String city,String district,String lng,String lat, int page,
                              int limit,
            @NonNull HttpRespCallback<RespCinemaList> callback)
    {
        callback.setRespModelClass(RespCinemaList.class);

        String url = generateUrl("/cinema/list","city", encode(city), "district", encode(district),
                "lng", String.valueOf(lng), "lat", String.valueOf(lat),"page", String.valueOf(page),"limit", String.valueOf(limit));

        return get(url, callback);
    }

//      获取搜索结果
//      "code": xxx,
//
//            "message": "xxx",
//
//            "films": [
//        {
//            "filmID": xxxx,
//
//                "name": "xxx",
//
//                "country": "xxx",
//
//                "post": "xxxx",
//
//                "director": "xxxx",
//
//                "cast": "xxxx",
//
//                "dymIndex": xxx,
//
//                "releaseDate": "xxx"
//
//            "stubIndex": xxxx
//        }
//        ]

    public static class RespSearchFilmList extends BaseRespModel
    {
        public RespSearchResultFilmList result = null;
    }
    public static class RespSearchResultFilmList extends BaseRespModel
    {
        public String sum="";
        public ArrayList<SearchFilmModel> films = new ArrayList<>();
    }
    public static class SearchFilmModel extends BaseRespModel
    {
        public String filmID = "";
        public String name = "";
        public String country = "";
        public String post = "";
        public String director = "";
        public String cast = "";
        public String dymIndex = "";
        public String releaseDate = "";
        public String stubIndex = "";
        public String status = "";
    }
//    /film/search?name=xxx&cat=xxx&year=xxx&country=xxx&order=xx&page=xxx&limit=xxx
//    name电影名称, cat电影类型,year发行年份，country电影所属国家，order排序方法
//    以上都是可选的，
//    order不填默认按发行年份排序  填stub按票根指数，填dym按大银幕指数

    public Call getSearchList(String name,
                              String cat,
                              String year,
                              String country,
                              String page,
                              String limit,
                              @NonNull HttpRespCallback<RespSearchFilmList> callback)
    {
        callback.setRespModelClass(RespSearchFilmList.class);

        String str="";
        if(!TextUtils.isEmpty(cat)){
            str+="cat="+(cat);
        }
        if(!TextUtils.isEmpty(country)){
            str+="&country="+(country);
        }
        str+="&limit="+limit;
        if(!TextUtils.isEmpty(name)){
            str+="&name="+name;
        }
        str+="&page="+page;
        if(!TextUtils.isEmpty(year)){
            str+="&year="+year;
        }
        if(str.charAt(0)=='&'){
            str=str.substring(1,str.length());
        }
        String sign= CipherUtils.HmacSHA256(str,ConfigInfo.hmackey);

        String url = generateUrl("/film/search/v2","name", encode(name), "cat", String.valueOf(cat),"year", String.valueOf(year),
                "country", String.valueOf(country), "page", String.valueOf(page),"limit", String.valueOf(limit),"sign",sign);

        return get(url, callback);
    }
//    74 获取热搜电影名称推荐接口
    public static class RespSearchFilmHotList extends BaseRespModel
    {
        public ArrayList<String> films = new ArrayList<>();
    }
    public Call getSearchFilmHotList( @NonNull HttpRespCallback<RespSearchFilmHotList> callback)
    {
        callback.setRespModelClass(RespSearchFilmHotList.class);

        String url = generateUrl("/hot/searching/film/list");

        return get(url, callback);
    }

//    75 获取热搜电影影评推荐接口
    public static class RespSearchReviewHotList extends BaseRespModel
    {
        public ArrayList<String> critics = new ArrayList<>();
    }
    public Call getSearchReviewHotList( @NonNull HttpRespCallback<RespSearchReviewHotList> callback)
    {
        callback.setRespModelClass(RespSearchReviewHotList.class);

        String url = generateUrl("/hot/searching/critic/list");

        return get(url, callback);
    }

    //    72 搜索电影相关影评
    public static class RespSearchReviewList extends BaseRespModel
    {
        public RespSearchResultReviewList result = new RespSearchResultReviewList();
    }
    public static class RespSearchResultReviewList
    {
        public String sum="";
        public ArrayList<SearchReviewCinecismModel> cinecisms = new ArrayList<>();
    }
    public static class SearchReviewCinecismModel extends BaseRespModel
    {
        public String cinecismID = "";
        public String title = "";
        public String srcMedia = "";
        public String srcScore = "";
        public String srcUrl = "";
        public String logo = "";
        public String summary = "";
        public ArrayList<SearchReviewFilmModel> film = new ArrayList<>();
        public String createTime = "";
        public SearchReviewWriterModel writer = new SearchReviewWriterModel();
        public String opinion = "";
    }
    public static class SearchReviewFilmModel
    {
        public String filmID = "";
        public String name = "";
        public String post = "";
        public String country = "";
        public String director = "";
        public String cast = "";
        public String digest = "";
        public String releaseDate = "";
        public String dymIndex = "";
        public String status = "";
    }
    public static class SearchReviewWriterModel
    {
        public String criticID = "";
        public String name = "";
        public String title = "";
        public String avatar = "";
    }
    public Call getSearchReviewList(String name,
                              String page,
                              String limit,
                              @NonNull HttpRespCallback<RespSearchReviewList> callback)
    {
        callback.setRespModelClass(RespSearchReviewList.class);

        String url = generateUrl("/cinecism/of/film","filmName", encode(name), "page", String.valueOf(page),"limit", String.valueOf(limit));

        return get(url, callback);

    }

//    73 搜索影评人接口
    public static class RespSearchAuthorList extends BaseRespModel
    {
        public RespSearchResultAuthorList result = new RespSearchResultAuthorList();
    }
    public static class RespSearchResultAuthorList
    {
        public String sum="";
        public ArrayList<CriticRespModel> critics = new ArrayList<>();
    }
    public Call getSearchAuthorList(String name,
                                    String page,
                                    String limit,
                                    @NonNull HttpRespCallback<RespSearchAuthorList> callback)
    {
        callback.setRespModelClass(RespSearchAuthorList.class);

        String url = generateUrl("/critic/search","name", encode(name), "page", String.valueOf(page),"limit", String.valueOf(limit));

        return get(url, callback);

    }


//    85 综合搜索
//    url: /synth/search?key=xxxxx
    public static class RespSearchResultList extends BaseRespModel
    {
        public RespSearchResultALLList result = new RespSearchResultALLList();
    }
    public static class RespSearchResultALLList
    {
        public ArrayList<SearchFilmModel> films = new ArrayList<>();
        public ArrayList<CriticRespModel> critics = new ArrayList<>();
        public ArrayList<FilmReviewRespModel> cinecisms = new ArrayList<>();
    }
    public Call getSearchResultList(String name,@NonNull HttpRespCallback<RespSearchResultList> callback)
    {
        callback.setRespModelClass(RespSearchResultList.class);

        String url = generateUrl("/synth/search","key", encode(name));

        return get(url, callback);

    }



    /**
     * 38 关注影评人
     url: /user/follow/critic?user_id=xxx&user_token=xxx
     method: POST
     请求:
     {
     "criticID": xxxx
     }
     返回:
     {
     “code":xx,
     "message": "xx"
     }
     */
    public static class ReqFollowCritic extends BaseReqModel
    {
        public long criticID = 0;
    }

    public static class RespFollowCritic extends BaseRespModel
    {
        //
    }

    public Call followCritic(@NonNull ReqFollowCritic req,
                             @NonNull HttpRespCallback<RespFollowCritic> callback)
    {
        callback.setRespModelClass(RespFollowCritic.class);

        String url = generateUrl(true, "/user/follow/critic");

        return post(url, GSON.toJson(req), callback);
    }

    /**
     * 39 关注电影
     url: /user/follow/film?user_id=xxx&user_token=xxx
     method: POST
     请求:
     {
     "filmID": xxxx
     }
     返回:
     {
     “code":xx,
     "message": "xx"
     }
     */
    public static class ReqFollowFilm extends BaseReqModel
    {
        public long filmID = 0;
    }

    public static class RespFollowFilm extends BaseRespModel
    {
        //
    }

    public Call followFilm(@NonNull ReqFollowFilm req,
                           @NonNull HttpRespCallback<RespFollowFilm> callback)
    {
        callback.setRespModelClass(RespFollowFilm.class);

        String url = generateUrl(true, "/user/follow/film");

        return post(url, GSON.toJson(req), callback);
    }

    /**
     *40 取消影评人关注
     url: /user/unfollow/critic?user_id=xxx&user_token=xxx
     method: POST
     请求:
     {
     "criticID": xxxx
     }
     返回:
     {
     “code":xx,
     "message": "xx"
     }
     */
    public static class ReqUnFollowCritic extends BaseReqModel
    {
        public long criticID = 0;
    }

    public static class RespUnFollowCritic extends BaseRespModel
    {
        //
    }

    public Call unFollowCritic(@NonNull ReqUnFollowCritic req,
                               @NonNull HttpRespCallback<RespUnFollowCritic> callback)
    {
        callback.setRespModelClass(RespUnFollowCritic.class);

        String url = generateUrl(true, "/user/unfollow/critic");

        return post(url, GSON.toJson(req), callback);
    }

    /**
     * 41 取消电影关注
     url: /user/unfollow/film
     method: POST
     请求:
     {
     "filmID": xxxx
     }
     返回:
     {
     “code":xx,
     "message": "xx"
     }
     */
    public static class ReqUnFollowFilm extends BaseReqModel
    {
        public long filmID = 0;
    }

    public static class RespUnFollowFilm extends BaseRespModel
    {
        //
    }

    public Call unFollowFilm(@NonNull ReqUnFollowFilm req,
                             @NonNull HttpRespCallback<RespUnFollowFilm> callback)
    {
        callback.setRespModelClass(RespUnFollowFilm.class);

        String url = generateUrl(true, "/user/unfollow/film");

        return post(url, GSON.toJson(req), callback);
    }

    /**
     * 42 获取关注的影评人
     url: /user/critics/followed?user_id=xxx&user_token=xxx&page=xxx&limit=xxx
     method: GET
     返回：
     {
     "code": 0,
     "message": "xxxx",
     critics:[
     {
     "criticID":xxx,
     "title":“xxx",   //头衔
     "name": "xxxx",
     "avatar": "xxxx"
     "cinecismNum": xxx
     "cinecism": [
     {
     "cinecismID":xxx
     "title": "xxx",
     "opinion": 0 | 1,

     "film": [{
     "filmID": xxx
     "name": "xxx",
     "post": "xxx"
     }.....]
     "createTime": "xxx"
     }
     ]
     }
     ......

     ]
     }
     */


    /**
     * 43获取关注的电影列表
     url: /user/films/followed?user_id=xxxx&user_token=xxx&page=xxx&limit=xxxx
     method: GET
     返回：
     {
     "code": xxx,
     "message": "xxx",
     "films": [
     {
     "filmID": xxxx,
     "name": "xxx",
     "country": "xxx",
     "post": "xxxx",
     "director": "xxxx",
     "cast": "xxxx",
     "dymIndex": xxx,
     "releaseDate": "xxx"
     "stubIndex": xxxx
     }
     ]
     }
     */

    /**
     * 44 搜索影片接口
     url:/film/search?name=xxx&cat=xxx&year=xxx&country=xxx&order=xx&page=xxx&limit=xxx
     name电影名称, cat电影类型,year发行年份，country电影所属国家，order排序方法
     以上都是可选的，
     order不填默认按发行年份排序  填stub按票根指数，填dym按大银幕指数
     method: GET
     返回
     {
     "code": xxx,
     "message": "xxx",
     "films": [
     {
     "filmID": xxxx,
     "name": "xxx",
     "country": "xxx",
     "post": "xxxx",
     "director": "xxxx",
     "cast": "xxxx",
     "dymIndex": xxx,
     "releaseDate": "xxx"
     "stubIndex": xxxx
     }
     ]
     }
     */

    /**
     * 45 获取影评详情界面相关影评
     url:/cinecism/:cnid/film/:fid/relative
     method: GET
     返回：
     {
     "code": 0,
     "message": "xxx",
     "cinecisms":
     [
     {
     "cinecismID":xxx,
     "title":“xxx",
     "srcMedia": "xxx", //媒体来源
     "srcUrl" : "xxx" //原文链接
     "srcScore": xxxx //原媒体评分
     "opinion": 0 | 1,
         "writer": {
             "criticID":xxx,
             "title":“xxx",   //头衔
             "name": "xxxx",
             "avatar": "xxxx"
         }

     }
     ......
     ]
     }
     */

    public static class RespReviewAboutReview extends BaseRespModel
    {
        public ArrayList<FilmReviewRespModel> cinecisms = null;
        public int sum = 0;
    }

    public Call getReviewAboutReview(long cnid, long fid, int page, int limit,
                                   @NonNull HttpRespCallback<RespReviewAboutReview> callback)
    {
        callback.setRespModelClass(RespReviewAboutReview.class);

        String url = "/cinecism/" + cnid + "/film/" + String.valueOf(fid) + "/relative";

        url = generateUrl(url, "page", String.valueOf(page), "limit", String.valueOf(limit));

        return get(url, callback);
    }

    /**
     * 49 点赞影评
     url : /user/support/cinecism?user_id=xxx&user_token=xxx
     method: POST
     请求
     {
     "cinecismID": xxxx
     }
     返回
     {
     "code" :xxxx,
     "message" : "xxx"
     }
     */
    public static class ReqSupportReview extends BaseReqModel
    {
        public long cinecismID = 0;
    }

    public static class RespSupportReview extends BaseRespModel
    {
        public int sum = 0;
    }

    public Call supportFilmReview(@NonNull ReqSupportReview req,
                                  @NonNull HttpRespCallback<RespSupportReview> callback)
    {
        callback.setRespModelClass(RespSupportReview.class);

        String url = generateUrl(true, "/user/support/cinecism");

        return post(url, GSON.toJson(req), callback);
    }


//    53 获取影院正在放映的影片
//    http://api-test.dymfilm.com/cinema/99/showing/film/list?date=2015-12-10
//    "code": xxx,
//    "message": xxxx,
//     "films":{
//              "sum":xxxx, //购票总人数
//              "price": xxxx, //兑换券价格
//
//            "list"[
//                    {
//               "filmID": xxxx,
//            "name": "xxx",
//            "country": "xxx",
//            "post": "xxxx",
//            "director": "xxxx",
//            "cast": "xxxx",
//            "releaseDate": "xxx"
//              }
//    ]


    public static class RespCinemaFilmList extends NetworkManager.BaseRespModel
    {
        public RespCinemaFilmLists films = new RespCinemaFilmLists();
    }
    public static class RespCinemaFilmLists
    {
        public String sum="";

        public ArrayList<CinemaFilmModel> list = new ArrayList<>();
    }
    public static class CinemaFilmModel
    {
        public String filmID = "";
        public String name = "";
        public String country = "";
        public String post = "";
        public String director = "";
        public String cast = "";
        public String dymIndex = "";
        public String releaseDate = "";
        public String stubIndex = "";
        public String price="";
    }

    public Call getCinemaFilmList(String cinemaId,String date,@NonNull HttpRespCallback<RespCinemaFilmList> callback){
        callback.setRespModelClass(RespCinemaFilmList.class);

        String str="";
        if(!TextUtils.isEmpty(date)){
            str+="date="+(date);
        }
        if(str.charAt(0)=='&'){
            str=str.substring(1,str.length());
        }
        String sign= CipherUtils.HmacSHA256(str,ConfigInfo.hmackey);

        String url = "/cinema/" + cinemaId + "/showing/film/list/v2";
        url = generateUrl(url, "date",date,"sign",sign);
        return get(url, callback);
    }

//    54 获取火票务在某影院关于某影片在某日的电影票列表
//
//    http://api-test.dymfilm.com/cinema/38/film/12404/ticket/list?date=2015-12-10

//    "code": xxxx,
//        "message": "xxxx",
//        "tickets": [
//    {
//        "startTime"："HH:mm",
//            "endTime"："HH:mm",
//            "price": xxx,
//            "remark": "xxx"  //一般就是2d/几号厅
//            "source": "xxx", //电影票信息来源
//            "srcLogo": "xxx"//来源logo
//    }
//    ]

    public static class MYDATE implements Serializable
    {
        public String date="";
        public String day = "";
        public String week = "";
        public String time = "";
    }
    public static class RespTicketPriceList extends BaseRespModel
    {
        public ArrayList<TicketPriceModel> tickets = null;
    }
    public static class TicketPriceModel
    {
        public boolean flag=false;
        public String startTime = "";
        public String endTime = "";
        public float price =0f;
        public String remark = "";
        public String language = "";
        public String source = "";
        public String srcLogo = "";
        public String srcAndroidEntry = "";
    }

    public Call getHotTicketPriceList(String cinemaId,String filmId,String date,@NonNull HttpRespCallback<RespTicketPriceList> callback){
        callback.setRespModelClass(RespTicketPriceList.class);

        String url = "/cinema/" + cinemaId + "/film/"+filmId+"/ticket/list/v2" ;

        String str="";
        if(!TextUtils.isEmpty(date)){
            str+="date="+(date);
        }
        if(str.charAt(0)=='&'){
            str=str.substring(1,str.length());
        }
        String sign= CipherUtils.HmacSHA256(str,ConfigInfo.hmackey);
//        LogUtils.e("sign  "+sign);

        url = generateUrl(url, "date",date,"sign",sign);

        return get(url, callback);
    }
//    55 获取比价电影票列表
//
//    url:/cinema/:cnid/film/:fid/other/ticket/list?date=yyyy-MM-dd&time=HH:mm  注意后面HH:mm中的冒号要url编码

    public Call getTicketPriceList(String cinemaId,String filmId,String date,String time,@NonNull HttpRespCallback<RespTicketPriceList> callback){
        callback.setRespModelClass(RespTicketPriceList.class);


        String str="";
        if(!TextUtils.isEmpty(date)){
            str+="date="+(date);
        }
        if(!TextUtils.isEmpty(time)){
            str+="&time="+(time);
        }
        if(str.charAt(0)=='&'){
            str=str.substring(1,str.length());
        }
        String sign= CipherUtils.HmacSHA256(str,ConfigInfo.hmackey);

        String url = "/cinema/" + cinemaId + "/film/"+filmId+"/other/ticket/list/v2" ;

        url = generateUrl(url, "date",date,"time",time,"sign",sign);

        return get(url, callback);
    }


//    56 请求生成订单接口
//    url: /ticket/order/generate?user_id=xxxx&user_token=xxx
//    method: POST
//    请求： {
//    "count": xxxx //总票数
//    "amount": xxxx//总价格
//}
//    返回：
//              "code": xxxx,
//            "message": "xxx",
//            "orderCode": "xxxx"

    public static class RespOrderCode extends BaseRespModel
    {
        public String orderCode = null;
    }

    public Call postRespOrderCode(int count,float amount,int filmID,int cinemaID,String mobile,String user_id,String user_token,@NonNull HttpRespCallback<RespOrderCode> callback){
        callback.setRespModelClass(RespOrderCode.class);
        String url = "/ticket/order/generate" ;
        url = generateUrl(url,"user_id",user_id,"user_token",user_token);
        String jsonstring="{\"amount\":"+amount+",\"cinemaID\":"+cinemaID+",\"filmID\":"+filmID+",\"count\":"+count+",\"mobile\":\""+mobile+"\"}";

        return post(url,jsonstring,callback);
    }

//    59 获取有效电影名称
//    url: /film/valid/list?filmID=xxx&&cinemaID=xxx
//    method:GET
//    {
//        "code": xxx,
//            "message": "xxx",
//            "films": [“xxx", "xxx", "xx"]
//
//    }
    public static class RespVoucherList extends NetworkManager.BaseRespModel
    {
        public RespVoucherLists films = new RespVoucherLists();
    }
    public static class RespVoucherLists extends NetworkManager.BaseRespModel
    {
        public String tktNum="";
        public ArrayList<String> list = new ArrayList<String>();
    }
    public Call getVoucherList(String filmId,String cinemaId,@NonNull HttpRespCallback<RespVoucherList> callback){
        callback.setRespModelClass(RespVoucherList.class);
        String url = "/film/valid/list" ;
        url = generateUrl(url, "filmID",filmId,"cinemaID",cinemaId);
        return get(url, callback);
    }

//    62 app设置接口
//
//    url:/app/settings
//
//    method:GET
//        返回
//    {"code":0,"message":"success","settings":{"version":"1.0","cityListUpdated":"0"}}

        public static class RespAppSetting extends NetworkManager.BaseRespModel
        {
            public RespAppSet settings = new RespAppSet();
        }
        public static class RespAppSet
        {
            public String android_version = "";
            public String updateDesc = "";
            public String androidUpdateDesc = "";
            public String cityListUpdated = "";
            public String androidDownloadUrl = "";
        }

    public Call getAppSetting(@NonNull HttpRespCallback<RespAppSetting> callback){
        callback.setRespModelClass(RespAppSetting.class);
        String url = "/app/settings" ;
        url = generateUrl(url);
        LogUtils.i("123",url);
        return get(url, callback);
    }


    /**
     * 58获取banner接口
     url: /app/main/banner/list?category=xxx               category为1表示主界面banner 为2表示影评界面banner
     method: GET
     {
     "code": 0,
     "message": "xxx",
     "banners":[
     {
     "img": "xxx",
     "url": "xxx"
     }
     ]
     }
     */
    public static class BannerModel
    {
        public String img = "";
        public String url = "";
    }

    public static class RespGetBanner extends BaseRespModel
    {
        public ArrayList<BannerModel> banners = null;
    }

    public Call getBanners(int category, @NonNull HttpRespCallback<RespGetBanner> callback)
    {
        callback.setRespModelClass(RespGetBanner.class);

        String url = generateUrl("/app/main/banner/list", "category", String.valueOf(category));

        return get(url, callback);
    }

    /**
     * 63 删除消息
     url：/user/delete/message?user_id=xxx&user_token=xxx
     method: POST
     请求:
     {
     "messageID": xxxx
     }
     返回:
     {
     "code": xxxx,
     "message":  "xxxx"
     }
     */
    public static class ReqDeleteMessage extends BaseReqModel
    {
        public long messageID = 0;
    }

    public static class RespDeleteMessage extends BaseRespModel
    {
        //
    }

    public Call deleteMessage(@NonNull ReqDeleteMessage req,
                              @NonNull HttpRespCallback<RespDeleteMessage> callback)
    {
        callback.setRespModelClass(RespDeleteMessage.class);

        String url = generateUrl(true, "/user/delete/message");

        return post(url, GSON.toJson(req), callback);
    }

    /**
     * 69 晒票评论接口

     url:/stub/comment/submit?user_id=xxxx&user_token=xxx
     method: POST
     请求:
     {
     "stubID":  xxx,
     "comment": "xxx"
     }
     返回:
     {
     "code": 0,
     "message": "xxx"
     }
     */
    public static class ReqStubComment {
        public long stubFollowID = 0;
        public long refSubFollow = 0;
    }
    public static class ReqCommentShareTicket extends BaseReqModel
    {
        public long stubID = 0;
        public String comment = "";
        public ReqStubComment subFollow = null;
    }

    public static class RespCommentFollowInfo {
        public long sutFollowID = 0;
        public ArrayList<StubComment> subFollows = new ArrayList<>();
    }
    public static class RespCommentShareTicket extends BaseRespModel
    {
        public RespCommentFollowInfo followInfo = null;
    }

    public Call commentShareTicket(@NonNull ReqCommentShareTicket req,
                                   @NonNull HttpRespCallback<RespCommentShareTicket> callback)
    {
        callback.setRespModelClass(RespCommentShareTicket.class);

        String url = generateUrl(true, "/stub/comment/submit");

        return post(url, GSON.toJson(req), callback);
    }

    /**
     * 70 获取晒票评论的接口

     url: /stub/:sid/comments?page=xxx&limit=xxxx

     返回：
     {
     "code": 0,
     "message": "xxx",
     "result":
        {
            "comments":
            [
                {
                    "followID": xxx,
                    "owner":
                    {
                        “userID": xxx,
                        "name": "xxx",
                        "avatar": "xxxx",
                        ”gender" : 1 | 2,
                        ”mobile": "xxxx“ //可选
                    },

                    "comment": "xxx",
                    "subFollows":
                    [
                        {
                            "subFollowID":xxx,
                            "owner":
                            {
                                “userID": xxx,
                                "name": "xxx",
                                "avatar": "xxxx",
                                ”gender" : 1 | 2,
                                ”mobile": "xxxx“ //可选
                            },

                            "target":
                            {    //此字段是可选项，没有表示此子回复未针对其他回复
                                “userID": xxx,
                                "name": "xxx",
                                "avatar": "xxxx",
                                ”gender" : 1 | 2,
                                ”mobile": "xxxx“ //可选
                            },

                            "comment": "xxxx",
                            "createTime": xxxxx//时间戳
                        }

                    .........

                    ]

                    "createTime": xxxx   //时间戳
                }

            ],
            “sum": xxxx
        }
     }
     */
    public static class StubComment
    {
        public long subFollowID = 0;
        public UserModel owner = new UserModel();
        public UserModel target = null;
        public String comment = "";
        public long createTime = 0;
    }

    public static class SharedTicketComment
    {
        public UserModel owner = new UserModel();

        public String comment = "";

        public ArrayList<StubComment> subFollows = new ArrayList<>();

        public long followID = 0;

        public long createTime = 0;
    }

    public static class SharedTicketCommentResult
    {
        public ArrayList<SharedTicketComment> comments = new ArrayList<>();
        public int sum = 0;
    }

    public static class RespSharedTicketComment extends BaseRespModel
    {
        public SharedTicketCommentResult result = null;
    }

    public Call getSharedTicketComments(int page, int limit, long stubId,
                                        @NonNull HttpRespCallback<RespSharedTicketComment> callback)
    {
        callback.setRespModelClass(RespSharedTicketComment.class);

        String url = generateUrl(true, "/stub/" + stubId + "/comments",
                "page", String.valueOf(page), "limit", String.valueOf(limit));

        return get(url, callback);
    }


    /**
     * 71获取晒票支持者接口

     url: /stub/:sid/supporters?page=xxxx&limit=xxx

     method:GET

     返回：

     {

     "code":0,

     "message": "xxx",

     "supporters": [{

     "userID" : xxxx,

     "avatar" : "xxx"

     }

     ]

     }
     */
    public static class Supporters
    {
        public long userID = 0;
        public String avatar = "";
    }

    public static class RespGetSupporters extends BaseRespModel
    {
        public ArrayList<Supporters> supporters = null;
    }

    public Call getShareTicketSupporters(int page, int limit, long stubId,
                                         @NonNull HttpRespCallback<RespGetSupporters> callback)
    {
        callback.setRespModelClass(RespGetSupporters.class);

        String url = generateUrl(true, "/stub/" + stubId + "/supporters",
                "page", String.valueOf(page), "limit", String.valueOf(limit));

        return get(url, callback);
    }

    /**
     * 77 获取晒票详细数据接口

     url: /stub/:sid/detail?user_id=xxx&user_token=xxx user_id和user_token是可选的

     method: GET

     返回{
         "code": 0,
         "message": "xxx",
         "stub": {
             “stubID”: xx,
             "stubImage": {
                 “url”: “xxx”, //票根照片地址
                 "width": xxx,
                 "height":xxx
             },
             “opinion”:  0 |1, //态度
             “gender": 1 | 2  //1为男2为女

             “writer”:{
                 “userID” :xxx,
                 “name”: “xxx”,
                 “mobile” :”xxxx”, //可选
                 “avatar”: “xxxx”,
                 “location”: { //位置
                     “province”: “xxx”,
                     “city”: “xxx”,
                     “district”: “xxx”
                     “longitude”: xxxx,
                     “latitude”: xxxx
                 }
             }
             “showOffTime": xxxxx, //晒票根的时间戳
             “tags”: [“xx”, “xx”], //包含的标签
             “content” : “xxxx”, //内容
             “supportNum” : xx, //支持的数量
             “supported”: 0 | 1 // 是否支持过
             "shareUrl" : "xxxx"
         }
     }
     */

//    public static class SharedTicketDetail extends SharedTicketRespModel
//    {
//        public int gender = 1;
//        public String shareUrl = "";
//    }

    public static class RespSharedTicketDetail extends BaseRespModel
    {
        public SharedTicketRespModel stub = null;
    }

    public Call getSharedTicketDetail(String sid, @NonNull HttpRespCallback<RespSharedTicketDetail> callback)
    {
        callback.setRespModelClass(RespSharedTicketDetail.class);

        return get(generateUrl(true, "/stub/" + sid + "/detail"), callback);
    }

    /**
     * 78 更新jid和deviceType

     url:/user/jid/update?user_id=xxxx&user_token=xxx

     method: POST

     请求：

     {

     “jid": "xxxx",

     "deviceType": 1 | 2

     }

     返回：

     {

     ”code": xxx，

     "message": "xxx"

     }
     */

    public static class ReqUpdateJID extends BaseReqModel
    {
        public String jid = "";
        public int deviceType = 2;
    }

    public Call updateJid(@NonNull ReqUpdateJID req, HttpRespCallback<BaseRespModel> callback)
    {
        callback.setRespModelClass(BaseRespModel.class);

        String url = generateUrl(true, "/user/jid/update");

        return post(url, GSON.toJson(req), callback);
    }

    public Call fakeApi(@NonNull BaseReqModel req,
                        @NonNull HttpRespCallback<BaseRespModel> callback)
    {
        callback.setRespModelClass(BaseRespModel.class);
        return post(generateUrl("/test"), GSON.toJson(req), callback);
    }

    public Call fakePageApi(int page, int limit,
                            @NonNull BaseReqModel req,
                        @NonNull HttpRespCallback<BaseRespModel> callback)
    {
        callback.setRespModelClass(BaseRespModel.class);
        return post(generateUrl("/test"), GSON.toJson(req), callback);
    }

//    80 获取当前影院的在映影评列表
//    url: /cinema/:cnid/film/order/list  cnid是影院id
//    method: GET
//    返回：
//    {
//        "code": xxxx,
//            "message": "xxx",
//            "films": [
//        {
//            "filmID": xxxx,
//                "name": "xxx",
//                "country": "xxx",
//                "post": "xxxx",
//                "director": "xxxx",
//                "cast": "xxxx",
//                "dymIndex": xxx,
//                "releaseDate": "xxx"
//            "stubIndex": xxxx
//        }
//        ]

    public static class RespCinemaFilmPriceLists extends NetworkManager.BaseRespModel
    {
        public ArrayList<CinemaFilmPriceListModel> films = new ArrayList<>();
    }
    public static class CinemaFilmPriceListModel
    {
        public String filmID = "";
        public String name = "";
        public String country = "";
        public String post = "";
        public String director = "";
        public String cast = "";
        public String dymIndex = "";
        public String releaseDate = "";
        public String stubIndex = "";
    }

    public Call getCinemaFilmPriceList(String cinemaId,@NonNull HttpRespCallback<RespCinemaFilmPriceLists> callback){
        callback.setRespModelClass(RespCinemaFilmPriceLists.class);
        String url = "/cinema/" + cinemaId + "/film/order/list";
        return get(url, callback);
    }
    public Call getCinemaFilmPriceListv2(String cinemaId,@NonNull HttpRespCallback<RespCinemaFilmPriceLists> callback){
        callback.setRespModelClass(RespCinemaFilmPriceLists.class);
        String url = "/cinema/" + cinemaId + "/film/order/list/v2";
        return get(url, callback);
    }


//    81 获取城市中各区放映某电影的电影院数
//    url: /cinema/num/showing/film/:fid/in/city?city=xxx&sign=xxxx     该接口需要签名
//    method: GET
//    返回:
//    {
//        "code": 0,
//        “message": "success",
//        "result":[
//        {
//            "district": "xxxx",
//                "sum": xxxx
//        },
//        .......
//        ]
//    }

    public static class RespCinemaNumbers extends NetworkManager.BaseRespModel
    {
        public ArrayList<CinemaNumberModel> result = new ArrayList<>();
    }
    public static class CinemaNumberModel
    {
        public String district = "";
        public String sum = "";
    }

    public Call getCinemaNumbers(String filmid,String city,String date,@NonNull HttpRespCallback<RespCinemaNumbers> callback){
        callback.setRespModelClass(RespCinemaNumbers.class);
        String url = "/cinema/num/showing/film/" + filmid + "/in/city";
        String sign= CipherUtils.HmacSHA256("city="+city+"&date="+date, ConfigInfo.hmackey);
        String urll = generateUrl(url,
                "city", String.valueOf(city),"date", date,"sign", String.valueOf(sign));
        return get(urll, callback);
    }

    /**
     * 获取推荐影评人
     */
    public static class GetRecomCritics extends BaseRespModel
    {
        public ArrayList<CriticRespModel> critics = new ArrayList<>();
    }
    public Call getRecommendCritic(@NonNull HttpRespCallback<GetRecomCritics> callback) {
        String url = generateUrl("/critic/list/recommended");
        callback.setRespModelClass(GetRecomCritics.class);
        return get(url, callback);
    }
}
