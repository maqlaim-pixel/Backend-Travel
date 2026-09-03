// package com.travelvista.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.http.HttpMethod;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.CorsConfigurationSource;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// import java.util.List;

// @Configuration
// @EnableWebSecurity
// public class SecurityConfig {

//     private final JwtAuthFilter jwtAuthFilter;

//     public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
//         this.jwtAuthFilter = jwtAuthFilter;
//     }

//     @Bean
//     public PasswordEncoder passwordEncoder() {
//         return new BCryptPasswordEncoder();
//     }

//     @Bean
//     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//         http
//             .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//             .csrf(csrf -> csrf.disable())
//             .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//             .authorizeHttpRequests(auth -> auth
//                 // ── Public endpoints (no auth needed) ──
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/admin/login")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/admin/me")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher("/api/auth/**")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/packages/**")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/destinations/**")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/hotels/**")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/activities/**")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/blogs/**")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/testimonials")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/faqs")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/settings")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/dashboard/**")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher("/api/images/**")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/leads/public/submit")).permitAll()
//                 .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/health")).permitAll()
//                 // ── Everything else under /api requires authentication ──
//                 .anyRequest().authenticated()
//             )
//             .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

//         return http.build();
//     }

//     @Bean
//     public CorsConfigurationSource corsConfigurationSource() {
//         CorsConfiguration config = new CorsConfiguration();
//         config.setAllowedOriginPatterns(List.of("*"));
//         config.setAllowedOrigins(List.of());
//         config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//         config.setAllowedHeaders(List.of("*"));
//         config.setAllowCredentials(true);

//         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         source.registerCorsConfiguration("/api/**", config); // AntPathMatcher handles this fine in CorsConfigurationSource
//         return source;
//     }
// }

package com.travelvista.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // =========================
            // CORS
            // =========================
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // =========================
            // CSRF
            // =========================
            .csrf(csrf -> csrf.disable())

            // =========================
            // Stateless JWT Session
            // =========================
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // =========================
            // Authorization
            // =========================
            .authorizeHttpRequests(auth -> auth

                // ---------------------------------
                // OPTIONS / CORS preflight
                // ---------------------------------
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ---------------------------------
                // Admin Login
                // ---------------------------------
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/admin/login"
                ).permitAll()

                // ---------------------------------
                // Admin Me
                // ---------------------------------
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/admin/me"
                ).permitAll()

                // ---------------------------------
                // Authentication
                // ---------------------------------
                .requestMatchers("/api/auth/**").permitAll()

                // ---------------------------------
                // Public GET APIs
                // ---------------------------------
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/packages/**"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/destinations/**"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/hotels/**"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/activities/**"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/blogs/**"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/testimonials"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/faqs"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/settings"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/dashboard/**"
                ).permitAll()

                // ---------------------------------
                // Images
                // ---------------------------------
                .requestMatchers(
                    "/api/images/**"
                ).permitAll()

                // ---------------------------------
                // Public Leads
                // ---------------------------------
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/leads/public/submit"
                ).permitAll()

                // ---------------------------------
                // Health Check
                // ---------------------------------
                .requestMatchers(
                    HttpMethod.GET,
                    "/health"
                ).permitAll()

                // ---------------------------------
                // Everything else requires JWT
                // ---------------------------------
                .anyRequest().authenticated()
            )

            // =========================
            // JWT Filter
            // =========================
            .addFilterBefore(
                jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    // =========================================================
    // CORS CONFIGURATION
    // =========================================================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // Your Vercel frontend
        config.setAllowedOrigins(List.of(
            "https://frontend-travel-eyls-sigma.vercel.app"
        ));

        // HTTP methods
        config.setAllowedMethods(List.of(
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "PATCH",
            "OPTIONS"
        ));

        // Request headers
        config.setAllowedHeaders(List.of("*"));

        // Required if frontend sends cookies/auth credentials
        config.setAllowCredentials(true);

        // Cache preflight response
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        // IMPORTANT:
        // Apply CORS to ALL endpoints, not only /api/**
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
