package com.kuyun.plugin.framework.update;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.kuyun.plugin.framework.api.PluginConstante;
import com.kuyun.plugin.framework.model.ICheckPluginListener;
import com.kuyun.plugin.framework.model.PluginPath;
import com.kuyun.plugin.framework.update.PluginVersionData.Item;
import com.kuyun.smarttv.sdk.resource.api.debug.Console;
import com.kuyun.smarttv.sdk.resource.api.util.BaseRunnable;
import com.kuyun.smarttv.sdk.resource.api.util.Constants;
import com.kuyun.smarttv.sdk.resource.api.util.DateUtil;
import com.kuyun.smarttv.sdk.resource.api.util.StringUtils;

public class PluginUpdateRunnable implements BaseRunnable {

	private static final String TAG = "PluginVersionRunnable";

	private static final int MAX_RETRY_COUNT = 3;
	private long mSleepTime;
	private int mGetRetryCount;
	private Context mContext;
	private String mMd5s;

	private String mCopyRightId;

	private String pluginId;
	private ICheckPluginListener mICheckPluginListener;
	private IDownloadListener mIDownloadListener;

	public PluginUpdateRunnable(Context context, String md5s, String copyRightId) {
		mContext = context;
		mMd5s = md5s;
		mCopyRightId = copyRightId;
	}

	public PluginUpdateRunnable(Context context, String md5s,
			String copyRightId, String pluginId, ICheckPluginListener l) {
		mContext = context;
		mMd5s = md5s;
		mCopyRightId = copyRightId;
		this.pluginId = pluginId;
		mICheckPluginListener = l;
		if (!StringUtils.isNull(pluginId) && mICheckPluginListener != null) {
			mIDownloadListener = new IDownloadListener() {
				@Override
				public void onSucc(String pluginId) {
					doSuccListener();
				}

				@Override
				public void onFail(String pluginId, int code, String msg) {
					doErrListener(msg);
				}

			};
		}
	}

	@Override
	public void run() {
		try {
			if (mSleepTime > 0)
				Thread.sleep(mSleepTime);

			loadServer();

		} catch (InterruptedException e) {
			Console.d(TAG, "Exception = " + e);
			onError(e.getMessage());
		}
	}

	@Override
	public void loadLocal() {
	}

	@Override
	public void loadServer() {
		load();
	}

	public void load() {
		Console.d(TAG, "load: " + mMd5s);
		String ret = null;

		try {
			ret = UpdateService.getService().getData(mContext, mMd5s,
					mCopyRightId, pluginId);

		} catch (Exception e) {
			Console.printStackTrace(e);
			onError(e.getMessage());
			// mHandler.sendEmptyMessage(PluginUpdateManager.MSG_UPDATE_ALGORITHM_VERSION_FAILED);
			return;
		}

		if (ret != null) {
			Console.d(TAG, ret);
			try {
				JSONObject jo = new JSONObject(ret);
				JSONObject response = jo
						.getJSONObject(PluginConstante.KEY_RESPONSE);
				if (response != null) {
					String returnCode = response
							.getString(PluginConstante.KEY_RESULT);
					if (returnCode != null
							&& Constants.VALUE_RESULT_CODE_SUCCESS
									.equals(returnCode)) {
						Gson gson = new Gson();
						PluginVersionData data = gson.fromJson(ret,
								PluginVersionData.class);
						if (data != null) {
							onSucc(data);
							return;
						}
					}
				}
			} catch (JSONException e) {
				Console.printStackTrace(e);
			}

		}

		onError("null");
	}

	@Override
	public void save() {
	}

	private final static int TIMEOUT_SCAN = 3600; // seconds
	private final static int TIMEOUT_SHUTDOWN = 10; // seconds

	private final static int THREADS = 3; // FIXME: 并发数, plz set in options
	// again ?
	private ExecutorService mPool;

	private void download(Context context, String md5, String url,
			String pluginId) {
		if (mPool!=null&&!mPool.isShutdown() && !StringUtils.isNull(pluginId)) {
			mPool.execute(new PluginDownloadRunnable(context, md5, url,
					pluginId, mIDownloadListener));
		} else {
			if (mIDownloadListener != null)
				mIDownloadListener.onFail(pluginId, 0, "Pool isShutdown");
		}
	}

	private void onSucc(PluginVersionData data) {

		mGetRetryCount = 0;
		try {
			if (data != null) {
				List<Item> items = data.getItems();
				if (items != null && items.size() > 0) {
					if (items.size() == 1) {
						Item item = items.get(0);
						String pluginId = PluginPath
								.getPluginIdByFileName(item.url);
						if (!StringUtils.isNull(pluginId)) {
							PluginDownloadRunnable runnable = new PluginDownloadRunnable(
									mContext, item.md5, item.url, pluginId,
									mIDownloadListener);
							runnable.run();
						}else{
							if (mIDownloadListener != null)
								mIDownloadListener.onFail(pluginId, 0, "pluginId is null");
						}
					} else {
						//如果有多个插件需要同时升级,则使用线程池进行并发下载
						int num = Math.min(THREADS, items.size());

						mPool = Executors.newFixedThreadPool(num);

						for (Item item : items) {
							String pluginId = PluginPath
									.getPluginIdByFileName(item.url);
							download(mContext, item.md5, item.url, pluginId);
						}
					}
				} else {
					doErrListener("items is null");
				}
			} else {
				doErrListener("data is null");
			}
		} catch (Exception e) {
			e.printStackTrace();
			doErrListener("null");
		} finally {
			if (mPool != null) {
				mPool.shutdown();
				try {
					if (!mPool.awaitTermination(TIMEOUT_SCAN, TimeUnit.SECONDS)) {
						mPool.shutdownNow();
						Console.e(TAG, "Shutting down pool");
						if (!mPool.awaitTermination(TIMEOUT_SHUTDOWN,
								TimeUnit.SECONDS)) {
							Console.e(TAG, "Pool did not terminate");
						}
					}
				//	mPool = null;
				} catch (InterruptedException e) {
					Log.e(TAG, e.getMessage());
					mPool.shutdownNow();
				//	mPool = null;
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	private void onError(String err) {
		if (mGetRetryCount >= MAX_RETRY_COUNT) {
			mGetRetryCount = 0;
			doErrListener(err);
			return;
		}
		mGetRetryCount++;
		mSleepTime = DateUtil.getDelayTimeByRetryCount(mGetRetryCount,
				Constants.BASE_DELAY_TIME);

		Console.d(TAG, "mSleepTime=" + mSleepTime);

		run();
	}

	private void doSuccListener() {
		if (mICheckPluginListener != null && !StringUtils.isNull(pluginId)) {
			mICheckPluginListener.onSucc(pluginId);
		}
	}

	private void doErrListener(String err) {
		if (mICheckPluginListener != null && !StringUtils.isNull(pluginId)) {
			mICheckPluginListener.onFail(pluginId, 0, err);
		}
	}
}
