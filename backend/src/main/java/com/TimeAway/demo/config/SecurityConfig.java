package com.TimeAway.demo.config;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@EnableWebSecurity
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    @Autowired
    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthenticationProvider authenticationProvider
    ) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/assets/**", "/static/**", "/static/assets/verauto-logo.png", "/*.js", "/*.css", "/manifest.json");
    }
    @Bean
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable()) // Helpful if your frontend runs on a different localhost port
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
                        .requestMatchers("/{path:[^\\.]*}", "/**/{path:[^\\.]*}").permitAll()
                        .requestMatchers("/api/auth/**", "/uploads/**", "/api/internal/purge-images").permitAll()
                        .requestMatchers("/","/index.html","/favicon.svg", "/static/index.html", "/static/**", "/manifest.webmanifest", "/static/assets/manifest.webmanifest",
                                "/assets/**", "/static/timeAway.png", "/*.js", "/*.css", "/firebase-messaging-sw.js", "/*.json","/static/timeAway-192x192.png","/static/favicon.svg","/timeAway-192x192.png",
                                "/static/*.json", "/vite.svg", "/static/vite.svg", "/static/assets/vite.svg", "/timeAway.png", "/placeholder.jpeg", "/static/placeholder.jpeg").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/request/cancelPendingVacationRequest/**").hasAnyRole("EMPLOYEE","HR","ADMIN")
                        .requestMatchers("/api/v1/request/cancellation/**").hasAnyRole("EMPLOYEE","MANAGER","HR")
                        .requestMatchers(HttpMethod.GET,"/api/v1/employees/me","/api/v1/holiday/**").authenticated()
                        .requestMatchers("/api/v1/departments").authenticated()
                        .requestMatchers("/api/v1/request/approvedByResponsible/**").hasRole("MANAGER")
                        .requestMatchers("/api/v1/request/department&status").hasAnyRole("MANAGER","HR", "ADMIN")
                        .requestMatchers("/api/v1/request/rejected/**","/api/v1/request/approved/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/request/**").hasAnyRole("EMPLOYEE","HR","ADMIN","MANAGER")
                        .requestMatchers("/api/v1/request/cancelPendingVacationRequest/**"
                                ,"/api/v1/request/rejected/**","/api/v1/request/approved/**","/api/v1/request/employee/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/employees/department", "/api/v1/employees/**")
                        .hasAnyRole("HR","MANAGER","ADMIN")
                        .requestMatchers("/api/v1/employees/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/holiday/**").hasAnyRole( "ADMIN")
                        .requestMatchers("/api/v1/**").hasRole("ADMIN")
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
