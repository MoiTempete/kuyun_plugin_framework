package com.kuyun.plugin.framework.api;

import java.io.Serializable;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.kuyun.plugin.framework.model.PluginPath;
import com.kuyun.smarttv.sdk.resource.api.util.AsyncLoader;

/**
 * note: can use that like this.
 * 
 * @see {@link PluginBaseActivity.that}
 * @author jdeng
 */
public class PluginBaseActivity extends Activity {

	private static final String TAG = "PluginBaseActivity";

	public static final String FROM = "extra.from";
	public static final int FROM_INTERNAL = 0;
	public static final int FROM_EXTERNAL = 1;

	/**
	 * 代理activity，可以当作Context来使用，会根据需要来决定是否指向this
	 */
	protected Activity mProxyActivity;

	/**
	 * 等同于mProxyActivity，可以当作Context来使用，会根据需要来决定是否指向this<br/>
	 * 可以当作this来使用
	 */
	protected Activity that;
	protected int mFrom = FROM_INTERNAL;
	protected String mDexPath;

	private HashMap<String, Serializable> params;

	public void setProxy(Activity proxyActivity, String dexPath,
			HashMap<String, Serializable> params) {
		Log.d(TAG, "setProxy: proxyActivity= " + proxyActivity + ", dexPath= "
				+ dexPath);
		mProxyActivity = proxyActivity;
		that = mProxyActivity;
		mDexPath = dexPath;
		this.params = params;
	}

	public HashMap<String, Serializable> getParams() {
		return params;
	}

	public String getPluginInstanceId() {
		if (params != null) {
			return (String) params.get(PluginConstante.PARAMS_INSTANCE_ID);
		}
		return null;
	}

	public String getDataUrl() {
		if (params != null) {
			return (String) params.get(PluginConstante.PARAMS_DATA_URL);
		}
		return null;
	}

	public String getPluginId() {
		return PluginClientApi.getInstance().getPluginId(mDexPath);
	}

	public AsyncLoader getAsyncLoader() {
		return PluginClientApi.getInstance().getAsyncLoader();
	}

	// public BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
	// @Override
	// public void onReceive(Context context, Intent intent) {
	//
	// }
	// };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mFrom = savedInstanceState.getInt(FROM, FROM_INTERNAL);
		}
		if (mFrom == FROM_INTERNAL) {
			super.onCreate(savedInstanceState);
			mProxyActivity = this;
			that = mProxyActivity;
		}

		Log.d(TAG, "onCreate: from= "
				+ (mFrom == FROM_INTERNAL ? "FROM_INTERNAL" : "FROM_EXTERNAL"));
		// if (mBroadcastReceiver != null)
		// PluginClientApi.registerReceiver(that, mBroadcastReceiver);
	}

	public void startActivityByProxy(String className,
			HashMap<String, Serializable> params) {
		startActivityByProxy(className, params, ActivityInfo.LAUNCH_MULTIPLE);
	}

	public void startActivityByProxy(String className,
			HashMap<String, Serializable> params, int launchMode) {
		if (mProxyActivity == this) {
			Intent intent = new Intent();
			intent.setClassName(this, className);
			if (params != null) {
				intent.putExtra(PluginConstante.EXTRA_PARAMS, params);
			}
			mProxyActivity.startActivity(intent);
		} else {
			try {
				Intent intent = new Intent(
						PluginConstante.getProxyViewAction(launchMode));
				intent.putExtra(PluginConstante.EXTRA_DEX_PATH, mDexPath);
				intent.putExtra(PluginConstante.EXTRA_CLASS, className);
				if (params != null) {
					intent.putExtra(PluginConstante.EXTRA_PARAMS, params);
				}
				mProxyActivity.startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void startActivityForResultByProxy(String className,
			int requestCode, HashMap<String, Serializable> params) {
		if (mProxyActivity == this) {
			Intent intent = new Intent();
			intent.setClassName(this, className);
			mProxyActivity.startActivityForResult(intent, requestCode);
		} else {
			try {
				Intent intent = new Intent(PluginConstante.PROXY_VIEW_ACTION);
				intent.putExtra(PluginConstante.EXTRA_DEX_PATH, mDexPath);
				intent.putExtra(PluginConstante.EXTRA_CLASS, className);
				if (params != null) {
					intent.putExtra(PluginConstante.EXTRA_PARAMS, params);
				}
				mProxyActivity.startActivityForResult(intent, requestCode);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void startPlugin(String pluginId, String className,
			HashMap<String, Serializable> params) {
		if (mProxyActivity == this) {
			Intent intent = new Intent();
			intent.setClassName(this, className);
			if (params != null) {
				intent.putExtra(PluginConstante.EXTRA_PARAMS, params);
			}
			mProxyActivity.startActivity(intent);
		} else {
			Intent intent = new Intent(PluginConstante.PROXY_VIEW_ACTION);
			String dexPath = PluginPath.getPluginDexPath(that, pluginId);
			intent.putExtra(PluginConstante.EXTRA_DEX_PATH, dexPath);
			intent.putExtra(PluginConstante.EXTRA_CLASS, className);
			if (params != null) {
				intent.putExtra(PluginConstante.EXTRA_PARAMS, params);
			}
			mProxyActivity.startActivity(intent);
		}
	}

	public void startPluginForResultByProxy(String pluginId, String className,
			int requestCode, HashMap<String, Serializable> params) {
		if (mProxyActivity == this) {
			Intent intent = new Intent();
			intent.setClassName(this, className);
			mProxyActivity.startActivityForResult(intent, requestCode);
		} else {
			Intent intent = new Intent(PluginConstante.PROXY_VIEW_ACTION);
			String dexPath = PluginPath.getPluginDexPath(that, pluginId);
			intent.putExtra(PluginConstante.EXTRA_DEX_PATH, dexPath);
			intent.putExtra(PluginConstante.EXTRA_CLASS, className);
			if (params != null) {
				intent.putExtra(PluginConstante.EXTRA_PARAMS, params);
			}
			mProxyActivity.startActivityForResult(intent, requestCode);
		}
	}

	@Override
	public void setContentView(View view) {
		if (mProxyActivity == this) {
			super.setContentView(view);
		} else {
			mProxyActivity.setContentView(view);
		}
	}

	@Override
	public void setContentView(View view, LayoutParams params) {
		if (mProxyActivity == this) {
			super.setContentView(view, params);
		} else {
			mProxyActivity.setContentView(view, params);
		}
	}

	@Override
	public void setContentView(int layoutResID) {
		if (mProxyActivity == this) {
			super.setContentView(layoutResID);
		} else {
			mProxyActivity.setContentView(layoutResID);
		}
	}

	@Override
	public void addContentView(View view, LayoutParams params) {
		if (mProxyActivity == this) {
			super.addContentView(view, params);
		} else {
			mProxyActivity.addContentView(view, params);
		}
	}

	@Override
	public View findViewById(int id) {
		if (mProxyActivity == this) {
			return super.findViewById(id);
		} else {
			return mProxyActivity.findViewById(id);
		}
	}

	@Override
	public void onDestroy() {
		if (mProxyActivity == this) {
			super.onDestroy();
		}
		// if (mBroadcastReceiver != null)
		// PluginClientApi.unregisterReceiver(that, mBroadcastReceiver);
	}

	@Override
	public void onNewIntent(Intent intent) {
		if (mProxyActivity == this) {
			super.onNewIntent(intent);
		}
	}

	@Override
	public void onPause() {
		if (mProxyActivity == this) {
			super.onPause();
		}
	}

	@Override
	public void onRestart() {
		if (mProxyActivity == this) {
			super.onRestart();
		}
	}

	@Override
	public void onResume() {
		if (mProxyActivity == this) {
			super.onResume();
		}
	}

	@Override
	public void onStart() {
		if (mProxyActivity == this) {
			super.onStart();
		}
	}

	@Override
	public void onStop() {
		if (mProxyActivity == this) {
			super.onStop();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mProxyActivity == this) {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (mProxyActivity == this) {
			super.onSaveInstanceState(outState);
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}
}
