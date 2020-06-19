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
import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.function.Function;

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

    public static CertificateAuthority generateCertificateAuthority() throws CertificateGenerationUnsupportedException {
        CertChainAndKey certChainAndKey = generateCertChainAndKey(
                "RSA",
                "SHA256WithRSA",
                "WireMock Local Self Signed Root Certificate",
                Period.ofYears(10),
                CertificateAuthority::certificateAuthorityExtensions
        );
        return new CertificateAuthority(certChainAndKey.certificateChain, certChainAndKey.key);
    }

    private static CertificateExtensions certificateAuthorityExtensions(X509Key publicKey) {
        try {
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
        } catch (IOException e) {
            return throwUnchecked(e, null);
        }
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
            CertChainAndKey certChainAndKey = generateCertChainAndKey(keyType, "SHA256With" + keyType, hostName.getAsciiName(), Period.ofYears(1), x509Key -> subjectAlternativeName(hostName));
            X509Certificate signed = sign(certChainAndKey.certificateChain[0]);
            X509Certificate[] fullChain = prepend(signed, certificateChain);
            return new CertChainAndKey(fullChain, certChainAndKey.key);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | CertificateException | SignatureException | NoSuchMethodError | VerifyError | NoClassDefFoundError e) {
            throw new CertificateGenerationUnsupportedException(
                "Your runtime does not support generating certificates at runtime",
                e
            );
        }
    }

    private static CertChainAndKey generateCertChainAndKey(
        String keyType,
        String sigAlg,
        String subjectName,
        Period validity,
        Function<X509Key, CertificateExtensions> extensionBuilder
    ) throws CertificateGenerationUnsupportedException {
        try {
            // TODO inline CertAndKeyGen logic so we don't depend on sun.security.tools.keytool
            CertAndKeyGen newCertAndKey = new CertAndKeyGen(keyType, sigAlg);
            newCertAndKey.generate(2048);
            PrivateKey newKey = newCertAndKey.getPrivateKey();

            ZonedDateTime start = ZonedDateTime.now();
            ZonedDateTime end = start.plus(validity);

            X509Certificate certificate = newCertAndKey.getSelfCertificate(
                    new X500Name("CN=" + subjectName),
                    Date.from(start.toInstant()),
                    Duration.between(start, end).getSeconds(),
                    extensionBuilder.apply(newCertAndKey.getPublicKey())
            );
            return new CertChainAndKey(new X509Certificate[]{ certificate }, newKey);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | CertificateException | SignatureException | NoSuchMethodError | VerifyError | NoClassDefFoundError | IOException e) {
            throw new CertificateGenerationUnsupportedException(
                    "Your runtime does not support generating certificates at runtime",
                    e
            );
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
