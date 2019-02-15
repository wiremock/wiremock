package com.github.tomakehurst.wiremock.stubbing;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Template;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.DefinitionBasedTemplateCache;
import com.github.tomakehurst.wiremock.extension.responsetemplating.DefinitionBasedTemplateSource;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.CacheStrategy;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.extension.responsetemplating.DefinitionBasedTemplateSource.fromString;
import static com.github.tomakehurst.wiremock.extension.responsetemplating.DefinitionBasedTemplateSource.fromTextFile;
import static java.lang.String.format;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class InMemoryStubMappingsCacheTest {

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();
    private InMemoryStubMappings mappings = null;
    private FileSource files = null;
    private DefinitionBasedTemplateCache cache = null;
    private Map<DefinitionBasedTemplateSource, Template> cacheSnapshot = null;


    @Test
    public void addEmptyMappingDontUpdateTemplateCache() {
        mappings.addMapping(mapping(1, null));
        assertContent();
    }

    @Test
    public void addNeverCachedResponseMappingDontUpdateTemplateCache() {
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Never)));
        assertContent();
    }

    @Test
    public void addOnCallCacheResponseMappingDontUpdateTemplateCache() {
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.OnCall)));
        assertContent();
    }

    @Test
    public void addAlwaysCacheResponseMappingAddItToTemplateCache() {
        StubMapping mapping = mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                 .withBodyFile("body-001.txt")
                                                 .withCacheStrategy(CacheStrategy.Always));
        mappings.addMapping(mapping);
        assertContent(mapping);
    }

    @Test
    public void addTwoAlwaysCacheResponseMappingAddItOnceToTemplateCache() {
        StubMapping mapping = mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                 .withBodyFile("body-001.txt")
                                                 .withCacheStrategy(CacheStrategy.Always));
        mappings.addMapping(mapping);
        snapshot();
        mappings.addMapping(mapping(2, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Always)));
        assertUnchanged();
    }

    @Test
    public void addAndRemoveAlwaysCacheResponseMappingLeftEmptyTemplateCache() {
        StubMapping mapping = mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                 .withBodyFile("body-001.txt")
                                                 .withCacheStrategy(CacheStrategy.Always));
        mappings.addMapping(mapping);
        mappings.removeMapping(mapping);
        assertContent();
    }

    @Test
    public void addAndRemoveOnCallCacheResponseMappingDontUpdateTemplateCache() {
        StubMapping mapping = mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                 .withBodyFile("body-001.txt")
                                                 .withCacheStrategy(CacheStrategy.OnCall));
        mappings.addMapping(mapping);
        mappings.removeMapping(mapping);
        assertContent();
    }

    @Test
    public void addTwiceAndRemoveAlwaysCacheResponseMappingLeftOneEntryInTemplateCache() {
        StubMapping mapping = mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                 .withBodyFile("body-001.txt")
                                                 .withCacheStrategy(CacheStrategy.Always));
        mappings.addMapping(mapping);
        snapshot();
        mappings.addMapping(mapping(2, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Always)));
        mappings.removeMapping(mapping);
        assertUnchanged();
    }

    @Test
    public void addAndUpdateNeverCacheResponseMappingDontUpdateTemplateCache() {
        snapshot();
        StubMapping mapping = mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                 .withBodyFile("body-001.txt")
                                                 .withCacheStrategy(CacheStrategy.Never));
        mappings.addMapping(mapping);
        assertUnchanged();
        mappings.editMapping(mapping);
        assertUnchanged();
    }

    @Test
    public void addNeverCacheResponseMappingAndUpdateOnCallCacheResponseMappingDontUpdateTemplateCache() {
        snapshot();
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Never)));
        assertUnchanged();
        mappings.editMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.OnCall)));
        assertUnchanged();
    }

    @Test
    public void addNeverCacheResponseMappingAndUpdateAlwaysCacheResponseMappingUpdateTemplateCacheOnce() {
        snapshot();
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Never)));
        assertUnchanged();
        StubMapping mapping = mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                 .withBodyFile("body-001.txt")
                                                 .withCacheStrategy(CacheStrategy.Always));
        mappings.editMapping(mapping);
        assertContent(mapping);
    }

    @Test
    public void addAlwaysCacheResponseMappingAndUpdateNeverCacheResponseMappingLeftEmptyTemplateCache() {
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Always)));
        mappings.editMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                .withBodyFile("body-001.txt")
                                                .withCacheStrategy(CacheStrategy.Never)));
        assertContent();
    }

    @Test
    public void addAlwaysCacheResponseMappingAndUpdateOnCallCacheResponseMappingUpdateTemplateCacheOnce() {
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Always)));
        snapshot();
        mappings.editMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                .withBodyFile("body-001.txt")
                                                .withCacheStrategy(CacheStrategy.OnCall)));
        assertUnchanged();
    }

    @Test
    public void addAlwaysCacheResponseMappingAndUpdateAlwaysCacheResponseMappingUpdateTemplateCacheOnce() {
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Always)));
        snapshot();
        mappings.editMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                .withBodyFile("body-001.txt")
                                                .withCacheStrategy(CacheStrategy.Always)));
        assertUnchanged();
    }

    @Test
    public void addNeverCacheResponseMappingAndUpdateNeverCacheWithDifferentPathResponseMappingDontUpdateTemplateCache() {
        snapshot();
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Never)));
        mappings.editMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                .withBodyFile("body-002.txt")
                                                .withCacheStrategy(CacheStrategy.Never)));
        assertUnchanged();
    }

    @Test
    public void addNeverCacheResponseMappingAndUpdateNeverCacheWithTextContentResponseMappingDontUpdateTemplateCache() {
        snapshot();
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Never)));
        mappings.editMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                .withBody("")
                                                .withCacheStrategy(CacheStrategy.Never)));
        assertUnchanged();
    }

    @Test
    public void addNeverCacheResponseMappingAndUpdateNeverCacheWithBinaryContentResponseMappingDontUpdateTemplateCache() {
        snapshot();
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Never)));
        mappings.editMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                .withBody(new byte[0])
                                                .withCacheStrategy(CacheStrategy.Never)));
        assertUnchanged();
    }

    @Test
    public void addNeverCacheResponseMappingAndUpdateOnCallCacheWithDifferentPathResponseMappingDontUpdateTemplateCache() {
        snapshot();
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Never)));
        mappings.editMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                .withBodyFile("body-002.txt")
                                                .withCacheStrategy(CacheStrategy.OnCall)));
        assertUnchanged();
    }

    @Test
    public void addNeverCacheResponseMappingAndUpdateOnCallCacheWithTextContentResponseMappingDontUpdateTemplateCache() {
        snapshot();
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Never)));
        mappings.editMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                .withBody("")
                                                .withCacheStrategy(CacheStrategy.OnCall)));
        assertUnchanged();
    }

    @Test
    public void addNeverCacheResponseMappingAndUpdateOnCallCacheWithBinaryContentResponseMappingDontUpdateTemplateCache() {
        snapshot();
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Never)));
        mappings.editMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                .withBody(new byte[0])
                                                .withCacheStrategy(CacheStrategy.OnCall)));
        assertUnchanged();
    }

    @Test
    public void addNeverCacheResponseMappingAndUpdateAlwaysCacheWithDifferentPathResponseMappingUpdateTemplateCacheOnce() {
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Never)));
        StubMapping mapping = mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                 .withBodyFile("body-002.txt")
                                                 .withCacheStrategy(CacheStrategy.Always));
        mappings.editMapping(mapping);
        assertContent(mapping);
    }

    @Test
    public void addNeverCacheResponseMappingAndUpdateAlwaysCacheWithTextContentResponseMappingUpdateTemplateCacheOnce() {
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Never)));
        StubMapping mapping = mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                 .withBody("")
                                                 .withCacheStrategy(CacheStrategy.Always));
        mappings.editMapping(mapping);
        assertContent(mapping);
    }

    @Test
    public void addNeverCacheResponseMappingAndUpdateAlwaysCacheWithBinaryContentResponseMappingDontUpdateTemplateCache() {
        snapshot();
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Never)));
        mappings.editMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                .withBody(new byte[0])
                                                .withCacheStrategy(CacheStrategy.Always)));
        assertUnchanged();
    }

    @Test
    public void addAlwaysCacheResponseMappingAndUpdateNeverCacheWithDifferentPathResponseMappingLeftEmptyTemplateCache() {
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Always)));
        mappings.editMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                .withBodyFile("body-002.txt")
                                                .withCacheStrategy(CacheStrategy.Never)));
        assertContent();
    }

    @Test
    public void addAlwaysCacheResponseMappingAndUpdateNeverCacheWithTextContentResponseMappingLeftEmptyTemplateCache() {
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Always)));
        mappings.editMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                .withBody("")
                                                .withCacheStrategy(CacheStrategy.Never)));
        assertContent();
    }

    @Test
    public void addAlwaysCacheResponseMappingAndUpdateNeverCacheWithBinaryContentResponseMappingLeftEmptyTemplateCache() {
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Always)));
        mappings.editMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                .withBody(new byte[0])
                                                .withCacheStrategy(CacheStrategy.Never)));
        assertContent();
    }

    @Test
    public void addAlwaysCacheResponseMappingAndUpdateOnCallCacheWithDifferentPathResponseMappingLeftEmptyTemplateCache() {
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Always)));
        mappings.editMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                .withBodyFile("body-002.txt")
                                                .withCacheStrategy(CacheStrategy.OnCall)));
        assertContent();
    }

    @Test
    public void addAlwaysCacheResponseMappingAndUpdateOnCallCacheWithTextContentResponseMappingLeftEmptyTemplateCache() {
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Always)));
        mappings.editMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                .withBody("")
                                                .withCacheStrategy(CacheStrategy.OnCall)));
        assertContent();
    }

    @Test
    public void addAlwaysCacheResponseMappingAndUpdateOnCallCacheWithBinaryContentResponseMappingLeftEmptyTemplateCache() {
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Always)));
        mappings.editMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                .withBody(new byte[0])
                                                .withCacheStrategy(CacheStrategy.OnCall)));
        assertContent();
    }

    @Test
    public void addAlwaysCacheResponseMappingAndUpdateAlwaysCacheWithDifferentPathResponseMappingUpdateTemplateCacheTwice() {
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Always)));
        StubMapping mapping = mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                 .withBodyFile("body-002.txt")
                                                 .withCacheStrategy(CacheStrategy.Always));
        mappings.editMapping(mapping);
        assertContent(mapping);
    }

    @Test
    public void addAlwaysCacheResponseMappingAndUpdateAlwaysCacheWithTextContentResponseMappingUpdateTemplateCacheTwice() {
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Always)));
        StubMapping mapping = mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                 .withBody("")
                                                 .withCacheStrategy(CacheStrategy.Always));
        mappings.editMapping(mapping);
        assertContent(mapping);
    }

    @Test
    public void addAlwaysCacheResponseMappingAndUpdateAlwaysCacheWithBinaryContentResponseMappingLeftEmptyTemplateCache() {
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Always)));
        mappings.editMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                .withBody(new byte[0])
                                                .withCacheStrategy(CacheStrategy.Always)));
        assertContent();
    }

    @Test
    public void resetDontUpdateCache() {
        snapshot();
        mappings.reset();
        assertUnchanged();
    }

    @Test
    public void addAndResetLeftEmptyTemplateCache() {
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Always)));
        mappings.reset();
        assertContent();
    }

    @Test
    public void addTwiceAndResetLeftEmptyTemplateCache() {
        mappings.addMapping(mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Always)));
        mappings.addMapping(mapping(2, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Always)));
        mappings.reset();
        assertContent();
    }

    @Test
    public void updateFileContentAfterBetweenTwoAddAlwaysCacheBodyFileStubMappingUpdateCache() throws InterruptedException {
        Thread.sleep(1_000);
        StubMapping mapping = mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                 .withBodyFile("body-001.txt")
                                                 .withCacheStrategy(CacheStrategy.Always));
        mappings.addMapping(mapping);
        snapshot();
        files.writeTextFile("body-001.txt", "update");
        mappings.addMapping(mapping(2, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Always)));
        assertTemplateChanged(mapping);
    }

    @Test
    public void addTwiceAlwaysCacheStubMappingDontUpdateCache() {
        StubMapping mapping = mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                 .withBodyFile("body-001.txt")
                                                 .withCacheStrategy(CacheStrategy.Always));
        mappings.addMapping(mapping);
        snapshot();
        mappings.addMapping(mapping(2, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                               .withBodyFile("body-001.txt")
                                               .withCacheStrategy(CacheStrategy.Always)));
        assertUnchanged();
    }

    @Test
    public void addContentMatchingExistingCachedFileAddNewCacheEntry() {
        StubMapping mapping01 = mapping(1, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                 .withBodyFile("body-001.txt")
                                                 .withCacheStrategy(CacheStrategy.Always));
        mappings.addMapping(mapping01);
        StubMapping mapping02 = mapping(2, responseDefinition().withTransformers(ResponseTemplateTransformer.NAME)
                                                 .withBody("body-001.txt")
                                                 .withCacheStrategy(CacheStrategy.Always));
        mappings.addMapping(mapping02);
        assertContent(mapping01, mapping02);

        Map<DefinitionBasedTemplateSource, Template> templates = cache.getCachedTemplates();
        Template template01 = templates.get(mappingToSource(mapping01));
        Template template02 = templates.get(mappingToSource(mapping02));
        assertThat(template01, allOf(notNullValue(), not(sameInstance(template02))));
    }

    @Before
    public void init() {
        Handlebars handlebars = new Handlebars();
        ResponseDefinitionTransformer transformer = new ResponseTemplateTransformer(false, handlebars, Collections.<String, Helper>emptyMap());
        Map<String, ResponseDefinitionTransformer> transformers = ImmutableMap.of(ResponseTemplateTransformer.NAME, transformer);
        FileSource workingDirectory = new SingleRootFileSource(tempDir.getRoot());
        mappings = new InMemoryStubMappings(Collections.<String, RequestMatcherExtension>emptyMap(), transformers, workingDirectory);
        cache = (DefinitionBasedTemplateCache) handlebars.getCache();
        files = workingDirectory.child(WireMockApp.FILES_ROOT);
        files.createIfNecessary();
        files.writeTextFile("body-001.txt", "");
        files.writeTextFile("body-002.txt", "");
    }

    private StubMapping mapping(int id, ResponseDefinitionBuilder response) {
        StubMapping mapping = new StubMapping();
        mapping.setId(UUID.fromString(format("0-0-0-0-%03d", id)));
        if (response != null) {
            mapping.setResponse(response.build());
        }
        return mapping;
    }

    private void assertContent(StubMapping... mappings) {
        Set<DefinitionBasedTemplateSource> sources = new HashSet<>(mappings.length);
        for (StubMapping mapping : mappings) {
            DefinitionBasedTemplateSource source;
            source = mappingToSource(mapping);
            sources.add(source);
        }
        assertThat("cached sources", cache.getCachedTemplates().keySet(), equalTo(sources));
    }

    private DefinitionBasedTemplateSource mappingToSource(StubMapping mapping) {
        DefinitionBasedTemplateSource source;
        UUID id = mapping.getId();
        ResponseDefinition res = mapping.getResponse();
        if (res.specifiesTextBodyContent()) {
            source = fromString(id, res.getBody(), res.getCacheStrategy());
        } else if (res.specifiesBodyFile()) {
            source = fromTextFile(id, files.getTextFileNamed(res.getBodyFileName()), res.getCacheStrategy());
        } else {
            throw new UnsupportedOperationException();
        }
        return source;
    }

    private void snapshot() {
        cacheSnapshot = cache.getCachedTemplates();
    }
    private void assertUnchanged() {
        Map<DefinitionBasedTemplateSource, Template> actual = cache.getCachedTemplates();
        assertThat("cached sources", actual.keySet(), equalTo(cacheSnapshot.keySet()));
        for (Map.Entry<DefinitionBasedTemplateSource, Template> pair : actual.entrySet()) {
            assertThat(format("%s template", pair.getKey()), pair.getValue(), sameInstance(cacheSnapshot.get(pair.getKey())));
        }
    }
    private void assertTemplateChanged(StubMapping mapping) {
        DefinitionBasedTemplateSource source = mappingToSource(mapping);
        assertThat(source.toString(), cache.getCachedTemplates().get(source), allOf(notNullValue(), not(sameInstance(cacheSnapshot.get(source)))));
    }
}
