package com.kuyun.plugin.framework.update;

import java.io.File;

import android.content.Context;
import android.content.pm.ActivityInfo;

import com.kuyun.plugin.framework.exception.PluginException;
import com.kuyun.plugin.framework.model.KyPlugin;
import com.kuyun.plugin.framework.model.KyPluginManager;
import com.kuyun.plugin.framework.model.PluginPath;
import com.kuyun.plugin.framework.utils.DLUtils;
import com.kuyun.plugin.framework.utils.MD5;
import com.kuyun.plugin.framework.utils.MyFileUtils;
import com.kuyun.smarttv.sdk.resource.api.util.BaseRunnable;
import com.kuyun.smarttv.sdk.resource.api.util.StringUtils;

public class PluginDownloadRunnable implements BaseRunnable {
	private String md5;
	private String url;
	private String pluginId;
	private Context context;
	private IDownloadListener mIDownloadListener;

	public PluginDownloadRunnable(Context context, String md5, String url,
			String pluginId, IDownloadListener l) {
		this.context = context;
		this.md5 = md5;
		this.url = url;
		this.pluginId = pluginId;
		mIDownloadListener = l;
	}

	@Override
	public void run() {
		loadServer();
	}

	@Override
	public void loadLocal() {

	}

	@Override
	public void loadServer() {

		PluginUpdateManager.getInstance().addDownloadingPlugin(pluginId);

		String dir = PluginPath.getPluginPath(context);
		String fileName = PluginPath.getPluginFileName(pluginId);
		String fileName_temp = fileName + ".tmp";
		// MyOkHttpClient client = new MyOkHttpClient();
		boolean bSucc = false;
		String err = null;
		try {
			if (HttpHelper.downloadFile(url, dir, fileName_temp)) {
				// if (MyOkHttpClient.downloadFile(url, dir, fileName_temp)) {
				File sourceFile = new File(dir, fileName_temp);
				// check md5
				if (!checkMd5(sourceFile.getAbsolutePath())) {
					MyFileUtils.deleteFile(sourceFile);
					return;
				}

				File destFile = new File(dir, fileName);
				if (MyFileUtils.rename(sourceFile, destFile)) {

					// 必须删除dex或oat,包括so文件,否则动态装载会失败
					PluginPath.deleteOldData(context, pluginId);
					
					try {
						KyPlugin plugin = KyPluginManager.getInstance()
								.loadPluginByPath(context,
										destFile.getAbsolutePath());

						ActivityInfo info = DLUtils.getMainActivityInfo(context, plugin.packageInfo);
						
						plugin.mainClass = info.name;
						plugin.launchMode = info.launchMode;
						
						KyPluginManager.getInstance().removePluginClassLoader(plugin.pluginPath);

						DLUtils.prevOptimizationDex(context, plugin.pluginPath,
								plugin.mainClass);

						KyPluginManager.getInstance()
								.regPlugin(context, plugin);

						PluginPath.saveSoFromApk(context, plugin.pluginId,
								plugin.pluginPath);

						bSucc = true;
						onSucc();

					} catch (PluginException e) {
						e.printStackTrace();
						err = e.getMessage();
					}
				} else {
					err = "rename fail";
				}
			} else {
				err = "download fail";
			}
		} catch (Exception e) {
			e.printStackTrace();
			err = e.getMessage();
		} finally {
			if (!bSucc) {
				onErr(err);
			}
		}
	}

	public boolean checkMd5(String path) {
		if (StringUtils.isNull(md5))
			return true;
		String md5_ = null;
		try {
			md5_ = MD5.getMd5(path);
			return md5.equalsIgnoreCase(md5_);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void save() {

	}

	public void onSucc() {
		PluginUpdateManager.getInstance().removeDownloadingPlugin(pluginId);
		if (mIDownloadListener != null) {
			mIDownloadListener.onSucc(pluginId);
		}
	}

	public void onErr(String err) {
		PluginUpdateManager.getInstance().removeDownloadingPlugin(pluginId);
		if (mIDownloadListener != null) {
			mIDownloadListener.onFail(pluginId, 0, err);
		}
	}
}
