package com.kuyun.plugin.framework.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kuyun.plugin.framework.model.FetchDataRunnable;
import com.kuyun.plugin.framework.model.KyPluginBroadcasterManager;
import com.kuyun.plugin.framework.model.KyPluginManager;
import com.kuyun.plugin.framework.model.PluginPath;
import com.kuyun.smarttv.sdk.resource.api.IKyLoadResourceListener;
import com.kuyun.smarttv.sdk.resource.api.util.AsyncLoader;
import com.kuyun.smarttv.sdk.resource.api.util.StringUtils;
import com.kuyun.smarttv.sdk.resource.net.Parameter;

public class PluginClientApi {
	private static PluginClientApi instance;

	private PluginClientApi() {
	}

	public static PluginClientApi getInstance() {
		if (instance == null) {
			synchronized (PluginClientApi.class) {
				if (instance == null)
					instance = new PluginClientApi();
				return instance;
			}
		} else {
			return instance;
		}
	}

	public AsyncLoader getAsyncLoader() {
		return KyPluginManager.getInstance().getAsyncLoader();
	}

	public String getPluginId(String mDexPath) {
		return PluginPath.getPluginIdByFileName(mDexPath);
	}

	public static String getPluginSoPath(Context context, String pluginId,
			String soName) {
		return PluginPath.getPluginSoPath(context, pluginId, soName);
	}

	public static String getPluginSoPath(Context context, String pluginId) {
		return PluginPath.getPluginSoPath(context, pluginId);
	}

	/**
	 * 播放多媒体广告
	 * 
	 * @param context
	 * @param pluginId
	 * @param params
	 */
	public static void playMultMediaAd(Context context, String pluginId,
			HashMap params) {

		try {
			pluginId = pluginId == null ? PluginConstante.PLUGIN_AD_VIDEO
					: pluginId;
			KyPluginManager.getInstance().openPlugin(context, pluginId, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// try {
		// Intent intent = new Intent(PluginConstante.VIEW_ACTION_AD_VIDEO);
		// intent.setPackage(context.getPackageName());
		// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// intent.putExtra(PluginConstante.EXTRA_PARAMS, params);
		// context.startActivity(intent);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	public static void registerReceiver(Context context,
			BroadcastReceiver mBroadcastReceiver) {
		if (mBroadcastReceiver != null) {
			KyPluginBroadcasterManager.getInstance().registerReceivers(context,
					mBroadcastReceiver);
		}
	}

	public static void unregisterReceiver(Context context,
			BroadcastReceiver mBroadcastReceiver) {
		if (mBroadcastReceiver != null)
			KyPluginBroadcasterManager.getInstance().unregisterReceiver(
					context, mBroadcastReceiver);
	}

	public static void fetchData(String url, List<Parameter> params,
			IKyLoadResourceListener<Object> l, Class<Object> classOfT) {
		AsyncLoader mAsyncLoader = KyPluginManager.getInstance()
				.getAsyncLoader();
		FetchDataRunnable<Object> mBaseRunnable = new FetchDataRunnable<Object>(
				url, params, l, classOfT);
		mAsyncLoader.load(mBaseRunnable);
	}

}
