package com.github.tomakehurst.wiremock.http.ssl;

import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.AuthorityKeyIdentifierExtension;
import sun.security.x509.BasicConstraintsExtension;
import sun.security.x509.CertificateExtensions;
import sun.security.x509.DNSName;
import sun.security.x509.GeneralName;
import sun.security.x509.GeneralNames;
import sun.security.x509.KeyIdentifier;
import sun.security.x509.KeyUsageExtension;
import sun.security.x509.SubjectAlternativeNameExtension;
import sun.security.x509.SubjectKeyIdentifierExtension;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;
import sun.security.x509.X509Key;

import javax.net.ssl.SNIHostName;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import static com.github.tomakehurst.wiremock.common.ArrayFunctions.prepend;
import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("sunapi")
public class CertificateAuthority {

    private final X509Certificate[] certificateChain;
    private final PrivateKey key;

    public CertificateAuthority(X509Certificate[] certificateChain, PrivateKey key) {
        this.certificateChain = requireNonNull(certificateChain);
        if (certificateChain.length == 0) {
            throw new IllegalArgumentException("Chain must have entries");
        }
        this.key = requireNonNull(key);
    }

    public static CertificateAuthority generateCertificateAuthority() throws NoSuchAlgorithmException, InvalidKeyException, CertificateException, SignatureException, NoSuchProviderException, IOException {
        CertAndKeyGen newCertAndKey = new CertAndKeyGen("RSA", "SHA256WithRSA");
        newCertAndKey.generate(2048);
        PrivateKey newKey = newCertAndKey.getPrivateKey();

        X509Certificate certificate = newCertAndKey.getSelfCertificate(
                x500Name("WireMock Local Self Signed Root Certificate"),
                new Date(),
                (long) 365 * 24 * 60 * 60 * 10,
                certificateAuthorityExtensions(newCertAndKey.getPublicKey())
        );
        return new CertificateAuthority(new X509Certificate[]{ certificate }, newKey);
    }

    private static CertificateExtensions certificateAuthorityExtensions(X509Key publicKey) throws IOException {
        KeyIdentifier keyId = new KeyIdentifier(publicKey);
        byte[] keyIdBytes = keyId.getIdentifier();
        CertificateExtensions extensions = new CertificateExtensions();
        extensions.set(AuthorityKeyIdentifierExtension.NAME, new AuthorityKeyIdentifierExtension(keyId, null, null));

        extensions.set(BasicConstraintsExtension.NAME, new BasicConstraintsExtension(true, Integer.MAX_VALUE));

        KeyUsageExtension keyUsage = new KeyUsageExtension(new boolean[7]);
        keyUsage.set(KeyUsageExtension.KEY_CERTSIGN, true);
        keyUsage.set(KeyUsageExtension.CRL_SIGN, true);
        extensions.set(KeyUsageExtension.NAME, keyUsage);

        extensions.set(SubjectKeyIdentifierExtension.NAME, new SubjectKeyIdentifierExtension(keyIdBytes));

        return extensions;
    }

    public X509Certificate[] certificateChain() {
        return certificateChain;
    }

    public PrivateKey key() {
        return key;
    }

    CertChainAndKey generateCertificate(
        String keyType,
        SNIHostName hostName
    ) throws CertificateGenerationUnsupportedException {
        try {
            // TODO inline CertAndKeyGen logic so we don't depend on sun.security.tools.keytool
            CertAndKeyGen newCertAndKey = new CertAndKeyGen(keyType, "SHA256With" + keyType, null);
            newCertAndKey.generate(2048);
            PrivateKey newKey = newCertAndKey.getPrivateKey();

            X509Certificate certificate = newCertAndKey.getSelfCertificate(
                    x500Name(hostName.getAsciiName()),
                    new Date(),
                    (long) 365 * 24 * 60 * 60,
                    subjectAlternativeName(hostName)
            );

            X509Certificate signed = sign(certificate);
            X509Certificate[] fullChain = prepend(signed, certificateChain);
            return new CertChainAndKey(fullChain, newKey);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | CertificateException | SignatureException | NoSuchMethodError | VerifyError | NoClassDefFoundError e) {
            throw new CertificateGenerationUnsupportedException(
                "Your runtime does not support generating certificates at runtime",
                e
            );
        }
    }

    private static X500Name x500Name(String name){
        try {
            return new X500Name("CN=" + name);
        } catch (IOException e) {
            // X500Name throws IOException for a parse error (which isn't an IO problem...)
            // An SNIHostName should be guaranteed not to have a parse issue
            return throwUnchecked(e, null);
        }
    }

    private static CertificateExtensions subjectAlternativeName(SNIHostName hostName) {
        GeneralName name = new GeneralName(dnsName(hostName));
        GeneralNames names = new GeneralNames();
        names.add(name);
        try {
            CertificateExtensions extensions = new CertificateExtensions();
            extensions.set(SubjectAlternativeNameExtension.NAME, new SubjectAlternativeNameExtension(names));
            return extensions;
        } catch (IOException e) {
            // it's an in memory op, should be impossible...
            return throwUnchecked(e, null);
        }
    }

    private static DNSName dnsName(SNIHostName name) {
        try {
            return new DNSName(name.getAsciiName());
        } catch (IOException e) {
            // DNSName throws IOException for a parse error (which isn't an IO problem...)
            // An SNIHostName should be guaranteed not to have a parse issue
            return throwUnchecked(e, null);
        }
    }

    private X509Certificate sign(X509Certificate certificate) throws CertificateException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        X509Certificate issuerCertificate = certificateChain[0];
        Principal issuer = issuerCertificate.getSubjectDN();
        String issuerSigAlg = issuerCertificate.getSigAlgName();

        byte[] inCertBytes = certificate.getTBSCertificate();
        X509CertInfo info = new X509CertInfo(inCertBytes);
        try {
            info.set(X509CertInfo.ISSUER, issuer);
        } catch (IOException e) {
            return throwUnchecked(e, null);
        }

        X509CertImpl outCert = new X509CertImpl(info);
        outCert.sign(key, issuerSigAlg);

        return outCert;
    }
}
