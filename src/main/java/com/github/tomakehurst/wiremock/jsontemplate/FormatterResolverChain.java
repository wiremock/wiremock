package com.github.tomakehurst.wiremock.jsontemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of {@link IFormatterResolver} that tries to get an
 * {@link IFormatter} from several {@link IFormatterResolver} implementations in
 * sequence. If the first one returns null, the next one is tried, until there
 * are no more IFormatterResolver implementations to use.
 * 
 */
public class FormatterResolverChain implements IFormatterResolver {

	private List<IFormatterResolver> formatterResolvers;

	/**
	 * Construct a FormatterResolverChain that uses the elements of the given
	 * list to find formatters.
	 * 
	 * @param formatterResolvers
	 *            a list of {@link IFormatterResolver} objects.
	 */
	public FormatterResolverChain(List<IFormatterResolver> formatterResolvers) {
		if (formatterResolvers != null) {
			this.formatterResolvers = new ArrayList<IFormatterResolver>(
					formatterResolvers);
		} else {
			this.formatterResolvers = new ArrayList<IFormatterResolver>();
		}
	}

	/**
	 * Construct a FormatterResolverChain with an empty list of resolvers.
	 */
	public FormatterResolverChain() {
		this(null);
	}

	/**
	 * Get a formatter for the given name. Tries each element of the
	 * {@link IFormatterResolver} chain in sequence. If one returns a non-null
	 * value, that value is returned. If every element of the chain returns
	 * null, null is returned.
	 * 
	 * @param formatterName
	 *            the name of the formatter to get
	 * @return the first formatter found, or null if no element of the chain can
	 *         find a formatter
	 * 
	 */
	public IFormatter getFormatter(String formatterName) {
		for (IFormatterResolver resolver : this.formatterResolvers) {
			IFormatter formatter = resolver.getFormatter(formatterName);
			if (formatter != null) {
				return formatter;
			}
		}
		return null;
	}

	/**
	 * Gets the list of {@link IFormatterResolver} implementations used to find
	 * formatters. The caller can modify the returned list and this object will
	 * use the modified list.
	 * 
	 * @return the list of {@link IFormatterResolver} implementations used by
	 *         this object.
	 */
	public List<IFormatterResolver> getChain() {
		return this.formatterResolvers;
	}

}
