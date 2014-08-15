package com.kuyun.plugin.framework.update;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.kuyun.smarttv.sdk.resource.api.util.StringUtils;
import com.kuyun.smarttv.sdk.resource.net.HttpClient;
import com.kuyun.smarttv.sdk.resource.net.MyOkHttpClient;
import com.kuyun.smarttv.sdk.resource.net.Parameter;
import com.kuyun.smarttv.sdk.resource.net.URLHelper;

public class UpdateService {

	private static final String TAG = "UpdateService";

	/**
	 * 使用单利模式，本类创建的实例
	 */
	private static UpdateService instance;

	/**
	 * 使用单利模式，所以构造函数被屏蔽
	 */
	private UpdateService() {
	}

	public static synchronized UpdateService getService() {
		if (instance == null) {
			instance = new UpdateService();
		}

		return instance;
	}

	public String getData(Context context, String md5List, String copyRightId,
			String pluginId) throws Exception {

		List<Parameter> params = new ArrayList<Parameter>();

		Parameter param = new Parameter();
		param.setName("Action");
		param.setValue("update");
		params.add(param);

		param = new Parameter();
		param.setName("copyRightId");
		param.setValue(copyRightId);
		params.add(param);

		param = new Parameter();
		param.setName("md5List");
		param.setValue(md5List);
		params.add(param);
		/**
		 * 检测单个插件是否有更新
		 */
		if (!StringUtils.isNull(pluginId)) {
			param = new Parameter();
			param.setName("pluginId");
			param.setValue(pluginId);
			params.add(param);
		}

		String paramsString = URLHelper.getParamsString(params, true);

		// MyOkHttpClient client = new MyOkHttpClient();
		// String ret = client.httpGet(HttpHelper.HOST_UPDATE_PLUGIN_VERSION,
		// paramsString);

		HttpClient client = new HttpClient();
		String ret = client.httpGet(HttpHelper.HOST_UPDATE_PLUGIN_VERSION,
				paramsString);

		return ret;
	}
}
