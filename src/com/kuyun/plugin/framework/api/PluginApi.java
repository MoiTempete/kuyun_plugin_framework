package com.kuyun.plugin.framework.api;

import java.io.Serializable;
import java.util.HashMap;

import android.content.Context;

import com.kuyun.plugin.framework.exception.PluginException;
import com.kuyun.plugin.framework.model.KyPluginManager;
import com.kuyun.smarttv.sdk.resource.api.card.data.CardData;

public class PluginApi {
	private static PluginApi instance;

	private PluginApi() {
	}

	public static PluginApi getInstance() {
		if (instance == null) {
			synchronized (PluginApi.class) {
				if (instance == null)
					instance = new PluginApi();
				return instance;
			}
		} else {
			return instance;
		}
	}

	public void init(final Context context) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					KyPluginManager.getInstance().init(context);
				} catch (PluginException e) {
					e.printStackTrace();
				}
			}

		}).start();
	}

	public void regProxyActivity(Class clss) {
		KyPluginManager.getInstance().regProxyActivity(clss);
	}

	public void regProxyActivityForSingleInstance(Class clss) {
		KyPluginManager.getInstance().regProxyActivityForSingleInstance(clss);
	}

	public void regProxyActivityForSingleTask(Class clss) {
		KyPluginManager.getInstance().regProxyActivityForSingleTask(clss);
	}

	public void regProxyActivityForSingleTop(Class clss) {
		KyPluginManager.getInstance().regProxyActivityForSingleTask(clss);
	}

	public void openPlugin(Context context, CardData data) {
		if (data == null)
			return;
		HashMap<String, Serializable> params = new HashMap<String, Serializable>();
		params.put(PluginConstante.PARAMS_DATA_URL, data.url);
		params.put(PluginConstante.PARAMS_INSTANCE_ID, data.instanceId);
		try {
			KyPluginManager.getInstance().openPlugin(context, data.id, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
