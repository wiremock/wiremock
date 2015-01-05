import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.junit.Test;

public class KeyStoreTest {

    @Test
    public void blah() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        final String keystore = "test-keystore-pwd";
        final String password = "password";

        final String srcPath = "/Users/Robert/Workspaces/wiremock/src/test/resources/";
        final String buildPath = "/Users/Robert/Workspaces/wiremock/build/resources/test/";
        final String buildKeyStorePath = buildPath+keystore;
        final String keyStorePath = buildKeyStorePath;

        FileInputStream inStream = new FileInputStream(keyStorePath);
        KeyStore keyStore = KeyStore.getInstance("JKS");
        System.out.println("Loading "+keyStorePath+" with "+password);
        keyStore.load(inStream, password.toCharArray());
    }
}
