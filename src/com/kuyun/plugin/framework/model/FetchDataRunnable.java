package com.kuyun.plugin.framework.model;

import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kuyun.smarttv.sdk.resource.api.IKyLoadResourceListener;
import com.kuyun.smarttv.sdk.resource.api.data.KyRsErrData;
import com.kuyun.smarttv.sdk.resource.api.debug.Console;
import com.kuyun.smarttv.sdk.resource.api.util.AbsRunnable;
import com.kuyun.smarttv.sdk.resource.api.util.Constants;
import com.kuyun.smarttv.sdk.resource.api.util.StringUtils;
import com.kuyun.smarttv.sdk.resource.net.HttpClient;
import com.kuyun.smarttv.sdk.resource.net.IHoldConnect;
import com.kuyun.smarttv.sdk.resource.net.Parameter;
import com.kuyun.smarttv.sdk.resource.net.URLHelper;

public class FetchDataRunnable<T> extends AbsRunnable implements IHoldConnect {

	private static final String TAG = "GetTvInfoRunnable";

	private String url;

	private List<Parameter> params;

	private Class<T> classOfT;

	IKyLoadResourceListener<T> mIKyGetResourceListener;

	public FetchDataRunnable(String url, List<Parameter> params,
			IKyLoadResourceListener<T> l, Class<T> classOfT) {
		this.url = url;
		this.params = params;
		this.classOfT = classOfT;
		mIKyGetResourceListener = l;
	}

	@Override
	public void run() {
		loadServer();
	}

	@Override
	public void loadLocal() {

	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadServer() {
		String ret = null;
		try {
			ret = getJson();
		} catch (Exception e) {
			if (Console.isPrintStackTrace) {
				e.printStackTrace();
			}
			doErr(e.getMessage());
		}
		if (ret != null) {
			try {

				Console.print("ret:" + ret);
				JSONObject jo = new JSONObject(ret);
				JSONObject response = jo.getJSONObject(Constants.KEY_RESPONSE);
				if (response != null) {
					String returnCode = response
							.getString(Constants.KEY_RESULTE_CODE);
					if (returnCode != null
							&& Constants.VALUE_RESULT_CODE_SUCCESS
									.equals(returnCode)) {
						Object o;
						try {
							o = classOfT.newInstance();
							Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
							o = gson.fromJson(ret, classOfT);
							if (mIKyGetResourceListener != null) {
								sendLoadSucc(mIKyGetResourceListener, o);
							}
						} catch (InstantiationException e) {
							e.printStackTrace();
							doErr(e.getMessage());
						} catch (IllegalAccessException e) {
							e.printStackTrace();
							doErr(e.getMessage());
						}

					} else {
						doErr("");
					}
				} else {
					doErr("");
				}
			} catch (JSONException e) {
				if (Console.isPrintStackTrace) {
					e.printStackTrace();
				}
				doErr(e.getMessage());
			}

		} else {
			doErr("");
		}

	}

	@Override
	public void save() {

	}

	@Override
	public void holdConnect(HttpURLConnection conn) {
	}

	@Override
	public void releaseConnect() {
	}

	@Override
	public HttpURLConnection getConnect() {
		return null;
	}

	private void doErr(String msg) {
		if (mIKyGetResourceListener != null) {
			sendLoadFailure(mIKyGetResourceListener,
					KyRsErrData.errData(0, msg));
		}
	}

	private String getJson() throws Exception {
		if (StringUtils.isNull(url))
			return null;
		if (params == null)
			params = new ArrayList<Parameter>();
		String paramsString = URLHelper.getParamsString(params, true);
		HttpClient client = new HttpClient();
		String ret = null;
		ret = client.httpGet(url, paramsString);
		return ret;
	}
}
