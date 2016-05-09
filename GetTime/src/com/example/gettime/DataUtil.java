package com.example.gettime;

public class DataUtil {

	
	/**
	 * 网络提交方式，0.post 1.get
	 */
	public static final String[] NET_METHOD = { "POST", "GET" };
	/**
	 * 提交成功
	 */
	public static final int NET_CONNECT_SUCCESS_CODE = 200;

	/** 没有网络 */
	public static final int NETWORKTYPE_INVALID = 0;
	/** wap网络 */
	public static final int NETWORKTYPE_WAP = 1;
	/** 2G网络 */
	public static final int NETWORKTYPE_2G = 2;
	/** 3G网络 */
	public static final int NETWORKTYPE_3G = 3;
	/** 4G网络 */
	public static final int NETWORKTYPE_4G = 4;
	/** wifi网络 */
	public static final int NETWORKTYPE_WIFI = 5;
	/**
	 * 信号类型
	 */
	public static final String[] NETWORKTYPE = {"无信号","WAP","2G","3G","4G","WIFI"};
	
	/**
	 * 手机信息刷新时间（毫秒）
	 */
	public static final int PHONE_UPDATE_TIME = 1000;
	
	/**
	 * 数据已经得到
	 */
	public static final int SENDINFO = 1;
	/**
	 * 数据库可以刷新了
	 */
	public static final int SENDDATABASE = 0;
	
	/**
	 * 信号强度已经拿到
	 */
	public static final int SENDSIGNALSTRENGH = 2;
	/**
	 * IP/ISP 已经拿到
	 */
	public static final int SENDIPANDISP = 3;
}
