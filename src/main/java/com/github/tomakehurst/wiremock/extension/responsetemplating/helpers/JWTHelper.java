package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Options;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class JWTHelper extends HandlebarsHelper<Object> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
        String signAlgo = options.hash("algo", SignatureAlgorithm.RS256.name());
        String key = options.hash("key", null);
        String claims = options.hash("claims", null);
        String payload = options.hash("payload", null);
        String header = options.hash("header", null);

        try {
            return createJWT(SignatureAlgorithm.valueOf(signAlgo), key, jsonToMap(claims), payload, jsonToMap(header));
        } catch (Exception e) {
            return this.handleError(e.getMessage(), e);
        }
    }

    private Map<String, Object> jsonToMap(String json) {
        if (json == null) {
            return new HashMap<>();
        }

        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<HashMap<String, Object>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    private String createJWT(SignatureAlgorithm signatureAlgorithm, final String apiKey,
                             final Map<String, Object> claims,
                             final String payload,
                             final Map<String, Object> header) throws NoSuchAlgorithmException, InvalidKeySpecException {

        if (signatureAlgorithm == null) {
            signatureAlgorithm = SignatureAlgorithm.NONE;
        }

        final JwtBuilder jwtBuilder = Jwts.builder();

        if (signatureAlgorithm != SignatureAlgorithm.NONE) {

            if (StringUtils.isBlank(apiKey)) {
                throw new IllegalStateException("key must not be empty in case algo is defined");
            }

            byte[] apiKeyBytes = Base64.getDecoder().decode(apiKey);

            Key key;
            switch (signatureAlgorithm.getFamilyName()) {
                case "HMAC":
                    key = new SecretKeySpec(apiKeyBytes, signatureAlgorithm.getJcaName());
                    break;
                case "ECDSA":
                    final KeyFactory ecdsaKf = KeyFactory.getInstance("ECDSA", new BouncyCastleProvider());
                    key = ecdsaKf.generatePrivate(new PKCS8EncodedKeySpec(apiKeyBytes));
                    break;
                case "RSA":
                default:
                    final KeyFactory rsaKf = KeyFactory.getInstance(signatureAlgorithm.getFamilyName());
                    key = rsaKf.generatePrivate(new PKCS8EncodedKeySpec(apiKeyBytes));
                    break;
            }

            jwtBuilder.signWith(key, signatureAlgorithm);
        }

        if (claims != null && !claims.isEmpty()) {
            jwtBuilder.setClaims(claims);
        } else if (payload != null) {
            jwtBuilder.setPayload(payload);
        }

        if (header != null && !header.isEmpty()) {
            jwtBuilder.setHeader(header);
        }

        return jwtBuilder.compact();
    }
}
