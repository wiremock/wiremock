package com.github.tomakehurst.wiremock.jsontemplate;

/**
 * An interface for objects that can format a value.
 */
public interface IFormatter {
	/**
	 * Format the given value.
	 * 
	 * @param value
	 *            the value to format
	 * @return the formatted value
	 */
	public Object format(Object value);
}
