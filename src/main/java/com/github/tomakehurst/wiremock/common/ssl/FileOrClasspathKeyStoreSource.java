package com.github.tomakehurst.wiremock.common.ssl;

import com.google.common.io.Resources;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.EnumSet;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static java.nio.file.attribute.PosixFilePermission.*;
import static java.nio.file.attribute.PosixFilePermissions.asFileAttribute;

public class FileOrClasspathKeyStoreSource extends AbstractKeyStoreSource {

    private final String path;

    public FileOrClasspathKeyStoreSource(String path, String keyStoreType, char[] keyStorePassword) {
        super(keyStoreType, keyStorePassword);
        this.path = path;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    protected InputStream createInputStream() {
        try {
            if (exists()) {
                return new FileInputStream(path);
            } else {
                return Resources.getResource(path).openStream();
            }
        } catch (IOException e) {
            return throwUnchecked(e, InputStream.class);
        }
    }

    @Override
    public boolean exists() {
        return new File(path).isFile();
    }

    @Override
    public void save(KeyStore keyStore) {
        Path created = createKeystoreFile(Paths.get(path));
        try (FileOutputStream fos = new FileOutputStream(created.toFile())) {
            keyStore.store(fos, keyStorePassword);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            throwUnchecked(e);
        }
    }

    private static Path createKeystoreFile(Path path) {
        FileAttribute<?>[] privateDirAttrs = new FileAttribute<?>[0];
        FileAttribute<?>[] privateFileAttrs = new FileAttribute<?>[0];
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            privateDirAttrs = new FileAttribute<?>[] { asFileAttribute(EnumSet.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE)) };
            privateFileAttrs = new FileAttribute<?>[] { asFileAttribute(EnumSet.of(OWNER_READ, OWNER_WRITE)) };
        }

        try {
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent(), privateDirAttrs);
            }
            return Files.createFile(path, privateFileAttrs);
        } catch (IOException e) {
            return throwUnchecked(e, Path.class);
        }
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FileOrClasspathKeyStoreSource that = (FileOrClasspathKeyStoreSource) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path);
    }
}
