package com.github.tomakehurst.wiremock.http.ssl;

import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.CertificateExtensions;
import sun.security.x509.DNSName;
import sun.security.x509.GeneralName;
import sun.security.x509.GeneralNames;
import sun.security.x509.SubjectAlternativeNameExtension;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

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

import static java.util.Objects.requireNonNull;

class CertificateAuthority {

    private final X509Certificate[] certificateChain;
    private final PrivateKey key;

    CertificateAuthority(X509Certificate[] certificateChain, PrivateKey key) {
        this.certificateChain = requireNonNull(certificateChain);
        if (certificateChain.length == 0) {
            throw new IllegalArgumentException("Chain must have entries");
        }
        this.key = requireNonNull(key);
    }

    CertChainAndKey generateCertificate(
        String keyType,
        String requestedNameString
    ) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, CertificateException, SignatureException, IOException {
        CertAndKeyGen newCertAndKey = new CertAndKeyGen(keyType, "SHA256With"+keyType, null);
        newCertAndKey.generate(2048);
        PrivateKey newKey = newCertAndKey.getPrivateKey();

        X509Certificate certificate = newCertAndKey.getSelfCertificate(
                new X500Name("CN=" + requestedNameString),
                new Date(),
                (long) 365 * 24 * 60 * 60,
                subjectAlternativeName(requestedNameString)
        );

        X509Certificate[] signingChain = certificateChain;

        X509Certificate signed = sign(certificate);
        X509Certificate[] fullChain = new X509Certificate[signingChain.length + 1];
        fullChain[0] = signed;
        System.arraycopy(signingChain, 0, fullChain, 1, signingChain.length);
        return new CertChainAndKey(fullChain, newKey);
    }

    private static CertificateExtensions subjectAlternativeName(String requestedNameString) throws IOException {
        GeneralName name = new GeneralName(new DNSName(requestedNameString));
        GeneralNames names = new GeneralNames();
        names.add(name);
        SubjectAlternativeNameExtension subjectAlternativeNameExtension = new SubjectAlternativeNameExtension(names);

        CertificateExtensions extensions = new CertificateExtensions();
        extensions.set(SubjectAlternativeNameExtension.NAME, subjectAlternativeNameExtension);
        return extensions;
    }

    private X509Certificate sign(X509Certificate certificate) {
        try {
            X509Certificate issuerCertificate = certificateChain[0];
            Principal issuer = issuerCertificate.getSubjectDN();
            String issuerSigAlg = issuerCertificate.getSigAlgName();

            byte[] inCertBytes = certificate.getTBSCertificate();
            X509CertInfo info = new X509CertInfo(inCertBytes);
            info.set(X509CertInfo.ISSUER, issuer);

            X509CertImpl outCert = new X509CertImpl(info);
            outCert.sign(key, issuerSigAlg);

            return outCert;
        } catch (Exception ex) {
            // TODO log failure to generate certificate
        }
        return null;
    }
}
