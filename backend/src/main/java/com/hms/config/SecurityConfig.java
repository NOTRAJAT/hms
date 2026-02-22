package com.hms.config;

import com.hms.security.JwtAuthFilter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {
  private final JwtAuthFilter jwtAuthFilter;
  private final List<String> allowedOrigins;

  public SecurityConfig(
      JwtAuthFilter jwtAuthFilter,
      @Value("${app.cors.allowed-origins}") String allowedOrigins
  ) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
        .map(String::trim)
        .filter(value -> !value.isBlank())
        .collect(Collectors.toList());
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource)
      throws Exception {
    http
        .csrf(csrf -> csrf
            .csrfTokenRepository(csrfTokenRepository())
            .csrfTokenRequestHandler(csrfTokenRequestAttributeHandler())
            .ignoringRequestMatchers(
                "/api/customers/register",
                "/api/customers/login",
                "/api/customers/logout"
            )
        )
        .httpBasic(httpBasic -> httpBasic.disable())
        .formLogin(formLogin -> formLogin.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/customers/register", "/api/customers/login", "/api/customers/logout").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
            .requestMatchers("/api/admin/complaints/**").hasAnyRole("ADMIN", "STAFF")
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CookieCsrfTokenRepository csrfTokenRepository() {
    CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
    repository.setCookiePath("/");
    return repository;
  }

  @Bean
  public CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler() {
    return new CsrfTokenRequestAttributeHandler();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(allowedOrigins);
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-XSRF-TOKEN", "X-Requested-With"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
