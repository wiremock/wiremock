package com.tomakehurst.wiremock.http;

import java.lang.reflect.Field;
import java.net.Socket;

import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;

public class HttpServletResponseUtils {

	public static Socket getUnderlyingSocketFrom(HttpServletResponse httpServletResponse) {
		HttpConnection httpConnection = getPrivateField(httpServletResponse, "_connection");
		Object channelEndPoint = httpConnection.getEndPoint();
		return getPrivateField(channelEndPoint, "_socket");
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T getPrivateField(Object obj, String name) {
		try {
			Field field = obj.getClass().getDeclaredField(name);
			field.setAccessible(true);
			return (T) field.get(obj);
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
}
