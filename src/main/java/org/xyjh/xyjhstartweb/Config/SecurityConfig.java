package org.xyjh.xyjhstartweb.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.xyjh.xyjhstartweb.duduplan.security.DuduPlanAccessTokenFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final DuduPlanAccessTokenFilter duduPlanAccessTokenFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, DuduPlanAccessTokenFilter duduPlanAccessTokenFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.duduPlanAccessTokenFilter = duduPlanAccessTokenFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/license/**").permitAll()
                        .requestMatchers("/account/add").permitAll()
                        .requestMatchers("/api/admin/licenses/login").permitAll()
                        .requestMatchers("/api/admin/users/login").permitAll()
                        .requestMatchers("/api/account-xyjh/sync/refresh").permitAll()
                        .requestMatchers("/api/dudu-plan/auth/**", "/api/dudu-plan/health", "/api/dudu-plan/realtime").permitAll()
                        .requestMatchers("/api/files/**").permitAll()
                        .requestMatchers("/files/**").permitAll()
                        .requestMatchers("/api/admin/**", "/api/account-xyjh/**", "/api/dudu-plan/chat/**").authenticated()
                        .anyRequest().denyAll()
                )
                .addFilterBefore(duduPlanAccessTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, DuduPlanAccessTokenFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "https://kakaweb.ltd"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
