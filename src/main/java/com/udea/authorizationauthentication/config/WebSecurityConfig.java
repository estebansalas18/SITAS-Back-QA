package com.udea.authorizationauthentication.config;

import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;


/**
 * Configuration class for web security.
 * This class enables web security and defines configurations for password encoding and HTTP security.
 *
 * @author Natalia García
 * @author Héctor Güiza
 * @author Jeisson Barrantes
 * @author Hellen Rubio
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    /**
     * Defines a bean for PasswordEncoder to use BCrypt hashing.
     *
     * @return a BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Defines a bean for SecurityFilterChain to set up HttpSecurity.
     *
     * @param http the HttpSecurity object to configure
     * @return a SecurityFilterChain instance
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/public/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
                /*.headers(headers ->
                        headers.xssProtection(
                                xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                        ).contentSecurityPolicy(
                                cps -> cps.policyDirectives("default-src 'self';base-uri 'self';"
                                        + "font-src 'self' https: data:;form-action 'self';"
                                        + "frame-ancestors 'self';img-src 'self' data:;"
                                        + "object-src 'none';script-src 'self';"
                                        + "script-src-attr 'none';style-src 'self' https: 'unsafe-inline';"
                                        + "upgrade-insecure-requests")
                        ));*/
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${JWT_SECRET_KEY}") String secretKey) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
