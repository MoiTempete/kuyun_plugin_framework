package com.kuyun.plugin.framework.api;

import android.content.pm.ActivityInfo;

public class PluginConstante {
	public static final String PLUGIN_AD_VIDEO = "video";

	public static final String FROM = "extra.from";
	public static final int FROM_INTERNAL = 0;
	public static final int FROM_EXTERNAL = 1;

	public static final String EXTRA_PARAMS = "extra.params";
	public static final String EXTRA_DEX_PATH = "extra.dex.path";
	public static final String EXTRA_CLASS = "extra.class";
	public static final String EXTRA_PLUGINID_PATH = "extra.pluginId";

	public static final String PARAMS_INSTANCE_ID = "instanceId";
	public static final String PARAMS_DATA_URL = "dataUrl";

	public static final String PROXY_VIEW_ACTION = "com.kuyun.plugin.VIEW";
	public static final String PROXY_VIEW_ACTION_SINGLETOP = "com.kuyun.plugin.VIEW.singletop";
	public static final String PROXY_VIEW_ACTION_SINGLETASK = "com.kuyun.plugin.VIEW.singletask";
	public static final String PROXY_VIEW_ACTION_SINGLEINSTANCE = "com.kuyun.plugin.VIEW.singleinstance";
	
	public static final String VIEW_ACTION_AD_VIDEO = "com.kuyun.plugin.ad.video.view";

	public static final String KEY_RESPONSE = "Response";
	public static final String KEY_RESULT = "result-code";

	public static final String BROADCASTER_ACTION = "com.kuyun.plugin.data.action";
	public static final String BROADCASTER_PARAMNAME = "params";
	public static final String BROADCASTER_PARAM_KEY_PLAY_FINISH = "playFinish";

	public final static String getProxyViewAction(int launchMode) {
		switch (launchMode) {
		case ActivityInfo.LAUNCH_MULTIPLE:
			return PROXY_VIEW_ACTION;
		case ActivityInfo.LAUNCH_SINGLE_INSTANCE:
			return PROXY_VIEW_ACTION_SINGLEINSTANCE;
		case ActivityInfo.LAUNCH_SINGLE_TASK:
			return PROXY_VIEW_ACTION_SINGLETASK;
		case ActivityInfo.LAUNCH_SINGLE_TOP:
			return PROXY_VIEW_ACTION_SINGLETOP;
		}
		return PROXY_VIEW_ACTION;
	}
}
