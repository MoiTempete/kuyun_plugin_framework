package com.kuyun.plugin.framework.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kuyun.smarttv.sdk.resource.api.util.StringUtils;

/**
 * 
 * @author jdeng { "plugins":[ { "md5":"698d51a19d8a121ce581499d7b701668",
 *         "id":"1" } , { "md5":"bcbe3365e6ac95ea2c0343a2395834dd", "id":"2" } ,
 *         { "md5":"d41d8cd98f00b204e9800998ecf8427e", "id":"3" } ] }
 */
public class PluginConfig {
	private final static long MAX_CHECH_INTERVAL = 60000;// 600l; //10分钟检测
	private static final long serialVersionUID = 0L;

	/**
	 * Examples: [{"md5":"698d51a19d8a121ce581499d7b701668","id":"1"},{"md5":
	 * "bcbe3365e6ac95ea2c0343a2395834dd"
	 * ,"id":"2"},{"md5":"d41d8cd98f00b204e9800998ecf8427e","id":"3"}]
	 */
	@SerializedName("plugins")
	@Expose
	public List<PluginItem> items = new ArrayList<PluginItem>();

	private HashMap<String, PluginItem> ItemMap = new HashMap<String, PluginItem>();

	public void initMap() {
		if (items == null || items.size() == 0)
			return;
		synchronized (items) {
			int size = items.size();
			for (int i = 0; i < size; i++) {
				if (i < items.size()) {
					PluginItem ii = items.get(i);
					ItemMap.put(ii.id, ii);
				} else {
					break;
				}
			}
		}
	}

	public boolean contain(PluginItem item) {
		return items.contains(item);
	}

	public void addItem(PluginItem item) {
		synchronized (items) {
			if (!items.contains(item)) {
				items.add(item);
			}
			ItemMap.put(item.id, item);
		}
	}

	public void updateItem(PluginItem item) {
		synchronized (items) {
			items.remove(item);
			ItemMap.remove(item);
			items.add(item);
			ItemMap.put(item.id, item);
		}
	}

	public void rmItem(PluginItem item) {
		synchronized (items) {
			items.remove(item);
			ItemMap.remove(item);
		}
	}

	public boolean isContain(PluginItem item) {
		if (item == null || items == null)
			return false;
		return items.contains(item);
	}

	public PluginItem findItem(String id) {
		if (StringUtils.isNull(id))
			return null;
		PluginItem item = ItemMap.get(id);
		if (item != null)
			return item;
		synchronized (items) {
			int size = items.size();
			for (int i = 0; i < size; i++) {
				if (i < items.size()) {
					PluginItem ii = items.get(i);
					if (id.equals(ii.id)) {
						ItemMap.put(id, ii);
						return ii;
					}
				} else {
					break;
				}

			}
			return null;
		}
	}

	public boolean hasPlugin() {
		return items != null && items.size() > 0;
	}

	@Override
	protected void finalize() throws Throwable {
		if (items != null)
			items.clear();
		if (ItemMap != null)
			ItemMap.clear();
		super.finalize();
	}

	public static class PluginItem implements Serializable {

		private static final long serialVersionUID = 0L;

		/**
		 * Examples: "698d51a19d8a121ce581499d7b701668",
		 * "bcbe3365e6ac95ea2c0343a2395834dd",
		 * "d41d8cd98f00b204e9800998ecf8427e"
		 */
		@Expose
		public String md5;

		/**
		 * Examples: "1", "2", "3"
		 */
		@Expose
		public String id;

		@Expose
		public String mainClass;
		
		@Expose
		public int launchMode;

		/**
		 * 只在内存中做记录
		 */
		@Expose
		public long lastCheckTime;

		public boolean isCheckTimeout() {
			long curr = System.currentTimeMillis();
			long val = curr - lastCheckTime;
			return val >= MAX_CHECH_INTERVAL;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof PluginItem))
				return false;
			return ((PluginItem) o).id.equalsIgnoreCase(id);
		}

		// @Override
		// public int hashCode() {
		// int hash = 7;
		// hash = (37 * hash) + (null == id ? 0 : id.hashCode());
		// return hash;
		// }
	}
}
