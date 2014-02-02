package com.github.tomakehurst.wiremock.jsontemplate;


import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TemplateCompiler {

	/**
	 * Compile the template string, calling methods on the 'program builder'.
	 * 
	 * @param template
	 *            The template string. It should not have any compilation
	 *            options in the header -- those are parsed by
	 *            FromString/FromFile
	 * @param builder
	 *            A program builder implementing {@link IProgramBuilder}. If not
	 *            supplied, {@link DefaultProgramBuilder} is used.
	 * @param options
	 *            A {@link TemplateCompileOptions} object controlling the
	 *            compilation options.
	 * @return The compiled program (obtained from the builder)
	 */
	static Section compile(String template, IProgramBuilder builder,
			TemplateCompileOptions options) {
		if (options == null) {
			options = new TemplateCompileOptions();
		}
		if (builder == null) {
			builder = new DefaultProgramBuilder(options.getMoreFormatters());
		}
		String[] metas = splitMeta(options.getMeta());
		String metaLeft = metas[0];
		String metaRight = metas[1];
		char formatChar = options.getFormatChar();
		if (formatChar != '|' && formatChar != ':') {
			throw new ConfigurationError(String.format(
					"Only format characters : and | are accepted (got %s)", ""
							+ formatChar));
		}
		String defaultFormatter = options.getDefaultFormatter();

		Pattern tokenRe = makeTokenRegex(metaLeft, metaRight);
		// cache the lengths
		int metaLeftLength = metaLeft.length();
		int metaRightLength = metaRight.length();
		// and this map for lookup
		HashMap<String, String> keywordLookup = new HashMap<String, String>();
		keywordLookup.put("meta-left", metaLeft);
		keywordLookup.put("meta-right", metaRight);
		keywordLookup.put("space", " ");
		keywordLookup.put("tab", "\t");
		keywordLookup.put("newline", "\n");
		// and this pattern for format splitting
		Pattern formatSplitRe = Pattern.compile(Pattern.quote("" + formatChar));

		Matcher matcher = tokenRe.matcher(template);
		int balanceCounter = 0;
		int lastMatchIndex = 0;
		while (matcher.find()) {
			if (matcher.start() > lastMatchIndex) {
				// there's some text before
				// PROCESS TEXT
				String token = template.substring(lastMatchIndex, matcher
						.start());
				builder.append(new LiteralStatement(token));
			}
			lastMatchIndex = matcher.end();

			// TOKEN
			String token = matcher.group();
			boolean hadNewline = false;
			if (token.endsWith("\n")) {
				hadNewline = true;
				token = token.substring(0, token.length() - 1);
			}
			assert token.startsWith(metaLeft);
			assert token.endsWith(metaRight);
			token = token.substring(metaLeftLength, token.length()
					- metaRightLength);

			if (token.startsWith("#"))
				continue; // comment

			if (token.startsWith(".")) {
				token = token.substring(1);
				String literal = keywordLookup.get(token);
				if (literal != null) {
					builder.append(new LiteralStatement(literal));
					continue;
				}

				// 
				Matcher sectionMatcher = SECTION_RE.matcher(token);
				if (sectionMatcher.find()) {
					String repeated = sectionMatcher.group(1);
					String sectionName = sectionMatcher.group(3);
					builder.newSection(repeated != null, sectionName);
					balanceCounter += 1;
					continue;
				}

				if (token.equals("or") || token.equals("alternates with")) {
					builder.newClause(token);
				}

				if (token.equals("end")) {
					balanceCounter -= 1;
					if (balanceCounter < 0) {
						throw new TemplateSyntaxError(
								String
										.format(
												"Got too many %send%s statements. You may have mistyped an earlier 'section' or 'repeated section' directive.",
												metaLeft, metaRight));
					}
					builder.endSection();
				}
				continue;
			}

			// Now we know the directive is a substitution
			String[] parts = formatSplitRe.split(token);
			String name;
			String[] formatters;
			if (parts.length == 1) {
				if (defaultFormatter == null) {
					throw new MissingFormatter(
							"This template requires explicit formatters.");
				}
				name = token;
				formatters = new String[] { defaultFormatter };
			} else {
				name = parts[0];
				formatters = new String[parts.length - 1];
				System.arraycopy(parts, 1, formatters, 0, formatters.length);
			}
			builder.appendSubstitution(name, formatters);
			if (hadNewline)
				builder.append(new LiteralStatement("\n"));
		}
		// TRAILING TEXT
		builder
				.append(new LiteralStatement(template.substring(lastMatchIndex)));

		if (balanceCounter != 0) {
			throw new TemplateSyntaxError(String.format(
					"Got too few %send%s statements.", metaLeft, metaRight));
		}

		return builder.getRoot();
	}

	private static Pattern SECTION_RE = Pattern
			.compile("(repeated)?\\s*(section)\\s+(\\S+)");

	private static ConcurrentHashMap<String, Pattern> tokenPatternCache = new ConcurrentHashMap<String, Pattern>(
			20);

	private static Pattern makeTokenRegex(String metaLeft, String metaRight) {
		String cacheKey = metaLeft + "!" + metaRight;
		if (tokenPatternCache.containsKey(cacheKey)) {
			return tokenPatternCache.get(cacheKey);
		}
		Pattern result = Pattern.compile("(" + Pattern.quote(metaLeft) + ".+?"
				+ Pattern.quote(metaRight) + "\\\n?)");
		tokenPatternCache.put(cacheKey, result);
		return result;
	}

	private static String[] splitMeta(String meta) {
		int n = meta.length();
		if (n % 2 == 1) {
			throw new ConfigurationError(String.format(
					"%s has an odd number of characters", meta));
		}
		return new String[] { meta.substring(0, n / 2), meta.substring(n / 2) };
	}
}
