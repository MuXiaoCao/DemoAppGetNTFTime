package com.example.gettime;

import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class NetUtil {

	/* =================网络参数==================== */
	public static final String NET_NEWS_PATH = "http://op.juhe.cn/onebox/news/words?dtype=xml&key=4be403b0c81f9eccfb20dccc4b37a51f";
	public static final int NET_READ_TIMEOUT = 8000;
	public static final int NET_CONNECT_TIMEOUT = 8000;


	/*
	 * ================================Mlab======================================
	 * ===
	 */
	
	/**
	 * 网络提交方式，0.post 1.get
	 */
	public static final String[] NET_METHOD = { "POST", "GET" };
	/**
	 * 提交成功
	 */
	public static final int NET_CONNECT_SUCCESS_CODE = 200;
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
	public static final String VIDEOCALLSERVER_STRING = "http://115.28.88.171/VideoCallServer/getTime";

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
							socket.send(outgoing);

							// Get NTP Response
							//
							// byte[] buffer = new byte[512];
							DatagramPacket incoming = new DatagramPacket(data,
									data.length);
							socket.receive(incoming);
							double destinationTimestamp = getCurrentTime();
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
		conn.setRequestMethod(NET_METHOD[1]);
		conn.setReadTimeout(NET_READ_TIMEOUT);
		conn.setConnectTimeout(NET_CONNECT_TIMEOUT);
		long start = getCurrentTime();
		if (conn.getResponseCode() == NET_CONNECT_SUCCESS_CODE) {
			long end = getCurrentTime();
			InputStream is = conn.getInputStream();
			byte[] buffer = new byte[1024];
			is.read(buffer);
			JSONObject jsonObject = new JSONObject(new String(buffer));
			jsonObject.put(NetUtil.ORIGINATE_KEY, start);
			jsonObject.put(NetUtil.DESTINATION_KEY, end);
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
			System.out.println("=========================" + receive + "," + originnate + ","  + transmit + "," + destination + ","+json.getDouble(SERVER_OFFSET_KEY));
			return (double) ((receive - originnate) + (transmit - destination))
					/ 2.0 + json.getDouble(SERVER_OFFSET_KEY);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0.0;
	}
	public static long getCurrentTime() {
		return (System.currentTimeMillis() / 1000) + 2208988800L;
	}
}
