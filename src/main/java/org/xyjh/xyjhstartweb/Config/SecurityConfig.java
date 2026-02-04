package org.xyjh.xyjhstartweb.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer; // <-- 新增导入
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration; // <-- 新增导入
import org.springframework.web.cors.CorsConfigurationSource; // <-- 新增导入
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // <-- 新增导入

import java.util.Arrays; // <-- 新增导入
import java.util.List; // <-- 新增导入

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 【修改点 1】: 添加 .cors() 来启用CORS配置
                .cors(Customizer.withDefaults())

                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // 客户端接口：全部放行
                        .requestMatchers("/api/license/**").permitAll()
                        .requestMatchers("/account/add").permitAll()

                        // 【修改点 2】: 修正并添加你前端正在使用的登录路径
                        .requestMatchers("/api/admin/licenses/login").permitAll()
                        .requestMatchers("/api/admin/users/login").permitAll() // <-- 确保这个路径被允许

                        // 其他所有管理员接口：必须认证
                        .requestMatchers("/api/admin/**").authenticated()
                        // 任何其他未匹配的请求：全部拒绝
                        .anyRequest().denyAll()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 【修改点 3】: 添加一个新的 Bean 来定义详细的CORS规则
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. 允许你的前端来源
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));

        // 2. 允许所有请求方法 (GET, POST, PUT, DELETE, OPTIONS)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));

        // 3. 允许所有请求头 (特别是 Authorization 和 Content-Type)
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 4. 是否允许发送凭证 (例如 Cookies)，如果需要的话
        // configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有 API 路径应用此CORS配置
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}