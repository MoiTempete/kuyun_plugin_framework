package com.kuyun.plugin.framework.api;

import android.os.Bundle;

import com.kuyun.plugin.framework.model.KyPluginBroadcasterManager;

public class PluginMultMediaPlayerActivity extends PluginBaseActivity {

	@Override
	public void finish() {
		Bundle params = new Bundle();
		params.putBoolean(
				PluginConstante.BROADCASTER_PARAM_KEY_PLAY_FINISH, true);
		KyPluginBroadcasterManager.getInstance().sendBroadcastData(that,
				params);
	}

}
