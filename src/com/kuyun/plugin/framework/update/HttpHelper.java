package com.kuyun.plugin.framework.update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import com.kuyun.smarttv.sdk.resource.api.debug.Console;

public class HttpHelper {

	public static final String TAG = "HttpHelper";
	
	private static final int TIME_OUT = 1000 * 30; // 超时
    private static final String METHOD_POST = "POST";
    private static final String METHOD_GET = "GET";
    private static final int HTTP_OK = 200;
    private final String CHARTSET = "UTF-8"; // 字符编码
    private static final int BUFFER = 1024 * 8;// 缓冲区

	public static final String ENCODING = "UTF-8";

	public static String HOST_UPDATE_PLUGIN_VERSION;
	public static String HOST = "http://logonext.tv.kuyun.com";
	private static String SUBPATH = "/cardsysapi/";
	public static boolean TEST = true;

	public static String HOST_GET_TVID_BYNAME;

	public static void setVersion(boolean bTest) {
		if (bTest) {
			HOST = "http://test.logonext.tv.kuyun.com";
		} else {
			HOST = "http://logonext.tv.kuyun.com";
		}

		HOST_UPDATE_PLUGIN_VERSION = HOST + SUBPATH + "api";
	}

	static {
		setVersion(TEST);
	}

	public static boolean downloadFile(String urlStr, String filePath,
			String fileName)throws Exception {
		boolean result = false;

		URL url = null;
		HttpURLConnection conn = null;
		InputStream inStream = null;
		FileOutputStream fos = null;

		try {
			File des = new File(filePath);
			if (!des.isDirectory()) {
				des.mkdirs();
			}

			File algorithmFile = new File(filePath, fileName);

			if (algorithmFile.exists() && algorithmFile.isFile()) {
				algorithmFile.delete();
			}

			// algorithmFile.createNewFile();

			int currentSize = 0;
			long totalSize = 0;

			StringBuilder urlBuilder = new StringBuilder();
			urlBuilder.append(urlStr);

			URI uri = new URI(urlBuilder.toString());
			url = new URL(uri.toString());

			conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setConnectTimeout(TIME_OUT);
			conn.setRequestMethod(METHOD_GET);
			conn.setRequestProperty("accept", "*/*");
			conn.connect();

			int responseCode = conn.getResponseCode();

			if (responseCode == HTTP_OK) {
				inStream = conn.getInputStream();
				totalSize = conn.getContentLength();

				fos = new FileOutputStream(algorithmFile);
				byte buffer[] = new byte[BUFFER];
				int readSize = 0;
				while ((readSize = inStream.read(buffer)) > 0) {
					fos.write(buffer, 0, readSize);
					fos.flush();
					currentSize += readSize;
				}

				if (totalSize < 0 || currentSize == totalSize) {
					result = true;
				}
			}
		}  finally {
			try {
				if (fos != null) {
					fos.close();
				}
				if (inStream != null) {
					inStream.close();
				}
				if (conn != null) {
					conn.disconnect();
				}
			} catch (Exception e) {
				Console.printStackTrace(e);
				result = false;
			}
		}

		return result;
	}

}
