package com.github.tomakehurst.wiremock.jetty94;

import com.github.tomakehurst.wiremock.common.ssl.ReadOnlyFileOrClasspathKeyStoreSource;

import java.io.FileOutputStream;
import java.io.IOException;
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

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static java.nio.file.attribute.PosixFilePermission.*;
import static java.nio.file.attribute.PosixFilePermissions.asFileAttribute;

public class WritableFileOrClasspathKeyStoreSource extends ReadOnlyFileOrClasspathKeyStoreSource {

    public WritableFileOrClasspathKeyStoreSource(String path, String keyStoreType, char[] keyStorePassword) {
        super(path, keyStoreType, keyStorePassword);
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
}
