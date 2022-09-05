package com.example.pim.config;

import com.example.pim.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/products/**").hasAnyRole("ADMIN", "EDITOR", "REVIEWER")
                .requestMatchers("/api/categories/**").hasAnyRole("ADMIN", "EDITOR")
                .requestMatchers("/api/attributes/**").hasAnyRole("ADMIN", "EDITOR")
                .requestMatchers("/api/product-families/**").hasAnyRole("ADMIN", "EDITOR")
                .requestMatchers("/api/channels/**").hasAnyRole("ADMIN", "EDITOR")
                .requestMatchers("/api/export-templates/**").hasAnyRole("ADMIN", "EDITOR")
                .requestMatchers("/api/bulk-operations/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(org.springframework.security.config.Customizer.withDefaults());
        return http.build();
    }
}
