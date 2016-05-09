package com.example.gettime;

import java.text.NumberFormat;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private double offset;
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == 1) {
				serverTime = Long.valueOf((String)msg.getData().get("serverTime"));
				clientTime = Long.valueOf((String)msg.getData().get("clientTime"));
			}else if (msg.what == NetUtil.SUCCESS_TIME_WHAT) {
				SUCCESS_SERVER = NTF_SERVER;
				offset = (double)msg.obj;
				tv_offse.setText("服务器：" + SUCCESS_SERVER + msg.getData().getString(NetUtil.STATUE_KEY) + "\n"
						+ "时间误差：" + offset + "ms\n"
						+ "服务器接收时间：" + nf.format(msg.getData().getDouble(NetUtil.RECEIVE_KEY)) + "\n"
						+ "本地发送时间：" + nf.format(msg.getData().getDouble(NetUtil.ORIGINATE_KEY)) + "\n"
						+ "服务器发送时间：" + nf.format(msg.getData().getDouble(NetUtil.TRANSMIT_KEY)) + "\n"
						+ "本地接收时间：" + nf.format(msg.getData().getDouble(NetUtil.DESTINATION_KEY)));
			}else if(msg.what == NetUtil.FLESH_TIME_WHAT){
				double localtime = (System.currentTimeMillis()/1000.0) + 2208988800.0;
				double time = localtime + offset;
				
				textView.setText("本地时间" + nf.format(localtime) + "\n"
						+ "校准时间：" + nf.format(time));
				try {
					Thread.sleep(20);
					if (!isStop) {
						Message msg1 = new Message();
						msg1.what = NetUtil.FLESH_TIME_WHAT;
						handler.sendMessage(msg1);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else if (msg.what == NetUtil.FAIL_TIME_WHAT) {
				tv_offse.setText("服务器：" + NTF_SERVER + msg.getData().getString(NetUtil.STATUE_KEY) + "\n"
						+ "上次获取的时间误差：" + offset + "s\n"
						+ "服务器接收时间：" + nf.format(msg.getData().getDouble(NetUtil.RECEIVE_KEY)) + "\n"
						+ "本地发送时间：" + nf.format(msg.getData().getDouble(NetUtil.ORIGINATE_KEY)) + "\n"
						+ "服务器发送时间：" + nf.format(msg.getData().getDouble(NetUtil.TRANSMIT_KEY)) + "\n"
						+ "本地接收时间：" + nf.format(msg.getData().getDouble(NetUtil.DESTINATION_KEY)));
			}else if (msg.what == NetUtil.GET_TIMEING_WHAT) {
				tv_offse.setText("校准时间中，请稍后。。。");
			}
		}

	};
	Bundle bundle = new Bundle();
	Message message = new Message();
	TextView textView;
	TextView tv_offse;
	Button button_start;
	Button button_stop;
	Button button_restart;
	ListView lv_ntf_server;
	static Thread mThread;
	boolean isStop = true;
	Long serverTime;
	Long clientTime;
    String NTF_SERVER = NetUtil.NTF_SERVER[0];
	String SUCCESS_SERVER = NetUtil.NTF_SERVER[0];
	NumberFormat nf = NumberFormat.getInstance();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		nf.setGroupingUsed(false);
		//getServerTime();
		//getTime();

	}

	private void getServerTime() {
		//NetUtil.NET_INFO_PATH = "http://www.muxiaocao.cn/MyTime/Controller";
		/*NetUtil.NET_INFO_PATH = "http://127.0.0.1:8080/VideoCallServer/getTime";
		NetUtil.GetServerInfo(handler, "serverTime","clientTime");*/
		NetUtil.GetTimeFromServer(handler,NTF_SERVER);
	}

	private void initView() {
		// TODO Auto-generated method stub
		textView = (TextView) findViewById(R.id.tv_time);
		tv_offse = (TextView) findViewById(R.id.tv_offsetTime);
		button_start = (Button) findViewById(R.id.bt_time_start);
		button_stop = (Button) findViewById(R.id.bt_time_stop);
		button_restart = (Button) findViewById(R.id.bt_time_restart);
		button_start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (!isStop) {
					return;
				}
				isStop = false;
				message = new Message();
				message.what = NetUtil.FLESH_TIME_WHAT;
				handler.sendMessage(message);
			}
		});
		button_stop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				isStop = true;
			}
		});
		button_restart.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				getServerTime();
				if (isStop) {
					isStop = false;
					message = new Message();
					message.what = NetUtil.FLESH_TIME_WHAT;
					handler.sendMessage(message);
				}
			}
		});
		lv_ntf_server = (ListView) findViewById(R.id.lv_ntf_server);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, NetUtil.NTF_SERVER);
		lv_ntf_server.setAdapter(adapter);
		lv_ntf_server.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			/**
            *
            * @param parent 当前ListView
            * @param view 代表当前被点击的条目
            * @param position 当前条目的位置
            * @param id 当前被点击的条目的id
            */
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Toast.makeText(parent.getContext(), NetUtil.NTF_SERVER[position], Toast.LENGTH_LONG).show();
				NTF_SERVER = NetUtil.NTF_SERVER[position];
			}
			
		});
	}

	private void getTime() {

		mThread = new Thread() {
			@Override
			public void run() {

				while (true) {
					if (!isStop) {
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						bundle = new Bundle();
						message = new Message();
						
						message.obj = SystemClock.elapsedRealtime() + (serverTime - clientTime);
						handler.sendMessage(message);
					}
				}

			}

		};

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
