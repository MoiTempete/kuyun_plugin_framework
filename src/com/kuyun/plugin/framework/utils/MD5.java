package com.kuyun.plugin.framework.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {

	private static char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	private static String toHexString(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
			sb.append(hexChar[b[i] & 0x0f]);
		}
		return sb.toString();
	}

	public static String getMd5(byte[] byteBuffer) {
		String md5String = "";
		try {
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			mdInst.update(byteBuffer);
			byte[] res = mdInst.digest();
			md5String = toHexString(res);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return md5String;
	}

	public static String getMd5(String filePath)throws Exception {
		InputStream fis = null;
		try {
			fis = new FileInputStream(filePath);

			byte[] buffer = new byte[1024];
			MessageDigest complete = MessageDigest.getInstance("MD5");
			int numRead;

			do {
				numRead = fis.read(buffer);
				if (numRead > 0) {
					complete.update(buffer, 0, numRead);
				}
			} while (numRead != -1);

			return toHexString(complete.digest());
		}finally {
			if (fis != null)
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	 public static void main(String args[]) {
		 try {
			  long time = System.currentTimeMillis();
	           System.out.println(getMd5("D:/worker/android/动态加载/demo/dynamic-load-apk/DynamicLoadApk/DynamicLoadClient/bin/DynamicLoadClient.apk"));
	           
	           System.out.println(System.currentTimeMillis()-time);
	           
	           
	           
	           byte[] bytes = MyFileUtils.loadBytesFromFile("D:/worker/android/动态加载/demo/dynamic-load-apk/DynamicLoadApk/DynamicLoadClient/bin/DynamicLoadClient.apk");
	           
	           time = System.currentTimeMillis();
	           System.out.println(getMd5(bytes));
	           System.out.println(System.currentTimeMillis()-time);
	           
	           // output :
	           //  0bb2827c5eacf570b6064e24e0e6653b
	           // ref :
	           //  http://www.apache.org/dist/
	           //          tomcat/tomcat-5/v5.5.17/bin
	           //              /apache-tomcat-5.5.17.exe.MD5
	           //  0bb2827c5eacf570b6064e24e0e6653b *apache-tomcat-5.5.17.exe
	       }
	       catch (Exception e) {
	           e.printStackTrace();
	       }
	 }
}
