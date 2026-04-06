/*
 * Copyright (C) 2026 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.http.ssl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Date;
import javax.net.ssl.SNIHostName;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.bc.BcX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class CertificateAuthority {

  public static final String SIG_ALG_PREFIX = "SHA256With";
  private static final Period CA_VALIDITY = Period.ofYears(10);
  private static final Period CERT_VALIDITY = Period.ofYears(1);
  public static final String CA_SUBJECT = "CN=WireMock Local Self Signed Root Certificate";

  private final X509Certificate[] certificateChain;
  private final PrivateKey key;

  public CertificateAuthority(X509Certificate[] certificateChain, PrivateKey key) {
    this.certificateChain = requireNonNull(certificateChain);
    if (certificateChain.length == 0) {
      throw new IllegalArgumentException("Chain must have entries");
    }
    this.key = requireNonNull(key);
  }

  public static CertificateAuthority generateCertificateAuthority()
      throws CertificateGenerationUnsupportedException {
    try {
      String keyType = "RSA";
      KeyPair pair = generateKeyPair(keyType);
      var certificate =
          buildCertificate(
              SIG_ALG_PREFIX + keyType,
              pair.getPublic(),
              pair.getPrivate(),
              pair.getPublic(),
              CA_VALIDITY,
              new X500Principal(CA_SUBJECT),
              new X500Principal(CA_SUBJECT),
              null,
              true);

      return new CertificateAuthority(new X509Certificate[] {certificate}, pair.getPrivate());
    } catch (NoSuchAlgorithmException
        | CertificateException
        | IOException
        | OperatorCreationException
        | NoSuchMethodError
        | VerifyError
        | NoClassDefFoundError
        | IllegalAccessError e) {
      throw new CertificateGenerationUnsupportedException(
          "Your runtime does not support generating certificates at runtime", e);
    }
  }

  public X509Certificate[] certificateChain() {
    return certificateChain;
  }

  public PrivateKey key() {
    return key;
  }

  CertChainAndKey generateCertificate(String keyType, SNIHostName hostName)
      throws CertificateGenerationUnsupportedException {
    try {
      KeyPair pair = generateKeyPair(keyType);
      X509Certificate issuer = certificateChain[0];

      var certificate =
          buildCertificate(
              SIG_ALG_PREFIX + keyType,
              pair.getPublic(),
              key,
              issuer.getPublicKey(),
              CERT_VALIDITY,
              new X500Principal("CN=" + hostName.getAsciiName()),
              issuer.getIssuerX500Principal(),
              hostName.getAsciiName(),
              false);

      return new CertChainAndKey(prepend(certificate, certificateChain), pair.getPrivate());
    } catch (NoSuchAlgorithmException
        | CertificateException
        | IOException
        | OperatorCreationException
        | NoSuchMethodError
        | VerifyError
        | NoClassDefFoundError
        | IllegalAccessError e) {
      throw new CertificateGenerationUnsupportedException(
          "Your runtime does not support generating certificates at runtime", e);
    }
  }

  private static KeyPair generateKeyPair(String keyType) throws NoSuchAlgorithmException {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance(keyType);
    keyGen.initialize(2048, new SecureRandom());
    return keyGen.generateKeyPair();
  }

  public static X509Certificate buildCertificate(
      String sigAlg,
      PublicKey publicKey,
      PrivateKey signerPrivateKey,
      PublicKey signerPublicKey,
      Period validity,
      X500Principal subject,
      X500Principal issuer,
      String sanDnsName,
      boolean isCA)
      throws IOException, OperatorCreationException, CertificateException {
    ZonedDateTime start = ZonedDateTime.now().minus(Period.ofDays(1));
    return buildCertificate(
        sigAlg,
        publicKey,
        signerPrivateKey,
        signerPublicKey,
        Date.from(start.toInstant()),
        Date.from(start.plus(validity).toInstant()),
        subject,
        issuer,
        sanDnsName,
        isCA);
  }

  public static X509Certificate buildCertificate(
      String sigAlg,
      PublicKey publicKey,
      PrivateKey signerPrivateKey,
      PublicKey signerPublicKey,
      Date notBefore,
      Date notAfter,
      X500Principal subject,
      X500Principal issuer,
      String sanDnsName,
      boolean isCA)
      throws IOException, CertificateException, OperatorCreationException {
    SubjectPublicKeyInfo subjectKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
    SubjectPublicKeyInfo signerKeyInfo =
        SubjectPublicKeyInfo.getInstance(signerPublicKey.getEncoded());

    X509v3CertificateBuilder builder =
        new JcaX509v3CertificateBuilder(
            issuer,
            BigInteger.valueOf(new SecureRandom().nextLong()).abs(),
            notBefore,
            notAfter,
            subject,
            publicKey);

    BcX509ExtensionUtils extUtils = new BcX509ExtensionUtils();
    builder.addExtension(
        Extension.subjectKeyIdentifier, false, extUtils.createSubjectKeyIdentifier(subjectKeyInfo));
    builder.addExtension(
        Extension.authorityKeyIdentifier,
        false,
        extUtils.createAuthorityKeyIdentifier(signerKeyInfo));

    if (isCA) {
      builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
      builder.addExtension(
          Extension.keyUsage, true, new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign));
    }

    if (sanDnsName != null) {
      builder.addExtension(
          Extension.subjectAlternativeName,
          false,
          new GeneralNames(new GeneralName(GeneralName.dNSName, sanDnsName)));
    }
    ContentSigner signer = new JcaContentSignerBuilder(sigAlg).build(signerPrivateKey);
    X509CertificateHolder holder = builder.build(signer);
    return new JcaX509CertificateConverter().getCertificate(holder);
  }

  public static <T> T[] prepend(T t, T[] original) {
    @SuppressWarnings("unchecked")
    T[] newArray =
        (T[]) Array.newInstance(original.getClass().getComponentType(), original.length + 1);
    newArray[0] = t;
    System.arraycopy(original, 0, newArray, 1, original.length);
    return newArray;
  }
}
