package com.github.tomakehurst.wiremock.jsontemplate;

/**
 * An interface for objects that can get an {@link IFormatter} for a given name.
 * 
 */
public interface IFormatterResolver {
	/**
	 * Get a formatter with the given name.
	 * 
	 * @param formatterName
	 *            the name of the formatter.
	 * @return the appropriate IFormatter implementation, or null if the
	 *         implementation cannot be found using this resolver.
	 */
	public IFormatter getFormatter(String formatterName);
}
