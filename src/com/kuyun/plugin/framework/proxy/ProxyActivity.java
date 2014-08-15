/**
 * by jdeng
 */
package com.kuyun.plugin.framework.proxy;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.kuyun.plugin.framework.api.PluginBaseActivity;
import com.kuyun.plugin.framework.api.PluginConstante;
import com.kuyun.plugin.framework.model.KyPluginManager;
import com.kuyun.plugin.framework.model.PluginPath;
import com.kuyun.plugin.framework.update.PluginUpdateManager;
import com.kuyun.smarttv.sdk.resource.api.debug.Console;

import dalvik.system.DexClassLoader;

public class ProxyActivity extends Activity {

	private static final String TAG = "ProxyActivity";

	private String mClass;
	private String mDexPath;

	private HashMap params;

	private AssetManager mAssetManager;
	private Resources mResources;
	private Theme mTheme;
	private ClassLoader mClassLoader;

	private PluginBaseActivity mRemoteActivity;
	private HashMap<String, Method> mActivityLifecircleMethods = new HashMap<String, Method>();

	protected void loadResources() {
		try {
			AssetManager assetManager = AssetManager.class.newInstance();
			Method addAssetPath = assetManager.getClass().getMethod(
					"addAssetPath", String.class);
			addAssetPath.invoke(assetManager, mDexPath);
			mAssetManager = assetManager;
		} catch (Exception e) {
			e.printStackTrace();
		}
		Resources superRes = super.getResources();
		mResources = new Resources(mAssetManager, superRes.getDisplayMetrics(),
				superRes.getConfiguration());
		mTheme = mResources.newTheme();
		mTheme.setTo(super.getTheme());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDexPath = getIntent().getStringExtra(PluginConstante.EXTRA_DEX_PATH);
		mClass = getIntent().getStringExtra(PluginConstante.EXTRA_CLASS);

		String pluginId = PluginPath.getPluginIdByFileName(mDexPath);
		if (PluginUpdateManager.getInstance().isDownloadingPlugin(pluginId)) {
			Console.d(TAG, pluginId + ":正在下载");
			finish();
			return;
		}

		Bundle mBundle = getIntent().getExtras();
		if (mBundle != null
				&& mBundle.containsKey(PluginConstante.EXTRA_PARAMS)) {
			Object o = mBundle.get(PluginConstante.EXTRA_PARAMS);
			if (o != null && o instanceof HashMap)
				params = (HashMap) mBundle.get(PluginConstante.EXTRA_PARAMS);
		}

		Console.d(TAG, "mClass=" + mClass + " mDexPath=" + mDexPath);
		long currTime = System.currentTimeMillis();
		loadResources();
		if (mClass == null) {
			launchTargetActivity();
		} else {
			launchTargetActivity(mClass);
		}
		Console.d(TAG,
				"open plugin consume time:"
						+ (System.currentTimeMillis() - currTime));
	}

	protected void launchTargetActivity() {
		PackageInfo packageInfo = getPackageManager().getPackageArchiveInfo(
				mDexPath, 1);
		if (packageInfo == null) {
			Console.e(TAG, "launchTargetActivity failed packageInfo is null:"
					+ mDexPath);
			return;
		}
		if ((packageInfo.activities != null)
				&& (packageInfo.activities.length > 0)) {
			String activityName = packageInfo.activities[0].name;
			mClass = activityName;
			launchTargetActivity(mClass);
		}
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	protected void launchTargetActivity(final String className) {
		Console.d(TAG, "start launchTargetActivity, className=" + className);
		// File dexOutputDir = this.getDir("dex", Context.MODE_PRIVATE);
		// final String dexOutputPath = dexOutputDir.getAbsolutePath();
		// final String dexOutputPath = PluginPath.getPluginDexPath(this);
		//
		// // ClassLoader localClassLoader = ClassLoader.getSystemClassLoader();
		// // DexClassLoader dexClassLoader = new DexClassLoader(mDexPath,
		// // dexOutputPath, null, localClassLoader);
		//
		//
		// //需要cach插件的dexClassLoader,同一个插件一定只能使用同一个dexClassLoader的实例,
		// //否则在加载so的时候将出现以下错误:
		// //java.lang.UnsatisfiedLinkError: unknown failure
		// //Shared lib 'xxxx.so' already opened by CL xxx can't open in xxxx
		// ClassLoader dexClassLoader = KyPluginManager.getInstance()
		// .getPluginClassLoader(mDexPath);
		//
		// if (dexClassLoader == null) {
		// dexClassLoader = new DexClassLoader(mDexPath, dexOutputPath, null,
		// getClassLoader());
		// KyPluginManager.getInstance().addPluginClassLoader(mDexPath,
		// dexClassLoader);
		// }

		ClassLoader dexClassLoader = KyPluginManager.getInstance()
				.getPluginClassLoader(this, mDexPath);
		mClassLoader = dexClassLoader;
		try {
			Class<?> localClass = dexClassLoader.loadClass(className);
			Constructor<?> localConstructor = localClass
					.getConstructor(new Class[] {});
			Object instance = localConstructor.newInstance(new Object[] {});
			setRemoteActivity(instance);
			Log.d(TAG, "instance = " + instance);

			instantiateLifecircleMethods(localClass);

			Method setProxy = localClass.getMethod("setProxy", new Class[] {
					Activity.class, String.class, HashMap.class });
			setProxy.setAccessible(true);
			setProxy.invoke(instance, new Object[] { this, mDexPath, params });

			Bundle bundle = new Bundle();
			bundle.putInt(PluginConstante.FROM, PluginConstante.FROM_EXTERNAL);
			if (mRemoteActivity != null) {
				mRemoteActivity.onCreate(bundle);
			} else {
				Method onCreate = mActivityLifecircleMethods.get("onCreate");
				onCreate.invoke(instance, new Object[] { bundle });
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void instantiateLifecircleMethods(Class<?> localClass) {
		if (mRemoteActivity != null)
			return;
		String[] methodNames = new String[] { "onRestart", "onStart",
				"onResume", "onPause", "onStop", "onDestory" };
		for (String methodName : methodNames) {
			Method method = null;
			try {
				method = localClass.getDeclaredMethod(methodName);
				method.setAccessible(true);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
			mActivityLifecircleMethods.put(methodName, method);
		}

		Method onCreate = null;
		try {
			onCreate = localClass.getDeclaredMethod("onCreate",
					new Class[] { Bundle.class });
			onCreate.setAccessible(true);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		mActivityLifecircleMethods.put("onCreate", onCreate);

		Method onActivityResult = null;
		try {
			onActivityResult = localClass.getDeclaredMethod("onActivityResult",
					new Class[] { int.class, int.class, Intent.class });
			onActivityResult.setAccessible(true);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		mActivityLifecircleMethods.put("onActivityResult", onActivityResult);
	}

	protected void setRemoteActivity(Object activity) {
		try {
			if (activity instanceof PluginBaseActivity)
				mRemoteActivity = (PluginBaseActivity) activity;
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
	}

	@Override
	public AssetManager getAssets() {
		return mAssetManager == null ? super.getAssets() : mAssetManager;
	}

	@Override
	public Resources getResources() {
		return mResources == null ? super.getResources() : mResources;
	}

	@Override
	public Theme getTheme() {
		return mTheme == null ? super.getTheme() : mTheme;
	}

	@Override
	public ClassLoader getClassLoader() {
		ClassLoader classLoader = new ClassLoader(super.getClassLoader()) {
			@Override
			public Class<?> loadClass(String className)
					throws ClassNotFoundException {
				Class<?> clazz = null;
				clazz = mClassLoader.loadClass(className);
				Log.d(TAG, "load class:" + className);
				if (clazz == null) {
					clazz = getParent().loadClass(className);
				}
				// still not found
				if (clazz == null) {
					throw new ClassNotFoundException(className);
				}

				return clazz;
			}
		};
		return classLoader;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult resultCode=" + resultCode);
		Method onActivityResult = mActivityLifecircleMethods
				.get("onActivityResult");
		if (onActivityResult != null) {
			if (mRemoteActivity != null) {
				mRemoteActivity.onActivityResult(requestCode, resultCode, data);
			} else {
				try {
					onActivityResult.invoke(mRemoteActivity, new Object[] {
							requestCode, resultCode, data });
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mRemoteActivity != null) {
			mRemoteActivity.onStart();

		} else {
			Method onStart = mActivityLifecircleMethods.get("onStart");
			if (onStart != null) {
				try {
					// onStart.invoke(mRemoteActivity, new Object[] {});
					onStart.invoke(mRemoteActivity);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (mRemoteActivity != null) {
			mRemoteActivity.onRestart();

		} else {
			Method onRestart = mActivityLifecircleMethods.get("onRestart");
			if (onRestart != null) {
				try {
					// onRestart.invoke(mRemoteActivity, new Object[] {});
					onRestart.invoke(mRemoteActivity);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mRemoteActivity != null) {
			mRemoteActivity.onResume();

		} else {
			Method onResume = mActivityLifecircleMethods.get("onResume");
			if (onResume != null) {
				try {
					// onResume.invoke(mRemoteActivity, new Object[] {});
					onResume.invoke(mRemoteActivity);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (mRemoteActivity != null) {
			mRemoteActivity.onNewIntent(intent);
		}
	}

	@Override
	protected void onPause() {
		if (mRemoteActivity != null) {
			mRemoteActivity.onPause();
		} else {
			Method onPause = mActivityLifecircleMethods.get("onPause");
			if (onPause != null) {
				try {
					// onPause.invoke(mRemoteActivity, new Object[] {});
					onPause.invoke(mRemoteActivity);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		super.onPause();

	}

	@Override
	protected void onStop() {
		if (mRemoteActivity != null) {
			mRemoteActivity.onStop();
		} else {
			Method onStop = mActivityLifecircleMethods.get("onStop");
			if (onStop != null) {
				try {
					// onStop.invoke(mRemoteActivity, new Object[] {});
					onStop.invoke(mRemoteActivity);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (mRemoteActivity != null) {
			mRemoteActivity.onDestroy();
		} else {
			Method onDestroy = mActivityLifecircleMethods.get("onDestroy");
			if (onDestroy != null) {
				try {
					onDestroy.invoke(mRemoteActivity);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		super.onDestroy();
	}

	@Override
	public void finish() {
		if (mRemoteActivity != null) {
			mRemoteActivity.finish();
		}
		super.finish();
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		if (mRemoteActivity != null) {
			mRemoteActivity.onAttachFragment(fragment);
		}
		super.onAttachFragment(fragment);
	}

	@Override
	public void onAttachedToWindow() {
		if (mRemoteActivity != null) {
			mRemoteActivity.onAttachedToWindow();
		}
		super.onAttachedToWindow();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (mRemoteActivity != null) {
			mRemoteActivity.onConfigurationChanged(newConfig);
		}
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onContentChanged() {
		if (mRemoteActivity != null) {
			mRemoteActivity.onContentChanged();
		}
		super.onContentChanged();
	}

	@Override
	public void onDetachedFromWindow() {
		if (mRemoteActivity != null) {
			mRemoteActivity.onDetachedFromWindow();
		}
		super.onDetachedFromWindow();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean b = false;
		if (mRemoteActivity != null) {
			b = mRemoteActivity.onKeyDown(keyCode, event);
		}
		return b || super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		boolean b = false;
		if (mRemoteActivity != null) {
			b = mRemoteActivity.onKeyLongPress(keyCode, event);
		}
		return b || super.onKeyLongPress(keyCode, event);
	}

	@Override
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		boolean b = false;
		if (mRemoteActivity != null) {
			b = mRemoteActivity.onKeyMultiple(keyCode, repeatCount, event);
		}
		return b || super.onKeyMultiple(keyCode, repeatCount, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean b = false;
		if (mRemoteActivity != null) {
			b = mRemoteActivity.onKeyUp(keyCode, event);
		}
		return b || super.onKeyUp(keyCode, event);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (mRemoteActivity != null) {
			mRemoteActivity.onSaveInstanceState(outState);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean b = false;
		if (mRemoteActivity != null) {
			b = mRemoteActivity.onTouchEvent(event);
		}
		return b || super.onTouchEvent(event);
	}

}
