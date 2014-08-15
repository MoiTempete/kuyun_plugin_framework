package com.kuyun.plugin.framework.update;

public interface IDownloadListener {
	public void onSucc(String pluginId);

	public void onFail(String pluginId, int code, String msg);
}
