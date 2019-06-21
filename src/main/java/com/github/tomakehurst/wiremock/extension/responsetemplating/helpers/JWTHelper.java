package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Options;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
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
            return OBJECT_MAPPER.readValue(json, new TypeReference<HashMap<String, Object>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    private String createJWT(final SignatureAlgorithm signatureAlgorithm, final String apiKey,
                             final Map<String, Object> claims,
                             final String payload,
                             final Map<String, Object> header) throws NoSuchAlgorithmException, InvalidKeySpecException {

        if (!signatureAlgorithm.isJdkStandard()) {
            throw new IllegalStateException("Not a JDK standard which is not supported");
        }

        byte[] apiKeyBytes = DatatypeConverter.parseBase64Binary(apiKey);

        Key key;
        switch (signatureAlgorithm.getFamilyName()){
            case "HMAC":
                key = new SecretKeySpec(apiKeyBytes, signatureAlgorithm.getJcaName());
                break;
            case "RSA":
            case "ECDSA":
            default:
                final KeyFactory keyFactory = KeyFactory.getInstance(signatureAlgorithm.getFamilyName());
                key = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(apiKeyBytes));
                break;
        }

        final JwtBuilder jwtBuilder = Jwts.builder().signWith(key, signatureAlgorithm);


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
