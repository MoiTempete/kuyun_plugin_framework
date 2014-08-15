package com.kuyun.plugin.framework.exception;

import java.io.PrintStream;
import java.io.PrintWriter;

public class PluginException extends Exception {
	private Exception mException;

	public PluginException(Exception e) {
		mException = e;
	}

	@Override
	public String getMessage() {
		if (mException != null)
			return mException.getMessage();
		return super.getMessage();
	}

	@Override
	public void printStackTrace() {
		if (mException != null) {
			mException.printStackTrace();
			return;
		}
		super.printStackTrace();
	}

	@Override
	public void printStackTrace(PrintStream err) {
		if (mException != null) {
			mException.printStackTrace(err);
			return;
		}
		super.printStackTrace(err);
	}

	@Override
	public void printStackTrace(PrintWriter err) {
		if (mException != null) {
			mException.printStackTrace(err);
			return;
		}
		super.printStackTrace(err);
	}

}
