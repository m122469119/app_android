package com.dym.film.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import android.content.Context;

import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;

public class MatStatsUtil {


//	事件ID	事件名称	事件类型
//	more_leaderboard	观影指数查看全部		普通事件
//	more_comingsoon	 	即将上映查看全部		普通事件
//	more_review			影评详情影评查看更多	普通事件
//	more_show			影评详情晒票查看更多	普通事件
//	buy_tickets			影评详情比价购票		普通事件
//	more_news			热点资讯查看全部		普通事件
//	huo_portal			火票务				普通事件
//	show_around			周围晒票				普通事件
//	ticket_show			我要晒				普通事件
//	film_attitude		晒票值				普通事件
//	film_attitude1		晒票不值				普通事件
//	show_complete		马上晒票根			普通事件
//	show_share			晒票成功马上分享		普通事件
//	ticket_exchange		详情兑换				普通事件
//	critics_recommend	影评影评人			普通事件
//	critics_more		影评人更多			普通事件
//	film_review			影评列表				普通事件
//	buy_now_tickets		通用卷立即抢购		普通事件
//	submit_order		提交订单				普通事件
//	pay_click			确认支付				普通事件
//	pay_click_wechat	微信确认支付			普通事件
//	movie_search		搜索					普通事件
//	=================以上废除====================

	public static final String CINEMA_SWITCH="cinema_switch"; //比价切换影院

	public static final String REVIEW_LIST="review_list"; //影评tab
	public static final String CRITICS_LIST="critics_list"; //影评人tab
	public static final String REVIEW_SHARE="review_share"; //影评详情分享
	public static final String REVIEW_LIKE="review_like"; //影评详情点赞

	public static final String SHOW_AROUND="show_around"; //晒票周边
	public static final String TICKET_SHOW="ticket_show"; //晒票
	public static final String ADD_TAG="add_tag"; //晒票 自定义
	public static final String SHOW_SUBMIT="show_submit"; //发布晒票
	public static final String SHOW_SHARE="show_share"; //晒票详情分享
	public static final String SHOW_LIKE="show_like"; //晒票详情点赞
	public static final String SHOW_COMMENT="show_comment"; //晒票详情评论

	public static final String MY="my"; //主页 个人中心
	public static final String HOT="hot"; //主页 热映
	public static final String COMING="coming"; //主页 待映
	public static final String BOARD="board"; //主页 榜单

	public static final String FILM="film"; //主页 tab 影片
	public static final String REVIEW="review"; //主页 tab 影评
	public static final String TICKET="ticket"; //主页 tab 购票
	public static final String SHOW="show"; //主页 tab 晒票

	public static final String FAV_FILM="fav_film"; //影片详情 收藏
	public static final String MORE_VIDEO="more_video"; //影片详情 全部视频
	public static final String MORE_PIC="more_pic"; //影片详情 全部海报
	public static final String MORE_REVIEW="more_review"; //影片详情 全部影评
	public static final String MORE_SHOW="more_show"; //影片详情 全部晒票
	public static final String BUT_TICKET2="buy_ticket2"; //影片详情 比价购票
	public static final String TICKET_SHOW2="ticket_show2"; //影片详情 我要晒票




	/**
	 * 初始化
	 * @param context
	 */
	
	public static void init(Context context){
		MobclickAgent.setDebugMode(false);
		MobclickAgent.setCatchUncaughtExceptions(true);
		/** 设置是否对日志信息进行加密, 默认false(不加密). */
//		AnalyticsConfig.enableEncrypt(false);
	}
	
	public static void onResume(Context context,String Tag){
		MobclickAgent.onPageStart(Tag);
		MobclickAgent.onResume(context);
	}
	public static void onPause(Context context,String Tag){
		MobclickAgent.onPageEnd(Tag);
		MobclickAgent.onPause(context);
	}
	public static void onStop(Context context){

	}
	
	/**
	 * 错误上报 用法 try{ }catch(Exception e){
	 * StatService.reportException(Activity,Exception); }
	 */
	public static void uploadExp(Context context, Exception e) {
		MobclickAgent.reportError(context, e);
	}

	/**
	 * 自定义事件
	 * 
	 * @param context
	 * @param type
	 *            事件标识
	 * @param id
	 *            事件参数
	 */
	public static void eventClick(Context context, String type, String id) {

		MobclickAgent.onEvent(context, type);

//		HashMap<String,String> map = new HashMap<String,String>();
//		map.put(type, id);
//		MobclickAgent.onEvent(context, type, map);
	}
	public static void eventClick(Context context, String type) {

		MobclickAgent.onEvent(context, type);

//		HashMap<String,String> map = new HashMap<String,String>();
//		map.put(type, id);
//		MobclickAgent.onEvent(context, type, map);
	}

	/**
	 * 统计应用对某个外部接口（特别是网络类的接口，如连接、登陆、下载等）的调用情况。
	 * 当开发者用到某个外部接口，可调用该函数将一些指标进行上报，MTA将统计出每个接口的调用情况， 并在接口可用性发生变化时进行告警通知;
	 * 
	 * @param context
	 * @param url
	 */
	public static void interfaceTest(final Context context, final String url) {
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				// 新建监控接口对象
//				String ip =url;
//				StatAppMonitor monitor = new StatAppMonitor("ping:"+ip);
//				Runtime run = Runtime.getRuntime();
//				Process proc = null;
//				try {
//					String str = "ping -c 3 -i 0.2 -W 1 " + ip;
//					long starttime = System.currentTimeMillis();
//					// 被监控的接口
//					proc = run.exec(str);
//
//					proc.waitFor();
//					long difftime = System.currentTimeMillis() - starttime;
//					// 设置接口耗时
//					monitor.setMillisecondsConsume(difftime);
//					int retCode = proc.waitFor();
//					// 设置接口返回码
//					monitor.setReturnCode(retCode);
//					// 设置请求包大小，若有的话
//					monitor.setReqSize(1000);
//					// 设置响应包大小，若有的话
//					monitor.setRespSize(2000);
//					// 设置抽样率
//					// 默认为1，表示100%。
//					// 如果是50%，则填2(100/50)，如果是25%，则填4(100/25)，以此类推。
//					monitor.setSampling(2);
//					if (retCode == 0) {
//						// logger.debug("ping连接成功");
//						// 标记为成功
//						monitor.setResultType(StatAppMonitor.SUCCESS_RESULT_TYPE);
//					} else {
//						// logger.debug("ping测试失败");
//						// 标记为逻辑失败，可能由网络未连接等原因引起的
//						// 但对于业务来说不是致命的，是可容忍的
//						monitor.setResultType(StatAppMonitor.LOGIC_FAILURE_RESULT_TYPE);
//					}
//				} catch (Exception e) {
//					// 接口调用出现异常，致命的，标识为失败
//					monitor.setResultType(StatAppMonitor.FAILURE_RESULT_TYPE);
//				} finally {
//					proc.destroy();
//				}
//				// 上报接口监控
//				StatService.reportAppMonitorStat(context, monitor);
//
//				//网速监控
//				Map<String, Integer> map = new HashMap<String, Integer>();
//				map.put(url, 80);
//				StatService.testSpeed(context, map);
//			}
//		}).start();


	}

}
