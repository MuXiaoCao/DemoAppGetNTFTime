package com.example.gettime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import android.R.string;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Xml;

public class NetUtil {

	/* =================网络参数==================== */
	public static final String NET_NEWS_PATH = "http://op.juhe.cn/onebox/news/words?dtype=xml&key=4be403b0c81f9eccfb20dccc4b37a51f";
	public static final int NET_READ_TIMEOUT = 8000;
	public static final int NET_CONNECT_TIMEOUT = 8000;

	/**
	 * IP地址
	 */
	public static String IP;

	/**
	 * ISP(Internet Service Provider)，互联网服务提供商
	 */
	public static String ISP;

	/**
	 * 获取ip地址的网址
	 */
	public static String NET_IP_PATH = "http://ip.cn/";
	/**
	 * 编码格式
	 */
	public static String CODEMETHOD = "utf-8";
	/**
	 * 获取服务器信息的网址
	 */
	public static String NET_INFO_PATH;

	/**
	 * 通过访问网页获取外网IP
	 * 
	 * @return
	 */
	public static void GetNetIPAndISP(final Handler handler) {

		new Thread() {
			@Override
			public void run() {
				URL infoUrl = null;
				InputStream inStream = null;
				try {
					// https://www.apnic.net/apnic-info/whois_search/your-ip
					// http://ip168.com/ http://www.cmyip.com/
					infoUrl = new URL(NET_IP_PATH);
					URLConnection connection = infoUrl.openConnection();
					HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
					int responseCode = httpURLConnection.getResponseCode();

					if (responseCode == HttpURLConnection.HTTP_OK) {
						inStream = httpURLConnection.getInputStream();
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(inStream, CODEMETHOD));
						String line = null;
						while ((line = reader.readLine()) != null) {
							if (line.contains("<code>")) {
								int start = line.indexOf("<code>");
								int end = line.indexOf("</code>");
								IP = line.substring(start + 6, end);
								start = line.indexOf("来自");
								end = line.indexOf("</", start);
								ISP = line.substring(start + 3, end);
							}
						}
						inStream.close();
					} else {
						IP = "连接失败";
						ISP = "连接失败";
					}
				} catch (Exception e) {
					IP = "网络异常";
					ISP = "网络异常";
				} finally {
					Message message = new Message();
					message.what = DataUtil.SENDIPANDISP;
					handler.sendMessage(message);
				}
			}
		}.start();

	}

	private static ArrayList<String> new_list;

	/**
	 * 获取新闻列表
	 * 
	 * @return
	 */
	public static ArrayList<String> getNew_list() {
		return new_list;
	}

	/**
	 * 链接服务器
	 */
	public static void getNewsInfo(final Handler handler) {

		final String path = NetUtil.NET_NEWS_PATH;
		Thread thread = new Thread() {

			public void run() {
				try {
					URL url = new URL(path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setRequestMethod(DataUtil.NET_METHOD[1]);
					conn.setReadTimeout(NET_READ_TIMEOUT);
					conn.setConnectTimeout(NET_CONNECT_TIMEOUT);

					if (conn.getResponseCode() == DataUtil.NET_CONNECT_SUCCESS_CODE) {

						InputStream is = conn.getInputStream();

						parseNewsXml(is);

						handler.sendEmptyMessage(1);
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		thread.start();
	}

	/**
	 * 解析新闻数据
	 * 
	 * @param is
	 * @return
	 */
	private static boolean parseNewsXml(InputStream is) {

		boolean flag = false;
		XmlPullParser xml = Xml.newPullParser();
		try {
			xml.setInput(is, "utf-8");
			int type = xml.getEventType();

			while (type != XmlPullParser.END_DOCUMENT) {
				switch (type) {
				case XmlPullParser.START_TAG:
					if ("result".equals(xml.getName())) {
						new_list = new ArrayList<String>();
					} else if ("item".equals(xml.getName())) {
						new_list.add(xml.nextText());
					}
					break;

				}
				type = xml.next();
			}

			flag = true;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}

	/**
	 * 获取服务器返回信息
	 * 
	 * @param handler
	 */
	public static void GetServerInfo(final Handler handler, final String key1,
			final String key2) {
		final String path = NetUtil.NET_INFO_PATH;
		Thread thread = new Thread() {

			public void run() {
				try {
					URL url = new URL(path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setRequestMethod(DataUtil.NET_METHOD[1]);
					conn.setReadTimeout(NET_READ_TIMEOUT);
					conn.setConnectTimeout(NET_CONNECT_TIMEOUT);

					if (conn.getResponseCode() == DataUtil.NET_CONNECT_SUCCESS_CODE) {

						InputStream is = conn.getInputStream();

						byte[] buffer = new byte[13];

						is.read(buffer);

						Message message = new Message();
						Bundle bundle = new Bundle();

						bundle.putString(key1, new String(buffer));
						bundle.putString(key2, SystemClock.elapsedRealtime()
								+ "");
						System.out.println("=============="
								+ new String(buffer));
						message.setData(bundle);
						message.what = 1;
						handler.sendMessage(message);
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		thread.start();
	}

	/*
	 * ================================Mlab======================================
	 * ===
	 */

	public static final int SUCCESS_TIME_WHAT = 3;
	public static final int FLESH_TIME_WHAT = 4;
	public static final int FAIL_TIME_WHAT = 5;
	public static final int GET_TIMEING_WHAT = 6;
	public static final String RECEIVE_KEY = "receiveTime";
	public static final String ORIGINATE_KEY = "originateTime";
	public static final String TRANSMIT_KEY = "transmitTime";
	public static final String DESTINATION_KEY = "destinationTime";
	public static final String SERVER_OFFSET_KEY = "offsetTime";
	public static final String STATUE_KEY = "fail";
	public static final String OBJECT_KEY = "object";
	public static final String RESULT_KEY = "result";

	public static final String[] NTF_SERVER = { "time.asia.apple.com",
			"cn.pool.ntp.org", "tw.pool.ntp.org" };
	public static final String VIDEOCALLSERVER_STRING = "192.168.8.4:8080/VideoCallServer/getTime";

	public static void GetTimeFromServer(final Handler handler,
			final String server) {
		new Thread() {

			@Override
			public void run() {
				super.run();
				handler.sendEmptyMessage(GET_TIMEING_WHAT);
				int retry = 0;
				int port = 123;
				int timeout = 3000;
				// get the address and NTP address request
				//
				InetAddress ipv4Addr = null;
				try {
					ipv4Addr = InetAddress.getByName(server);// 更多NTP时间服务器参考附注
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				}

				int serviceStatus = -1;
				DatagramSocket socket = null;
				try {
					socket = new DatagramSocket();
					socket.setSoTimeout(timeout); // will force the
					// InterruptedIOException

					for (int attempts = 0; attempts <= retry
							&& serviceStatus != 1; attempts++) {
						try {
							// Send NTP request
							//
							byte[] data = new NtpMessage().toByteArray();
							DatagramPacket outgoing = new DatagramPacket(data,
									data.length, ipv4Addr, port);
							long sendTime = System.currentTimeMillis();
							socket.send(outgoing);

							// Get NTP Response
							//
							// byte[] buffer = new byte[512];
							DatagramPacket incoming = new DatagramPacket(data,
									data.length);
							socket.receive(incoming);
							double destinationTimestamp = (System
									.currentTimeMillis() / 1000.0) + 2208988800.0;
							// 这里要加2208988800，是因为获得到的时间是格林尼治时间，所以要变成东八区的时间，否则会与与北京时间有8小时的时差

							// Validate NTP Response
							// IOException thrown if packet does not decode as
							// expected.
							NtpMessage msg = new NtpMessage(incoming.getData());
							double localClockOffset = ((msg.receiveTimestamp - msg.originateTimestamp) + (msg.transmitTimestamp - destinationTimestamp)) / 2;
							double result = Double.parseDouble(String.format(
									"%.3f", localClockOffset));
							Message message = new Message();
							message.what = NetUtil.SUCCESS_TIME_WHAT;
							message.obj = result;
							Bundle bundle = new Bundle();
							bundle.putString(NetUtil.STATUE_KEY, "校准成功");
							bundle.putDouble(NetUtil.RECEIVE_KEY,
									msg.receiveTimestamp);
							bundle.putDouble(NetUtil.ORIGINATE_KEY,
									msg.originateTimestamp);
							bundle.putDouble(NetUtil.TRANSMIT_KEY,
									msg.transmitTimestamp);
							bundle.putDouble(NetUtil.DESTINATION_KEY,
									destinationTimestamp);
							message.setData(bundle);
							handler.sendMessage(message);
							// System.out.println("poll: valid NTP request received the local clock offset is "
							// + localClockOffset + ", responseTime(响应时间)= " +
							// responseTime + "ms" + "\n");
							// System.out.println("poll: NTP message : " +
							// msg.toString() + "\n");
							serviceStatus = 1;
						} catch (Exception ex) {
							// Ignore, no response received.
							Message message = new Message();
							message.what = NetUtil.FAIL_TIME_WHAT;
							Bundle bundle = new Bundle();
							bundle.putString(NetUtil.STATUE_KEY,
									"校准失败,正在尝试连接videoCall服务器...");
							bundle.putDouble(NetUtil.RECEIVE_KEY, 0.0);
							bundle.putDouble(NetUtil.ORIGINATE_KEY, 0.0);
							bundle.putDouble(NetUtil.TRANSMIT_KEY, 0.0);
							bundle.putDouble(NetUtil.DESTINATION_KEY, 0.0);
							message.setData(bundle);
							handler.sendMessage(message);
							JSONObject json = getTimeFromVideoCallServer();
							message = new Message();
							if (json == null) {
								message.what = NetUtil.FAIL_TIME_WHAT;
								bundle.putString(NetUtil.STATUE_KEY, "校准失败");
							} else {
								message.what = NetUtil.SUCCESS_TIME_WHAT;
								message.obj = json
										.getDouble(NetUtil.RESULT_KEY);
								bundle = new Bundle();
								bundle.putString(NetUtil.STATUE_KEY,
										"校准成功，来自videocall服务器");
								bundle.putString(NetUtil.OBJECT_KEY,
										json.toString());
								bundle.putDouble(NetUtil.RECEIVE_KEY, json.getLong(NetUtil.RECEIVE_KEY));
								bundle.putDouble(NetUtil.ORIGINATE_KEY, json.getLong(NetUtil.ORIGINATE_KEY));
								bundle.putDouble(NetUtil.TRANSMIT_KEY, json.getLong(NetUtil.TRANSMIT_KEY));
								bundle.putDouble(NetUtil.DESTINATION_KEY, json.getLong(NetUtil.DESTINATION_KEY));
								message.setData(bundle);
							}
							message.setData(bundle);
							handler.sendMessage(message);
							ex.printStackTrace();
						}
					}
				} catch (Exception ex) {
					ex.fillInStackTrace();

				} finally {
					if (socket != null)
						socket.close();
				}
			}

		}.start();

	}

	private static JSONObject getTimeFromVideoCallServer() throws Exception {
		URL url = new URL(VIDEOCALLSERVER_STRING);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod(DataUtil.NET_METHOD[1]);
		conn.setReadTimeout(NET_READ_TIMEOUT);
		conn.setConnectTimeout(NET_CONNECT_TIMEOUT);
		long start = System.currentTimeMillis();
		if (conn.getResponseCode() == DataUtil.NET_CONNECT_SUCCESS_CODE) {
			long end = System.currentTimeMillis();
			InputStream is = conn.getInputStream();
			byte[] buffer = new byte[1024];
			is.read(buffer);
			JSONObject jsonObject = new JSONObject(new String(buffer));
			jsonObject.put(NetUtil.ORIGINATE_KEY, start);
			jsonObject.put(DESTINATION_KEY, end);
			jsonObject.put(NetUtil.RESULT_KEY, getTimeFromJson(jsonObject));
			return jsonObject;
		}
		return null;
	}

	private static double getTimeFromJson(JSONObject json) {
		try {
			long receive = json.getLong(NetUtil.RECEIVE_KEY);
			long originnate = json.getLong(NetUtil.ORIGINATE_KEY);
			long transmit = json.getLong(NetUtil.TRANSMIT_KEY);
			long destination = json.getLong(NetUtil.DESTINATION_KEY);
			return (double) ((receive - originnate) + (transmit - destination))
					/ 2.0 + json.getDouble(SERVER_OFFSET_KEY);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0.0;
	}
}
