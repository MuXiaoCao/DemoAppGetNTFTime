package com.example.gettime;

public class DataUtil {

	
	/**
	 * �����ύ��ʽ��0.post 1.get
	 */
	public static final String[] NET_METHOD = { "POST", "GET" };
	/**
	 * �ύ�ɹ�
	 */
	public static final int NET_CONNECT_SUCCESS_CODE = 200;

	/** û������ */
	public static final int NETWORKTYPE_INVALID = 0;
	/** wap���� */
	public static final int NETWORKTYPE_WAP = 1;
	/** 2G���� */
	public static final int NETWORKTYPE_2G = 2;
	/** 3G���� */
	public static final int NETWORKTYPE_3G = 3;
	/** 4G���� */
	public static final int NETWORKTYPE_4G = 4;
	/** wifi���� */
	public static final int NETWORKTYPE_WIFI = 5;
	/**
	 * �ź�����
	 */
	public static final String[] NETWORKTYPE = {"���ź�","WAP","2G","3G","4G","WIFI"};
	
	/**
	 * �ֻ���Ϣˢ��ʱ�䣨���룩
	 */
	public static final int PHONE_UPDATE_TIME = 1000;
	
	/**
	 * �����Ѿ��õ�
	 */
	public static final int SENDINFO = 1;
	/**
	 * ���ݿ����ˢ����
	 */
	public static final int SENDDATABASE = 0;
	
	/**
	 * �ź�ǿ���Ѿ��õ�
	 */
	public static final int SENDSIGNALSTRENGH = 2;
	/**
	 * IP/ISP �Ѿ��õ�
	 */
	public static final int SENDIPANDISP = 3;
}
