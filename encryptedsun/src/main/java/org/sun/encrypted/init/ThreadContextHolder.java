package org.sun.encrypted.init;

public class ThreadContextHolder {

	private static ThreadLocal<String> UserIdThreadLocalHolder = new ThreadLocal<String>();

	public static void setUserId(String did) {
		UserIdThreadLocalHolder.set(did);
	}

	public static String getUserId() {
		return UserIdThreadLocalHolder.get();
	}

}
