/*
 * Copyright (C) 2020-2025 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.ArrayFunctions.prepend;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Vector;
import javax.net.ssl.SNIHostName;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.*;

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

  public static CertificateAuthority generateCertificateAuthority()
      throws CertificateGenerationUnsupportedException {
    try {
      KeyPair pair = generateKeyPair("RSA");
      String sigAlg = "SHA256WithRSA";
      X500Name subjectName = new X500Name("CN=" + "WireMock Local Self Signed Root Certificate");
      X509CertInfo info =
          makeX509CertInfo(
              sigAlg,
              subjectName,
              ZonedDateTime.now().minus(Period.ofDays(1)),
              Period.ofYears(10),
              pair.getPublic(),
              certificateAuthorityExtensions(new KeyIdentifier(pair.getPublic())));

      X509CertImpl certificate = selfSign(info, pair.getPrivate(), sigAlg, subjectName);

      return new CertificateAuthority(new X509Certificate[] {certificate}, pair.getPrivate());
    } catch (NoSuchAlgorithmException
        | NoSuchProviderException
        | InvalidKeyException
        | CertificateException
        | SignatureException
        | NoSuchMethodError
        | VerifyError
        | NoClassDefFoundError
        | IOException
        | IllegalAccessError e) {
      throw new CertificateGenerationUnsupportedException(
          "Your runtime does not support generating certificates at runtime", e);
    }
  }

  private static X509CertImpl selfSign(
      X509CertInfo info, PrivateKey privateKey, String sigAlg, X500Name subjectName)
      throws CertificateException,
          NoSuchAlgorithmException,
          InvalidKeyException,
          NoSuchProviderException,
          SignatureException,
          IOException {
    info.set(X509CertInfo.ISSUER, subjectName);
    X509CertImpl certificate = new X509CertImpl(info);
    certificate.sign(privateKey, sigAlg);
    return certificate;
  }

  private static CertificateExtensions certificateAuthorityExtensions(KeyIdentifier keyId)
      throws IOException {
    CertificateExtensions extensions = new CertificateExtensions();

    extensions.set(
        AuthorityKeyIdentifierExtension.NAME,
        new AuthorityKeyIdentifierExtension(keyId, null, null));
    extensions.set(
        BasicConstraintsExtension.NAME, new BasicConstraintsExtension(true, Integer.MAX_VALUE));
    extensions.set(KeyUsageExtension.NAME, certificateAuthorityKeyUsageExtension());
    extensions.set(
        SubjectKeyIdentifierExtension.NAME,
        new SubjectKeyIdentifierExtension(keyId.getIdentifier()));

    return extensions;
  }

  private static KeyUsageExtension certificateAuthorityKeyUsageExtension() throws IOException {
    KeyUsageExtension keyUsage = new KeyUsageExtension(new boolean[7]);
    keyUsage.set(KeyUsageExtension.KEY_CERTSIGN, true);
    keyUsage.set(KeyUsageExtension.CRL_SIGN, true);
    return keyUsage;
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
      String sigAlg = "SHA256With" + keyType;
      X509CertInfo info =
          makeX509CertInfo(
              sigAlg,
              new X500Name("CN=" + hostName.getAsciiName()),
              ZonedDateTime.now().minus(Period.ofDays(1)),
              Period.ofYears(1),
              pair.getPublic(),
              certificateExtensions(hostName));

      X509CertImpl certificate = sign(info);

      X509Certificate[] fullChain = prepend(certificate, certificateChain);
      return new CertChainAndKey(fullChain, pair.getPrivate());
    } catch (NoSuchAlgorithmException
        | NoSuchProviderException
        | InvalidKeyException
        | CertificateException
        | SignatureException
        | NoSuchMethodError
        | VerifyError
        | NoClassDefFoundError
        | IOException
        | IllegalAccessError e) {
      throw new CertificateGenerationUnsupportedException(
          "Your runtime does not support generating certificates at runtime", e);
    }
  }

  private X509CertImpl sign(X509CertInfo info)
      throws CertificateException,
          IOException,
          NoSuchAlgorithmException,
          InvalidKeyException,
          NoSuchProviderException,
          SignatureException {
    X509Certificate issuerCertificate = certificateChain[0];
    info.set(X509CertInfo.ISSUER, issuerCertificate.getSubjectDN());

    X509CertImpl certificate = new X509CertImpl(info);
    certificate.sign(key, issuerCertificate.getSigAlgName());
    return certificate;
  }

  private static KeyPair generateKeyPair(String keyType) throws NoSuchAlgorithmException {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance(keyType);
    keyGen.initialize(2048, new SecureRandom());
    return keyGen.generateKeyPair();
  }

  private static X509CertInfo makeX509CertInfo(
      String sigAlg,
      X500Name subjectName,
      ZonedDateTime start,
      Period validity,
      PublicKey publicKey,
      CertificateExtensions certificateExtensions)
      throws IOException, CertificateException, NoSuchAlgorithmException {
    ZonedDateTime end = start.plus(validity);

    X509CertInfo info = new X509CertInfo();
    // Add all mandatory attributes
    info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
    info.set(
        X509CertInfo.SERIAL_NUMBER,
        new CertificateSerialNumber(new java.util.Random().nextInt() & 0x7fffffff));
    info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(AlgorithmId.get(sigAlg)));
    info.set(X509CertInfo.SUBJECT, subjectName);
    info.set(X509CertInfo.KEY, new CertificateX509Key(publicKey));
    info.set(
        X509CertInfo.VALIDITY,
        new CertificateValidity(Date.from(start.toInstant()), Date.from(end.toInstant())));
    info.set(X509CertInfo.EXTENSIONS, certificateExtensions);
    return info;
  }

  private static CertificateExtensions certificateExtensions(SNIHostName hostName)
      throws IOException {
    CertificateExtensions extensions = new CertificateExtensions();

    extensions.set(SubjectAlternativeNameExtension.NAME, subjectAlternativeNameExtension(hostName));
    extensions.set(BasicConstraintsExtension.NAME, new BasicConstraintsExtension(false, -1));
    extensions.set(KeyUsageExtension.NAME, certificateKeyUsageExtension());
    extensions.set(ExtendedKeyUsageExtension.NAME, extendedKeyUsageExtension());

    return extensions;
  }

  private static KeyUsageExtension certificateKeyUsageExtension() throws IOException {
    boolean[] keyUsage = new boolean[9];
    keyUsage[0] = true; // digitalSignature
    keyUsage[2] = true; // keyEncipherment
    return new KeyUsageExtension(keyUsage);
  }

  private static ExtendedKeyUsageExtension extendedKeyUsageExtension() throws IOException {
    Vector<ObjectIdentifier> extendedUsages = new Vector<>();
    extendedUsages.add(ObjectIdentifier.of("1.3.6.1.5.5.7.3.1")); // TLS Web Server Authentication
    return new ExtendedKeyUsageExtension(extendedUsages);
  }

  private static SubjectAlternativeNameExtension subjectAlternativeNameExtension(
      SNIHostName hostName) throws IOException {
    GeneralName name = new GeneralName(new DNSName(hostName.getAsciiName()));
    GeneralNames names = new GeneralNames();
    names.add(name);
    return new SubjectAlternativeNameExtension(names);
  }
}
