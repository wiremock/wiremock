package com.github.tomakehurst.wiremock.common.filemaker;

import com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine;
import com.github.tomakehurst.wiremock.matching.AnythingPattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class FilenameMaker {
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final String DEFAULT_FILENAME_TEMPLATE = "{{{method}}}-{{{path}}}-{{{id}}}.json";

    private final TemplateEngine templateEngine;
    private final String filenameTemplate;

    public FilenameMaker(TemplateEngine templateEngine, String filenameTemplate) {
        this.templateEngine = templateEngine;
        this.filenameTemplate = filenameTemplate;
    }

    public FilenameMaker(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
        this.filenameTemplate = DEFAULT_FILENAME_TEMPLATE;
    }

    public String filenameFor(StubMapping stubMapping) {
        FilenameTemplateModel model = new FilenameTemplateModel(stubMapping);
        String safeFilename = makeSafeFileName(model, "json");
        return templateEngine.getUncachedTemplate(filenameTemplate).apply(safeFilename);
    }

    public String makeSafeFileName(FilenameTemplateModel model, String extension) {
        StubMapping mapping = model.getStubMapping();
        String suffix = "-" + mapping.getId() + "." + extension;
        if (isNotEmpty(mapping.getName())) {
            return makeSafeName(mapping.getName()) + suffix;
        }

        UrlPattern urlMatcher = mapping.getRequest().getUrlMatcher();

        if (urlMatcher.getPattern() instanceof AnythingPattern) {
            return suffix.substring(1);
        }

        String expectedUrl = urlMatcher.getExpected();
        URI uri = URI.create(sanitise(expectedUrl));
        return makeSafeNameFromUrl(uri.getPath()) + suffix;
    }

    private String makeSafeNameFromUrl(String urlPath) {
        String startingPath = urlPath.replace("/", "_");
        return makeSafeName(startingPath);
    }

    private String makeSafeName(String name) {
        String nowhitespace = WHITESPACE.matcher(name).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = sanitise(normalized);

        slug = slug.replaceAll("^[_]*", "");
        slug = slug.replaceAll("[_]*$", "");

        slug = StringUtils.truncate(slug, 200);

        return slug.toLowerCase(Locale.ENGLISH);
    }

    private String sanitise(String s) {
        return NON_ALPHANUMERIC.matcher(s).replaceAll("");
    }
}
