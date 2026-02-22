package com.hms.security;

import com.hms.config.JwtProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final JwtProperties properties;

  public JwtAuthFilter(JwtService jwtService, JwtProperties properties) {
    this.jwtService = jwtService;
    this.properties = properties;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      String token = extractToken(request);
      if (token != null) {
        try {
          String userId = jwtService.parseUserId(token);
          String role = jwtService.parseRole(token);
          String authority = switch (role == null ? "" : role.trim().toUpperCase()) {
            case "ADMIN" -> "ROLE_ADMIN";
            case "STAFF" -> "ROLE_STAFF";
            default -> "ROLE_CUSTOMER";
          };
          UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
              userId,
              null,
              List.of(new SimpleGrantedAuthority(authority))
          );
          auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception ignored) {
          SecurityContextHolder.clearContext();
        }
      }
    }
    filterChain.doFilter(request, response);
  }

  private String extractToken(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if (properties.getCookieName().equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }
}
