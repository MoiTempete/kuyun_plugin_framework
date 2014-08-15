package com.kuyun.plugin.framework.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.kuyun.plugin.framework.api.PluginConstante;

public class KyPluginBroadcasterManager {
	private static KyPluginBroadcasterManager instance;

	public static KyPluginBroadcasterManager getInstance() {
		if (instance == null) {
			synchronized (KyPluginBroadcasterManager.class) {
				if (instance == null)
					instance = new KyPluginBroadcasterManager();
				return instance;
			}
		} else {
			return instance;
		}
	}

	public  void registerReceivers(Context context,
			BroadcastReceiver mBroadcastReceiver) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(PluginConstante.BROADCASTER_ACTION);
		LocalBroadcastManager.getInstance(context).registerReceiver(
				mBroadcastReceiver, filter);
	}

	public  void unregisterReceiver(Context context,
			BroadcastReceiver mBroadcastReceiver) {
		try {
			if (context != null && mBroadcastReceiver != null) {
				LocalBroadcastManager.getInstance(context).unregisterReceiver(
						mBroadcastReceiver);
			}
		} catch (Exception e) {
		}
	}

	public void sendBroadcastData(Context context, Bundle params) {
		try {
			if (context != null) {
				Intent intent = new Intent(PluginConstante.BROADCASTER_ACTION);
				intent.putExtra(PluginConstante.BROADCASTER_PARAMNAME, params);
				LocalBroadcastManager.getInstance(context)
						.sendBroadcast(intent);
			}
		} catch (Exception e) {
		}
	}
	
}
