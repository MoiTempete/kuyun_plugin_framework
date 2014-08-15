package com.kuyun.plugin.framework.utils;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.kuyun.plugin.framework.model.KyPluginManager;
import com.kuyun.plugin.framework.model.PluginPath;
import com.kuyun.smarttv.sdk.resource.api.debug.Console;

import dalvik.system.DexClassLoader;

public class DLUtils {
	private final static String TAG = "DLUtils";

	public static PackageInfo getPackageInfo(Context context, String apkFilepath) {
		PackageManager pm = context.getPackageManager();
		PackageInfo pkgInfo = null;
		try {
			pkgInfo = pm.getPackageArchiveInfo(apkFilepath,
					PackageManager.GET_ACTIVITIES);
		} catch (Exception e) {
			// should be something wrong with parse
			e.printStackTrace();
		}

		return pkgInfo;
	}

	public static Drawable getAppIcon(Context context, String apkFilepath) {
		PackageManager pm = context.getPackageManager();
		PackageInfo pkgInfo = getPackageInfo(context, apkFilepath);
		if (pkgInfo == null) {
			return null;
		}

		// Workaround for http://code.google.com/p/android/issues/detail?id=9151
		ApplicationInfo appInfo = pkgInfo.applicationInfo;
		if (Build.VERSION.SDK_INT >= 8) {
			appInfo.sourceDir = apkFilepath;
			appInfo.publicSourceDir = apkFilepath;
		}

		return pm.getApplicationIcon(appInfo);
	}

	public static CharSequence getAppLabel(Context context, String apkFilepath) {
		PackageManager pm = context.getPackageManager();
		PackageInfo pkgInfo = getPackageInfo(context, apkFilepath);
		if (pkgInfo == null) {
			return null;
		}

		// Workaround for http://code.google.com/p/android/issues/detail?id=9151
		ApplicationInfo appInfo = pkgInfo.applicationInfo;
		if (Build.VERSION.SDK_INT >= 8) {
			appInfo.sourceDir = apkFilepath;
			appInfo.publicSourceDir = apkFilepath;
		}

		return pm.getApplicationLabel(appInfo);
	}

	public static String getMainClass(Context context, String dexPath) {
		PackageInfo packageInfo = context.getPackageManager()
				.getPackageArchiveInfo(dexPath, 1);
		return getMainClass(context, packageInfo);
	}

	public static String getMainClass(Context context, PackageInfo packageInfo) {
		if (packageInfo == null) {
			Console.e("DLUtils", "getMainClass failed packageInfo is null:");
			return null;
		}
		if ((packageInfo.activities != null)
				&& (packageInfo.activities.length > 0)) {

			return packageInfo.activities[0].name;
		}
		return null;
	}

	public static ActivityInfo getMainActivityInfo(Context context,
			PackageInfo packageInfo) {
		if (packageInfo == null) {
			Console.e("DLUtils", "getMainClass failed packageInfo is null:");
			return null;
		}
		if ((packageInfo.activities != null)
				&& (packageInfo.activities.length > 0)) {

			return packageInfo.activities[0];
		}
		return null;
	}

	public static void prevOptimizationDex(Context context, String dexPath,
			String className) {
		
		ClassLoader dexClassLoader = KyPluginManager.getInstance()
				.getPluginClassLoader(context, dexPath);
		
		
//		final String dexOutputPath = PluginPath.getPluginDexPath(context);
//		DexClassLoader dexClassLoader = new DexClassLoader(dexPath,
//				dexOutputPath, null, context.getClassLoader());
		try {
			dexClassLoader.loadClass(className);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
