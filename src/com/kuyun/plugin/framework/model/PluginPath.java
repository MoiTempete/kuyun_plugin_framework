package com.kuyun.plugin.framework.model;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import com.kuyun.plugin.framework.model.PluginConfig.PluginItem;
import com.kuyun.plugin.framework.utils.MD5;
import com.kuyun.plugin.framework.utils.MyFileUtils;
import com.kuyun.smarttv.sdk.resource.api.debug.Console;
import com.kuyun.smarttv.sdk.resource.api.util.FileUtil;
import com.kuyun.smarttv.sdk.resource.api.util.StringUtils;

public class PluginPath {

	public final static String PATH_PLUGIN_FRAMEWORK_ROOT = "/framework";

	public final static String ASSET_PATH_PLUGIN = "plugins";

	public final static String PATH_PLUGIN_ROOT = "ky_plugins";
	public final static String PATH_PLUGINS = PATH_PLUGIN_ROOT + "/plugins";
	// public final static String SUFIX_PLUGIN = ".kyplg";// 替换.apk
	// 后缀必须为.apk;否则装载
	// public final static String SUFIX_PLUGIN2 = ".apk";// 替换.apk
	public final static String SUFIX_PLUGIN = ".apk";//

	public final static String PLUGIN_CONFIG = "ky_plugin_config.json";

	public final static String PLUGIN_SO_PATH = "lib/armeabi/";
	public final static String PLUGIN_SO_SUFFIX = ".so";
	private static final int DATA_BUFFER = 8 * 1024;

	public static FilenameFilter fileNameFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name != null && name.endsWith(SUFIX_PLUGIN);
		}
	};

	public static boolean isKyPluginFile(String fileName) {
		if (StringUtils.isNull(fileName))
			return false;
		return fileName.endsWith(SUFIX_PLUGIN);
	}

	public static String getPluginPath(Context context, String pluginId) {
		String path = getPluginPath(context) + "/" + pluginId + SUFIX_PLUGIN;
		return path;
	}

	public static String getPluginDexPath(Context context) {
		File dexOutputDir = context.getDir("dex", Context.MODE_PRIVATE);
		return dexOutputDir.getAbsolutePath();
	}

	public static String getPluginDexPath(Context context, String pluginId) {
		String path = getPluginDexPath(context);
		return path + "/" + pluginId + ".dex";
	}

	public static String getPluginSoPath(Context context, String pluginId,
			String soName) {
		return getPluginSoPath(context, pluginId) + "/" + soName;
	}

	public static String getPluginSoPath(Context context, String pluginId) {
		String path = getPluginPath(context);
		PluginItem plugin = KyPluginManager.getInstance().getPluginItem(
				pluginId);
		String md5 = "md5";
		if (plugin != null) {
			md5 = plugin.md5;
		} else {
			try {
				md5 = MD5.getMd5(path);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return path + "/" + pluginId + "/" + md5;
	}

	public static String getPluginIdByFileName(String fileName) {
		if (StringUtils.isNull(fileName) || !isKyPluginFile(fileName))
			return null;
		int index = fileName.lastIndexOf("/");
		if (index != -1) {
			fileName = fileName.substring(index + 1);
		}
		return fileName.replace(SUFIX_PLUGIN, "");
	}

	public static String getPluginFileName(String id) {
		return id + SUFIX_PLUGIN;
	}

	public static String getPluginConfig(Context context) {
		String path = getPluginRootPath(context) + "/" + PLUGIN_CONFIG;
		return path;
	}

	public static String getPluginRootPath(Context context) {
		String path = getInnerRootPath(context) + "/" + PATH_PLUGIN_ROOT;
		return path;
	}

	public static String getPluginPath(Context context) {
		String path = getInnerRootPath(context) + "/" + PATH_PLUGINS;

		return path;
	}

	public static String getInnerRootPath(Context context) {
		String path = FileUtil.getKYTvDirPath();
		if (TextUtils.isEmpty(path)) {
			// path = FileUtil.getInternalMemoryRootPath(context);
			path = getInternalMemoryRootPath(context);
		}
		return path;
	}

	public static String getInternalMemoryRootPath(Context context) {
		return context.getFilesDir().getAbsolutePath()
				+ PATH_PLUGIN_FRAMEWORK_ROOT;
	}

	/**
	 * 从assets/plugins中拷贝plugin到应用目录下,如果应用目录下已经有该plugin,则不做拷贝
	 * 
	 * @param context
	 * @return
	 */
	public static PluginConfig copyPluginsFromAssets(Context context) {
		AssetManager assetManager = context.getAssets();
		String[] files = null;
		try {
			files = assetManager.list(ASSET_PATH_PLUGIN);
		} catch (IOException e) {
			Log.e("tag", "Failed to get asset file list.", e);
		}
		if (files == null || files.length == 0)
			return null;
		HashMap<String, String> map = new HashMap<String, String>();
		for (String file : files) {
			map.put(file, file);
		}
		String path = getPluginPath(context);
		File desFile = new File(path);
		if (!desFile.exists())
			desFile.mkdirs();
		else {
			File[] currFiles = desFile.listFiles(fileNameFilter);
			for (File file : currFiles) {
				String fileName = file.getName();
				map.remove(fileName);
			}
		}
		if (map.size() == 0)
			return null;
		String copyFiles[] = new String[map.size()];
		copyFiles = map.values().toArray(copyFiles);

		PluginConfig config = KyPluginManager.getInstance().loadPluginConfig(
				context);
		boolean bChange = false;
		if (config == null) {
			config = KyPluginManager.getInstance().newPluginConfig();
		}
		Console.d("copyAssets", "copyFiles.len=" + copyFiles.length);
		for (String filename : copyFiles) {
			InputStream in = null;
			OutputStream out = null;
			Console.d("copyAssets", "filename=" + filename);
			try {
				in = assetManager.open(ASSET_PATH_PLUGIN + "/" + filename);
				File outFile = new File(path, filename);
				out = new FileOutputStream(outFile);
				copyFile(in, out);
				out.flush();

				try {
					String apkPath = outFile.getAbsolutePath();
					String md5 = MD5.getMd5(apkPath);
					PluginItem item = new PluginItem();
					item.md5 = md5;
					item.id = getPluginIdByFileName(filename);
					config.addItem(item);
					bChange = true;

					saveSoFromApk(context, item.id, apkPath);

				} catch (Exception e) {
					e.printStackTrace();
				}

			} catch (IOException e) {
				e.printStackTrace();
				Log.e("tag", "Failed to copy asset file: " + filename, e);
			} finally {
				if (in != null)
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				in = null;
				if (out != null)
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				out = null;
			}
		}
		if (bChange && config != null) {
			KyPluginManager.getInstance().savePluginConfig(context, config);
		}
		return config;
	}

	private static void copyFile(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	public static void saveSoFromApk(Context context, String pluginId,
			String apkPath) {

		String soPath = getPluginSoPath(context, pluginId);
		File soFile = new File(soPath);
		if (!soFile.exists())
			soFile.mkdirs();

		String[] st = new String[2];
		byte b[] = new byte[1024];
		int length;
		ZipFile zipFile;
		try {
			zipFile = new ZipFile(new File(apkPath));
			Enumeration<?> enumeration = zipFile.entries();
			ZipEntry zipEntry = null;
			while (enumeration.hasMoreElements()) {
				zipEntry = (ZipEntry) enumeration.nextElement();
				if (zipEntry.isDirectory()) {

				} else {
					String name = zipEntry.getName();
					if (name.contains(PLUGIN_SO_PATH)
							&& name.endsWith(PLUGIN_SO_SUFFIX)) {
						FileOutputStream fos = null;
						BufferedOutputStream dest = null;
						InputStream in = null;
						try {
							in = zipFile.getInputStream(zipEntry);
							String fileName = name.replace(PLUGIN_SO_PATH, "");
							String destPath = soPath + "/" + fileName;
							File file = new File(destPath);
							if (file.exists()) {
								file.delete();
							}
							file.createNewFile();
							int count;
							byte data[] = new byte[DATA_BUFFER];
							fos = new FileOutputStream(file);
							dest = new BufferedOutputStream(fos, DATA_BUFFER);
							while ((count = in.read(data, 0, DATA_BUFFER)) != -1) {
								dest.write(data, 0, count);
							}
							dest.flush();
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							try {
								if (dest != null) {
									dest.close();
								}
								if (in != null) {
									in.close();
								}
								if (fos != null) {
									fos.close();
								}
							} catch (Exception e) {
							}
						}
					}
				}
			}
		} catch (IOException e) {
		}
	}

	public static boolean deleteOldData(Context context, String pluginId) {
		boolean b = false;
		if (context == null || StringUtils.isNull(pluginId))
			return b;
		String dexPath = PluginPath.getPluginDexPath(context);
		String dexFile = dexPath + "/" + pluginId + ".dex";
		String artFile = dexPath + "/" + pluginId + ".oat";
		MyFileUtils.deleteFile(new File(dexFile));
		MyFileUtils.deleteFile(new File(artFile));

		String soPath = PluginPath.getPluginSoPath(context, pluginId);
		MyFileUtils.deleteDir(new File(soPath));

		return b;
	}

}
