package com.vlatal.gitracker.serivce;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${supabase.jwks.url}")
    private String jwksUrl;

    private ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    @PostConstruct
    public void init() throws Exception {
        JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(new URL(jwksUrl));
        JWSVerificationKeySelector<SecurityContext> keySelector =
                new JWSVerificationKeySelector<>(JWSAlgorithm.ES256, jwkSource);

        jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWSKeySelector(keySelector);
        System.out.println("JwtService initialized with JWKS: " + jwksUrl);
    }

    private JWTClaimsSet extractAllClaims(String token) {
        try {
            return jwtProcessor.process(token, null);
        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public String extractPersonId(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpirationTime();
    }

    public String extractRole(String token) {
        try {
            JWTClaimsSet claims = extractAllClaims(token);
            Map<String, Object> userMetadata = (Map<String, Object>) claims.getClaim("user_metadata");
            if (userMetadata != null && userMetadata.containsKey("role")) {
                return userMetadata.get("role").toString();
            }
            // fallback: Supabase role claim
            return claims.getStringClaim("role");
        } catch (Exception e) {
            return null;
        }
    }

    public String extractName(String token) {
        try {
            JWTClaimsSet claims = extractAllClaims(token);
            Map<String, Object> userMetadata = (Map<String, Object>) claims.getClaim("user_metadata");
            if (userMetadata != null && userMetadata.containsKey("name")) {
                return userMetadata.get("name").toString();
            }
            return claims.getStringClaim("email");
        } catch (Exception e) {
            return null;
        }
    }

    public Boolean validateToken(String token) {
        try {
            return extractExpiration(token).after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}