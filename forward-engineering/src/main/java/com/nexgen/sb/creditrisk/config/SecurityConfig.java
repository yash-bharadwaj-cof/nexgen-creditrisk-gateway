package com.nexgen.sb.creditrisk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 6 configuration for REST endpoints.
 *
 * <ul>
 *   <li>Health and Swagger UI are accessible without credentials.</li>
 *   <li>SOAP endpoints under {@code /ws/**} are permit-all here; WS-Security
 *       UsernameToken is enforced at the CXF layer via {@link CxfConfig}.</li>
 *   <li>All other REST endpoints require HTTP Basic authentication.</li>
 *   <li>Sessions are stateless; CSRF protection is therefore not applicable.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(
            @Value("${spring.security.user.name}") String username,
            @Value("${spring.security.user.password}") String password) {
        // {noop} prefix signals Spring Security to skip hashing for this in-memory user.
        // For production deployments, externalize credentials and replace {noop} with
        // a BCrypt-encoded value using PasswordEncoderFactories.createDelegatingPasswordEncoder().
        UserDetails user = User.withUsername(username)
                .password("{noop}" + password)
                .roles("API_USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}
