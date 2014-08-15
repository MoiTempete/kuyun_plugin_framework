package com.kuyun.plugin.framework.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.kuyun.smarttv.sdk.resource.api.debug.Console;

public class MyFileUtils {

	private final static String TAG = "FileUtils";

	private final static int DATA_BUFFER = 8192;

	public static void save(String parameterString, String filePath) {

		File file = new File(filePath);
		BufferedWriter writer = null;
		try {
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			writer = new BufferedWriter(new FileWriter(file));
			writer.write(parameterString);
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String load(String filePath) {
		File file = new File(filePath);
		BufferedReader reader = null;
		StringBuffer buff = new StringBuffer();
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;

			while ((tempString = reader.readLine()) != null) {
				buff.append(tempString);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return buff.toString();
	}

	public static byte[] loadBytesFromFile(String filePath) {
		byte[] byteModel = null;

		File file = new File(filePath);
		if (!file.exists()) {
			Console.e(TAG, "File Not Exist = " + filePath);
			return byteModel;
		}

		try {
			FileInputStream fis = new FileInputStream(filePath);
			BufferedInputStream bis = new BufferedInputStream(fis);

			byteModel = new byte[fis.available()];

			// TODO:当模型文件非常大的情况，可能出问题
			// fis.read(byteModel);

			// int count = 0;
			// int pos = 0;
			// byte data[] = new byte[DATA_BUFFER];
			// while ((count = fis.read(data, 0, DATA_BUFFER)) != -1) {
			// System.arraycopy(data, 0, byteModel, pos, count);
			// pos += count;
			// }
			bis.read(byteModel);

			fis.close();

			Console.d(TAG, "load ok = " + filePath);

		} catch (IOException e) {
			e.printStackTrace();
			byteModel = null;
		}

		return byteModel;
	}

	public static void saveBytesToFile(byte[] bytes, String fileName) {
		FileOutputStream fop = null;
		File file;
		BufferedOutputStream bos = null;

		try {
			file = new File(fileName);
			if (file.exists()) {
				if (file.delete() == false) {
					Console.d(TAG, "saveBytesToBinFile file.delete fail");
					return;
				}
			}
			if (file.createNewFile() == false) {
				Console.d(TAG, "saveBytesToBinFile file.createNewFile fail");
				return;
			}
			// if file doesnt exists, then create it
			// if (!file.exists()) {
			// file.createNewFile();
			// }else{
			//
			// }

			fop = new FileOutputStream(file);
			bos = new BufferedOutputStream(fop);

			// fop.write(bytes);
			bos.write(bytes);
			bos.flush();

			// fop.flush();

			Console.d(TAG, "success to save = " + fileName);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				// if (fop != null) {
				// fop.close();
				// }
				if (bos != null) {
					bos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void deleteFile(File sourceFile) {
		if (sourceFile == null)
			return;
		if (sourceFile.exists()) {
			boolean b = sourceFile.delete();
			Console.d("deleteFile", "delete " + sourceFile.getAbsolutePath()
					+ ":" + b);
		}
	}

	public static void deleteDir(File dir) {
		if (!dir.exists())
			return;
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File f : files) {
				deleteDir(f);
			}
			dir.delete();
		} else
			dir.delete();
	}

	public static boolean rename(File sourceFile, File destFile) {
		if (sourceFile == null || destFile == null)
			return false;
		boolean bAllow = false;
		if (destFile.exists()) {
			if (destFile.delete()) {
				bAllow = true;
			}
		} else {
			bAllow = true;
		}
		if (bAllow && sourceFile.exists()) {
			return sourceFile.renameTo(destFile);
		}
		return false;
	}
}
