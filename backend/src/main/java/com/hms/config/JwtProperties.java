package com.hms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
  private String secret;
  private int expirationMinutes;
  private String cookieName;
  private boolean cookieSecure;

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public int getExpirationMinutes() {
    return expirationMinutes;
  }

  public void setExpirationMinutes(int expirationMinutes) {
    this.expirationMinutes = expirationMinutes;
  }

  public String getCookieName() {
    return cookieName;
  }

  public void setCookieName(String cookieName) {
    this.cookieName = cookieName;
  }

  public boolean isCookieSecure() {
    return cookieSecure;
  }

  public void setCookieSecure(boolean cookieSecure) {
    this.cookieSecure = cookieSecure;
  }
}
