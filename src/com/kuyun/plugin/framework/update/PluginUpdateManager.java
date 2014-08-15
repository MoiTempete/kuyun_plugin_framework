package com.kuyun.plugin.framework.update;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.kuyun.plugin.framework.model.ICheckPluginListener;
import com.kuyun.plugin.framework.model.KyPluginManager;
import com.kuyun.smarttv.sdk.resource.api.util.StringUtils;
import com.kuyun.smarttv.sdk.resource.net.URLHelper;

public class PluginUpdateManager {
	
	public static final int MSG_UPDATE_ALGORITHM_VERSION_SUCCESSED = 0;
	public static final int MSG_UPDATE_ALGORITHM_VERSION_FAILED = 1;
	public static final int MSG_DOWNLOAD_ALGORITHM_SUCCESSED = 2;
	public static final int MSG_DOWNLOAD_ALGORITHM_FAILED = 3;

	private static PluginUpdateManager instance;

	private List<String> downloadingList = new ArrayList<String>();
//	protected Handler mHandler = new Handler(Looper.getMainLooper()) {
//		@Override
//		public void handleMessage(Message msg) {
//
//		}
//	};

	private PluginUpdateManager() {

	}

	public static PluginUpdateManager getInstance() {
		if (instance == null) {
			synchronized (PluginUpdateManager.class) {
				if (instance == null)
					instance = new PluginUpdateManager();
				return instance;
			}
		} else {
			return instance;
		}
	}

	public boolean isDownloadingPlugin(String pluginId) {
		return downloadingList != null && downloadingList.contains(pluginId);
	}

	public void addDownloadingPlugin(String pluginId) {
		synchronized (downloadingList) {
			if (!downloadingList.contains(pluginId))
				downloadingList.add(pluginId);
		}
	}

	public void removeDownloadingPlugin(String pluginId) {
		synchronized (downloadingList) {
			downloadingList.remove(pluginId);
		}
	}

	public void clearDownloadingPlugin() {
		synchronized (downloadingList) {
			downloadingList.clear();
		}
	}

	private void checkUpdate(Context mContext) {
		String md5List = KyPluginManager.getInstance().getMd5List();
		if (StringUtils.isNull(md5List))
			return;
		checkUpdate(mContext, md5List, null, null);
	}

	private void checkUpdate(Context mContext, String md5List, String pluginId,
			ICheckPluginListener l) {
		if (StringUtils.isNull(md5List))
			return;
		String copyRightId = URLHelper.COPYRIGHT_ID;
		new PluginUpdateRunnable(mContext, md5List, copyRightId, pluginId, l)
				.load();
	}

	/**
	 * 从server检测是否有插件更新
	 */
	public void checkPluginChanged(final Context context) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				checkUpdate(context);
			}
		}).start();
	}

	public void checkPlugin(final Context context, final String md5,
			final String pluginId, final ICheckPluginListener l) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				checkUpdate(context, md5, pluginId, l);
			}
		}).start();
	}
}
