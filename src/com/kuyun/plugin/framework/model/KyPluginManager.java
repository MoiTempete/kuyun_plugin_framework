package com.kuyun.plugin.framework.model;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kuyun.plugin.framework.api.PluginConstante;
import com.kuyun.plugin.framework.exception.PluginException;
import com.kuyun.plugin.framework.model.PluginConfig.PluginItem;
import com.kuyun.plugin.framework.proxy.ProxyActivity;
import com.kuyun.plugin.framework.proxy.ProxyActivityForSingleInstance;
import com.kuyun.plugin.framework.proxy.ProxyActivityForSingleTask;
import com.kuyun.plugin.framework.proxy.ProxyActivityForSingleTop;
import com.kuyun.plugin.framework.update.HttpHelper;
import com.kuyun.plugin.framework.update.PluginUpdateManager;
import com.kuyun.plugin.framework.utils.DLUtils;
import com.kuyun.plugin.framework.utils.MD5;
import com.kuyun.plugin.framework.utils.MyFileUtils;
import com.kuyun.smarttv.sdk.resource.api.debug.Console;
import com.kuyun.smarttv.sdk.resource.api.util.AsyncLoader;
import com.kuyun.smarttv.sdk.resource.api.util.StringUtils;

import dalvik.system.DexClassLoader;

public class KyPluginManager {
	public static final int CHECK_PLUGIN_TIMEOUT = 15000;// 15s
	private final static String TAG = "KyPluginManager";
	private static KyPluginManager instance;

	private HashMap<String, KyPlugin> pluginMap = new HashMap<String, KyPlugin>();
	private PluginConfig mPluginConfig;

	private Class mProxyActivityClass;
	private Class mProxyActivityForSingleInstanceClass;
	private Class mProxyActivityForSingleTaskClass;
	private Class mProxyActivityForSingleTopClass;

	private AsyncLoader mAsyncLoader;

	Runnable checkTimeOutRunnable = null;
	private HashMap<String, HashMap> openParams = new HashMap<String, HashMap>();

	private HashMap<String, ClassLoader> pluginClassLoaderMap = new HashMap<String, ClassLoader>();

	protected Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {

		}
	};

	public void post(Runnable run) {
		if (mHandler != null && run != null) {
			mHandler.post(run);
		}
	}

	private KyPluginManager() {
		mAsyncLoader = new AsyncLoader();

	}

	public static KyPluginManager getInstance() {
		if (instance == null) {
			synchronized (KyPluginManager.class) {
				if (instance == null)
					instance = new KyPluginManager();
				return instance;
			}
		} else {
			return instance;
		}
	}

	/**
	 * 需要在子线程中执行
	 * 
	 * @param context
	 * @throws PluginException
	 */
	public void init(Context context) throws PluginException {

		pluginMap.clear();
		openParams.clear();

		clearPluginClassLoader();

		PluginPath.copyPluginsFromAssets(context);
		loadAllPlugin(context);
		PluginUpdateManager.getInstance().checkPluginChanged(context);
	}

	// public HashMap getOpenParams(String pluginId) {
	// synchronized (openParams) {
	// return openParams != null ? openParams.get(pluginId) : null;
	// }
	// }

	public void addPluginClassLoader(String pluginId, ClassLoader loader) {
		if (StringUtils.isNull(pluginId) || loader == null)
			return;
		synchronized (openParams) {
			if (pluginClassLoaderMap == null)
				pluginClassLoaderMap = new HashMap<String, ClassLoader>();
			pluginClassLoaderMap.put(pluginId, loader);
		}
	}

	public ClassLoader removePluginClassLoader(String pluginId) {
		synchronized (pluginClassLoaderMap) {
			return pluginClassLoaderMap.remove(pluginId);
		}
	}

	public ClassLoader getPluginClassLoader(String pluginId) {
		synchronized (pluginClassLoaderMap) {
			return pluginClassLoaderMap.get(pluginId);
		}
	}

	public void clearPluginClassLoader() {
		synchronized (pluginClassLoaderMap) {
			pluginClassLoaderMap.clear();
		}
	}

	public ClassLoader getPluginClassLoader(Context context, String dexPath) {
		ClassLoader loader = getPluginClassLoader(dexPath);
		if (loader == null) {
			String dexOutputPath = PluginPath.getPluginDexPath(context);
			loader = new DexClassLoader(dexPath, dexOutputPath, null,
					context.getClassLoader());

			addPluginClassLoader(dexPath, loader);
		}
		return loader;
	}

	public void addOpenParam(String pluginId, HashMap map) {
		synchronized (openParams) {
			if (map == null)
				map = new HashMap();
			openParams.put(pluginId, map);
		}
	}

	public HashMap removeOpenParam(String pluginId) {
		synchronized (openParams) {
			return openParams.remove(pluginId);
		}
	}

	public void clearOpenParam() {
		synchronized (openParams) {
			openParams.clear();
		}
		if (checkTimeOutRunnable != null)
			mHandler.removeCallbacks(checkTimeOutRunnable);
	}

	public AsyncLoader getAsyncLoader() {
		return mAsyncLoader;
	}

	public void regProxyActivity(Class clss) {
		mProxyActivityClass = clss;
	}

	public void regProxyActivityForSingleInstance(Class clss) {
		mProxyActivityForSingleInstanceClass = clss;
	}

	public void regProxyActivityForSingleTask(Class clss) {
		mProxyActivityForSingleTaskClass = clss;
	}

	public void regProxyActivityForSingleTop(Class clss) {
		mProxyActivityForSingleTopClass = clss;
	}

	public Class getProxyActivity(int launchMode) {
		switch (launchMode) {
		case ActivityInfo.LAUNCH_MULTIPLE:
			if (mProxyActivityClass != null)
				return mProxyActivityClass;
			return ProxyActivity.class;
		case ActivityInfo.LAUNCH_SINGLE_INSTANCE:
			if (mProxyActivityForSingleInstanceClass != null)
				return mProxyActivityForSingleInstanceClass;
			return ProxyActivityForSingleInstance.class;
		case ActivityInfo.LAUNCH_SINGLE_TASK:
			if (mProxyActivityForSingleTaskClass != null)
				return mProxyActivityForSingleTaskClass;
			return ProxyActivityForSingleTask.class;
		case ActivityInfo.LAUNCH_SINGLE_TOP:
			if (mProxyActivityForSingleTopClass != null)
				return mProxyActivityForSingleTopClass;
			return ProxyActivityForSingleTop.class;
		}

		if (mProxyActivityClass != null)
			return mProxyActivityClass;
		return ProxyActivity.class;
	}

	public boolean hasPlugin() {
		return pluginMap != null && pluginMap.size() > 0;
	}

	public boolean hasPlugin(String pluginId) {
		return pluginMap != null && pluginMap.containsKey(pluginId);
	}

	public KyPlugin getPlugin(String pluginId) {
		return pluginMap != null ? pluginMap.get(pluginId) : null;
	}

	public void regPlugin(Context context, KyPlugin plugin) {
		synchronized (pluginMap) {
			if (plugin != null) {
				pluginMap.put(plugin.pluginId, plugin);
				if (mPluginConfig != null) {
					PluginItem item = new PluginItem();
					item.id = plugin.pluginId;
					item.md5 = plugin.md5;
					item.mainClass = plugin.mainClass;
					item.launchMode = plugin.launchMode;
					item.lastCheckTime = System.currentTimeMillis();
					if (mPluginConfig.contain(item)) {
						mPluginConfig.updateItem(item);
					} else {
						mPluginConfig.addItem(item);
					}
					savePluginConfig(context, mPluginConfig);
				}
			}
		}
	}

	public String getMd5List() {
		if (pluginMap == null || pluginMap.size() == 0)
			return null;
		KyPlugin[] plugins = new KyPlugin[pluginMap.size()];
		plugins = pluginMap.values().toArray(plugins);
		StringBuffer buff = new StringBuffer();
		for (KyPlugin plugin : plugins) {
			buff.append(plugin.md5 + ",");
		}
		return buff.toString();
	}

	/**
	 * 
	 * @param context
	 * @param pluginId
	 * @param params
	 *            :参数
	 * @throws Exception
	 */
	public void openPlugin(Context context, String pluginId,
			HashMap<String, Serializable> params) throws Exception {

		// 清除上一次等待打开的插件.
		clearOpenParam();

		if (PluginUpdateManager.getInstance().isDownloadingPlugin(pluginId)) {
			Console.e(TAG, pluginId + ":正在下载");
			return;
		}
		// 如果插件不存在,则直接检测下载,在CHECK_PLUGIN_TIMEOUT时间范围内将直接弹出
		if (!hasPlugin(pluginId)) {
			addOpenParam(pluginId, params);
			checkPlugin(context, "1", pluginId, true);
			// throw new Exception(pluginId + ":插件不存在!");
			Console.e(TAG, pluginId + ":插件不存在!正在下载...");
			return;
		}

		/**
		 * TODO 是否检测卡片有更新???
		 */
		PluginItem item = mPluginConfig.findItem(pluginId);
		// 如果插件的检测时间过期,则直接检测下载,在CHECK_PLUGIN_TIMEOUT时间范围内将直接弹出
		if (item != null && item.isCheckTimeout()) {
			addOpenParam(pluginId, params);
			item.lastCheckTime = System.currentTimeMillis();
			checkPlugin(context, item.md5, pluginId, false);
		} else {
			startPlugin(context, pluginId, params);
		}
	}

	/**
	 * 
	 * @param context
	 * @param md5
	 * @param pluginId
	 * @param bNew
	 *            :是否是新的插件
	 */
	private void checkPlugin(final Context context, final String md5,
			final String pluginId, final boolean bNew) {
		checkTimeOutRunnable = new Runnable() {
			@Override
			public void run() {
				// 处理超时
				final HashMap params = removeOpenParam(pluginId);
				if (!bNew) {
					post(new Runnable() {
						@Override
						public void run() {
							try {
								startPlugin(context, pluginId, params);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				}
			}
		};
		mHandler.postDelayed(checkTimeOutRunnable, CHECK_PLUGIN_TIMEOUT);
		PluginUpdateManager.getInstance().checkPlugin(
				context,
				md5,
				pluginId,
				genCheckPluginListener(context, pluginId, bNew,
						checkTimeOutRunnable));
	}

	private ICheckPluginListener genCheckPluginListener(final Context context,
			final String pluginId, final boolean bNew,
			final Runnable checkTimeOutRunnable) {
		ICheckPluginListener l = new ICheckPluginListener() {
			@Override
			public void onSucc(final String pluginId) {
				mHandler.removeCallbacks(checkTimeOutRunnable);
				final HashMap params = removeOpenParam(pluginId);
				if (params == null)
					return;
				post(new Runnable() {
					@Override
					public void run() {
						try {
							startPlugin(context, pluginId, params);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

			}

			@Override
			public void onFail(final String pluginId, int code, String msg) {

				Console.e(TAG, "CheckPluginListener:onFail:" + msg);

				mHandler.removeCallbacks(checkTimeOutRunnable);
				final HashMap params = removeOpenParam(pluginId);
				if (params == null)
					return;
				if (!bNew) {
					post(new Runnable() {
						@Override
						public void run() {
							try {
								startPlugin(context, pluginId, params);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				}
			}
		};
		return l;
	}

	private void startPlugin(Context context, String pluginId,
			HashMap<String, Serializable> params) throws Exception {
		KyPlugin plugin = getPlugin(pluginId);
		if (plugin == null) {
			Console.e(TAG, "startPlugin:plugin is't registed");
			return;
		}
		int launchMode = plugin.launchMode;
		Intent intent = new Intent(context, getProxyActivity(launchMode));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(PluginConstante.EXTRA_DEX_PATH,
				PluginPath.getPluginPath(context, pluginId));
		if (plugin != null && !StringUtils.isNull(plugin.mainClass)) {
			intent.putExtra(PluginConstante.EXTRA_CLASS, plugin.mainClass);
		}
		intent.putExtra(PluginConstante.EXTRA_PARAMS, params);
		context.startActivity(intent);

	}

	private void loadAllPlugin(Context context) throws PluginException {

		PluginConfig config = loadPluginConfig(context);
		if (config != null && config.hasPlugin()) {
			synchronized (config.items) {
				int size = config.items.size();
				for (int i = 0; i < size; i++) {
					if (i < config.items.size()) {
						PluginItem ii = config.items.get(i);

						ii.lastCheckTime = System.currentTimeMillis();

						KyPlugin plugin = new KyPlugin();

						plugin.md5 = ii.md5;
						plugin.pluginId = ii.id;
						plugin.mainClass = ii.mainClass;
						plugin.launchMode = ii.launchMode;
						plugin.pluginPath = PluginPath.getPluginPath(context,
								ii.id);
						plugin.packageInfo = DLUtils.getPackageInfo(context,
								plugin.pluginPath);
						if (plugin.packageInfo != null)
							plugin.versionCode = plugin.packageInfo.versionCode;

						pluginMap.put(plugin.pluginId, plugin);
					}
				}
			}
			return;
		}

		String rootPath = PluginPath.getPluginPath(context);
		File rootDir = new File(rootPath);
		if (!rootDir.exists()) {
			rootDir.mkdirs();
			return;
		}
		mPluginConfig = new PluginConfig();

		String[] files = rootDir.list(PluginPath.fileNameFilter);
		if (files != null && files.length > 0) {
			for (String path : files) {
				try {
					KyPlugin plugin = loadPluginByPath(context, path);
					if (plugin != null) {

						DLUtils.prevOptimizationDex(context, plugin.pluginPath,
								plugin.mainClass);

						ActivityInfo info = DLUtils.getMainActivityInfo(
								context, plugin.packageInfo);

						plugin.mainClass = info.name;
						plugin.launchMode = info.launchMode;
						pluginMap.put(plugin.pluginId, plugin);

						PluginItem item = new PluginItem();
						item.id = plugin.pluginId;
						item.md5 = plugin.md5;
						item.mainClass = plugin.mainClass;
						item.launchMode = plugin.launchMode;

						item.lastCheckTime = System.currentTimeMillis();

						mPluginConfig.addItem(item);

					}
				} catch (PluginException e) {
					e.printStackTrace();
				}
			}
		}

		savePluginConfig(context, config);

	}

	public KyPlugin loadPluginById(Context context, String pluginId)
			throws PluginException {
		String path = PluginPath.getPluginPath(context, pluginId);
		return loadPluginByPath(context, path);
	}

	public KyPlugin loadPluginByPath(Context context, String path)
			throws PluginException {
		KyPlugin item = new KyPlugin();
		String md5 = null;
		try {
			md5 = MD5.getMd5(path);
		} catch (Exception e) {
			e.printStackTrace();
			throw new PluginException(e);
		}
		if (StringUtils.isNull(md5))
			throw new PluginException(new Exception(path + ":无法获取md5值!"));
		item.pluginPath = path;
		item.md5 = md5;
		item.pluginId = PluginPath.getPluginIdByFileName(path);
		item.packageInfo = DLUtils.getPackageInfo(context, item.pluginPath);
		if (item.packageInfo != null)
			item.versionCode = item.packageInfo.versionCode;
		return item;
	}

	protected PluginConfig loadPluginConfig(Context context) {
		if (mPluginConfig != null)
			return mPluginConfig;
		String path = PluginPath.getPluginConfig(context);
		String json = MyFileUtils.load(path);
		if (StringUtils.isNull(json))
			return null;
		try {
			// Gson gson = new Gson();
			Gson gson = new GsonBuilder()
					.excludeFieldsWithoutExposeAnnotation().create();
			mPluginConfig = gson.fromJson(json, PluginConfig.class);
			mPluginConfig.initMap();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mPluginConfig;
	}

	public PluginConfig newPluginConfig() {
		mPluginConfig = new PluginConfig();
		return mPluginConfig;
	}

	public PluginItem getPluginItem(String pluginId) {
		return mPluginConfig != null ? mPluginConfig.findItem(pluginId) : null;
	}

	protected void savePluginConfig(Context context, PluginConfig config) {
		if (config == null || !config.hasPlugin())
			return;
		try {
			String path = PluginPath.getPluginConfig(context);
			Gson gson = new GsonBuilder()
					.excludeFieldsWithoutExposeAnnotation().create();
			String json = gson.toJson(config);
			MyFileUtils.save(json, path);

			this.mPluginConfig = config;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setVersion(boolean bTest) {
		HttpHelper.setVersion(bTest);
	}

	public void onDestory() {

		if (mAsyncLoader != null) {
			mAsyncLoader.cancel("");
		}
		if (pluginMap != null)
			pluginMap.clear();
		mPluginConfig = null;
	}

	@Override
	protected void finalize() throws Throwable {
		if (pluginMap != null)
			pluginMap.clear();
		mPluginConfig = null;
		super.finalize();
	}

}
