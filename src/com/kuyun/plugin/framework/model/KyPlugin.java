package com.kuyun.plugin.framework.model;

import android.content.pm.PackageInfo;

public class KyPlugin implements IKyPlugin {
	public PackageInfo packageInfo;
	public String pluginId;
	public String pluginPath;
	public String mainClass;
	public int launchMode;
	public int versionCode;
	public String md5;

}
