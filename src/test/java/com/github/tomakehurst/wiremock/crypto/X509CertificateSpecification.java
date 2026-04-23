/*
 * Copyright (C) 2020-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.crypto;

import static java.util.Objects.requireNonNull;

import com.github.tomakehurst.wiremock.http.ssl.CertificateAuthority;
import java.io.IOException;
import java.security.KeyPair;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.operator.OperatorCreationException;

public class X509CertificateSpecification implements CertificateSpecification {

  private final String subject;
  private final String issuer;
  // java.time is JDK8 only
  private final Date notBefore;
  private final Date notAfter;

  public X509CertificateSpecification(String subject, String issuer, Date notBefore, Date notAfter)
      throws IOException {
    this.subject = requireNonNull(subject);
    this.issuer = requireNonNull(issuer);
    this.notBefore = requireNonNull(notBefore);
    this.notAfter = requireNonNull(notAfter);
  }

  @Override
  public X509Certificate certificateFor(KeyPair keyPair)
      throws CertificateException, IOException, OperatorCreationException {
    return CertificateAuthority.buildCertificate(
        CertificateAuthority.SIG_ALG_PREFIX + "RSA",
        keyPair.getPublic(),
        keyPair.getPrivate(),
        keyPair.getPublic(),
        notBefore,
        notAfter,
        new X500Principal(subject),
        new X500Principal(issuer),
        null,
        false);
  }
}
