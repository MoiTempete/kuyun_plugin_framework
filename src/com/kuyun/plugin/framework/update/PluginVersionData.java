package com.kuyun.plugin.framework.update;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.kuyun.smarttv.sdk.resource.api.data.BaseObject;

public class PluginVersionData extends BaseObject {

	private static final long serialVersionUID = 0L;

	/**
	 * Examples: {"result-code":"0","APIVersion":"1.3.0","TimeStamp":
	 * "2014-07-28 18:51:15"
	 * ,"items":[{"md5":"698d51a19d8a121ce581499d7b701668","url"
	 * :"http://172.16.8.12:8080/cardsys_api/new_version/4/1.apk"
	 * },{"md5":"bcbe3365e6ac95ea2c0343a2395834dd"
	 * ,"url":"http://172.16.8.12:8080/cardsys_api/new_version/4/2.apk"
	 * },{"md5":"d41d8cd98f00b204e9800998ecf8427e"
	 * ,"url":"http://172.16.8.12:8080/cardsys_api/new_version/4/3.apk"}]}
	 */
	@SerializedName("Response")
	public Response2 response;

	public List<Item> getItems() {
		return response != null ? response.items : null;
	}
	

	public static class Response2 implements Serializable {

		private static final long serialVersionUID = 0L;

		/**
		 * Examples: "0"
		 */
		@SerializedName("result-code")
		public String resultCode;

		/**
		 * Examples: "1.3.0"
		 */
		@SerializedName("APIVersion")
		public String aPIVersion;

		/**
		 * Examples: "2014-07-28 18:51:15"
		 */
		@SerializedName("TimeStamp")
		public String timeStamp;

		/**
		 * Examples: [{"md5":"698d51a19d8a121ce581499d7b701668","url":
		 * "http://172.16.8.12:8080/cardsys_api/new_version/4/1.apk"
		 * },{"md5":"bcbe3365e6ac95ea2c0343a2395834dd"
		 * ,"url":"http://172.16.8.12:8080/cardsys_api/new_version/4/2.apk"
		 * },{"md5":"d41d8cd98f00b204e9800998ecf8427e","url":
		 * "http://172.16.8.12:8080/cardsys_api/new_version/4/3.apk"}]
		 */
		@SerializedName("files")
		public List<Item> items;
	}

	public static class Item implements Serializable {

		private static final long serialVersionUID = 0L;

		/**
		 * Examples: "698d51a19d8a121ce581499d7b701668",
		 * "bcbe3365e6ac95ea2c0343a2395834dd",
		 * "d41d8cd98f00b204e9800998ecf8427e"
		 */
		@SerializedName("md5")
		public String md5;

		/**
		 * Examples: "http://172.16.8.12:8080/cardsys_api/new_version/4/1.apk",
		 * "http://172.16.8.12:8080/cardsys_api/new_version/4/2.apk",
		 * "http://172.16.8.12:8080/cardsys_api/new_version/4/3.apk"
		 */
		@SerializedName("url")
		public String url;
	}
}
