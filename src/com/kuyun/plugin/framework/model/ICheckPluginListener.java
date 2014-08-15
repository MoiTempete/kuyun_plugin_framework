package com.kuyun.plugin.framework.model;

public interface ICheckPluginListener {
	public void onSucc(String pluginId);

	public void onFail(String pluginId,int code, String msg);
}
