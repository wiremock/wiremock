package com.github.tomakehurst.wiremock.http.ssl;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import javax.net.ssl.X509TrustManager;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;

public class CompositeTrustManagerTest {

    private final Mockery context = new Mockery();
    {
        context.setDefaultResultForType(X509Certificate[].class, new X509Certificate[0]);
    }
    private final X509TrustManager trustManager1 = mockX509TrustManager("trustManager1");
    private final X509TrustManager trustManager2 = mockX509TrustManager("trustManager2");
    private final X509Certificate[] chain = new X509Certificate[0];
    private final String authType = "AN_AUTH_TYPE";

    @Test
    public void checkServerTrustedPassesForSingleTrustManager() throws CertificateException {

        context.checking(new Expectations() {{
            allowing(trustManager1).checkServerTrusted(chain, authType);
        }});

        CompositeTrustManager compositeTrustManager = new CompositeTrustManager(singletonList(
            trustManager1
        ));

        compositeTrustManager.checkServerTrusted(chain, authType);
    }

    @Test
    public void checkServerTrustedFailsForSingleTrustManager() throws CertificateException {

        final CertificateException invalidCertForTrustManager1 = new CertificateException("Invalid cert for trustManager1");

        context.checking(new Expectations() {{
            allowing(trustManager1).checkServerTrusted(chain, authType);
            will(throwException(invalidCertForTrustManager1));
        }});

        final CompositeTrustManager compositeTrustManager = new CompositeTrustManager(singletonList(
            trustManager1
        ));

        CertificateException thrown = assertThrows(CertificateException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                compositeTrustManager.checkServerTrusted(chain, authType);
            }
        });
        assertEquals(invalidCertForTrustManager1, thrown);
    }

    @Test
    public void checkServerTrustedIfBothWouldPass() throws CertificateException {

        context.checking(new Expectations() {{
            allowing(trustManager1).checkServerTrusted(chain, authType);
            allowing(trustManager2).checkServerTrusted(chain, authType);
        }});

        CompositeTrustManager compositeTrustManager = new CompositeTrustManager(asList(
            trustManager1,
            trustManager2
        ));

        compositeTrustManager.checkServerTrusted(chain, authType);
    }

    @Test
    public void checkServerTrustedIfFirstWouldPass() throws CertificateException {

        context.checking(new Expectations() {{
            allowing(trustManager1).checkServerTrusted(chain, authType);

            allowing(trustManager2).checkServerTrusted(chain, authType);
            will(throwException(new CertificateException("Invalid cert for trustManager2")));
        }});

        CompositeTrustManager compositeTrustManager = new CompositeTrustManager(asList(
            trustManager1,
            trustManager2
        ));

        compositeTrustManager.checkServerTrusted(chain, authType);
    }

    @Test
    public void checkServerTrustedIfSecondWouldPass() throws CertificateException {

        context.checking(new Expectations() {{
            allowing(trustManager1).checkServerTrusted(chain, authType);
            will(throwException(new CertificateException("Invalid cert for trustManager1")));

            allowing(trustManager2).checkServerTrusted(chain, authType);
        }});

        CompositeTrustManager compositeTrustManager = new CompositeTrustManager(asList(
            trustManager1,
            trustManager2
        ));

        compositeTrustManager.checkServerTrusted(chain, authType);
    }

    @Test
    public void checkServerNotTrustedIfNeitherPass() throws CertificateException {

        final CertificateException invalidCertForTrustManager2 = new CertificateException("Invalid cert for trustManager2");

        context.checking(new Expectations() {{
            allowing(trustManager1).checkServerTrusted(chain, authType);
            will(throwException(new CertificateException("Invalid cert for trustManager1")));

            allowing(trustManager2).checkServerTrusted(chain, authType);
            will(throwException(invalidCertForTrustManager2));
        }});

        final CompositeTrustManager compositeTrustManager = new CompositeTrustManager(
                asList(trustManager1, trustManager2)
        );

        CertificateException thrown = assertThrows(CertificateException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                compositeTrustManager.checkServerTrusted(chain, authType);
            }
        });

        assertEquals(invalidCertForTrustManager2, thrown);
    }

    @Test
    public void returnAllAcceptedIssuers() {

        final X509Certificate cert1 = new FakeX509Certificate("cert1");
        final X509Certificate cert2 = new FakeX509Certificate("cert2");
        final X509Certificate cert3 = new FakeX509Certificate("cert3");
        final X509Certificate cert4 = new FakeX509Certificate("cert4");

        final X509TrustManager trustManager1 = context.mock(X509TrustManager.class, "newTrustManager1");
        final X509TrustManager trustManager2 = context.mock(X509TrustManager.class, "newTrustManager2");

        context.checking(new Expectations() {{
            allowing(trustManager1).getAcceptedIssuers();
            will(returnValue(new X509Certificate[] { cert1, cert2 }));

            allowing(trustManager2).getAcceptedIssuers();
            will(returnValue(new X509Certificate[] { cert3, cert4 }));
        }});

        final CompositeTrustManager compositeTrustManager = new CompositeTrustManager(
                asList(trustManager1, trustManager2)
        );

        X509Certificate[] acceptedIssuers = compositeTrustManager.getAcceptedIssuers();

        assertArrayEquals(new X509Certificate[] { cert1, cert2, cert3, cert4 }, acceptedIssuers);

        acceptedIssuers[2] = new FakeX509Certificate("cert5");

        assertArrayEquals(new X509Certificate[] { cert1, cert2, cert3, cert4 }, compositeTrustManager.getAcceptedIssuers());
    }

    @After
    public void checkMockeryContextSatisfied() {
        context.assertIsSatisfied();
    }

    private X509TrustManager mockX509TrustManager(String name) {
        final X509TrustManager trustManager = context.mock(X509TrustManager.class, name);
        context.checking(new Expectations() {{
            allowing(trustManager).getAcceptedIssuers();
        }});
        return trustManager;
    }

    private static class FakeX509Certificate extends X509Certificate {

        private final String name;

        public FakeX509Certificate(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "X509Certificate{" + name + "}";
        }

        @Override
        public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public int getVersion() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public BigInteger getSerialNumber() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Principal getIssuerDN() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Principal getSubjectDN() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Date getNotBefore() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Date getNotAfter() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public byte[] getTBSCertificate() throws CertificateEncodingException {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public byte[] getSignature() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getSigAlgName() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getSigAlgOID() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public byte[] getSigAlgParams() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public boolean[] getIssuerUniqueID() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public boolean[] getSubjectUniqueID() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public boolean[] getKeyUsage() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public int getBasicConstraints() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public byte[] getEncoded() throws CertificateEncodingException {
            return name.getBytes();
        }

        @Override
        public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public PublicKey getPublicKey() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public boolean hasUnsupportedCriticalExtension() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Set<String> getCriticalExtensionOIDs() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Set<String> getNonCriticalExtensionOIDs() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public byte[] getExtensionValue(String oid) {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
