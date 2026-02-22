package com.hms.security;

import com.hms.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private static final String ROLE_CLAIM = "role";
  private final JwtProperties properties;
  private final SecretKey key;

  public JwtService(JwtProperties properties) {
    this.properties = properties;
    this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
  }

  public String createToken(String userId, String role) {
    Instant now = Instant.now();
    Instant expires = now.plus(properties.getExpirationMinutes(), ChronoUnit.MINUTES);
    return Jwts.builder()
        .subject(userId)
        .claim(ROLE_CLAIM, role)
        .issuedAt(Date.from(now))
        .expiration(Date.from(expires))
        .signWith(key)
        .compact();
  }

  public String parseUserId(String token) {
    Claims claims = parseClaims(token);
    return claims.getSubject();
  }

  public String parseRole(String token) {
    Claims claims = parseClaims(token);
    Object value = claims.get(ROLE_CLAIM);
    return value == null ? "CUSTOMER" : String.valueOf(value);
  }

  private Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public ResponseCookie buildAuthCookie(String token) {
    return ResponseCookie.from(properties.getCookieName(), token)
        .httpOnly(true)
        .secure(properties.isCookieSecure())
        .path("/")
        .sameSite("Lax")
        .maxAge(properties.getExpirationMinutes() * 60L)
        .build();
  }

  public ResponseCookie clearAuthCookie() {
    return ResponseCookie.from(properties.getCookieName(), "")
        .httpOnly(true)
        .secure(properties.isCookieSecure())
        .path("/")
        .sameSite("Lax")
        .maxAge(0)
        .build();
  }
}
