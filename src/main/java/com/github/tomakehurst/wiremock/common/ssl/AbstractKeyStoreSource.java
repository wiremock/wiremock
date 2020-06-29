package com.github.tomakehurst.wiremock.common.ssl;

import com.github.tomakehurst.wiremock.common.Source;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

public abstract class AbstractKeyStoreSource implements Source<KeyStore> {

    protected final String keyStoreType;
    protected final char[] keyStorePassword;

    protected AbstractKeyStoreSource(String keyStoreType, char[] keyStorePassword) {
        this.keyStoreType = keyStoreType;
        this.keyStorePassword = keyStorePassword;
    }

    public KeyStore load() {
        InputStream instream = null;
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            instream = createInputStream();
            trustStore.load(instream, keyStorePassword);
            return trustStore;
        } catch (Exception e) {
            return throwUnchecked(e, KeyStore.class);
        } finally {
            if (instream != null) {
                try {
                    instream.close();
                } catch (IOException ioe) {
                    throwUnchecked(ioe);
                }
            }
        }
    }

    protected abstract InputStream createInputStream();
    public abstract boolean exists();

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public char[] getKeyStorePassword() {
        return keyStorePassword;
    }
}
