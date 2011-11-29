package com.tomakehurst.wiremock.common;

public class LocalNotifier {

	private static ThreadLocal<Notifier> notifierHolder = new ThreadLocal<Notifier>();
	
	public static Notifier notifier() {
		Notifier notifier = notifierHolder.get();
		if (notifier == null) {
			notifier = new NullNotifier();
		}
		
		return notifier;
	}
	
	public static void set(Notifier notifier) {
		notifierHolder.set(notifier);
	}
	
	private static class NullNotifier implements Notifier {

		@Override
		public void info(String message) {
		}

		@Override
		public void error(String message) {
		}

		@Override
		public void error(String message, Throwable t) {
		}
		
	}
}
