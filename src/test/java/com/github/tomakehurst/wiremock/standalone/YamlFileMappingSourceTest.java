package com.github.tomakehurst.wiremock.standalone;

import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.common.NotWritableException;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.common.filemaker.FilenameMaker;
import com.github.tomakehurst.wiremock.stubbing.InMemoryStubMappings;
import com.github.tomakehurst.wiremock.stubbing.StoreBackedStubMappings;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.filePath;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class YamlFileMappingSourceTest {

    @TempDir
    public File tempDir;

    StoreBackedStubMappings stubMappings;
    YamlFileMappingsSource source;
    File stubMappingFile;

    @BeforeEach
    public void init() throws Exception {
        stubMappings = new InMemoryStubMappings();
    }

    private void configureWithMultipleMappingFile() throws Exception {
        stubMappingFile = File.createTempFile("multi", ".yaml", tempDir);
        Files.copy(
                Paths.get(filePath("multi-stub/multi.yaml")),
                stubMappingFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        load();
    }

    private void configureWithSingleMappingFile() throws Exception {
        stubMappingFile = File.createTempFile("single", ".yaml", tempDir);
        Files.copy(
                Paths.get(filePath("multi-stub/single.yaml")),
                stubMappingFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        load();
    }

    private void load() {
        source = new YamlFileMappingsSource(new SingleRootFileSource(tempDir), new FilenameMaker());
        source.loadMappingsInto(stubMappings);
    }

    @Test
    public void loadsMappingsViaClasspathFileSource() {
        ClasspathFileSource fileSource = new ClasspathFileSource("jar-filesource");

        YamlFileMappingsSource source = new YamlFileMappingsSource(fileSource, new FilenameMaker("{{#if name}}{{{name}}}{{else}}{{{method}}}-{{{url}}}{{/if}}-{{{id}}}.yaml"));
        StoreBackedStubMappings stubMappings = new InMemoryStubMappings();

        source.loadMappingsInto(stubMappings);

        List<StubMapping> allMappings = stubMappings.getAll();
        assertThat(allMappings, hasSize(3));

        List<String> mappingRequestUrls =
                allMappings.stream().map(stubMapping -> stubMapping.getRequest().getUrl()).collect(Collectors.toList());
        assertThat(mappingRequestUrls, containsInAnyOrder( "/test", "/second_test", "/third_test"));
    }

    @Test
    public void stubMappingFilesAreWrittenWithInsertionIndex() throws Exception {
        YamlFileMappingsSource source =
                new YamlFileMappingsSource(new SingleRootFileSource(tempDir), new FilenameMaker());

        StubMapping stub = get("/saveable").willReturn(ok()).build();
        source.save(stub);

        File savedFile = tempDir.listFiles()[0];
        String savedStub = FileUtils.readFileToString(savedFile, UTF_8);

        assertThat(savedStub, containsString("\"insertionIndex\" : 0"));
    }

    @Test
    public void stubMappingFilesWithOwnFileTemplateFormat() {
        YamlFileMappingsSource source =
                new YamlFileMappingsSource(
                        new SingleRootFileSource(tempDir),
                        new FilenameMaker("{{{request.method}}}-{{{request.url}}}.yaml"));

        StubMapping stub = get("/saveable").willReturn(ok()).build();
        source.save(stub);

        File savedFile = tempDir.listFiles()[0];

        assertEquals(savedFile.getName(), "get-saveable.yaml");
    }

    @Test
    public void refusesToRemoveStubMappingContainedInMultiFile() throws Exception {
        configureWithMultipleMappingFile();

        StubMapping firstStub = stubMappings.getAll().get(0);

        try {
            source.remove(firstStub);
            fail("Expected an exception to be thrown");
        } catch (Exception e) {
            assertThat(e, Matchers.instanceOf(NotWritableException.class));
            assertThat(
                    e.getMessage(),
                    is(
                            "Stubs loaded from multi-mapping files are read-only, and therefore cannot be removed"));
        }

        assertThat(stubMappingFile.exists(), is(true));
    }

    @Test
    public void refusesToRemoveAllWhenMultiMappingFilesArePresent() throws Exception {
        configureWithMultipleMappingFile();

        try {
            source.removeAll();
            fail("Expected an exception to be thrown");
        } catch (Exception e) {
            assertThat(e, Matchers.instanceOf(NotWritableException.class));
            assertThat(
                    e.getMessage(),
                    is(
                            "Some stubs were loaded from multi-mapping files which are read-only, so remove all cannot be performed"));
        }

        assertThat(stubMappingFile.exists(), is(true));
    }

    @Test
    public void refusesToSaveStubMappingOriginallyLoadedFromMultiMappingFile() throws Exception {
        configureWithMultipleMappingFile();

        StubMapping firstStub = stubMappings.getAll().get(0);

        try {
            source.save(firstStub);
            fail("Expected an exception to be thrown");
        } catch (Exception e) {
            assertThat(e, Matchers.instanceOf(NotWritableException.class));
            assertThat(
                    e.getMessage(),
                    is("Stubs loaded from multi-mapping files are read-only, and therefore cannot be saved"));
        }

        assertThat(stubMappingFile.exists(), is(true));
    }

    @Test
    public void savesStubMappingOriginallyLoadedFromSingleMappingFile() throws Exception {
        configureWithSingleMappingFile();

        StubMapping firstStub = stubMappings.getAll().get(0);
        firstStub.setName("New name");
        source.save(firstStub);

        assertThat(FileUtils.readFileToString(stubMappingFile, UTF_8), containsString("New name"));
    }

    @Test
    public void removesStubMappingOriginallyLoadedFromSingleMappingFile() throws Exception {
        configureWithSingleMappingFile();

        StubMapping firstStub = stubMappings.getAll().get(0);
        source.remove(firstStub);

        assertThat(stubMappingFile.exists(), is(false));
    }

}