package com.dym.film.alipay.sdk;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.dym.film.manager.NetworkManager;

public class ZhifubaoPayUtil {

	private  Activity context;
	private Handler mHandler;

	public ZhifubaoPayUtil(Activity context,Handler mhandler) {
		this.context = context;
		this.mHandler = mhandler;
	}

	// 商户PID
	public static final String PARTNER = "2088121362165395";
	// 商户收款账号
	public static final String SELLER = "2088121362165395";
	// 商户私钥，pkcs8格式
	public static final String RSA_PRIVATE = "MIICXwIBAAKBgQDN34/HKQVblh7a1MKKSV4XOXQfSl6roVBpzkToB35kAVpFOWir0cFnSq51vdcrav86XyJ/LGm3Eo1TMxY5xAcNV77W45p8A7LKIMyuYAHRYUM3KwUeOMECSgX4E7vSS4IKcVKsNIkVJ5pDMMjKjmboOBwq8lAHGBMUTKo4HNouWQIDAQABAoGBAMwyIR6uPkUwKhxtCSQ2TyAD+XyLvEjJsMOxNsFZpXA1sz6UCZDadNUpys635DKJZuuTrtiqyZQuz4QlWLclCsPWgzKQpRApUz2u9MKSpB3giKjwDTrPFXTgXQsb/rWxaFx6cOz/ZQ9WdaMvjnBLW1ysLpn5UUHoY5FIL4GoBVWxAkEA7biGDe1tjy+NsDYnrLlDphhlzXP4fTBi87O1nBnE+7epLsJsqyYHBpupKhBznOZx7G2Vio34IvrARo2aaPvaBQJBAN20IKN873WXE72niZblw4eGw1OgSGJltQaoeoFESmjhkKVrqu+s90fno5JmiMVzFmuFmew+qyyynpecw6tjr0UCQQCD0LHkvyblIsU6VI0jZYdxbk3k4s2C/XWhFKE7cb6cpXaWixCexnNW4rGmtYCmG9JH+2vUbGyPWOONO6/HTv/RAkEA2WvAInTwpWJe0ryN+3UitzNbNRTYR980k+4ADet6CbdEUmXvghMfgt3RFU8GzJScJSAlnoCrAC+/xjHP1rHw/QJBAK23Ix5LSxyGUUVk9T+WEH+VbjnlM3oeCSJK3D7YjCQj0m1+WT6NaF3A/9W1FfS4nhLnBIOjqxbWCwIpMWrupe4=";
	// 支付宝公钥
	public static final String RSA_PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDI6d306Q8fIfCOaTXyiUeJHkrIvYISRcc73s3vF1ZT7XN8RNPwJxo8pWaJMmvyTn9N4HQ632qJBVHf8sxHi/fEsraprwCtzvzQETrNRwVxLO5jVmRGi60j8Ue1efIlzPXV9je9mkjzOmdssymZkh2QhUrCmZYI/FCEa3/cNMW0QIDAQAB";
	
	public static final int SDK_PAY_FLAG = 1;

	public static final int SDK_CHECK_FLAG = 2;



	/**
	 * call alipay sdk pay. 调用SDK支付
	 * 
	 */
	public void pay(String title,String descption,String price,String code) {
		if (TextUtils.isEmpty(PARTNER) || TextUtils.isEmpty(RSA_PRIVATE)
				|| TextUtils.isEmpty(SELLER)) {
			Toast.makeText(context, "缺少配置",Toast.LENGTH_SHORT).show();
			return;
		}
		// 订单
		String orderInfo = getOrderInfo(title,descption,price,code);

		// 对订单做RSA 签名
		String sign = sign(orderInfo);
		try {
			// 仅需对sign 做URL编码
			sign = URLEncoder.encode(sign, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// 完整的符合支付宝参数规范的订单信息
		final String payInfo = orderInfo + "&sign=\"" + sign + "\"&"
				+ getSignType();

		Runnable payRunnable = new Runnable() {

			@Override
			public void run() {
				// 构造PayTask 对象
				PayTask alipay = new PayTask(context);
				// 调用支付接口，获取支付结果
				String result = alipay.pay(payInfo);

				Message msg = new Message();
				msg.what = SDK_PAY_FLAG;
				msg.obj = result;
				mHandler.sendMessage(msg);
			}
		};

		// 必须异步调用
		Thread payThread = new Thread(payRunnable);
		payThread.start();
	}

	/**
	 * check whether the device has authentication alipay account.
	 * 查询终端设备是否存在支付宝认证账户
	 * 
	 */
	public void check() {
		Runnable checkRunnable = new Runnable() {
			@Override
			public void run() {
				// 构造PayTask 对象
				PayTask payTask = new PayTask(context);
				// 调用查询接口，获取查询结果
				boolean isExist = payTask.checkAccountIfExist();

				Message msg = new Message();
				msg.what = SDK_CHECK_FLAG;
				msg.obj = isExist;
				mHandler.sendMessage(msg);
			}
		};

		Thread checkThread = new Thread(checkRunnable);
		checkThread.start();

	}

	/**
	 * get the sdk version. 获取SDK版本号
	 * 
	 */
	public String getSDKVersion(Activity context) {
		PayTask payTask = new PayTask(context);
		String version = payTask.getVersion();
		return version;
	}

	/**
	 * create the order info. 创建订单信息
	 * 
	 */
	public String getOrderInfo(String subject, String body, String price,String code) {

		// 签约合作者身份ID
		String orderInfo = "partner=" + "\"" + PARTNER + "\"";

		// 签约卖家支付宝账号
		orderInfo += "&seller_id=" + "\"" + SELLER + "\"";

		// 商户网站唯一订单号
		orderInfo += "&out_trade_no=" + "\"" + code + "\"";

		// 商品名称
		orderInfo += "&subject=" + "\"" + subject + "\"";

		// 商品详情
		orderInfo += "&body=" + "\"" + body + "\"";

		// 商品金额
		orderInfo += "&total_fee=" + "\"" + price + "\"";

		// 服务器异步通知页面路径
		orderInfo += "&notify_url=" + "\"" + NetworkManager.URL_BASE+"/alipay/notify"
				+ "\"";

		// 服务接口名称， 固定值
		orderInfo += "&service=\"mobile.securitypay.pay\"";

		// 支付类型， 固定值
		orderInfo += "&payment_type=\"1\"";

		// 参数编码， 固定值
		orderInfo += "&_input_charset=\"utf-8\"";

		// 设置未付款交易的超时时间
		// 默认30分钟，一旦超时，该笔交易就会自动被关闭。
		// 取值范围：1m～15d。
		// m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
		// 该参数数值不接受小数点，如1.5h，可转换为90m。
		orderInfo += "&it_b_pay=\"30m\"";

		// extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
		// orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

		// 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
		orderInfo += "&return_url=\"m.alipay.com\"";

		// 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
		// orderInfo += "&paymethod=\"expressGateway\"";

		return orderInfo;
	}

	/**
	 * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
	 * 
	 */
	public String getOutTradeNo() {
		SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss",
				Locale.getDefault());
		Date date = new Date();
		String key = format.format(date);

		Random r = new Random();
		key = key + r.nextInt();
		key = key.substring(0, 15);
		return key;
	}

	/**
	 * sign the order info. 对订单信息进行签名
	 * 
	 * @param content
	 *            待签名订单信息
	 */
	public String sign(String content) {
		return SignUtils.sign(content, RSA_PRIVATE);
	}

	/**
	 * get the sign type we use. 获取签名方式
	 * 
	 */
	public String getSignType() {
		return "sign_type=\"RSA\"";
	}

}
